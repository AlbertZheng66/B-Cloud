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
 * ���ڲ���͸���Ӧ�÷������Ĳ�������ͨ�������ƶ�����·����
 * FIXME: 1. ��Ҫһ���ػ���������֤�˼���һֱ��Ч�����Ӧ�úͼ�����Ϊ�໤ 
 * 2. ����ĸ�����һ�����⣡
 *
 * @author Albert
 */
public class MduManager {

    
    /**
     * MDU �ı��ض˿ں�
     */
    public static final String MDU_MANAGERMANAGER_PORT = "mduManager.managerPort";
    /**
     * MDU �� OID������Ӧ�÷��������������
     */
    public static final String MDU_MANAGERMANAGER_OID = "MduManager.oid";
    
    /**
     * ����ϵͳ�������׺
     */
    public static final String OS_COMMAND_SUFFIX = "suffix";
    
    
    /**
     * ע��ʧ��ʱ�������Եļ�������룩
     */
    public static final int REGISTER_RETRY_INTERVAL = SystemConfiguration.getInstance().readInt("mdu.registter.retry.interval", 5000);
    /**
     * ���ڹ���Ķ˿ں�
     */
    public final static int managerPort = SystemConfiguration.getInstance().readInt(MDU_MANAGERMANAGER_PORT, 12000);
    /**
     * ��־ʵ��
     */
    private final static Logger logger = Logger.getLogger(MduManager.class);
    /**
     * ��ǰ������ʵ��
     */
    private final PhyServer phyServer = new PhyServer();
    /**
     * Ψһʵ��
     */
    private static final MduManager instance = new MduManager();
    /**
     * ���ܺʹ�������ķ�����
     */
    private final CommandServer commandServer = new CommandServer(managerPort);
    /**
     * ��ŵ�ǰ���е�Ӧ�÷�����ʵ��
     */
    private final Map<String, AppServerInstance> instanceMap = Collections.synchronizedMap(new HashMap());
    
    /**
     * ��ŵ�ǰ���еĽ�����Ϣ,ע�������Ϣ���ܱ�ʵ����Ϣע��û���
     */
    private final Map<String, ProcessInfo> instanceProcessesMap = Collections.synchronizedMap(new HashMap());
    /**
     * MDU ����ʵ��
     */
    private final MduService mduService = CloudUtils.createMduService();
    /**
     * �Ƿ�ע��ɹ��ı��
     */
    private boolean registered = false;
    /**
     * ���ڷ���������Ϣ�Ķ�ʱ��
     */
    private final HeartBeating heartBeating = new HeartBeating();
    /**
     * ���ڷ�����ע��Ķ�ʱ����
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
        LogWriter.info2(logger, "����������������˿�[%s]", managerPort);
        // ��ϵͳע���ע���Լ��Ĺ���˿�
        Preferences.userRoot().put(MDU_MANAGERMANAGER_PORT, String.valueOf(managerPort));

        Thread commandThread = new Thread(new Runnable() {

            public void run() {
                // ������Ҫ����һ������˿�
                commandServer.startServer();
            }
        });
        commandThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            public void uncaughtException(Thread t, Throwable e) {
                // �������������ʧ��;
                LogWriter.warn2(logger, e, "�������������[%s]�����쳣��MDU ���˳���", commandServer);
                stop();
            }
        });
        commandThread.setName("MduManager.commandThread");
        commandThread.setDaemon(true);
        commandThread.start();


        //  ���ע��ʧ�ܣ�ARM����崻�������Ҫѭ��ע��
        registerTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    // �����������ע��
                    register();

                    // ע�ἰ�����ɹ�
                    registered = true;

                    // ����������ʱ��
                    heartBeating.start(new HeartBeatTask());

                    // �������й���ʵ��
                    List<AppServerInstance> instanceList = mduService.listAppServerInstances(phyServer);
                    startupServers(instanceList);

                    registerTimer.cancel();
                } catch (Throwable t) {
                    LogWriter.warn2(logger, t, "������[%s]ע��������������쳣��Ӧ�ý��Ժ�������ԡ�", phyServer);
                }
            }
        }, 0, REGISTER_RETRY_INTERVAL);


        // �ȴ��߳̽���
        LogWriter.info2(logger, "�������������������[%s]���ڵȴ�����", commandServer);
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
            // ���Ӧ���Ƿ��Ѿ�����������MduManager��������ֹͣ�������
            if (isAlive(asInstance)) {
                continue;
            }
            String workPath = asInstance.getWorkPath();
            if (StringUtils.isNotEmpty(workPath) && !(new File(workPath).exists())) {
                LogWriter.warn2(logger, "Ӧ�÷�����ʵ���Ĺ���Ŀ¼[%s]�����ڡ�", workPath);
                continue;
            }
            String cmd = asInstance.getStartupCmd();
            LogWriter.info2(logger, "��������Ӧ�÷�����ʵ��[%s]", asInstance);
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
     * ��������£�����Ҫ���ֹͣ�������ͻ���Ӧһֱ����
     */
    public void stop() {
        //stopFlag = true;
        LogWriter.info2(logger, "ֹͣ�����������[%s]", phyServer);
        try {
            // 
            LogWriter.info2(logger, "ֹͣ��������", heartBeating);
            heartBeating.cancel();
            registerTimer.cancel();
            LogWriter.info2(logger, "ֹͣ���������[%s]", commandServer);
            this.commandServer.stop();
            LogWriter.info2(logger, "ע�����������[%s]", phyServer);
            unregister();
        } catch (Throwable t) {
            LogWriter.warn2(logger, t, "ֹͣ�����������[%s]�ǳ����쳣��", phyServer);
        }
    }

    public PhyServer getPhyServer() {
        return phyServer;
    }

    /**
     * �������ע�ᣬ�����Լ�����ݡ�
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

        // ���ļ�����ȡ��workpath
        String workPath = SystemConfiguration.getInstance().readString("workPath",
                FilenameUtils.concat(System.getProperty("user.home"), "workspace"));
        phyServer.setWorkPath(workPath);
        
        phyServer.setState(PhyServerState.AVAILABLE);

        LogWriter.info2(logger, "��ʼע�����������ʵ��[%s]", phyServer);
        mduService.registerPhyServer(phyServer);
    }

    /**
     * ��ȡ�������������Ψһ����
     *
     * @return
     */
    private String getOid() {
        String oid = Preferences.userRoot().get(MDU_MANAGERMANAGER_OID, "");
        LogWriter.info2(logger, "��ע����л�ȡOID[%s]", oid);
        if (StringUtils.isEmpty(oid)) {
            oid = UUID.randomUUID().toString();
            // ��UUIDд��ע���
            Preferences.userRoot().put(MDU_MANAGERMANAGER_OID, oid);
        }
        return oid;
    }

    /**
     * �������ע����
     */
    public void unregister() {
        try {
            LogWriter.info2(logger, "ע�����������[%s]", phyServer);
            mduService.unregisterPhyServer(phyServer);
        } catch (Throwable t) {
            // �����н�һ�������ȴ���س�����ʰ�о֡�
            LogWriter.warn2(logger, t, "ע�����������[%s]�����쳣��", phyServer);
        }
    }

    public void registerAppServerInstance(AppServerInstance asInstance) {
        LogWriter.info2(logger, "ע��Ӧ�÷�����ʵ��[%s]", asInstance);
        if (asInstance == null || StringUtils.isEmpty(asInstance.getOid())) {
            return;
        }
        String oid = asInstance.getOid();
        if (!instanceMap.containsKey(oid)) {
            instanceMap.put(oid, asInstance);
        }
        if (instanceProcessesMap.containsKey(oid)) {
            // ʵ�������ȸ��¹�������Ϣ
            updateProcessInfo(instanceProcessesMap.get(oid));
        }
    }

    /**
     * ���·����������Ľ�����Ϣ��
     * @param process 
     */
    public void updateProcessInfo(ProcessInfo process) {
        LogWriter.info2(logger, "���½�����Ϣ[%s]", process);
        if (process == null) {
            return;
        }
        String instanceOid = process.getAppServerInstanceOid();
        instanceProcessesMap.put(instanceOid, process);
        AppServerInstance asInstance = instanceMap.get(instanceOid);
        LogWriter.info2(logger, "�ҵ���Ӧ��Ӧ�÷�����ʵ��������Ϣ[%s]", asInstance);
        // ���û�ҵ�,����ȥ���������в���
        if (asInstance == null) {
            asInstance = mduService.getAppServerInstance(instanceOid);
            if (asInstance != null) {
                this.instanceMap.put(instanceOid, asInstance);
            }
        }

        if (asInstance == null) {
            LogWriter.warn2(logger, "δ�ҵ�ʵ��[%s]��ע����Ϣ��", instanceOid);
            return;
        }
        String stopCmd = asInstance.getStopCmd();
        // ����ֹͣ������Ҫ�ġ����̺š���PID��
        Map params = CollectionUtils.toMap("pid", process.getPid());
        stopCmd = VarTemplate.format(stopCmd, params);
        asInstance.setStopCmd(stopCmd);
        // ����ǿ��ֹͣ��Kill��������Ҫ�ġ����̺š���PID��
        String killCmd = asInstance.getKillCmd();
        killCmd = VarTemplate.format(killCmd, params);
        asInstance.setKillCmd(killCmd);

        LogWriter.info2(logger, "����ǿ��ֹͣ����[%s]��ǿ��ֹͣ����[%s]", stopCmd, killCmd);
        mduService.updateAppServerInstance(asInstance);
    }
}
