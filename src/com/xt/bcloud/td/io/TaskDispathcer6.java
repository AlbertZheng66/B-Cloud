package com.xt.bcloud.td.io;

import com.xt.bcloud.comm.CloudUtils;
import com.xt.bcloud.td.http.ErrorFactory;
import com.xt.bcloud.td.http.HttpError;
import com.xt.bcloud.td.http.HttpException;
import com.xt.bcloud.td.Dumpable;
import com.xt.bcloud.td.Rewritable;
import com.xt.bcloud.td.http.HttpMessage;
import com.xt.bcloud.td.http.HttpRequestParser;
import com.xt.bcloud.td.http.HttpResponseParser;
import com.xt.bcloud.td.http.Request;
import com.xt.bcloud.td.CattleManager;
import com.xt.bcloud.td.http.Response;
import com.xt.bcloud.td.impl.DefaultDumper;
import com.xt.bcloud.td.impl.ErrorDumper;
import com.xt.bcloud.td.impl.VersionRewriter;
import com.xt.bcloud.worker.Cattle;
import com.xt.core.log.LogWriter;
import com.xt.core.utils.IOHelper;
import com.xt.gt.sys.SystemConfiguration;
import com.xt.gt.sys.SystemConstants;
import com.xt.gt.sys.loader.SystemLoader;
import com.xt.gt.sys.loader.SystemLoaderManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.log4j.Logger;
import static com.xt.bcloud.td.Contants.*;
import static com.xt.bcloud.td.http.HttpConstants.*;

/**
 * 使用传统的IO方式对消息进行转发和控制。这种方式的好处是处理简单，不需要过多的考虑HTTP协议本身的内容。
 * 其缺点是：需要创建太多的线程，即使采用线程池技术，也可能对支持更多的客户端产生影响。
 *
 * TODO: 考虑是否需要负责：流量控制，连接数限制！！！
 * @author albert
 */
public class TaskDispathcer6 {

    private final Logger logger = Logger.getLogger(TaskDispathcer6.class);
    /**
     * 占用的端口号，可通过命令行参数“-p”进行指定，默认是：6666。
     */
    private int port = 4900;  //6666;
    

    /**
     * 停止的标记
     */
    private volatile boolean stopFlag = false;

    // private static int seq = 0;

    /**
     * 可处理的最大连接数。
     */
    private static final int MAX_CONNECTION_COUNT = 100;

    /**
     * 当前正在处理的连接的个数。
     */
    private volatile int processingConnectionCount = 0;
    
    /**
     * 是否导出输入和输出的 HTTP 消息。这个参数不包括异常，如果应用出现了异常，
     * 其请求和响应都应该是被导出的。
     */
    private final boolean dump = false;
    
    /**
     * “工作实例管理”的实例，此实例为多线程复用实例，因此要求其方法为线程安全的方法。
     */
    private final CattleManager cattleManager = CattleManager.getInstance();
    /**
     * bindAddr 参数可以在 ServerSocket 的多穴主机 (multi-homed host) 上使用，
     * ServerSocket 仅接受对其地址之一的连接请求。如果 bindAddr 为 null，
     * 则默认接受任何/所有本地地址上的连接。
     */
    private InetAddress bindAddr;

    private final BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(100, true);

   
    private final Executor executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS, workQueue);

    public void start() {
        try {
            ServerSocket server = new ServerSocket(port, MAX_CONNECTION_COUNT, bindAddr);
            LogWriter.info2(logger, "Task dispatrer listening for connections on port %d,"
                    + " max connection=%d, bindingAddr=%s.", port, MAX_CONNECTION_COUNT, bindAddr);
            // server.setSoTimeout(0);
            while (!stopFlag) {
                Socket clientConn = server.accept();
                LogWriter.info2(logger, "accept client = %s", clientConn);
                Processor6 processor = new Processor6(clientConn, this.cattleManager);
                executor.execute(processor);
            }
        } catch (IOException ex) {
            LogWriter.error(logger, "建立服务连接时出现异常。", ex);
        }
    }

    public static void main(String[] args) {
        SystemLoaderManager slManager = SystemLoaderManager.getInstance();
        slManager.init(args);
        SystemConfiguration.getInstance().set(SystemConstants.APP_CONTEXT, new File("").getAbsolutePath());
        SystemLoader loader = slManager.getSystemLoader();
        if (loader.getConfigFile() != null) {
            SystemConfiguration.getInstance().load(loader.getConfigFile(), false);
        }
        // 初始化组管理器
        CattleManager.getInstance().init(); 
        TaskDispathcer6 td = new TaskDispathcer6();
        td.start();
    }
}

class Processor6 implements Runnable {

    private final Logger logger = Logger.getLogger(Processor6.class);

    private final Socket clientSocket;
    private final CattleManager cattleManager;
    /**
     * 标准导出器，将正常请求或者响应的原始信息导出文件等介质。
     */
    private Dumpable stdDumper = new DefaultDumper();
    /**
     * 当请求出现异常时（如超时，超大，方法不存在等），
     * 将使用此导出器导出相应的请求信息，已被检察。
     */
    private Dumpable errorDumper = (Dumpable) SystemConfiguration.getInstance().readObject("errorDumper", new ErrorDumper());
    /**
     * 记录线程的启动时间（用于计算超时）
     */
    private final long startTime;
    /**
     * 对请求和响应进行重写的复写器。
     */
    private Rewritable rewriter = new VersionRewriter();

    public Processor6(Socket clientSocket, CattleManager taskDispatcherChannel) {
        this.clientSocket = clientSocket;
        this.cattleManager = taskDispatcherChannel;
        startTime = System.currentTimeMillis();
    }

    public void run() {
        HttpRequestParser requestParser = null;
        try {
            LogWriter.info2(logger, "Connection established with %s ", clientSocket);

            clientSocket.setReceiveBufferSize(256 * 1024);
            InputStream clientIn = clientSocket.getInputStream();

            byte[] b = new byte[1024 * 1024]; // 每次读取的数据量
            int count = 0; // 一次读取的字节数

            Socket redirectSocket = null;

            // 将数据写到服务器端
            while (true) {
                requestParser = new HttpRequestParser();  // 需要复用
                HttpResponseParser responseParser = new HttpResponseParser();

                Request request = null;

                long _startTime = System.currentTimeMillis();
                while ((count = clientIn.read(b)) > 0) {
                    // 如果连接超时，系统将返回
                    if (System.currentTimeMillis() - _startTime > upTimeout) {
                        LogWriter.warn2(logger, "客户端[%s]发送的请求[%s]已经超时 %d(ms)。",
                                clientSocket,
                                requestParser.getLastMessage() != null
                                ? ((Request) requestParser.getLastMessage()).getRequestMethod()
                                : "<null>", upTimeout);
                        // dump 错误请求
                        HttpMessage msg = requestParser.getLastMessage();
                        if (msg != null) {
                            errorDumper.writReq(msg.getOriginalMessage());
                        }
                        // 返回“请求超时”错误代码
                        HttpError error = ErrorFactory.getInstance().create("408");
                        throw new HttpException(error);
                    }
                    byte[] _b = new byte[count];
                    System.arraycopy(b, 0, _b, 0, count);  // TODO: 会有多大的性能损耗？！

                    // 输出到测试文件
                    if (stdDumper != null) {
                        stdDumper.writReq(_b);
                    }

                    List<HttpMessage> requests = requestParser.parse(_b);
                    if (!requests.isEmpty()) {    // 读取至少一个完整的请求
                        request = (Request) requests.get(0);
                        break;
                    }
                }

                if (count < 0) {
//                    // 客户端关闭
//                    LogWriter.warn2(logger, "客户端[%s]异常关闭，系统将丢弃这个请求。", clientSocket);
//                    break;
                }


                // 如果分到的服务器实例失败，需要重新分配。
                boolean found = false;  // 是否找到
                Set<Cattle> excluded = new HashSet(2);
                Cattle cattle = null;
                while (!found) {
                    cattle = cattleManager.findCattle(request, excluded);
                    redirectSocket = createRedirectSocket(cattle);
                    found = (redirectSocket != null);
                    if (!found) {
                        excluded.add(cattle);
                    }
                }
                if (cattle == null) {
                    HttpError error = ErrorFactory.getInstance().create("503");
                    error.setLocalMessage(String.format("请求的域名[%s]已无可用实例。", request.getHost()));
                    throw new HttpException(error);
                }

                // 对请求进行重写
                if (rewriter != null) {
                    request = rewriter.rewrite(cattle, request);
                }

                // 将请求写入原始服务器
                writeToOrignalServer(redirectSocket, request);

                InputStream redirectIs = redirectSocket.getInputStream();

                _startTime = System.currentTimeMillis();


                Response response = null;
                while ((count = redirectIs.read(b)) > 0) {
                    // 如果连接超时，系统将返回
                    if (System.currentTimeMillis() - _startTime > downTimeout) {
                        LogWriter.info2(logger, "接收服务端[%s]的响应[%s]已经超时 %d(ms)。",
                                redirectSocket, request, downTimeout);

                        // dump 错误响应
                        HttpMessage msg = requestParser.getLastMessage();
                        if (msg != null) {
                            errorDumper.writReq(msg.getOriginalMessage());
                        }

                        // 返回超时请求（抛出一个超时请求）
                        throw new HttpException(ErrorFactory.ERROR_504);
                    }

                    byte[] _b = new byte[count];
                    System.arraycopy(b, 0, _b, 0, count);  // TODO: 会有多大的性能损耗？！

                    // 输出到测试文件
                    if (stdDumper != null) {
                        stdDumper.writRes(_b);
                    }

                    List<HttpMessage> responses = responseParser.parse(_b);
                    if (!responses.isEmpty()) {  // 至少读取一个完整的响应
                        response = (Response) responses.get(0);
                        break;
                    }
                }

                if (count < 0 && response == null) {
                    // “原始服务器”的网络中断，但是请求尚未读取结束
                    throw new HttpException(ErrorFactory.ERROR_500);
                }

                // 对请求进行重写
                if (rewriter != null) {
                    response = rewriter.rewrite(cattle, request, response);
                }

                // 输出到原始服务器
                writeToClient(clientSocket, response, request);
            }
        } catch (IOException ex) {
            // dump 错误响应
            if (requestParser != null) {
                HttpMessage msg = requestParser.getLastMessage();
                if (msg != null) {
                    errorDumper.writReq(msg.getOriginalMessage());
                }
            }
            // 异常的处理还需要再仔细的思考，各种异常所产生的“响应码”应该是不一样的。
            // 系统异常....
            // 客户端异常。
            System.err.println(ex);
        } catch (HttpException ex) {
            // 处理 Http 协议本身的错误
            if (clientSocket != null && !clientSocket.isClosed()) {
                try {
                    OutputStream os = clientSocket.getOutputStream();
                    ErrorFactory.writeTo(os, ex);
                    IOHelper.closeSilently(os);
                } catch (IOException ex1) {
                    LogWriter.warn2(logger, ex1, "处理 HTTP 异常[%s]时出现错误。", ex);
                }
            }
        } finally {
            // 关闭响应文件
            if (stdDumper != null) {
                stdDumper.closeReq();
                stdDumper.closeRes();
            }
            errorDumper.closeReq();
            errorDumper.closeRes();
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException ex) {
                LogWriter.warn(logger, "关闭客户端时参数异常。", ex);
            }

        }
    }

    /**
     * 将请求写入原始服务器。
     * @param redirectSocket
     * @param request
     * @throws IOException
     */
    private void writeToOrignalServer(Socket redirectSocket, Request request) throws IOException {
        LogWriter.debug2(logger, "将请求[%s]写入原始服务器[%s]", request, redirectSocket);
        if (request == null) {
            // 处理异常
            return;
        }

        OutputStream redirectOs = redirectSocket.getOutputStream();
        FileOutputStream fos = new FileOutputStream("e:\\dump\\test-" + System.currentTimeMillis());
        TeeOutputStream tos = new TeeOutputStream(redirectOs, fos);

        OutputStream output = tos;
        // 输出到原始服务器
        // redirectOs.write(request.getOriginalMessage());
        // 写请求行
        output.write(CloudUtils.toHeaderBytes(request.getMethodName()));
        output.write(BLANK);
        output.write(CloudUtils.toHeaderBytes(request.getContextPath()));
        output.write(BLANK);
        output.write(CloudUtils.toHeaderBytes(request.getVersion()));

        // 写请求头
        for (Iterator<Map.Entry<String, String>> it = request.getHeaders().entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, String> entry = it.next();
            output.write(CloudUtils.toHeaderBytes(entry.getKey()));
            output.write(COLON);
            output.write(CloudUtils.toHeaderBytes(entry.getValue()));
        }
        output.write(CRLF);  // 写一个消息体与消息体之间的空行

        if (request.getMessageBody() != null) {
            output.write(request.getMessageBody());
        }
        fos.close();
    }

    /**
     * 将响应写入客户端。
     * @param redirectSocket
     * @param response
     * @throws IOException
     */
    private void writeToClient(Socket clientSocket, Response response, Request req) throws IOException {
        LogWriter.debug2(logger, "将响应[%s]写回客户端[%s]", response, clientSocket);
        if (response == null) {
            LogWriter.warn2(logger, "针对请求[%s]的响应为空。", req);
            return;
        }
        OutputStream redirectOs = clientSocket.getOutputStream();
        // 输出到原始服务器
        redirectOs.write(response.getOriginalMessage());
    }

    /**
     * 将此请求转发给资源和应用管理器。
     * @param cattle 应用服务器 不可为空
     * @return
     */
    private Socket createRedirectSocket(Cattle cattle) {
        Socket redirectSocket = null;
        try {
            redirectSocket = new Socket(cattle.getIp(), cattle.getPort());
        } catch (IOException ex) {
            // FIXME: 抛出特定的异常（如果正好选中的牛已经不可用（宕机），需要重新选择一个特定的牛？）
            LogWriter.warn2(logger, ex, "连接服务器[%s],端口[%d]失败。", cattle.getIp(), cattle.getPort());
        }
        return redirectSocket;
    }
}
