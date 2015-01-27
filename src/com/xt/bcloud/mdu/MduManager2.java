package com.xt.bcloud.mdu;

import com.xt.bcloud.comm.CloudUtils;
import com.xt.bcloud.mdu.command.CommandServer;
import com.xt.bcloud.mdu.command.ProcessInfo;
import com.xt.bcloud.resource.server.ServerInfo;
import com.xt.core.app.Startable;
import com.xt.core.log.LogWriter;
import com.xt.core.utils.CollectionUtils;
import com.xt.core.utils.VarTemplate;
import com.xt.gt.sys.SystemConfiguration;
import com.xt.gt.sys.SystemConstants;
import com.xt.gt.sys.loader.SystemLoader;
import com.xt.gt.sys.loader.SystemLoaderManager;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.prefs.Preferences;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * 用于部署和更新应用服务器的部署器。通过规则制定部署路径。
 * FIXME: 需要一个守护进程来保证此监听一直生效，这个应用和监听互为监护
 * FIXME: MDU 自身的更新是一个问题！
 * @author Albert
 */
public class MduManager2 {

    /**
     * MDU 的本地端口号
     */
    public static final String MDU_MANAGERMANAGER_PORT = "MduManager.managerPort";
    /**
     * MDU 的 OID，用于应用服务器重启的情况
     */
    public static final String MDU_MANAGERMANAGER_OID = "MduManager.oid";
    /**
     * 用于管理的端口号
     */
    public final static int managerPort = SystemConfiguration.getInstance().readInt(MDU_MANAGERMANAGER_PORT, 12000);
    
    /**
     * 日志实例
     */
    private final static Logger logger = Logger.getLogger(MduManager2.class);
    
    /**
     * 当前服务器实例
     */
    private final PhyServer phyServer = new PhyServer();
    /**
     * 唯一实例
     */
    private static final MduManager2 instance = new MduManager2();
    /**
     * 接受和处理命令的服务器
     */
    private final CommandServer commandServer = new CommandServer(managerPort);
    /**
     * 存放当前所有的应用服务器实例
     */
    private final Map<String, AppServerInstance> instanceMap = Collections.synchronizedMap(new HashMap());
    
    private final MduService mduService = CloudUtils.createMduService();

    private MduManager2() {
    }

    static public MduManager2 getInstance() {
        return instance;
    }

    public boolean init() {
        String exeSubfix = SystemConfiguration.getInstance().readString("executable.subfix");
        if (StringUtils.isEmpty(exeSubfix)) {
            exeSubfix = CloudUtils.isWindows() ? "bat" : "sh";
            SystemConfiguration.getInstance().set("suffix", exeSubfix);
        }
        return true;
    }

    synchronized public void start() {
        LogWriter.info2(logger, "启动物理服务器，端口[%s]", managerPort);
        // 向系统注册表注册自己的管理端口
        Preferences.userRoot().put(MDU_MANAGERMANAGER_PORT, String.valueOf(managerPort));

        Thread t = new Thread(new Runnable() {

            public void run() {
                // 本身需要启动一个管理端口
                commandServer.startServer();
            }
        });
        t.setDaemon(true);
        t.start();

        // 向服务器进行注册
        // FIXME: 如果注册失败（ARM可能宕机），需要循环注册
        register();

        // 启动已有工作实例
        List<AppServerInstance> instanceList = mduService.listAppServerInstances(phyServer);
        startupServers(instanceList);
    }

    private List<AppServerInstance> startupServers(List<AppServerInstance> instanceList) {
        if (instanceList == null) {
            return Collections.EMPTY_LIST;
        }
        for (Iterator<AppServerInstance> it = instanceList.iterator(); it.hasNext();) {
            AppServerInstance asInstance = it.next();
            if (asInstance == null) {
                continue;
            }
            // 检查应用是否已经启动（用于MduManager可能意外停止的情况）
            if (isAlive(asInstance)) {
                continue;
            }
            LogWriter.info2(logger, "启动应用服务器实例[%s]", asInstance);
            CloudUtils.executeCommand(asInstance.getStartupCmd());
            this.instanceMap.put(asInstance.getOid(), asInstance);
        }
        return instanceList;
    }
    
    private boolean isAlive(AppServerInstance asInstance) {
        ServerInfo serverInfo = mduService.getServerInfo(asInstance);
        if (serverInfo == null) {
            return false;
        }
        return CloudUtils.isAlive(serverInfo);
    }

    /**
     * 正常情况下，不需要这个停止操作，客户端应一直运行
     */
    public void stop() {
        LogWriter.info2(logger, "停止此物理服务器[%s]", phyServer);
        unregister();
        this.commandServer.stop();
    }

    public PhyServer getPhyServer() {
        return phyServer;
    }

    /**
     * 向服务器注册，表明自己的身份。
     */
    public void register() {
        phyServer.setOid(getOid());
        phyServer.setValid(true);
        phyServer.setIp(CloudUtils.getLocalHostAddress());
        phyServer.setName(CloudUtils.getComputerName());
        phyServer.setManagerPort(managerPort);
        phyServer.setOsName(System.getProperty("os.name"));
        phyServer.setOsVersion(System.getProperty("os.version"));
        phyServer.setTempPath(System.getProperty("java.io.tmpdir"));
        phyServer.setUserPath(System.getProperty("user.dir"));
        
        // 从文件名中取得workpath
        String workPath = SystemConfiguration.getInstance().readString("workPath",
                FilenameUtils.concat(System.getProperty("user.home"), FilenameUtils.concat("workspace", "${_time}")));
        phyServer.setWorkPath(workPath);
        phyServer.setState(PhyServerState.AVAILABLE);
        
        LogWriter.info2(logger, "开始注册物理服务器实例[%s]", phyServer);
        mduService.registerPhyServer(phyServer);
    }

    /**
     * 读取此物理服务器的唯一编码
     * @return 
     */
    private String getOid() {
        String oid = Preferences.userRoot().get(MDU_MANAGERMANAGER_OID, "");
        LogWriter.info2(logger, "从注册表中获取OID[%s]", oid);
        if (StringUtils.isEmpty(oid)) {
            oid = UUID.randomUUID().toString();
            // 将UUID写入注册表
            Preferences.userRoot().put(MDU_MANAGERMANAGER_OID, oid);
        }
        return oid;
    }

    /**
     * 向服务器注销。
     */
    synchronized public void unregister() {
        LogWriter.info2(logger, "注销物理服务器[%s]", phyServer);
        mduService.unregisterPhyServer(phyServer);
    }

    synchronized public void registerAppServerInstance(AppServerInstance asInstance) {
        LogWriter.info2(logger, "注册应用服务器实例[%s]", asInstance);
        if (asInstance == null) {
            return;
        }
        instanceMap.put(asInstance.getOid(), asInstance);
    }

    public void updateProcessInfo(ProcessInfo process) {
        LogWriter.info2(logger, "更新进程信息[%s]", process);
        String instanceOid = process.getAppServerInstanceOid();
        AppServerInstance asInstance = instanceMap.get(instanceOid);
        LogWriter.info2(logger, "找到对应的应用服务器实例进程信息[%s]", asInstance);
        // 如果没找到,尝试去服务器进行查找
        if (asInstance == null) {
            asInstance = mduService.getAppServerInstance(instanceOid);
            if (asInstance != null) {
                this.instanceMap.put(instanceOid, asInstance);
            }
        }
        
        if (asInstance == null) {
            LogWriter.warn2(logger, "未找到实例[%s]的注册信息。", instanceOid);
            return;
        }
        String stopCmd = asInstance.getStopCmd();
        // 补充停止命令需要的“进程号”（PID）
        Map params = CollectionUtils.toMap("pid", process.getPid());
        stopCmd = VarTemplate.format(stopCmd, params);
        asInstance.setStopCmd(stopCmd);
        // 补充强制停止（Kill）命令需要的“进程号”（PID）
        String killCmd = asInstance.getKillCmd();
        killCmd = VarTemplate.format(killCmd, params);
        asInstance.setKillCmd(killCmd);
        
        LogWriter.info2(logger, "更新强制停止命令[%s]和强制停止命令[%s]", stopCmd, killCmd);
        mduService.updateAppServerInstance(asInstance);
    }
    
    static public void main(String[] argv) {
        // -l com.xt.gt.sys.loader.CommandLineSystemLoader -m CLIENT_SERVER -p local -f conf\gt-config.xml
        if (argv.length == 0) {
            argv = new String[]{"-l", "com.xt.gt.sys.loader.CommandLineSystemLoader",
                "-m", "CLIENT_SERVER",
                "-p", "local", "-f", "conf\\gt-config.xml"};
        }
        SystemLoaderManager slManager = SystemLoaderManager.getInstance();
        slManager.init(argv);
        SystemConfiguration.getInstance().set(SystemConstants.APP_CONTEXT, new File("").getAbsolutePath());
        SystemLoader loader = slManager.getSystemLoader();
        if (loader.getConfigFile() != null) {
            SystemConfiguration.getInstance().load(loader.getConfigFile(), false);
        }
        // 应用关闭时自动注销
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            public void run() {
                try {
                    MduManager2.getInstance().stop();
                } catch (Throwable t) {
                    LogWriter.warn2(logger, t, "物理服务器[%s]被注销时出现异常。",
                            MduManager2.getInstance().getPhyServer());
                }
            }
        }));
        MduManager2.instance.init();
        MduManager2.instance.start();
    }
}
