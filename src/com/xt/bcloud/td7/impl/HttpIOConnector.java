package com.xt.bcloud.td7.impl;

import com.xt.bcloud.td7.BufferFactory;
import com.xt.bcloud.td7.Contants;
import com.xt.bcloud.td7.TaskDispatcherException;
import com.xt.core.log.LogWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 *
 * @author Albert
 */
public class HttpIOConnector extends AbstractBlockingConnector {

    /**
     * �ɴ���������������
     */
    private static final int MAX_CONNECTION_COUNT = 100;
    private ServerSocketChannel serverSocketChannel = null;

    @Override
    public void init() {
        super.init();
        try {
            LogWriter.info2(logger, "��ʼ����������[%s:%d]�����������[%d]", bindAddr,
                    port, MAX_CONNECTION_COUNT);
//            ServerSocket server = new ServerSocket(port, MAX_CONNECTION_COUNT, bindAddr);
//            server.
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(true);
            SocketAddress sa = new InetSocketAddress(bindAddr, port);
            serverSocketChannel.bind(sa);
        } catch (IOException ex) {
            throw new TaskDispatcherException(String.format("�󶨶˿�[%d]ʱ�����쳣��", port), ex);
        }
    }

    /**
     *
     * @return
     */
    @Override
    protected Object listen() {
        if (serverSocketChannel == null) {
            LogWriter.warn2(logger, "����������[%s:%d]����ʧ�ܡ�", bindAddr, port);
            return null;
        }
        try {
            return serverSocketChannel.accept();
        } catch (IOException ex) {
            LogWriter.warn2(logger, ex, "����������[%s:%d]����ʧ�ܡ�", bindAddr, port);
            return null;
        }
    }

    @Override
    protected ByteBuffer readFromClient(Object client) throws IOException {
        // ͨ��Ϊ�յ����
        SocketChannel channel = ((SocketChannel) client);
        if (channel == null) {
            return null;
        }

        ByteBuffer buffer = BufferFactory.getInstance().allocate();
        int byteCount = channel.read(buffer);
        LogWriter.debug2(logger, "�ӿͻ���[%s]��ȡ��[%d]�ֽڡ�", channel, byteCount);
        if (byteCount < 0) {  // has reached the end of this stream
            return Contants.END_OF_FILE;
        }
        return buffer;
    }

    @Override
    protected boolean isAsynchronized() {
        return true;
    }

    @Override
    public String toString() {
        return "HttpIOConnector{" + ", server=" + serverSocketChannel + '}';
    }
}
