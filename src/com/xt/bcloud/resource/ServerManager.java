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
     * 参数：服务器的ID，通过此参数，可以在一个系统上同时注册多个服务器。 如果为配置此参数，系统的默认值为本机地址。
     */
    public static final String PARAM_SERVER_ID = "serverId";
    /**
     * 服务管理用的上下文(注意：配置的时候一定要以“/”开头) FIXME: 可以通过 JMX 从虚拟机中读取
     */
    public final String serverMgrContextPath = SystemConfiguration.getInstance().readString("serverMgr.contextPath", "/serv1");
    /**
     * 服务管理的端口号 FIXME: 可以通过 JMX 从虚拟机中读取
     */
    private final int port = SystemConfiguration.getInstance().readInt("sererMgr.port", 18000);
    private final int customizedJmxRmiPort = SystemConfiguration.getInstance().readInt("serverMgr.jmx.rmi.port", -1);
    /**
     * 已经注册的服务器。
     */
    private ServerInfo serverInfo;
//    /**
//     * 已经被出借出去的资源(牛)。
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
     * 资源状态
     */
    private ResourceState state;

    /**
     * 向资源工厂发起注册信息，通知服务可用
     *
     * @param profile
     */
    synchronized public void register(Profile profile) {
        if (serverInfo != null) {
            throw new ResourceException(String.format("服务器[%s]已经注册。", serverInfo));
        }
        ResourceService resourceSerivce = CloudUtils.createResourceService();

        ServerInfo _serverInfo = getServerInfo();
        LogWriter.info(logger, String.format("正在注册服务器[%s], 使用配置[%s]", _serverInfo, profile));
        resourceSerivce.registerServer(_serverInfo, profile);
        this.serverInfo = _serverInfo;  // 服务器只能注册一次

    }

    public ServerInfo getServerInfo() {
        ServerInfo _serverInfo = new ServerInfo();
        String ip = CloudUtils.getLocalHostAddress();
        // 取配置的服务器 ID
        String serverId = SystemConfiguration.getInstance().readString(PARAM_SERVER_ID, ip);
        _serverInfo.setId(serverId);
        _serverInfo.setIp(ip);
        _serverInfo.setManagerPort(port);
        _serverInfo.setAppServerInstanceOid(getAppServerInstanceOid());  // 可能为空
        _serverInfo.setContextPath(serverMgrContextPath);
        _serverInfo.setJmxRmiPort(CloudUtils.getJmxRmiPort(customizedJmxRmiPort));
        _serverInfo.setName(CloudUtils.getComputerName());
        return _serverInfo;
    }

    /**
     * // 从系统变量中读取应用服务器实例的OID（注意，这个OID是通过变量的方式写入到配置文件中）
     *
     * @return
     */
    private String getAppServerInstanceOid() {
        String oid = System.getProperty(Constants.APP_SERVER_INSTANCE_OID);
        if (org.apache.commons.lang.StringUtils.isEmpty(oid)) {
            oid = SystemConfiguration.getInstance().readString(Constants.APP_SERVER_INSTANCE_OID, "");
        }
        LogWriter.info2(logger, "读取到变量[%s=%s]", Constants.APP_SERVER_INSTANCE_OID, oid);
        return oid;
    }

    /**
     * 注销此服务器
     */
    synchronized public void unregister() {
        if (serverInfo == null) {
            return;
        }
        LogWriter.info(logger, String.format("正在注销服务器[%s]。", serverInfo));
        ResourceService resourceSerivce = CloudUtils.createResourceService();
        resourceSerivce.unregisterServer(serverInfo);
    }

    /**
     * 启动服务器资源
     */
    private void run(Profile profile) {
        this.profile = profile;

        // 启动管理控制台
        // createMgrServer();

        //TODO: 测试管理接口的启动是否成功

        // 向资源管理器注册服务器信息
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
     * 创建一个应用服务器。
     */
    private void createMgrServer() {
        ServerThread serverThread = new ServerThread(port, "/serverMgr", "E:/work/xthinker/B-Cloud/web/");
        Thread t = new Thread(serverThread);
        t.start();
    }

    /**
     * 启动一个服务器资源。 使用进程方式单独启动一个服务器
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
            throw new ResourceException("创建服务器进程时出现异常。", ex);
        }
    }

//    /**
//     * 使用线程方式启动一个服务器进程
//     * @param cattle
//     */
//    public void startServerThread(Cattle cattle, String deployPath) {
//        ServerThread serverThread = new ServerThread(cattle.getPort(), cattle.getContextPath(), deployPath);
//        Thread t = new Thread(serverThread);
//        t.start();
//    }
    /**
     * 停止一头牛的工作
     *
     * @param cattle
     */
    private void stop(Cattle cattle) {
    }

    /**
     * 解析系统参数
     *
     * @param args
     * @return
     */
    static private Profile parse(String[] args) {

        Profile profile = new Profile();

        Logger logger = Logger.getLogger(ServerManager.class);
        LogWriter.info(logger, "正在解析参数", StringUtils.join(args, ","));

        Options options = new Options();

        // 系统启动时将用到这个参数
        Option fileOpt = new Option("f", "file", true, "配置文件");
        Option ramOpt = new Option("r", "ram", true, "内存数量（单位：M）");
        Option cpuOpt = new Option("c", "cpu", true, "代理类型");
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
            throw new SystemException(String.format("解析命令行参数[%s]异常。", StringUtils.join(args, ",")), ex);
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
        // 检查环境变量的 JAVA_HOME 是否设置

        //Profile profile = parse(argv);
        Profile profile = new Profile();
        ServerManager sMgr = new ServerManager();
        sMgr.run(profile);
    }
}
