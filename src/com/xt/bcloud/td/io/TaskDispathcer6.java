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
 * ʹ�ô�ͳ��IO��ʽ����Ϣ����ת���Ϳ��ơ����ַ�ʽ�ĺô��Ǵ���򵥣�����Ҫ����Ŀ���HTTPЭ�鱾������ݡ�
 * ��ȱ���ǣ���Ҫ����̫����̣߳���ʹ�����̳߳ؼ�����Ҳ���ܶ�֧�ָ���Ŀͻ��˲���Ӱ�졣
 *
 * TODO: �����Ƿ���Ҫ�����������ƣ����������ƣ�����
 * @author albert
 */
public class TaskDispathcer6 {

    private final Logger logger = Logger.getLogger(TaskDispathcer6.class);
    /**
     * ռ�õĶ˿ںţ���ͨ�������в�����-p������ָ����Ĭ���ǣ�6666��
     */
    private int port = 4900;  //6666;
    

    /**
     * ֹͣ�ı��
     */
    private volatile boolean stopFlag = false;

    // private static int seq = 0;

    /**
     * �ɴ���������������
     */
    private static final int MAX_CONNECTION_COUNT = 100;

    /**
     * ��ǰ���ڴ�������ӵĸ�����
     */
    private volatile int processingConnectionCount = 0;
    
    /**
     * �Ƿ񵼳����������� HTTP ��Ϣ����������������쳣�����Ӧ�ó������쳣��
     * ���������Ӧ��Ӧ���Ǳ������ġ�
     */
    private final boolean dump = false;
    
    /**
     * ������ʵ��������ʵ������ʵ��Ϊ���̸߳���ʵ�������Ҫ���䷽��Ϊ�̰߳�ȫ�ķ�����
     */
    private final CattleManager cattleManager = CattleManager.getInstance();
    /**
     * bindAddr ���������� ServerSocket �Ķ�Ѩ���� (multi-homed host) ��ʹ�ã�
     * ServerSocket �����ܶ����ַ֮һ������������� bindAddr Ϊ null��
     * ��Ĭ�Ͻ����κ�/���б��ص�ַ�ϵ����ӡ�
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
            LogWriter.error(logger, "������������ʱ�����쳣��", ex);
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
        // ��ʼ���������
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
     * ��׼�����������������������Ӧ��ԭʼ��Ϣ�����ļ��Ƚ��ʡ�
     */
    private Dumpable stdDumper = new DefaultDumper();
    /**
     * ����������쳣ʱ���糬ʱ�����󣬷��������ڵȣ���
     * ��ʹ�ô˵�����������Ӧ��������Ϣ���ѱ���졣
     */
    private Dumpable errorDumper = (Dumpable) SystemConfiguration.getInstance().readObject("errorDumper", new ErrorDumper());
    /**
     * ��¼�̵߳�����ʱ�䣨���ڼ��㳬ʱ��
     */
    private final long startTime;
    /**
     * ���������Ӧ������д�ĸ�д����
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

            byte[] b = new byte[1024 * 1024]; // ÿ�ζ�ȡ��������
            int count = 0; // һ�ζ�ȡ���ֽ���

            Socket redirectSocket = null;

            // ������д����������
            while (true) {
                requestParser = new HttpRequestParser();  // ��Ҫ����
                HttpResponseParser responseParser = new HttpResponseParser();

                Request request = null;

                long _startTime = System.currentTimeMillis();
                while ((count = clientIn.read(b)) > 0) {
                    // ������ӳ�ʱ��ϵͳ������
                    if (System.currentTimeMillis() - _startTime > upTimeout) {
                        LogWriter.warn2(logger, "�ͻ���[%s]���͵�����[%s]�Ѿ���ʱ %d(ms)��",
                                clientSocket,
                                requestParser.getLastMessage() != null
                                ? ((Request) requestParser.getLastMessage()).getRequestMethod()
                                : "<null>", upTimeout);
                        // dump ��������
                        HttpMessage msg = requestParser.getLastMessage();
                        if (msg != null) {
                            errorDumper.writReq(msg.getOriginalMessage());
                        }
                        // ���ء�����ʱ���������
                        HttpError error = ErrorFactory.getInstance().create("408");
                        throw new HttpException(error);
                    }
                    byte[] _b = new byte[count];
                    System.arraycopy(b, 0, _b, 0, count);  // TODO: ���ж���������ģ���

                    // ����������ļ�
                    if (stdDumper != null) {
                        stdDumper.writReq(_b);
                    }

                    List<HttpMessage> requests = requestParser.parse(_b);
                    if (!requests.isEmpty()) {    // ��ȡ����һ������������
                        request = (Request) requests.get(0);
                        break;
                    }
                }

                if (count < 0) {
//                    // �ͻ��˹ر�
//                    LogWriter.warn2(logger, "�ͻ���[%s]�쳣�رգ�ϵͳ�������������", clientSocket);
//                    break;
                }


                // ����ֵ��ķ�����ʵ��ʧ�ܣ���Ҫ���·��䡣
                boolean found = false;  // �Ƿ��ҵ�
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
                    error.setLocalMessage(String.format("���������[%s]���޿���ʵ����", request.getHost()));
                    throw new HttpException(error);
                }

                // �����������д
                if (rewriter != null) {
                    request = rewriter.rewrite(cattle, request);
                }

                // ������д��ԭʼ������
                writeToOrignalServer(redirectSocket, request);

                InputStream redirectIs = redirectSocket.getInputStream();

                _startTime = System.currentTimeMillis();


                Response response = null;
                while ((count = redirectIs.read(b)) > 0) {
                    // ������ӳ�ʱ��ϵͳ������
                    if (System.currentTimeMillis() - _startTime > downTimeout) {
                        LogWriter.info2(logger, "���շ����[%s]����Ӧ[%s]�Ѿ���ʱ %d(ms)��",
                                redirectSocket, request, downTimeout);

                        // dump ������Ӧ
                        HttpMessage msg = requestParser.getLastMessage();
                        if (msg != null) {
                            errorDumper.writReq(msg.getOriginalMessage());
                        }

                        // ���س�ʱ�����׳�һ����ʱ����
                        throw new HttpException(ErrorFactory.ERROR_504);
                    }

                    byte[] _b = new byte[count];
                    System.arraycopy(b, 0, _b, 0, count);  // TODO: ���ж���������ģ���

                    // ����������ļ�
                    if (stdDumper != null) {
                        stdDumper.writRes(_b);
                    }

                    List<HttpMessage> responses = responseParser.parse(_b);
                    if (!responses.isEmpty()) {  // ���ٶ�ȡһ����������Ӧ
                        response = (Response) responses.get(0);
                        break;
                    }
                }

                if (count < 0 && response == null) {
                    // ��ԭʼ���������������жϣ�����������δ��ȡ����
                    throw new HttpException(ErrorFactory.ERROR_500);
                }

                // �����������д
                if (rewriter != null) {
                    response = rewriter.rewrite(cattle, request, response);
                }

                // �����ԭʼ������
                writeToClient(clientSocket, response, request);
            }
        } catch (IOException ex) {
            // dump ������Ӧ
            if (requestParser != null) {
                HttpMessage msg = requestParser.getLastMessage();
                if (msg != null) {
                    errorDumper.writReq(msg.getOriginalMessage());
                }
            }
            // �쳣�Ĵ�����Ҫ����ϸ��˼���������쳣�������ġ���Ӧ�롱Ӧ���ǲ�һ���ġ�
            // ϵͳ�쳣....
            // �ͻ����쳣��
            System.err.println(ex);
        } catch (HttpException ex) {
            // ���� Http Э�鱾��Ĵ���
            if (clientSocket != null && !clientSocket.isClosed()) {
                try {
                    OutputStream os = clientSocket.getOutputStream();
                    ErrorFactory.writeTo(os, ex);
                    IOHelper.closeSilently(os);
                } catch (IOException ex1) {
                    LogWriter.warn2(logger, ex1, "���� HTTP �쳣[%s]ʱ���ִ���", ex);
                }
            }
        } finally {
            // �ر���Ӧ�ļ�
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
                LogWriter.warn(logger, "�رտͻ���ʱ�����쳣��", ex);
            }

        }
    }

    /**
     * ������д��ԭʼ��������
     * @param redirectSocket
     * @param request
     * @throws IOException
     */
    private void writeToOrignalServer(Socket redirectSocket, Request request) throws IOException {
        LogWriter.debug2(logger, "������[%s]д��ԭʼ������[%s]", request, redirectSocket);
        if (request == null) {
            // �����쳣
            return;
        }

        OutputStream redirectOs = redirectSocket.getOutputStream();
        FileOutputStream fos = new FileOutputStream("e:\\dump\\test-" + System.currentTimeMillis());
        TeeOutputStream tos = new TeeOutputStream(redirectOs, fos);

        OutputStream output = tos;
        // �����ԭʼ������
        // redirectOs.write(request.getOriginalMessage());
        // д������
        output.write(CloudUtils.toHeaderBytes(request.getMethodName()));
        output.write(BLANK);
        output.write(CloudUtils.toHeaderBytes(request.getContextPath()));
        output.write(BLANK);
        output.write(CloudUtils.toHeaderBytes(request.getVersion()));

        // д����ͷ
        for (Iterator<Map.Entry<String, String>> it = request.getHeaders().entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, String> entry = it.next();
            output.write(CloudUtils.toHeaderBytes(entry.getKey()));
            output.write(COLON);
            output.write(CloudUtils.toHeaderBytes(entry.getValue()));
        }
        output.write(CRLF);  // дһ����Ϣ������Ϣ��֮��Ŀ���

        if (request.getMessageBody() != null) {
            output.write(request.getMessageBody());
        }
        fos.close();
    }

    /**
     * ����Ӧд��ͻ��ˡ�
     * @param redirectSocket
     * @param response
     * @throws IOException
     */
    private void writeToClient(Socket clientSocket, Response response, Request req) throws IOException {
        LogWriter.debug2(logger, "����Ӧ[%s]д�ؿͻ���[%s]", response, clientSocket);
        if (response == null) {
            LogWriter.warn2(logger, "�������[%s]����ӦΪ�ա�", req);
            return;
        }
        OutputStream redirectOs = clientSocket.getOutputStream();
        // �����ԭʼ������
        redirectOs.write(response.getOriginalMessage());
    }

    /**
     * ��������ת������Դ��Ӧ�ù�������
     * @param cattle Ӧ�÷����� ����Ϊ��
     * @return
     */
    private Socket createRedirectSocket(Cattle cattle) {
        Socket redirectSocket = null;
        try {
            redirectSocket = new Socket(cattle.getIp(), cattle.getPort());
        } catch (IOException ex) {
            // FIXME: �׳��ض����쳣���������ѡ�е�ţ�Ѿ������ã�崻�������Ҫ����ѡ��һ���ض���ţ����
            LogWriter.warn2(logger, ex, "���ӷ�����[%s],�˿�[%d]ʧ�ܡ�", cattle.getIp(), cattle.getPort());
        }
        return redirectSocket;
    }
}
