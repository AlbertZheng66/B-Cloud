package com.xt.bcloud.td7.impl;

import com.xt.bcloud.td.Contants;
import com.xt.bcloud.td.http.ErrorFactory;
import com.xt.bcloud.td.http.HttpException;
import com.xt.core.log.LogWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import com.xt.bcloud.td7.*;
import com.xt.bcloud.test.Utils;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Albert
 */
public class DefaultSyncTransmitter extends AbstractTransmitter implements SyncTransmitter {

    public DefaultSyncTransmitter(String ip, int port) {
        super(ip, port);
    }

    public boolean open() {
        if (serverSocketChannel == null || !serverSocketChannel.isConnected()) {
            // open or reopen the socket
            try {
                SocketAddress remote = new InetSocketAddress(ip, port);
                serverSocketChannel = SocketChannel.open(remote);
                serverSocketChannel.configureBlocking(true);
            } catch (IOException ex) {
                // �׳��ض����쳣���������ѡ�е�ţ�Ѿ������ã�崻�������Ҫ����ѡ��һ���ض���ţ����
                LogWriter.warn2(logger, ex, "���ӷ�����[%s],�˿�[%d]ʧ�ܡ�", ip, port);
                return false;
            }
        }
        return true;
    }

    public Response send(Request request) throws IOException {
        if (request == null) {
            return null;
        }

        writeTo(serverSocketChannel, request);
        serverSocketChannel.shutdownOutput();  // ����EOF�ַ�

        long _startTime = System.currentTimeMillis();

        int count = 0;
        Response response = null;
        final List<ByteBuffer> recycledBuffers = new ArrayList();  //FIXME: ʲôʱ����ձȽϺ����أ�
        ByteBuffer byteBuffer = createByteBuffer(recycledBuffers);
        
        final Parser parser = new HttpResponseParser();
        while ((count = serverSocketChannel.read(byteBuffer)) > -1) {
            // ������ӳ�ʱ��ϵͳ������
            if (isTimeout(_startTime)) {
                LogWriter.info2(logger, "���շ����[%s:%d]����Ӧ�Ѿ���ʱ[%d(ms)]��",
                        ip, port, Contants.downTimeout);
                break;
            }
            
            // ����������ļ�
            DumperFactory.getInstance().write(dumperPrefix + "_reading_from_server_",
                    serverSocketChannel.getRemoteAddress(), byteBuffer);

            // ������Ӧ��Ϣ
            response = (Response) parser.parse(byteBuffer);
            if (parser.finished()) {  // ���ٶ�ȡһ����������Ӧ
                break;
            }
            // ���´���Buffer
            byteBuffer = createByteBuffer(recycledBuffers);
        }


        if (count < 0 && response == null) {
            // ��ԭʼ���������������жϣ�����������δ��ȡ����
            throw new HttpException(ErrorFactory.ERROR_500);
        }
        return response;
    }

    private ByteBuffer createByteBuffer(final List<ByteBuffer> recycledBuffers) {
        ByteBuffer byteBuffer = BufferFactory.getInstance().allocate();
        recycledBuffers.add(byteBuffer);
        return byteBuffer;
    }

    @Override
    public void end() {
        try {
            if (serverSocketChannel != null) {
                serverSocketChannel.close();
            }
        } catch (IOException ex) {
            LogWriter.warn2(logger, ex, "�ر����ӵ�������[%s:%d]�Ŀͻ���ʱ�����쳣��", ip, port);
        }
    }
}
