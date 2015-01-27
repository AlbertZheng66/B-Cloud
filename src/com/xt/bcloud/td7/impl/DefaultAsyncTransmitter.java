package com.xt.bcloud.td7.impl;

import com.xt.bcloud.comm.CloudUtils;
import com.xt.bcloud.td.Contants;
import com.xt.bcloud.td.http.ErrorFactory;
import com.xt.bcloud.td7.AsyncTransmitter;
import com.xt.bcloud.td7.Parser;
import com.xt.bcloud.td7.Request;
import com.xt.bcloud.td7.Response;
import com.xt.core.log.LogWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.logging.Level;
import com.xt.bcloud.td.http.HttpException;
import com.xt.bcloud.td7.*;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

/**
 *
 * @author Albert
 */
public class DefaultAsyncTransmitter extends AbstractTransmitter implements AsyncTransmitter, Runnable {

    private Selector selector;
    private final Thread asyncTransmitterThread;

    public DefaultAsyncTransmitter(String ip, int port) {
        super(ip, port);
        // "thread=" + DefaultAsyncTransmitter.class.getName()
        asyncTransmitterThread = new Thread(this);
    }

    public boolean open() {
        // Allocate an unbound server socket channel

        try {
            SocketAddress sa = new InetSocketAddress(ip, port);
            serverSocketChannel = SocketChannel.open();
            // Create a new Selector for use below
            selector = Selector.open();

            // Set nonblocking mode for the listening socket
            serverSocketChannel.connect(sa);
            serverSocketChannel.configureBlocking(false);
            LogWriter.info2(logger, "���ӵ�������[%s:%d]", ip, port);

            // Register the SocketChannel with the Selector
            serverSocketChannel.register(selector, SelectionKey.OP_CONNECT);

        } catch (IOException ex) {
            LogWriter.warn2(logger, ex, "���Դ򿪵�������[%s:%d]�����ӳ���", ip, port);
            return false;
        }
        asyncTransmitterThread.start();
        return true;
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
                n = selector.select(1000);
            } catch (IOException ex) {
                LogWriter.warn2(logger, ex, "Select �ǳ����쳣��");
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
                if (key.isConnectable()) {
                    LogWriter.info2(logger, "����[%s:%d]�ɹ���", ip, port);
                }
                if (key.isWritable()) {
                    final Trasmitter0 trasmitter0 = (Trasmitter0) key.attachment();
                    if (trasmitter0 != null) {
                        trasmitter0.send(key);
                    } else {
                        // Key ע�����
                        LogWriter.warn2(logger, "Key[%s]ȱ��attachment", key);
                    }
                }

                // Is there data to read on this channel?
                if (key.isReadable()) {
                    final Trasmitter0 trasmitter0 = (Trasmitter0) key.attachment();
                    if (trasmitter0 != null) {
                        trasmitter0.receive(key);
                    } else {
                        // Key ע�����
                        LogWriter.warn2(logger, "Key[%s]ȱ��attachment", key);
                    }
                }
            }
        }
    }
    
    protected Response receive(SocketChannel socketChannel) throws IOException {
        // start to parse the response
        long _startTime = System.currentTimeMillis();

        int count = 0;
        Response response = null;
        ByteBuffer byteBuffer = BufferFactory.getInstance().allocate();
        final Parser parser = new HttpResponseParser();

        while ((count = socketChannel.read(byteBuffer)) > 0) {
            // ������ӳ�ʱ��ϵͳ������
            if (isTimeout(_startTime)) {
                LogWriter.info2(logger, "���շ����[%s:%d]����Ӧ�Ѿ���ʱ[%d(ms)]��",
                        ip, port, Contants.downTimeout);
                break;
            }

            // ����������ļ�
            DumperFactory.getInstance().write(dumperPrefix + "_receiving_from_server_", 
                    socketChannel.getRemoteAddress(), byteBuffer);

            // ������Ӧ��Ϣ
            response = (Response) parser.parse(byteBuffer);
            if (parser.finished()) {  // ���ٶ�ȡһ����������Ӧ
                break;
            }
        }

        if (count < 0 && response == null) {
            // ��ԭʼ���������������жϣ�����������δ��ȡ����
            throw new HttpException(ErrorFactory.ERROR_500);
        }
        return response;
    }

    public void asyncSend(Request msg, Callable callable) throws IOException {
        if (selector == null || msg == null || callable == null) {
            return;
        }
        final Trasmitter0 trasmitter0 = new Trasmitter0(msg, callable);
        SelectionKey key = serverSocketChannel.keyFor(this.selector);
        key.attach(trasmitter0);
        key.interestOps(SelectionKey.OP_WRITE);
        this.selector.wakeup();
    }

    private class Trasmitter0 {

        private final Request request;
        private final Callable callable;

        public Trasmitter0(Request request, Callable callable) {
            this.request = request;
            this.callable = callable;
        }

        public void send(SelectionKey key) {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            try {
                writeTo(socketChannel, request);
                key.interestOps(SelectionKey.OP_READ);
            } catch (IOException ex) {
                key.cancel();
                callable.handle(ex);
            }
        }

        public void receive(SelectionKey key) {
            try {
                Response response = DefaultAsyncTransmitter.this.receive(serverSocketChannel);
                callable.execute(response);
            } catch (Throwable t) {
                callable.handle(t);
            }
        }
    }

    public void end() {
        try {
            if (serverSocketChannel != null && serverSocketChannel.isOpen()) {
                serverSocketChannel.close();
            }
            if (selector != null) {
                selector.close();
            }
        } catch (IOException ex) {
            LogWriter.warn2(logger, ex, "���Թرյ�������[%s:%d]�����ӳ���", ip, port);
        }
    }
}
