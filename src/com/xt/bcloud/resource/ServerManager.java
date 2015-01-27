package com.xt.bcloud.resource;

import com.xt.bcloud.comm.CloudUtils;
import com.xt.bcloud.comm.Constants;
import com.xt.bcloud.comm.ServerThread;
import com.xt.bcloud.resource.server.ServerInfo;
import com.xt.bcloud.worker.Cattle;
import com.xt.core.exception.SystemException;
import com.xt.core.log.LogWriter;
import com.xt.core.utils.StringUtils;
import com.xt.gt.sys.SystemConfiguration;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.logging.Level;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

/**
 *
 * @author albert
 */
public class ServerManager {

    /**
     * ��������������ID��ͨ���˲�����������һ��ϵͳ��ͬʱע������������ ���Ϊ���ô˲�����ϵͳ��Ĭ��ֵΪ������ַ��
     */
    public static final String PARAM_SERVER_ID = "serverId";
    /**
     * ��������õ�������(ע�⣺���õ�ʱ��һ��Ҫ�ԡ�/����ͷ) FIXME: ����ͨ�� JMX ��������ж�ȡ
     */
    public final String serverMgrContextPath = SystemConfiguration.getInstance().readString("serverMgr.contextPath", "/serv1");
    /**
     * �������Ķ˿ں� FIXME: ����ͨ�� JMX ��������ж�ȡ
     */
    private final int port = SystemConfiguration.getInstance().readInt("sererMgr.port", 18000);
    private final int customizedJmxRmiPort = SystemConfiguration.getInstance().readInt("serverMgr.jmx.rmi.port", -1);
    /**
     * �Ѿ�ע��ķ�������
     */
    private ServerInfo serverInfo;
//    /**
//     * �Ѿ��������ȥ����Դ(ţ)��
//     */
//    private final Map<Cattle, Process> soldedCattles = new HashMap();
    private final static ServerManager instance = new ServerManager();
    private final Logger logger = Logger.getLogger(ServerManager.class);
    private Profile profile = new Profile();

    private ServerManager() {
    }

    static public ServerManager getInstance() {
        return instance;
    }
    /**
     * ��Դ״̬
     */
    private ResourceState state;

    /**
     * ����Դ��������ע����Ϣ��֪ͨ�������
     *
     * @param profile
     */
    synchronized public void register(Profile profile) {
        if (serverInfo != null) {
            throw new ResourceException(String.format("������[%s]�Ѿ�ע�ᡣ", serverInfo));
        }
        ResourceService resourceSerivce = CloudUtils.createResourceService();

        ServerInfo _serverInfo = getServerInfo();
        LogWriter.info(logger, String.format("����ע�������[%s], ʹ������[%s]", _serverInfo, profile));
        resourceSerivce.registerServer(_serverInfo, profile);
        this.serverInfo = _serverInfo;  // ������ֻ��ע��һ��

    }

    public ServerInfo getServerInfo() {
        ServerInfo _serverInfo = new ServerInfo();
        String ip = CloudUtils.getLocalHostAddress();
        // ȡ���õķ����� ID
        String serverId = SystemConfiguration.getInstance().readString(PARAM_SERVER_ID, ip);
        _serverInfo.setId(serverId);
        _serverInfo.setIp(ip);
        _serverInfo.setManagerPort(port);
        _serverInfo.setAppServerInstanceOid(getAppServerInstanceOid());  // ����Ϊ��
        _serverInfo.setContextPath(serverMgrContextPath);
        _serverInfo.setJmxRmiPort(CloudUtils.getJmxRmiPort(customizedJmxRmiPort));
        _serverInfo.setName(CloudUtils.getComputerName());
        return _serverInfo;
    }

    /**
     * // ��ϵͳ�����ж�ȡӦ�÷�����ʵ����OID��ע�⣬���OID��ͨ�������ķ�ʽд�뵽�����ļ��У�
     *
     * @return
     */
    private String getAppServerInstanceOid() {
        String oid = System.getProperty(Constants.APP_SERVER_INSTANCE_OID);
        if (org.apache.commons.lang.StringUtils.isEmpty(oid)) {
            oid = SystemConfiguration.getInstance().readString(Constants.APP_SERVER_INSTANCE_OID, "");
        }
        LogWriter.info2(logger, "��ȡ������[%s=%s]", Constants.APP_SERVER_INSTANCE_OID, oid);
        return oid;
    }

    /**
     * ע���˷�����
     */
    synchronized public void unregister() {
        if (serverInfo == null) {
            return;
        }
        LogWriter.info(logger, String.format("����ע��������[%s]��", serverInfo));
        ResourceService resourceSerivce = CloudUtils.createResourceService();
        resourceSerivce.unregisterServer(serverInfo);
    }

    /**
     * ������������Դ
     */
    private void run(Profile profile) {
        this.profile = profile;

        // �����������̨
        // createMgrServer();

        //TODO: ���Թ���ӿڵ������Ƿ�ɹ�

        // ����Դ������ע���������Ϣ
        // register(profile);

        // 
        startServer();
        while (true) {
            try {
                synchronized (this) {
                    wait(10000);
                }
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(ServerManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * ����һ��Ӧ�÷�������
     */
    private void createMgrServer() {
        ServerThread serverThread = new ServerThread(port, "/serverMgr", "E:/work/xthinker/B-Cloud/web/");
        Thread t = new Thread(serverThread);
        t.start();
    }

    /**
     * ����һ����������Դ�� ʹ�ý��̷�ʽ��������һ��������
     */
    private Process startServer() {
        // java -cp .\*;..\lib\*;..\lib\jetty\* com.xt.bcloud.worker.JettyIgniter -a appId -d "E:\work\xthinker\B-Cloud\web"  -c "/gt_demo" -p 8899
        String[] args = {};
        ProcessBuilder pb = new ProcessBuilder("java", "com.xt.bcloud.worker.JettyIgniter",
                "-a", "appId", "-d", "E:/work/xthinker/B-Cloud/web", "-c", "/gt_demo", "-p", "8899");
        Map<String, String> env = pb.environment();
        env.put("cp", ".\\*;..\\lib\\*;..\\lib\\jetty\\*");
        pb.directory(new File("E:/work/xthinker/B-Cloud/dist"));
        try {
            Process p = pb.start();
            // p.
            return p;
        } catch (IOException ex) {
            throw new ResourceException("��������������ʱ�����쳣��", ex);
        }
    }

//    /**
//     * ʹ���̷߳�ʽ����һ������������
//     * @param cattle
//     */
//    public void startServerThread(Cattle cattle, String deployPath) {
//        ServerThread serverThread = new ServerThread(cattle.getPort(), cattle.getContextPath(), deployPath);
//        Thread t = new Thread(serverThread);
//        t.start();
//    }
    /**
     * ֹͣһͷţ�Ĺ���
     *
     * @param cattle
     */
    private void stop(Cattle cattle) {
    }

    /**
     * ����ϵͳ����
     *
     * @param args
     * @return
     */
    static private Profile parse(String[] args) {

        Profile profile = new Profile();

        Logger logger = Logger.getLogger(ServerManager.class);
        LogWriter.info(logger, "���ڽ�������", StringUtils.join(args, ","));

        Options options = new Options();

        // ϵͳ����ʱ���õ��������
        Option fileOpt = new Option("f", "file", true, "�����ļ�");
        Option ramOpt = new Option("r", "ram", true, "�ڴ���������λ��M��");
        Option cpuOpt = new Option("c", "cpu", true, "��������");
        options.addOption(fileOpt).addOption(ramOpt).addOption(cpuOpt);
        BasicParser bp = new BasicParser();
        try {
            CommandLine commandLine = bp.parse(options, args);
            String configFile = commandLine.getOptionValue(fileOpt.getOpt());
            LogWriter.info(logger, "configFile", configFile);

            String ramString = commandLine.getOptionValue(ramOpt.getOpt());
            LogWriter.info(logger, "ramString", ramString);


            String cpuString = commandLine.getOptionValue(cpuOpt.getOpt());
            LogWriter.info(logger, "cpuString", cpuString);
        } catch (ParseException ex) {
            throw new SystemException(String.format("���������в���[%s]�쳣��", StringUtils.join(args, ",")), ex);
        }
        return profile;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    static public void main(String[] argv) {
        // ��黷�������� JAVA_HOME �Ƿ�����

        //Profile profile = parse(argv);
        Profile profile = new Profile();
        ServerManager sMgr = new ServerManager();
        sMgr.run(profile);
    }
}
