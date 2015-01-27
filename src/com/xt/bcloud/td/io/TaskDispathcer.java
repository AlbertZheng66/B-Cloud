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
 * 使用传统的IO方式对消息进行转发和控制。这种方式的好处是处理简单，不需要过多的考虑HTTP协议本身的内容。
 * 其缺点是：需要创建太多的线程，即使采用线程池技术，也可能对支持更多的客户端产生影响。
 * @author albert
 */
public class TaskDispathcer {

    private final int port = 4900;
    private final int maxConnectionCount = 10;
    /**
     * bindAddr 参数可以在 ServerSocket 的多穴主机 (multi-homed host) 上使用，
     * ServerSocket 仅接受对其地址之一的连接请求。如果 bindAddr 为 null，
     * 则默认接受任何/所有本地地址上的连接。
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

            // 将请求输出到测试文件
            TeeInputStream reqTis = new TeeInputStream(clientIn, testReqFile, false);

            byte[] b = new byte[1024 * 1024]; // 每次读取的数据量
            int count = 0; // 一次读取的字节数量

            // 用于转向的Socket
            Socket redirectSocket = new Socket("127.0.0.1", 8080);

            // 将数据写到服务器端
            OutputStream redirectOs = redirectSocket.getOutputStream();
            boolean readFlag = true;  // 第一次总是读取
            HttpRequestParser requestParser = new HttpRequestParser();
            HttpResponseParser responseParser = new HttpResponseParser();
            while (true) {
                requestParser.reset();
                responseParser.reset();

                HttpMessage request = null;
                // System.out.println("reqTis.available()=" + reqTis.available());
                readFlag = true;  // 第一次总是读取
                while (readFlag || reqTis.available() > 0) {
                    readFlag = false;
                    count = reqTis.read(b);
                    if (count < 0) {
                        break; // 网络中断
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
                    // 客户端关闭
                    break;
                }

                // 输出到原始服务器
                redirectOs.write(request.getOriginalMessage());

                InputStream redirectIs = redirectSocket.getInputStream();

                // 将响应输出到测试文件
                TeeInputStream resTis = new TeeInputStream(redirectIs, testResFile, true);

                clientSocket.setReceiveBufferSize(256 * 1024);
                OutputStream clientOs = clientSocket.getOutputStream();

                HttpMessage response = null;
                readFlag = true;  // 第一次总是读取
                //while (readFlag || resTis.available() > 0) {
                while ((count = resTis.read(b)) > 0) {
                    readFlag = false;
                    // count = resTis.read(b);
                    if (count < 0) {
                        break; // 网络中断
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
                    break; // 网络中断
                }
                // 输出到原始服务器
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
