package com.xt.bcloud.td.io;

import com.xt.bcloud.td.http.HttpMessage;
import com.xt.bcloud.td.http.HttpRequestParser;
import com.xt.bcloud.td.http.HttpResponseParser;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import org.apache.commons.io.input.TeeInputStream;

/**
 * ʹ�ô�ͳ��IO��ʽ����Ϣ����ת���Ϳ��ơ����ַ�ʽ�ĺô��Ǵ���򵥣�����Ҫ����Ŀ���HTTPЭ�鱾������ݡ�
 * ��ȱ���ǣ���Ҫ����̫����̣߳���ʹ�����̳߳ؼ�����Ҳ���ܶ�֧�ָ���Ŀͻ��˲���Ӱ�졣
 * @author albert
 */
public class TaskDispathcer {

    private final int port = 4900;
    private final int maxConnectionCount = 10;
    /**
     * bindAddr ���������� ServerSocket �Ķ�Ѩ���� (multi-homed host) ��ʹ�ã�
     * ServerSocket �����ܶ����ַ֮һ������������� bindAddr Ϊ null��
     * ��Ĭ�Ͻ����κ�/���б��ص�ַ�ϵ����ӡ�
     */
    private InetAddress bindAddr;

    public void start() {
        try {
            ServerSocket server = new ServerSocket(port, maxConnectionCount, bindAddr);
            System.out.println("Listening for connections on port " + server.getLocalPort());
            server.setSoTimeout(0);
//server.s
            while (true) {
                Socket clientConn = server.accept();
                System.out.println("accept clientConn=" + clientConn);
                Processor processor = new Processor(clientConn);
                Thread processorThread = new Thread(processor);
                processorThread.start();
            }

        } catch (IOException ex) {

            ex.printStackTrace();

        }
    }

    public static void main(String[] args) {
        TaskDispathcer td = new TaskDispathcer();
        td.start();
    }
}

class Processor implements Runnable {

    private final Socket clientSocket;

    public Processor(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        FileOutputStream testResFile = null;
        FileOutputStream testReqFile = null;
        try {
            testResFile = new FileOutputStream("e:\\res\\res-" + System.currentTimeMillis() + ".res", true);
            testReqFile = new FileOutputStream("e:\\req\\req-" + System.currentTimeMillis() + ".req", true);

            // clientConn.sets
            System.out.println("Connection established with " + clientSocket);
            InputStream clientIn = clientSocket.getInputStream();

            // ����������������ļ�
            TeeInputStream reqTis = new TeeInputStream(clientIn, testReqFile, false);

            byte[] b = new byte[1024 * 1024]; // ÿ�ζ�ȡ��������
            int count = 0; // һ�ζ�ȡ���ֽ�����

            // ����ת���Socket
            Socket redirectSocket = new Socket("127.0.0.1", 8080);

            // ������д����������
            OutputStream redirectOs = redirectSocket.getOutputStream();
            boolean readFlag = true;  // ��һ�����Ƕ�ȡ
            HttpRequestParser requestParser = new HttpRequestParser();
            HttpResponseParser responseParser = new HttpResponseParser();
            while (true) {
                requestParser.reset();
                responseParser.reset();

                HttpMessage request = null;
                // System.out.println("reqTis.available()=" + reqTis.available());
                readFlag = true;  // ��һ�����Ƕ�ȡ
                while (readFlag || reqTis.available() > 0) {
                    readFlag = false;
                    count = reqTis.read(b);
                    if (count < 0) {
                        break; // �����ж�
                    }
                    byte[] _b = new byte[count];
                    System.arraycopy(b, 0, _b, 0, count);
                    List<HttpMessage> requests = requestParser.parse(_b);
                    if (!requests.isEmpty()) {
                        request = requests.get(0);
                        break;
                    } else {
                        request = responseParser.getLastMessage();
                    }
                }
                if (count < 0) {
                    // �ͻ��˹ر�
                    break;
                }

                // �����ԭʼ������
                redirectOs.write(request.getOriginalMessage());

                InputStream redirectIs = redirectSocket.getInputStream();

                // ����Ӧ����������ļ�
                TeeInputStream resTis = new TeeInputStream(redirectIs, testResFile, true);

                clientSocket.setReceiveBufferSize(256 * 1024);
                OutputStream clientOs = clientSocket.getOutputStream();

                HttpMessage response = null;
                readFlag = true;  // ��һ�����Ƕ�ȡ
                //while (readFlag || resTis.available() > 0) {
                while ((count = resTis.read(b)) > 0) {
                    readFlag = false;
                    // count = resTis.read(b);
                    if (count < 0) {
                        break; // �����ж�
                    }
                    if (count == 0) {
                        continue;
                    }
                    byte[] _b = new byte[count];
                    System.arraycopy(b, 0, _b, 0, count);
                    List<HttpMessage> responses = responseParser.parse(_b);
                    if (!responses.isEmpty()) {
                        response = responses.get(0);
                        break;
                    } else {
                        response = responseParser.getLastMessage();
                    }
                }
                if (count < 0) {
                    break; // �����ж�
                }
                // �����ԭʼ������
                if (response != null) {
                    if (response.getOriginalMessage() == null) {
                        System.out.println("response=" + response);
                    }
                    clientOs.write(response.getOriginalMessage());
                }
            }
            System.out.println("end.............");

        } catch (IOException ex) {
            System.err.println(ex);
        } finally {
            try {
                testResFile.close();
                testReqFile.close();

                if (clientSocket != null) {
                    clientSocket.close();
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
    }
}
