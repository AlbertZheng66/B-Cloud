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
 * ���ڲ���͸���Ӧ�÷������Ĳ�������ͨ�������ƶ�����·����
 * FIXME: ��Ҫһ���ػ���������֤�˼���һֱ��Ч�����Ӧ�úͼ�����Ϊ�໤
 * FIXME: MDU ����ĸ�����һ�����⣡
 * @author Albert
 */
public class MduManager2 {

    /**
     * MDU �ı��ض˿ں�
     */
    public static final String MDU_MANAGERMANAGER_PORT = "MduManager.managerPort";
    /**
     * MDU �� OID������Ӧ�÷��������������
     */
    public static final String MDU_MANAGERMANAGER_OID = "MduManager.oid";
    /**
     * ���ڹ���Ķ˿ں�
     */
    public final static int managerPort = SystemConfiguration.getInstance().readInt(MDU_MANAGERMANAGER_PORT, 12000);
    
    /**
     * ��־ʵ��
     */
    private final static Logger logger = Logger.getLogger(MduManager2.class);
    
    /**
     * ��ǰ������ʵ��
     */
    private final PhyServer phyServer = new PhyServer();
    /**
     * Ψһʵ��
     */
    private static final MduManager2 instance = new MduManager2();
    /**
     * ���ܺʹ�������ķ�����
     */
    private final CommandServer commandServer = new CommandServer(managerPort);
    /**
     * ��ŵ�ǰ���е�Ӧ�÷�����ʵ��
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
        LogWriter.info2(logger, "����������������˿�[%s]", managerPort);
        // ��ϵͳע���ע���Լ��Ĺ���˿�
        Preferences.userRoot().put(MDU_MANAGERMANAGER_PORT, String.valueOf(managerPort));

        Thread t = new Thread(new Runnable() {

            public void run() {
                // ������Ҫ����һ������˿�
                commandServer.startServer();
            }
        });
        t.setDaemon(true);
        t.start();

        // �����������ע��
        // FIXME: ���ע��ʧ�ܣ�ARM����崻�������Ҫѭ��ע��
        register();

        // �������й���ʵ��
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
            // ���Ӧ���Ƿ��Ѿ�����������MduManager��������ֹͣ�������
            if (isAlive(asInstance)) {
                continue;
            }
            LogWriter.info2(logger, "����Ӧ�÷�����ʵ��[%s]", asInstance);
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
     * ��������£�����Ҫ���ֹͣ�������ͻ���Ӧһֱ����
     */
    public void stop() {
        LogWriter.info2(logger, "ֹͣ�����������[%s]", phyServer);
        unregister();
        this.commandServer.stop();
    }

    public PhyServer getPhyServer() {
        return phyServer;
    }

    /**
     * �������ע�ᣬ�����Լ�����ݡ�
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
        
        // ���ļ�����ȡ��workpath
        String workPath = SystemConfiguration.getInstance().readString("workPath",
                FilenameUtils.concat(System.getProperty("user.home"), FilenameUtils.concat("workspace", "${_time}")));
        phyServer.setWorkPath(workPath);
        phyServer.setState(PhyServerState.AVAILABLE);
        
        LogWriter.info2(logger, "��ʼע�����������ʵ��[%s]", phyServer);
        mduService.registerPhyServer(phyServer);
    }

    /**
     * ��ȡ�������������Ψһ����
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
    synchronized public void unregister() {
        LogWriter.info2(logger, "ע�����������[%s]", phyServer);
        mduService.unregisterPhyServer(phyServer);
    }

    synchronized public void registerAppServerInstance(AppServerInstance asInstance) {
        LogWriter.info2(logger, "ע��Ӧ�÷�����ʵ��[%s]", asInstance);
        if (asInstance == null) {
            return;
        }
        instanceMap.put(asInstance.getOid(), asInstance);
    }

    public void updateProcessInfo(ProcessInfo process) {
        LogWriter.info2(logger, "���½�����Ϣ[%s]", process);
        String instanceOid = process.getAppServerInstanceOid();
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
        // Ӧ�ùر�ʱ�Զ�ע��
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            public void run() {
                try {
                    MduManager2.getInstance().stop();
                } catch (Throwable t) {
                    LogWriter.warn2(logger, t, "���������[%s]��ע��ʱ�����쳣��",
                            MduManager2.getInstance().getPhyServer());
                }
            }
        }));
        MduManager2.instance.init();
        MduManager2.instance.start();
    }
}
