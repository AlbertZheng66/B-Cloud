package com.xt.bcloud.td7.impl;

import com.xt.bcloud.td7.Request;
import com.xt.bcloud.td7.Response;
import com.xt.bcloud.worker.Cattle;
import com.xt.core.log.LogWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.*;
import static com.xt.bcloud.td.Contants.*;
import com.xt.bcloud.td.http.ErrorFactory;
import com.xt.bcloud.td.http.HttpException;
import com.xt.bcloud.td7.*;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Albert
 */
abstract public class AbstractBlockingConnector extends AbstractConnector {

    protected final BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(100, true);
    protected final Executor executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS, workQueue);
    private final Rewritable rewritable = new VersionRewriter();

    public AbstractBlockingConnector() {
    }

    public void run() {

        while (!stopFlag) {
            Object client = listen();
            LogWriter.debug2(logger, "从客户端[%s]接收到请求", client);
            final Action action = new Action(client);
            if (isAsynchronized()) {
                // run in the asynchronized mode
                executor.execute(action);
            } else {
                action.run();
            }
        }
    }

    class Action implements Runnable {

        private final Object client;

        public Action(Object client) {
            this.client = client;
        }

        public void run() {
            final List<ByteBuffer> recycledBuffers = new ArrayList();
            try {
                Request request = read(client, recycledBuffers);
                if (request == null) {
                    // 消息尚未读取完整或者客户端被异常关闭，记录一个错误消息
                    LogWriter.warn2(logger, "客户端[%s]异常关闭，系统将丢弃这个请求。", client);
                    return;
                }
                // 如果分到的服务器实例失败，需要重新分配。
                Cattle cattle = chooser.select(request);
                if (cattle == null) {
                    // 抛出无服务异常
                    throw new HttpException(ErrorFactory.ERROR_503);
                }

                // 对请求进行转换（重写）
                request = rewritable.rewrite(cattle, request);

                // 将消息进行转发，并解析相应的响应请求
                SyncTransmitter transmitter = TransmitterFactory.getInstance().getTransmitter(cattle.getIp(),
                        cattle.getPort());
                Response response = transmitter.send(request);

                // 返回请求
                SocketChannel socketChannel = ((SocketChannel) client);
                writeToClient(socketChannel, response);
            } catch (IOException ex) {
                LogWriter.warn2(logger, ex, "读取数据异常。");
                //FIXME: 如果客户端正在开启，可以输出部分错误信息
                // if (client.isOpen())
            } catch (Throwable ex) {
                // 输出服务处理异常信息
                SocketChannel socketChannel = ((SocketChannel) client);
                writeException(socketChannel, ex);
            } finally {
//                SocketChannel socketChannel = ((SocketChannel) client);
//                try {
//                    socketChannel.shutdownOutput();
//                } catch (IOException ex) {
//                    LogWriter.warn2(logger, ex, "关闭输出通道错误。");
//                }
                // recycle all buffers
                for (ByteBuffer byteBuffer : recycledBuffers) {
                    BufferFactory.getInstance().dispose(byteBuffer);
                }
            }
        }
    }

    /**
     * 是否需要异步
     *
     * @return
     */
    protected abstract boolean isAsynchronized();

    /**
     * 开始监听请求
     *
     * @return
     */
    protected abstract Object listen();

    /**
     * 从客户端读取数据，如果返回null，表示读取结束
     *
     * @param client
     * @return
     * @throws IOException
     */
    protected abstract ByteBuffer readFromClient(Object client) throws IOException;

    private Request read(final Object client, final List<ByteBuffer> recycledBuffers) throws IOException {
        Request request = null;
        final Parser parser = new HttpRequestParser();
        long _startTime = System.currentTimeMillis();
        ByteBuffer byteBuffer;
        while ((byteBuffer = readFromClient(client)) != null) {
            if (byteBuffer == Contants.END_OF_FILE) {
                parser.end();  // 主动结束
                return request; // 读取结束
            }

            // the buffer is put into a list and will be recycled later
            recycledBuffers.add(byteBuffer);
            SocketAddress clientAddress =  ((SocketChannel) client).getRemoteAddress();
            // 如果连接超时，系统将返回
            if (isTimeout(_startTime, clientAddress, parser)) {
                return null;
            }

            // 输出到测试文件
            DumperFactory.getInstance().write(dumperPrefix + "_receiving_",
                   clientAddress, byteBuffer);

            // FIXME:如果头解析结束了，就可以向“真正的服务器”可以开始查找服务器，并发送消息了
            // 解析完整的Http请求，并丢弃多余部分
            request = (Request) parser.parse(byteBuffer);
            if (parser.finished()) {
                break;  // 一个消息读取结束
            }
        }
        return request;
    }
}
