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
            LogWriter.debug2(logger, "�ӿͻ���[%s]���յ�����", client);
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
                    // ��Ϣ��δ��ȡ�������߿ͻ��˱��쳣�رգ���¼һ��������Ϣ
                    LogWriter.warn2(logger, "�ͻ���[%s]�쳣�رգ�ϵͳ�������������", client);
                    return;
                }
                // ����ֵ��ķ�����ʵ��ʧ�ܣ���Ҫ���·��䡣
                Cattle cattle = chooser.select(request);
                if (cattle == null) {
                    // �׳��޷����쳣
                    throw new HttpException(ErrorFactory.ERROR_503);
                }

                // ���������ת������д��
                request = rewritable.rewrite(cattle, request);

                // ����Ϣ����ת������������Ӧ����Ӧ����
                SyncTransmitter transmitter = TransmitterFactory.getInstance().getTransmitter(cattle.getIp(),
                        cattle.getPort());
                Response response = transmitter.send(request);

                // ��������
                SocketChannel socketChannel = ((SocketChannel) client);
                writeToClient(socketChannel, response);
            } catch (IOException ex) {
                LogWriter.warn2(logger, ex, "��ȡ�����쳣��");
                //FIXME: ����ͻ������ڿ���������������ִ�����Ϣ
                // if (client.isOpen())
            } catch (Throwable ex) {
                // ����������쳣��Ϣ
                SocketChannel socketChannel = ((SocketChannel) client);
                writeException(socketChannel, ex);
            } finally {
//                SocketChannel socketChannel = ((SocketChannel) client);
//                try {
//                    socketChannel.shutdownOutput();
//                } catch (IOException ex) {
//                    LogWriter.warn2(logger, ex, "�ر����ͨ������");
//                }
                // recycle all buffers
                for (ByteBuffer byteBuffer : recycledBuffers) {
                    BufferFactory.getInstance().dispose(byteBuffer);
                }
            }
        }
    }

    /**
     * �Ƿ���Ҫ�첽
     *
     * @return
     */
    protected abstract boolean isAsynchronized();

    /**
     * ��ʼ��������
     *
     * @return
     */
    protected abstract Object listen();

    /**
     * �ӿͻ��˶�ȡ���ݣ��������null����ʾ��ȡ����
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
                parser.end();  // ��������
                return request; // ��ȡ����
            }

            // the buffer is put into a list and will be recycled later
            recycledBuffers.add(byteBuffer);
            SocketAddress clientAddress =  ((SocketChannel) client).getRemoteAddress();
            // ������ӳ�ʱ��ϵͳ������
            if (isTimeout(_startTime, clientAddress, parser)) {
                return null;
            }

            // ����������ļ�
            DumperFactory.getInstance().write(dumperPrefix + "_receiving_",
                   clientAddress, byteBuffer);

            // FIXME:���ͷ���������ˣ��Ϳ����������ķ����������Կ�ʼ���ҷ���������������Ϣ��
            // ����������Http���󣬲��������ಿ��
            request = (Request) parser.parse(byteBuffer);
            if (parser.finished()) {
                break;  // һ����Ϣ��ȡ����
            }
        }
        return request;
    }
}
