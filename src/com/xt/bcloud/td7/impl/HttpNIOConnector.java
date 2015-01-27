package com.xt.bcloud.td7.impl;

import com.xt.bcloud.comm.CloudUtils;
import com.xt.bcloud.td.http.ErrorFactory;
import com.xt.bcloud.td.http.HttpException;
import com.xt.bcloud.td7.*;
import com.xt.bcloud.td7.AsyncTransmitter.Callable;
import com.xt.bcloud.test.PrintServer;
import com.xt.bcloud.worker.Cattle;
import com.xt.core.log.LogWriter;
import com.xt.gt.sys.SystemConfiguration;
import com.xt.gt.sys.SystemConstants;
import com.xt.gt.sys.loader.SystemLoader;
import com.xt.gt.sys.loader.SystemLoaderManager;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 *
 * @author Albert
 */
public class HttpNIOConnector extends AbstractConnector implements Runnable {

    private Selector selector;
    
    private final Thread asynConnectorThread;

    public HttpNIOConnector() {
        // "thread=" + DefaultAsyncTransmitter.class.getName()
        asynConnectorThread = new Thread(this);
    }

    @Override
    public void init() {
        super.init();
        System.out.println("Listening on port " + port);
        // Allocate an unbound server socket channel
        ServerSocketChannel serverChannel;
        try {
            serverChannel = ServerSocketChannel.open();
            // Get the associated ServerSocket to bind it with
            ServerSocket serverSocket = serverChannel.socket();
            // Create a new Selector for use below
            selector = Selector.open();

            // Set the port the server channel will listen to
            serverSocket.bind(new InetSocketAddress(port));
            // Set nonblocking mode for the listening socket
            serverChannel.configureBlocking(false);
            // Register the ServerSocketChannel with the Selector
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException ex) {
            LogWriter.warn2(logger, ex, "启动应用出错。", port);
        }
        // 启动一个处理处理线程
        asynConnectorThread.start();
    }

    public void run() {
        if (selector == null) {
            return;
        }
        while (!stopFlag) {
            // This may block for a long time. Upon returning, the
            // selected set contains keys of the ready channels.
            int n = 0;
            try {
                n = selector.select();
            } catch (IOException ex) {
                LogWriter.warn2(logger, ex, "An exception occured when selecting", selector);
            }
            if (n == 0) {
                continue; // nothing to do
            }
            // Get an iterator over the set of selected keys
            Iterator it = selector.selectedKeys().iterator();
            // Look at each key in the selected set
            while (it.hasNext()) {
                SelectionKey key = (SelectionKey) it.next();
                // Remove key from selected set; it's been handled
                it.remove();

                // Is a new connection coming in?
                if (key.isAcceptable()) {
                    if (!accept(key)) {
                        continue;
                    }
                }

                // Is there data to read on this channel?
                // FIXME: 会不会因为客户端阻塞而引起长时间停机或者饥饿？
                if (key.isReadable()) {
                    if (!read(key)) {
                        continue;
                    }
                }

                if (key.isWritable()) {
                    if (!write(key)) {
                        continue;
                    }
                }
            }
        } // end of while
    }

    private boolean accept(SelectionKey key) {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel;
        try {
            clientChannel = serverChannel.accept();
            LogWriter.info2(logger, "accepted a client[%s]", clientChannel.getRemoteAddress());
            CloudUtils.registerChannel(selector, clientChannel, SelectionKey.OP_READ, new Connector0());
        } catch (IOException ex) {
            LogWriter.warn2(logger, ex, "建立通道[%s]时出现异常。", serverChannel);
            return false;
        }
        return true;
    }

    private boolean read(SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        try {
            Connector0 c0 = (Connector0) key.attachment();
            if (c0 == null) {
                throw new HttpException("Cannot find the attachement", ErrorFactory.ERROR_506);
            }

            boolean finished = c0.read(clientChannel);
            if (!finished) {
                // we need to read more
                CloudUtils.registerChannel(selector, clientChannel, SelectionKey.OP_READ, c0);
            }
        } catch (Throwable t) {
            writeException(clientChannel, t);
            return false;
        }
        return true;
    }

    private boolean write(SelectionKey key) {
        final SocketChannel clientChannel = (SocketChannel) key.channel();
        try {
            Connector0 c0 = (Connector0) key.attachment();
            if (c0 == null) {
                // Key 注册错误。
                writeException(clientChannel, new HttpException("Cannot find the attachement", ErrorFactory.ERROR_506));
                return false;
            }
            Response response = c0.response;
            if (response == null) {
                // 写错误信息
                writeException(clientChannel, new HttpException("Null response", ErrorFactory.ERROR_506));
                return false;
            }
            writeToClient(clientChannel, response);
        } catch (Throwable t) {
            writeException(clientChannel, t);
            return false;
        }
        return true;
    }
// ----------------------------------------------------------

    class Connector0 {

        private final Parser parser = new HttpRequestParser();
        private AsyncTransmitter transmitter = null;
        private final long _startTime = System.currentTimeMillis();
        private Response response;

        public Connector0() {
        }

        public boolean read(final SocketChannel clientChannel) throws IOException {
            ByteBuffer byteBuffer = BufferFactory.getInstance().allocate();
            int count = -1;
            while ((count = clientChannel.read(byteBuffer)) > 0) { // 无信息可读就直接返回
                // the buffer is put into a list and will be recycled later
                //recycledBuffers.add(byteBuffer);
                // 如果连接超时，系统将返回
                if (isTimeout(_startTime, clientChannel.getRemoteAddress(), parser)) {
                    // 结束socket
                    return true;
                }

//                    // 输出到测试文件
//                    if (stdDumper != null) {
//                        stdDumper.writReq(_b);
//                    }


                // 如果头解析结束了，就可以向“真正的服务器”可以开始查找服务器，并发送消息了
                // 解析完整的Http请求，并丢弃多余部分
                Request request = (Request) parser.parse(byteBuffer);
                // pass it through to one of the original servers as soon as the message is parsed
                if (parser.finished()) {
                    // start writing to the original server
                    if (request == null) {
                        // 消息尚未读取完整或者客户端被异常关闭，记录一个错误消息
                        LogWriter.warn2(logger, "客户端[%s]异常关闭，系统将丢弃这个请求。", clientChannel);
                        throw new HttpException(ErrorFactory.ERROR_406);
                    }
                    // 开始发送消息
                    if (transmitter == null) {
                        // 如果分到的服务器实例失败，需要重新分配。
                        Cattle cattle = chooser.select(request);
                        if (cattle == null) {
                            // 抛出无服务异常
                            throw new HttpException(ErrorFactory.ERROR_503);
                        }

                        // 将消息进行转发，并解析相应的响应请求
                        transmitter = TransmitterFactory.getInstance().getAsyncTransmitter(cattle.getIp(),
                                cattle.getPort());
                    }
                    transmitter.asyncSend(request, new Callable() {

                        public void execute(Response response) {
                            // 返回请求
                            Connector0.this.response = response;

                            // 解析结束才进行注册
                            CloudUtils.registerChannel(selector, clientChannel,
                                    SelectionKey.OP_WRITE, Connector0.this);
                        }

                        public void handle(Throwable t) {
                            writeException(clientChannel, t);
                        }
                    });
                    break;  // 一个消息读取结束
                }
            }  // end of while
            return (parser.finished());
        }
    }

    /**
     * Test of init method, of class HttpNIOConnector.
     */
    static public void main(String[] args) {
        SystemLoaderManager slManager = SystemLoaderManager.getInstance();
        slManager.init(args);
        SystemConfiguration.getInstance().set(SystemConstants.APP_CONTEXT, new File("").getAbsolutePath());
        SystemLoader loader = slManager.getSystemLoader();
        if (loader.getConfigFile() != null) {
            SystemConfiguration.getInstance().load(loader.getConfigFile(), false);
        }
        HttpNIOConnector httpNIOConnector = new HttpNIOConnector();
        httpNIOConnector.setChooser(new Chooser() {

            public Cattle select(Request request) {
                return new Cattle("id", null, null, null, "127.0.0.1", 58080);
            }
        });
        httpNIOConnector.setPort(4900);
        httpNIOConnector.init();
        while (true) {
            try {
                Thread.sleep(500000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}
