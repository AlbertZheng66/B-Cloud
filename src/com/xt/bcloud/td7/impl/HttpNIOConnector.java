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
            LogWriter.warn2(logger, ex, "����Ӧ�ó���", port);
        }
        // ����һ���������߳�
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
                // FIXME: �᲻����Ϊ�ͻ�������������ʱ��ͣ�����߼�����
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
            LogWriter.warn2(logger, ex, "����ͨ��[%s]ʱ�����쳣��", serverChannel);
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
                // Key ע�����
                writeException(clientChannel, new HttpException("Cannot find the attachement", ErrorFactory.ERROR_506));
                return false;
            }
            Response response = c0.response;
            if (response == null) {
                // д������Ϣ
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
            while ((count = clientChannel.read(byteBuffer)) > 0) { // ����Ϣ�ɶ���ֱ�ӷ���
                // the buffer is put into a list and will be recycled later
                //recycledBuffers.add(byteBuffer);
                // ������ӳ�ʱ��ϵͳ������
                if (isTimeout(_startTime, clientChannel.getRemoteAddress(), parser)) {
                    // ����socket
                    return true;
                }

//                    // ����������ļ�
//                    if (stdDumper != null) {
//                        stdDumper.writReq(_b);
//                    }


                // ���ͷ���������ˣ��Ϳ����������ķ����������Կ�ʼ���ҷ���������������Ϣ��
                // ����������Http���󣬲��������ಿ��
                Request request = (Request) parser.parse(byteBuffer);
                // pass it through to one of the original servers as soon as the message is parsed
                if (parser.finished()) {
                    // start writing to the original server
                    if (request == null) {
                        // ��Ϣ��δ��ȡ�������߿ͻ��˱��쳣�رգ���¼һ��������Ϣ
                        LogWriter.warn2(logger, "�ͻ���[%s]�쳣�رգ�ϵͳ�������������", clientChannel);
                        throw new HttpException(ErrorFactory.ERROR_406);
                    }
                    // ��ʼ������Ϣ
                    if (transmitter == null) {
                        // ����ֵ��ķ�����ʵ��ʧ�ܣ���Ҫ���·��䡣
                        Cattle cattle = chooser.select(request);
                        if (cattle == null) {
                            // �׳��޷����쳣
                            throw new HttpException(ErrorFactory.ERROR_503);
                        }

                        // ����Ϣ����ת������������Ӧ����Ӧ����
                        transmitter = TransmitterFactory.getInstance().getAsyncTransmitter(cattle.getIp(),
                                cattle.getPort());
                    }
                    transmitter.asyncSend(request, new Callable() {

                        public void execute(Response response) {
                            // ��������
                            Connector0.this.response = response;

                            // ���������Ž���ע��
                            CloudUtils.registerChannel(selector, clientChannel,
                                    SelectionKey.OP_WRITE, Connector0.this);
                        }

                        public void handle(Throwable t) {
                            writeException(clientChannel, t);
                        }
                    });
                    break;  // һ����Ϣ��ȡ����
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
