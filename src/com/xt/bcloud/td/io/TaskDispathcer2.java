package com.xt.bcloud.td.io;

import com.xt.bcloud.td.http.HttpMessage;
import com.xt.bcloud.td.http.HttpRequestParser;
import com.xt.bcloud.td.http.HttpResponseParser;
import com.xt.bcloud.td.http.Request;
import com.xt.bcloud.td.CattleManager;
import com.xt.bcloud.worker.Cattle;
import com.xt.core.utils.IOHelper;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 * ʹ�ô�ͳ��IO��ʽ����Ϣ����ת���Ϳ��ơ����ַ�ʽ�ĺô��Ǵ���򵥣�����Ҫ����Ŀ���HTTPЭ�鱾������ݡ�
 * ��ȱ���ǣ���Ҫ����̫����̣߳���ʹ�����̳߳ؼ�����Ҳ���ܶ�֧�ָ���Ŀͻ��˲���Ӱ�졣
 * 
 * TODO: �����Ƿ���Ҫ�����������ƣ����������ƣ�����
 * @author albert
 */
public class TaskDispathcer2 {

    static int seq = 0;
    private final int port = 4900;
    private final int maxConnectionCount = 10;
    private final CattleManager taskDispatcherChannel = CattleManager.getInstance();

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
            // server.setSoTimeout(0);
//server.s
            while (true) {
                Socket clientConn = server.accept();
                System.out.println("accept clientConn=" + clientConn);
                Processor2 processor = new Processor2(clientConn, this.taskDispatcherChannel);
                Thread processorThread = new Thread(processor, "thread-" + seq);
                processorThread.start();
            }
        } catch (IOException ex) {

            ex.printStackTrace();

        }
    }

    public static void main(String[] args) {
        TaskDispathcer2 td = new TaskDispathcer2();
        td.start();
    }
}

class Processor2 implements Runnable {

    private final Socket clientSocket;
    private final CattleManager taskDispatcherChannel;

    public Processor2(Socket clientSocket, CattleManager taskDispatcherChannel) {
        this.clientSocket = clientSocket;
        this.taskDispatcherChannel = taskDispatcherChannel;
    }

    public void run() {
        FileOutputStream testResFile = null;
        FileOutputStream testReqFile = null;
        try {

            // clientConn.sets
            System.out.println("Connection established with " + clientSocket);
            InputStream clientIn = clientSocket.getInputStream();

            byte[] b = new byte[1024 * 1024]; // ÿ�ζ�ȡ��������
            int count = 0; // һ�ζ�ȡ���ֽ�����

            // ����ת���Socket


            Socket redirectSocket = null;

            // ������д����������
            OutputStream redirectOs = null;
            while (true) {
                HttpRequestParser requestParser = new HttpRequestParser();
                HttpResponseParser responseParser = new HttpResponseParser();

                HttpMessage request = null;

                String seq = String.format("%d-%d", System.currentTimeMillis(), TaskDispathcer2.seq++);
                testReqFile = new FileOutputStream("e:\\req\\req-" + seq + ".req");
                while ((count = clientIn.read(b)) > 0) {
                    // System.out.println("clientIn.count=" + count);
                    byte[] _b = new byte[count];
                    System.arraycopy(b, 0, _b, 0, count);

                    // ����������ļ�
                    IOHelper.i2o(new ByteArrayInputStream(_b), testReqFile, false, false);

                    List<HttpMessage> requests = requestParser.parse(_b);
                    if (!requests.isEmpty()) {    // ��ȡ����һ������������
                        request = requests.get(0);
                        break;
                    }
                }
                testReqFile.close();
                if (count < 0) {
                    // �ͻ��˹ر�
                    break;
                }

                if (redirectSocket == null) {
                    Cattle cattle = taskDispatcherChannel.findCattle((Request)request, null);
                    if (cattle == null) {
                        // TODO: ���һ���̶�����ҳ����Ӧ����δע�ᣬ���߷����ڴ�Ӧ�õķ�������δ��������
                        // ��������ת������Դ��Ӧ�ù�������
                        return ;
                    }
                    //
                    redirectSocket = new Socket(cattle.getIp(), cattle.getPort());
                    redirectOs = redirectSocket.getOutputStream();
                }

                // �����ԭʼ������
                redirectOs.write(request.getOriginalMessage());

                InputStream redirectIs = redirectSocket.getInputStream();

                clientSocket.setReceiveBufferSize(256 * 1024);
                OutputStream clientOs = clientSocket.getOutputStream();


                HttpMessage response = null;
                testResFile = new FileOutputStream("e:\\res\\res-" + seq + ".res", true);
                while ((count = redirectIs.read(b)) > 0) {
                    byte[] _b = new byte[count];
                    // System.out.println("redirectIs count=" + count);
                    System.arraycopy(b, 0, _b, 0, count);
                    // ����������ļ�
                    IOHelper.i2o(new ByteArrayInputStream(_b), testResFile, false, false);

                    List<HttpMessage> responses = responseParser.parse(_b);
                    if (!responses.isEmpty()) {  // ���ٶ�ȡһ����������Ӧ
                        response = responses.get(0);
                        break;
                    }
                }
                testResFile.close();
                if (count < 0) {
                    break; // �����ж�
                }

                // �����ԭʼ������
                System.out.println("����� response=" + response);
                if (response != null) {
                    if (response.getOriginalMessage() == null) {
                        System.out.println("response=" + response);
                    }
                    FileOutputStream refResFile = new FileOutputStream("e:\\res\\res-" + seq + ".res.ref", true);
                    IOHelper.i2o(new ByteArrayInputStream(response.getOriginalMessage()), refResFile, true, true);
                    clientOs.write(response.getOriginalMessage());
                }

                System.out.println("�����ļ� seq=" + seq);
            }
            System.out.println("end.............");
        } catch (IOException ex) {
            System.err.println(ex);
        } finally {
            try {

                if (clientSocket != null) {
                    clientSocket.close();
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
    }
}

