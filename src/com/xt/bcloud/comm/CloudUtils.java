package com.xt.bcloud.comm;

import com.xt.bcloud.mdu.MduException;
import com.xt.bcloud.mdu.MduService;
import com.xt.bcloud.mdu.command.ProcessInfo;
import com.xt.bcloud.mdu.service.MakingService;
import com.xt.bcloud.resource.EchoService;
import com.xt.bcloud.resource.ResourceException;
import com.xt.bcloud.resource.ResourceService;
import com.xt.core.exception.SystemException;
import com.xt.core.json.JsonBuilder;
import com.xt.proxy.Proxy;
import com.xt.proxy.ServiceFactory;
import com.xt.proxy.impl.http.stream.HttpStreamProxy;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.HashMap;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import static com.xt.bcloud.resource.arm.ArmConf.*;
import com.xt.bcloud.resource.server.ServerInfo;
import com.xt.bcloud.td.http.ErrorFactory;
import java.io.*;
import java.util.Map;
import static com.xt.bcloud.td.http.HttpConstants.*;
import com.xt.bcloud.td.http.HttpException;
import com.xt.bcloud.td7.BufferFactory;
import com.xt.core.app.Stoper;
import com.xt.core.log.LogWriter;
import com.xt.core.utils.CommandLineParser;
import com.xt.gt.sys.SystemConfiguration;
import java.net.*;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.*;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/**
 * �� B-Cloud ��صĸ���������.
 *
 * @author albert
 */
public class CloudUtils {

    private final static Logger logger = Logger.getLogger(CloudUtils.class);
    /**
     * ר�����ڼ�¼�������־
     */
    private final static Logger commandLogger = Logger.getLogger(CloudUtils.class.getName() + ".command");
    private static final byte[] NULL_BYTES = new byte[0];

    /**
     * ����ϵͳ��ǰ�Ĺ���Ŀ¼
     *
     * @return
     */
    static public File getTempDir() {
        String tempDir = SystemConfiguration.getInstance().readString("temp.dir");
        if (StringUtils.isEmpty(tempDir)) {
            tempDir = System.getProperty("java.io.tmpdir");
        }
        if (StringUtils.isEmpty(tempDir)) {
            tempDir = "temp";
        }
        File dir = new File(tempDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * ͨ��������ȡJMX �ӿ�
     *
     * @return
     */
    static public int getJmxRmiPort(int customizedJmxRmiPort) {
        int jmxRmiPort = -1;
        // ����û�δ�Զ��壬��ϵͳ�����ж�ȡ��
        if (customizedJmxRmiPort < 0) {
            String temp = System.getProperty("com.sun.management.jmxremote.port");
            if (org.apache.commons.lang.StringUtils.isNotEmpty(temp)) {
                jmxRmiPort = Integer.parseInt(temp);
            }
        } else {
            jmxRmiPort = customizedJmxRmiPort;
        }
        return jmxRmiPort;
    }

    /**
     * ���������Ƿ��������С�
     *
     * @param serverInfo
     * @return
     */
    static public boolean isAlive(String ip, int managerPort, String contextPath) {
        if (StringUtils.isEmpty(ip)) {
            return false;
        }
        // ����Դ����������Դ
        String proxyUrl = String.format("http://%s:%d%s", ip,
                managerPort, contextPath);
        Proxy proxy = new HttpStreamProxy(proxyUrl);
        try {
            EchoService echoService = ServiceFactory.getInstance().getService(EchoService.class, proxy);
            if (null != echoService.echo("Hi")) {
                return true;
            }
        } catch (Throwable t) {
            // ignored
            LogWriter.warn2(logger, t, "��ȡURL[%s]��EchoService��ʧ��", proxyUrl);
        }
        return false;
    }

    /**
     * ���������Ƿ��������С�
     *
     * @param serverInfo
     * @return
     */
    static public boolean isAlive(ServerInfo serverInfo) {
        if (serverInfo == null) {
            return false;
        }
        return isAlive(serverInfo.getIp(), serverInfo.getManagerPort(),
                serverInfo.getContextPath());
    }

    /**
     * ���������Ƿ��������С�
     *
     * @param processInfo
     * @return
     */
    static public boolean isAlive(ProcessInfo processInfo) {
        if (processInfo == null) {
            return false;
        }
        return isAlive(processInfo.getIp(), processInfo.getManagerPort(),
                processInfo.getContextPath());
    }

    /**
     * ����ǿ��ֹͣ�����ģ�塣
     *
     * @return
     */
    static public String getKillCmdTemplate() {
        if (isWindows()) {
            return "tskill ${pid}";
        } else {
            return "kill -9 ${pid}";
        }
    }

    /**
     * �ж�ϵͳ�����͡�
     *
     * @return
     */
    static public boolean isWindows() {
        String osName = System.getProperty("os.name", "");
        return osName.toLowerCase().contains("windows");
    }

    public static ProcessResult executeCommand(final String cmdStr) {
        CommandLineParser clp = new CommandLineParser();
        String[] cmds = clp.parse(cmdStr);
        return executeCommand(cmds);
    }

    /**
     * ִ��һ��������ؽ��̺�
     *
     * @param cmd
     * @return
     */
    public static ProcessResult executeCommand(final String[] cmd) {
        if (cmd == null || cmd.length == 0) {
            throw new SystemException("��ִ�е������Ϊ�ա�");
        }
        final ProcessResult processInfo = new ProcessResult();

        Thread commandThread = new Thread(new Runnable() {

            public void run() {
                try {
                    // ���������е��ļ�·��
//                    LogWriter.info2(logger, "����ǰ������[%s]", cmd);
//                    String[] _cmd = FilenameUtils.normalize(cmd[0]);
                    LogWriter.info2(logger, "��ʼִ�е�����[%s]", StringUtils.join(cmd, " "));
                    ProcessBuilder processBuilder = new ProcessBuilder(cmd);

                    File file = new File(cmd[0]);
                    processBuilder.directory(file.getParentFile());
                    Process process = processBuilder.start();


                    BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    String input = null;
                    String error = null;
                    long startTime = System.currentTimeMillis();
                    while ((input = inputReader.readLine()) != null
                            && ((error = errorReader.readLine()) != null)
                            // ֻ��¼һ��ʱ���ڵ���־��
                            && (System.currentTimeMillis() - startTime < 5 * 60 * 1000)) {
                        if (input != null) {
                            commandLogger.info(input);
                        }
                        if (error != null) {
                            commandLogger.info(error);
                        }

                        // �ڳ����������ֹ
                        if (Stoper.getInstance().isStoped()) {
                            break;
                        }
                    }
//                    process.

                    //int exitVal = process.waitFor();
                    //processInfo.setExitVal(exitVal);
                } catch (Exception ex) {
                    throw new MduException(String.format("����[%s]��������", StringUtils.join(cmd, " ")), ex);
                }
            }
        });

        commandThread.setName("CloudUtils.executeCommand");
        commandThread.setDaemon(true);
        commandThread.start();

        //TODO: ѭ�����Ӧ�ò����Ƿ�ɹ�

//        ProcessBuilder processBuilder = new ProcessBuilder(cmd);

//        try {
//            process = processBuilder.start();
//        } catch (IOException ex) {
//            throw new MduException(String.format("����[%s]��������", cmd), ex);
//        }
//        if ("java.lang.UnixProcess".equals(process.getClass().getName())) {
//            // Unix ƽ̨�ϻ�ȡPID
//        } else {
//            // Windows ƽ̨�ϻ�ȡPID
//        }
        return processInfo;
    }

    /**
     * �õ���ǰ��Ӧ������
     *
     * @return
     */
    public static String getComputerName() {
        //System.getProperties().list(System.out);

        // ��������
        Map<String, String> map = System.getenv();
//        for (Map.Entry<String, String> entry : map.entrySet()) {
//            String key = entry.getKey();
//            String value = entry.getValue();
//            System.out.println("getComputerName key=" + key + "; value=" + value);
//        }

        if (isWindows()) {
            String userName = map.get("USERNAME");// ��ȡ�û���
            String computerName = map.get("COMPUTERNAME");// ��ȡ�������
            String userDomain = map.get("USERDOMAIN");// ��ȡ���������
            return String.format("%s:%s:%s", userDomain, computerName, userName);
        } else {
            // ��linux�����²������岻ͬ
            String ip = getLocalHostAddress();
            String userName = map.get("USER");
            String hostName = map.get("HOSTNAME");
            return String.format("%s:%s:%s", ip, hostName, userName);
        }
    }

    /**
     * ����һ�� MDU ������ʵ����
     *
     * @return
     */
    public static MduService createMduService() {
        Proxy proxy = createArmProxy();
        MduService mduService = ServiceFactory.getInstance().getService(MduService.class, proxy);
        return mduService;
    }

    /**
     * ����һ����Դ���������ʵ����
     *
     * @return
     */
    public static MakingService createMakingService() {
        Proxy proxy = createArmProxy();
        MakingService makingService = ServiceFactory.getInstance().getService(MakingService.class, proxy);
        return makingService;
    }

    /**
     * ����һ����Դ���������ʵ����
     *
     * @return
     */
    public static ResourceService createResourceService() {
        Proxy proxy = createArmProxy();
        ResourceService resourceSerivce = ServiceFactory.getInstance().getService(ResourceService.class, proxy);
        return resourceSerivce;
    }

    static public URL createArmUrl(Class clazz, String methodName, Object[] params) {
        StringBuilder strBld = new StringBuilder();
        strBld.append("/").append(CONTEXT);
        strBld.append("jsonClient.action?jsonValue=");
        // ����
        HashMap<String, Object> request = new HashMap();
        request.put("serviceClassName", clazz.getName());
        request.put("methodName", methodName);
        request.put("params", params);
        // request.put("type", "");
        JsonBuilder builder = new JsonBuilder();
        strBld.append(builder.build(request));
        try {
            URL url = new URL(PROTOCOL, IP, PORT, strBld.toString());
            return url;
        } catch (MalformedURLException ex) {
            throw new SystemException("��Դ��Ӧ�ù������ĵ�ַ����", ex);
        }
    }

    /**
     * ����һ�����Է�����Դ��Ӧ�õĴ���
     *
     * @return
     */
    static public Proxy createArmProxy() {
        if (StringUtils.isEmpty(URL)) {
            throw new ResourceException("δ��ȡ��Ӧ�ù������ĵ�ַ��");
        }
        List<String> availableUrls = new ArrayList<String>();
        availableUrls.add(URL);
        if (URLS != null) {
            availableUrls.addAll(Arrays.asList(URLS));
        }
        for (Iterator<String> it = availableUrls.iterator(); it.hasNext();) {
            String url = it.next();
            if (isAccessible(url)) {
                Proxy proxy = new HttpStreamProxy(url);
                return proxy;
            }
        }
        throw new ResourceException("û�п��õ���Դ��������");
    }

    static public boolean isAccessible(String url) {
        if (StringUtils.isEmpty(url)) {
            return false;
        }
        try {
            URL url0 = new URL(String.format("%s%s", url, "ping.txt"));
            URLConnection conn = url0.openConnection();
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(1000);
            conn.connect();
            InputStream is = conn.getInputStream();
            is.read(new byte[2]);
            is.close();
            return true;
        } catch (MalformedURLException ex) {
            LogWriter.warn2(logger, ex, "��ַ[%s]��ʽ����", url);
        } catch (IOException ex) {
            LogWriter.warn2(logger, ex, "���ܷ��ʵ�ַ[%s]��", url);
        }
        return false;
    }

    /**
     * ��һ���ַ���д��ͨ����
     *
     * @param buf
     * @param value ��������ַ������������ASCII ���뷽ʽ
     * @param channel
     * @throws java.io.UnsupportedEncodingException
     * @throws java.io.IOException
     */
    static public void write(ByteBuffer buf, String value, ByteChannel channel)
            throws UnsupportedEncodingException, IOException {
        if (value == null) {
            return;
        }
        byte[] bytes = value.getBytes(HTTP_HEADER_ENCODING);
        int count = 0;
        // ����Ҫд������ݴ��ڻ�����ʱ����Ҫʹ�ö��д�뷽ʽ
        while (count * buf.capacity() < bytes.length) {
            buf.clear();
            int length = ((count + 1) * buf.capacity() > bytes.length) ? bytes.length - count * buf.capacity() : buf.capacity();
            buf.put(bytes, count * buf.capacity(), length);
            buf.flip();
            channel.write(buf);
            count++;
        }
    }

    static public byte[] toHeaderBytes(String header) throws UnsupportedEncodingException {
        if (StringUtils.isEmpty(header)) {
            return NULL_BYTES;
        }
        byte[] bytes = header.getBytes(HTTP_HEADER_ENCODING);
        return bytes;
    }

    static public ByteBuffer toHeaderBuffer(String header) {
        if (StringUtils.isEmpty(header)) {
            return BufferFactory.getInstance().wrap(NULL_BYTES);
        }
        byte[] bytes;
        try {
            bytes = header.getBytes(HTTP_HEADER_ENCODING);
        } catch (UnsupportedEncodingException ex) {
            throw new SystemException(String.format("����[%s]�����ڡ�", HTTP_HEADER_ENCODING), ex);
        }
        return BufferFactory.getInstance().wrap(bytes);
    }

    /**
     * ��һ���ֽ�д�����ͨ��
     *
     * @param buf ָ���Ļ�����
     * @param b ָ��ֱ��
     * @param channel
     * @throws java.io.UnsupportedEncodingException
     * @throws java.io.IOException
     */
    static public void writeByte(ByteBuffer buf, byte b, ByteChannel channel)
            throws UnsupportedEncodingException, IOException {
        buf.clear();
        buf.put(b);
        buf.flip();
        channel.write(buf);
    }

    /**
     * �������һ��UUID�� ע���˷��������ڲ�ʹ�ã������㷨������ʱ�޸ġ�
     *
     * @return
     */
    static public String generateOid() {
        return UUID.randomUUID().toString();
    }

    /**
     * ���ر��ص�IP��ַ��
     * FIXME: ����ǰ׺���ر��ص�ַ��
     * @return
     */
    static public String getLocalHostAddress() {
        String host = SystemConfiguration.getInstance().readString("system.localAddress");
        if (StringUtils.isEmpty(host)) {
            try {
                InetAddress ia = InetAddress.getLocalHost();
                // ��Linux�汾�£�����������Ƿ��ء�127.0.0.1��.(������Ϊ�ǡ�/etc/hosts��д�ò�ǡ��)��������Σ�����ͨ���ӿڷ�ʽ�ڲ���һ�Ρ�
                if (!ia.isLoopbackAddress()) {
                    host = ia.getHostAddress();
                } else {
                    for (Enumeration<NetworkInterface> it = NetworkInterface.getNetworkInterfaces(); it.hasMoreElements();) {
                        NetworkInterface ni = it.nextElement();
                        if (ni.isLoopback()) {
                            continue;
                        }
                        for (Enumeration<InetAddress> it2 = ni.getInetAddresses(); it2.hasMoreElements();) {
                            InetAddress ia2 = it2.nextElement();
                            if (!ia2.isLoopbackAddress()) {
                                host = ia2.getHostAddress();
                                break;
                            }
                        }
                    }
                }
            } catch (SocketException ex) {
                throw new ResourceException("��ȡ���� IP ��ַ�쳣��", ex);
            } catch (UnknownHostException ex) {
                throw new ResourceException("��ȡ���� IP ��ַ�쳣��", ex);
            }
        }
        return host;
    }

    static public SelectionKey registerChannel(Selector selector,
            SelectableChannel channel, int ops) {
        return registerChannel(selector, channel, ops, null);
    }

    /**
     * Register the given channel with the given selector for the given
     * operations of interest
     */
    static public SelectionKey registerChannel(Selector selector,
            SelectableChannel channel, int ops, Object att) {
        if (channel == null) {
            return null; // could happen
        }
        try {
            // Set the new channel nonblocking
            channel.configureBlocking(false);

            // Register it with the selector
            return channel.register(selector, ops, att);
        } catch (IOException ex) {
            throw new HttpException(ErrorFactory.ERROR_503, ex);
        }
    }
}
