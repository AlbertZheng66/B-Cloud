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
 * 使用传统的IO方式对消息进行转发和控制。这种方式的好处是处理简单，不需要过多的考虑HTTP协议本身的内容。
 * 其缺点是：需要创建太多的线程，即使采用线程池技术，也可能对支持更多的客户端产生影响。
 * 
 * TODO: 考虑是否需要负责：流量控制，连接数限制！！！
 * @author albert
 */
public class TaskDispathcer2 {

    static int seq = 0;
    private final int port = 4900;
    private final int maxConnectionCount = 10;
    private final CattleManager taskDispatcherChannel = CattleManager.getInstance();

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

            byte[] b = new byte[1024 * 1024]; // 每次读取的数据量
            int count = 0; // 一次读取的字节数量

            // 用于转向的Socket


            Socket redirectSocket = null;

            // 将数据写到服务器端
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

                    // 输出到测试文件
                    IOHelper.i2o(new ByteArrayInputStream(_b), testReqFile, false, false);

                    List<HttpMessage> requests = requestParser.parse(_b);
                    if (!requests.isEmpty()) {    // 读取至少一个完整的请求
                        request = requests.get(0);
                        break;
                    }
                }
                testReqFile.close();
                if (count < 0) {
                    // 客户端关闭
                    break;
                }

                if (redirectSocket == null) {
                    Cattle cattle = taskDispatcherChannel.findCattle((Request)request, null);
                    if (cattle == null) {
                        // TODO: 输出一个固定的网页（此应用尚未注册，或者服务于此应用的服务器尚未启动）。
                        // 将此请求转发给资源和应用管理器。
                        return ;
                    }
                    //
                    redirectSocket = new Socket(cattle.getIp(), cattle.getPort());
                    redirectOs = redirectSocket.getOutputStream();
                }

                // 输出到原始服务器
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
                    // 输出到测试文件
                    IOHelper.i2o(new ByteArrayInputStream(_b), testResFile, false, false);

                    List<HttpMessage> responses = responseParser.parse(_b);
                    if (!responses.isEmpty()) {  // 至少读取一个完整的响应
                        response = responses.get(0);
                        break;
                    }
                }
                testResFile.close();
                if (count < 0) {
                    break; // 网络中断
                }

                // 输出到原始服务器
                System.out.println("输出： response=" + response);
                if (response != null) {
                    if (response.getOriginalMessage() == null) {
                        System.out.println("response=" + response);
                    }
                    FileOutputStream refResFile = new FileOutputStream("e:\\res\\res-" + seq + ".res.ref", true);
                    IOHelper.i2o(new ByteArrayInputStream(response.getOriginalMessage()), refResFile, true, true);
                    clientOs.write(response.getOriginalMessage());
                }

                System.out.println("结束文件 seq=" + seq);
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

