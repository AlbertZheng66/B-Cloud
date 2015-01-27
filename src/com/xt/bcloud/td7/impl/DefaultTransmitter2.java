package com.xt.bcloud.td7.impl;

import com.xt.bcloud.comm.CloudUtils;
import com.xt.bcloud.td.Contants;
import com.xt.bcloud.td.http.ErrorFactory;
import com.xt.bcloud.td.http.HttpException;
import com.xt.core.log.LogWriter;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;
import static com.xt.bcloud.td.http.HttpConstants.*;
import com.xt.bcloud.td.http.HttpError;
import com.xt.bcloud.td7.BufferFactory;
import com.xt.bcloud.td7.Parser;
import com.xt.bcloud.td7.Request;
import com.xt.bcloud.td7.Response;
import com.xt.bcloud.td7.Transmitter;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 *
 * @author Albert
 */
public class DefaultTransmitter2 implements Transmitter {

    private final Logger logger = Logger.getLogger(DefaultTransmitter2.class);
    private final String ip;
    private final int port;
    private Socket redirectSocket;
    private final Parser parser = new HttpResponseParser();

    public DefaultTransmitter2(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public boolean open() {
        if (redirectSocket == null || redirectSocket.isClosed()) {
            // open or reopen the socket
            try {
                redirectSocket = new Socket();
                SocketAddress remote = new InetSocketAddress(ip, port);
                redirectSocket.connect(remote, Contants.upTimeout);
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

        writeTo(request);

        InputStream is = redirectSocket.getInputStream();

        long _startTime = System.currentTimeMillis();

        int count = 0;
        Response response = null;
        byte[] bytes = new byte[1024];
        while ((count = is.read(bytes)) > -1) {
            // ������ӳ�ʱ��ϵͳ������
            if (isTimeout(_startTime)) {
                LogWriter.info2(logger, "���շ����[%s:%d]����Ӧ�Ѿ���ʱ[%d(ms)]��",
                        ip, port, Contants.downTimeout);
                break;
            }
            ByteBuffer byteBuffer = BufferFactory.getInstance().wrap(bytes);

//                    // ����������ļ�
//                    if (stdDumper != null) {
//                        stdDumper.writRes(_b);
//                    }


            // ������Ӧ��Ϣ
            response = (Response) parser.parse(byteBuffer);
            if (response != null) {  // ���ٶ�ȡһ����������Ӧ
                // response = (Response) responses.get(0);
                break;
            }
            bytes = new byte[1024];
        }

        if (count < 0 && response == null) {
            // ��ԭʼ���������������жϣ�����������δ��ȡ����
            throw new HttpException(ErrorFactory.ERROR_500);
        }
        return response;
    }

    protected boolean isTimeout(long startTime) {
        if (System.currentTimeMillis() - startTime > Contants.downTimeout) {

            // dump ��������
//                        HttpMessage msg = requestParser.getLastMessage();
//                        if (msg != null) {
//                            errorDumper.writReq(msg.getOriginalMessage());
//                        }
            // ���ء�����ʱ���������
            HttpError error = ErrorFactory.getInstance().create("408");
            throw new HttpException(error);
        }
        return false;
    }

    private void writeTo(Request request) throws IOException {
        // ������Ϣ;
        OutputStream os = redirectSocket.getOutputStream();
        System.out.println("socketChanel=" + os);
        try {

            //:IF if need to dump the message, which is sending to the original server
//        FilesocketChannelStream fos = new FilesocketChannelStream("e:\\dump\\test-" + System.currentTimeMillis());
//        TeesocketChannelStream tos = new TeesocketChannelStream(redirectOs, fos);


            // �����ԭʼ������
            // д������
            os.write(CloudUtils.toHeaderBytes(request.getMethodName()));
            os.write(BLANK);
            os.write(CloudUtils.toHeaderBytes(request.getContextPath()));
            os.write(BLANK);
            os.write(CloudUtils.toHeaderBytes(request.getVersion()));

            // д����ͷ
            for (Iterator<Map.Entry<String, String>> it = request.getHeader().getFields().entrySet().iterator();
                    it.hasNext();) {
                Map.Entry<String, String> entry = it.next();
                os.write(CloudUtils.toHeaderBytes(entry.getKey()));
                os.write(COLON);
                os.write(CloudUtils.toHeaderBytes(entry.getValue()));
            }
            os.write(CRLF);  // дһ����Ϣ������Ϣ��֮��Ŀ���

            if (request.getBody() != null) {
                for (int i = 0; i < request.getBody().getBuffers().length; i++) {
                    ByteBuffer bb = request.getBody().getBuffers()[i];
                    os.write(bb.array());    
                }
            }
        } finally {
            // �Ƿ���Ҫ�����ر�
//            if (socketChannel != null) {
//                socketChannel.close();
//            }
        }
    }

    public void end() {
        try {
            if (redirectSocket != null) {
                redirectSocket.close();
            }
        } catch (IOException ex) {
            LogWriter.warn2(logger, ex, "�ر����ӵ�������[%s:%d]�Ŀͻ���ʱ�����쳣��", ip, port);
        }
    }
}
