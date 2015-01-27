package com.xt.bcloud.mdu;

import com.xt.bcloud.comm.CloudUtils;
import com.xt.bcloud.comm.HeartBeating;
import com.xt.bcloud.mdu.command.CommandServer;
import com.xt.bcloud.mdu.command.ProcessInfo;
import com.xt.bcloud.resource.server.ServerInfo;
import com.xt.core.app.Stoper;
import com.xt.core.log.LogWriter;
import com.xt.core.utils.CollectionUtils;
import com.xt.core.utils.VarTemplate;
import com.xt.gt.sys.SystemConfiguration;
import java.io.File;
import java.util.*;
import java.util.prefs.Preferences;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * 用于部署和更新应用服务器的部署器。通过规则制定部署路径。
 * FIXME: 1. 需要一个守护进程来保证此监听一直生效，这个应用和监听互为监护 
 * 2. 自身的更新是一个问题！
 *
 * @author Albert
 */
public class MduManager {

    
    /**
     * MDU 的本地端口号
     */
    public static final String MDU_MANAGERMANAGER_PORT = "mduManager.managerPort";
    /**
     * MDU 的 OID，用于应用服务器重启的情况
     */
    public static final String MDU_MANAGERMANAGER_OID = "MduManager.oid";
    
    /**
     * 操作系统的命令后缀
     */
    public static final String OS_COMMAND_SUFFIX = "suffix";
    
    
    /**
     * 注册失败时进行重试的间隔（毫秒）
     */
    public static final int REGISTER_RETRY_INTERVAL = SystemConfiguration.getInstance().readInt("mdu.registter.retry.interval", 5000);
    /**
     * 用于管理的端口号
     */
    public final static int managerPort = SystemConfiguration.getInstance().readInt(MDU_MANAGERMANAGER_PORT, 12000);
    /**
     * 日志实例
     */
    private final static Logger logger = Logger.getLogger(MduManager.class);
    /**
     * 当前服务器实例
     */
    private final PhyServer phyServer = new PhyServer();
    /**
     * 唯一实例
     */
    private static final MduManager instance = new MduManager();
    /**
     * 接受和处理命令的服务器
     */
    private final CommandServer commandServer = new CommandServer(managerPort);
    /**
     * 存放当前所有的应用服务器实例
     */
    private final Map<String, AppServerInstance> instanceMap = Collections.synchronizedMap(new HashMap());
    
    /**
     * 存放当前所有的进程信息,注意进程信息可能比实例信息注册得还早
     */
    private final Map<String, ProcessInfo> instanceProcessesMap = Collections.synchronizedMap(new HashMap());
    /**
     * MDU 服务实例
     */
    private final MduService mduService = CloudUtils.createMduService();
    /**
     * 是否注册成功的标记
     */
    private boolean registered = false;
    /**
     * 用于发送心跳信息的定时器
     */
    private final HeartBeating heartBeating = new HeartBeating();
    /**
     * 用于服务器注册的定时器。
     */
    private final Timer registerTimer = new Timer(true);

    private MduManager() {
    }

    static public MduManager getInstance() {
        return instance;
    }

    public boolean init() {
        String exeSubfix = SystemConfiguration.getInstance().readString("executable.suffix");
        if (StringUtils.isEmpty(exeSubfix)) {
            exeSubfix = CloudUtils.isWindows() ? "bat" : "sh";
            SystemConfiguration.getInstance().set(OS_COMMAND_SUFFIX, exeSubfix);
        }
        return true;
    }

    public void start() {
        LogWriter.info2(logger, "启动物理服务器，端口[%s]", managerPort);
        // 向系统注册表注册自己的管理端口
        Preferences.userRoot().put(MDU_MANAGERMANAGER_PORT, String.valueOf(managerPort));

        Thread commandThread = new Thread(new Runnable() {

            public void run() {
                // 本身需要启动一个管理端口
                commandServer.startServer();
            }
        });
        commandThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            public void uncaughtException(Thread t, Throwable e) {
                // 启动命令服务器失败;
                LogWriter.warn2(logger, e, "启动命令服务器[%s]出现异常，MDU 将退出。", commandServer);
                stop();
            }
        });
        commandThread.setName("MduManager.commandThread");
        commandThread.setDaemon(true);
        commandThread.start();


        //  如果注册失败（ARM可能宕机），需要循环注册
        registerTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    // 向服务器进行注册
                    register();

                    // 注册及启动成功
                    registered = true;

                    // 启动心跳定时器
                    heartBeating.start(new HeartBeatTask());

                    // 启动已有工作实例
                    List<AppServerInstance> instanceList = mduService.listAppServerInstances(phyServer);
                    startupServers(instanceList);

                    registerTimer.cancel();
                } catch (Throwable t) {
                    LogWriter.warn2(logger, t, "服务器[%s]注册或者启动出现异常，应用将稍后进行重试。", phyServer);
                }
            }
        }, 0, REGISTER_RETRY_INTERVAL);


        // 等待线程结束
        LogWriter.info2(logger, "启动结束，命令服务器[%s]正在等待命令", commandServer);
        Stoper.getInstance().waitFor();
    }

    private class HeartBeatTask extends TimerTask {

        @Override
        public void run() {
            mduService.psBeat(phyServer);
        }
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
            String workPath = asInstance.getWorkPath();
            if (StringUtils.isNotEmpty(workPath) && !(new File(workPath).exists())) {
                LogWriter.warn2(logger, "应用服务器实例的工作目录[%s]不存在。", workPath);
                continue;
            }
            String cmd = asInstance.getStartupCmd();
            LogWriter.info2(logger, "尝试启动应用服务器实例[%s]", asInstance);
            CloudUtils.executeCommand(cmd);
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
        //stopFlag = true;
        LogWriter.info2(logger, "停止此物理服务器[%s]", phyServer);
        try {
            // 
            LogWriter.info2(logger, "停止心跳程序", heartBeating);
            heartBeating.cancel();
            registerTimer.cancel();
            LogWriter.info2(logger, "停止命令服务器[%s]", commandServer);
            this.commandServer.stop();
            LogWriter.info2(logger, "注销物理服务器[%s]", phyServer);
            unregister();
        } catch (Throwable t) {
            LogWriter.warn2(logger, t, "停止此物理服务器[%s]是出现异常。", phyServer);
        }
    }

    public PhyServer getPhyServer() {
        return phyServer;
    }

    /**
     * 向服务器注册，表明自己的身份。
     */
    public void register() {
        if (registered) {
            return;
        }
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
                FilenameUtils.concat(System.getProperty("user.home"), "workspace"));
        phyServer.setWorkPath(workPath);
        
        phyServer.setState(PhyServerState.AVAILABLE);

        LogWriter.info2(logger, "开始注册物理服务器实例[%s]", phyServer);
        mduService.registerPhyServer(phyServer);
    }

    /**
     * 读取此物理服务器的唯一编码
     *
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
    public void unregister() {
        try {
            LogWriter.info2(logger, "注销物理服务器[%s]", phyServer);
            mduService.unregisterPhyServer(phyServer);
        } catch (Throwable t) {
            // 不进行进一步处理，等待监控程序收拾残局。
            LogWriter.warn2(logger, t, "注销物理服务器[%s]出现异常。", phyServer);
        }
    }

    public void registerAppServerInstance(AppServerInstance asInstance) {
        LogWriter.info2(logger, "注册应用服务器实例[%s]", asInstance);
        if (asInstance == null || StringUtils.isEmpty(asInstance.getOid())) {
            return;
        }
        String oid = asInstance.getOid();
        if (!instanceMap.containsKey(oid)) {
            instanceMap.put(oid, asInstance);
        }
        if (instanceProcessesMap.containsKey(oid)) {
            // 实例可能先更新过进程信息
            updateProcessInfo(instanceProcessesMap.get(oid));
        }
    }

    /**
     * 更新发布服务器的进程信息。
     * @param process 
     */
    public void updateProcessInfo(ProcessInfo process) {
        LogWriter.info2(logger, "更新进程信息[%s]", process);
        if (process == null) {
            return;
        }
        String instanceOid = process.getAppServerInstanceOid();
        instanceProcessesMap.put(instanceOid, process);
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
}
