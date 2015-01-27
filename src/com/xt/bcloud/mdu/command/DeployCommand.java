package com.xt.bcloud.mdu.command;

import com.xt.bcloud.comm.*;
import com.xt.bcloud.mdu.*;
import com.xt.bcloud.mdu.service.MakingService;
import com.xt.bcloud.resource.server.ServerInfo;
import com.xt.core.app.Stoper;
import com.xt.core.json.JsonBuilder;
import com.xt.core.log.LogWriter;
import com.xt.core.utils.*;
import com.xt.gt.sys.SystemConfiguration;
import java.io.*;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Albert
 */
public class DeployCommand extends AbstractMduCommnad {
    
    public final static String DEPLOY = "deploy";

    private final static String BACKUP_DIR = SystemConfiguration.getInstance().readString("mdu.backupDir", System.getProperty("java.io.tmpdir"));
    
    private final MduService mduService = CloudUtils.createMduService();
    
    
    private final MakingService makingService = CloudUtils.createMakingService();
    
    /**
     * �Ƿ�Ӧ�÷����������Ƿ�ɹ������ȴ�ʱ����Ĭ��Ϊ��2���ӡ�
     */
    private final static int MAX_WAITING_TIME = SystemConfiguration.getInstance().readInt("mdu.maxWaitingTime", 2 * 60 * 1000); 
    
    /**
     * �Ƿ�Ӧ�÷����������Ƿ�ɹ��Ĳ��Լ����Ĭ��Ϊ��5���ӡ�
     */
    private final static int WAITING_INTERVAL = SystemConfiguration.getInstance().readInt("mdu.waitingInterval", 5 * 1000); 
   
    public DeployCommand() {
        this.name = DEPLOY;
    }

    @Override
    public Serializable execute() {
        LogWriter.info2(logger, "start updating server[%s]", getParam());
        AppServerTemplate appServerTemplate = (AppServerTemplate) getParam();
        AppServerInstance appServerInstance = deploy(appServerTemplate);
        LogWriter.info2(logger, "Ӧ�÷�����[%s]����ɹ���",
                appServerInstance);
        MduManager.getInstance().registerAppServerInstance(appServerInstance);
        LogWriter.info2(logger, "Ӧ�÷�����[%s]ע��ɹ���",
                appServerInstance);
        // ����Ӧ��ʵ��
        return appServerInstance;
    }

    /**
     * ����һ��Ӧ�÷�����������ͬʱ����������Ҫ���в����滻��
     */
    public AppServerInstance deploy(AppServerTemplate appServerTemplate) {
        // ���㷢��·��
        PhyServer phyServer = MduManager.getInstance().getPhyServer();
        if (phyServer == null || appServerTemplate == null) {
            throw new MduException("��Դ��Ϣ��ȫ");
        }
        LogWriter.info2(logger, "��ʼ�����������[%s]�ϲ���Ӧ�÷�����[%s]",
                phyServer, appServerTemplate);

        // ���ڼ��㶯̬�����Ĳ���
        final Map params = new HashMap();
        // ����Java���Ա���
        params.putAll(System.getProperties());
        
        // ����ϵͳ����
        params.putAll(SystemConfiguration.getInstance().getParams());
        // ���������������ز���
        Pair[] props = BeanHelper.getProperties(phyServer);
        for (Pair pair : props) {
            Object _value = processParam(pair.getName(), pair.getValue(), params);
            params.put(pair.getName(), _value);
        }
        // ����Ӧ�÷�����ʵ��ID��ͨ�������ķ�ʽд�������
        // TODO: д��ģ���ļ�
        String instanceOid = UUID.randomUUID().toString();
        LogWriter.info2(logger, "Ӧ�÷�����ʵ����OID[%s]", instanceOid);
        params.put(Constants.APP_SERVER_INSTANCE_OID, instanceOid);
        params.put(Constants.APP_SERVER_TEMPLATE_OID, appServerTemplate.getOid());
        createParams(appServerTemplate, params);

        File workPath = getWorkPath(phyServer, appServerTemplate, params);
        params.put("workPath", workPath.getAbsoluteFile());  // ��Ϊ�����棨10�У��ŵ�Params�Ĳ���ֵ���ܲ�һ�¡�
        LogWriter.info2(logger, "Ӧ�÷�����ʵ�Ĺ���·��[%s]", workPath);


        // �ӷ��������ط�����
        LogWriter.info2(logger, "��ʼ������Դ��");
        InputStream is = makingService.downloadStream(appServerTemplate);
        // ԭѹ�������Ƶ�����·��
        File backupFile = createBackup(appServerTemplate);
        LogWriter.info2(logger, "����Դ�����Ƶ�����·��[%s]", backupFile.getAbsolutePath());
        try {
            FileOutputStream fos = new FileOutputStream(backupFile);
            long count = IOHelper.i2o(is, fos, true, true);
            if (count <= 0) {
                throw new MduException(String.format("ģ���ļ�������[%s]�����ڡ�", appServerTemplate.getStorePath()));
                
            }
        } catch (FileNotFoundException ex) {
            throw new MduException(String.format("�ļ�[%s]�����ڡ�", backupFile.getAbsolutePath()), ex);
        }

        // ��ѹ��������·��
        LogWriter.info2(logger, "����Դ����ѹ��������·��[%s]", workPath);
        try {
            FileInputStream fis = new FileInputStream(backupFile.getAbsoluteFile());
            UnZip unzip = new UnZip();
            unzip.unZip(fis, workPath);
        } catch (FileNotFoundException ex) {
            throw new MduException(String.format("�ļ�[%s]�����ڡ�", backupFile.getAbsolutePath()), ex);
        }

        // �滻�ļ�ģ���еı���
        LogWriter.info2(logger, "��ʼ�滻�ļ�ģ���еı���");
        VarProcessorUtils.processVars(workPath, params);

        // ����������
        System.out.println("��������ģ��=" + appServerTemplate.getStartupCmd());
        String startupCmd = VarTemplate.format(appServerTemplate.getStartupCmd(), params, true);  // ��������
        System.out.println("��������=" + startupCmd);
        startupCmd = FilenameUtils.normalize(startupCmd);  // ע�⣺���滯(���������滰��./��,�����./exe.sh����Ϊ��exe.sh��)
        LogWriter.info2(logger, "ʹ����������Ӧ�÷�����ʵ��[%s]", startupCmd);
        //TODO: ��Ҫʹ���첽������Դ
        ProcessResult pi = CloudUtils.executeCommand(startupCmd);
//        if (pi.getExitVal() != 0) {
//            throw new MduException(String.format("ִ������[%s]����\n ������:%s"
//                    + " \n ������Ϣ��%s", startupCmd, pi.getExitVal(), pi.getOutput()));
//        }

        AppServerInstance asInstance = createAppServerInstance(instanceOid,
                appServerTemplate, phyServer, startupCmd, params);
        asInstance.setWorkPath(workPath.getAbsolutePath());
        waitForStartuping(asInstance);
        
        LogWriter.info2(logger, "����Ӧ�÷�����ʵ��[%s]��ɡ�", asInstance);
        return asInstance;
    }

    /**
     * �ȴ�Ӧ�÷���������
     * @param asInstance 
     */
    private void waitForStartuping(AppServerInstance asInstance) {
        //FIXME: ���ֲ�ͬ���͵ķ�����̽���Ƿ񷢲��ɹ��ķ�ʽ��һ����
        // Ŀǰ�����������ֱ�ӷ���
        if (asInstance.getServerType() == ServerType.TASK_DISPATCHER) {
            return;
        }
        boolean alive = false;
        
        long startTime = System.currentTimeMillis();  // ��ʼʱ��
        while (!alive 
                && ((System.currentTimeMillis() - startTime) < MAX_WAITING_TIME)
                && !Stoper.getInstance().isStoped()) {
            try {
                Thread.sleep(WAITING_INTERVAL); // �ȴ�����
                ServerInfo serverInfo = mduService.getServerInfo(asInstance);
                LogWriter.info2(logger, "��ȡӦ��ʵ��[%s]�ķ�������Ϣ[%s]", asInstance, serverInfo);
                if (serverInfo != null) {
                    alive = CloudUtils.isAlive(serverInfo);
                }
                LogWriter.info2(logger, "������:isAlive[%s]:[%s]", asInstance, alive);
            } catch (Exception ex) {
                // do nothing, just waiting for its startup
                LogWriter.warn2(logger, ex, "I'm warting for the process of startuping of [%s]", asInstance);
            }
        }
    }

    protected File createBackup(AppServerTemplate appServerTemplate) {
        StringBuilder subdir = new StringBuilder();
        subdir.append(appServerTemplate.getName()).append(File.separator);
        subdir.append(appServerTemplate.getVersion()).append(File.separator);
        subdir.append(DateUtils.toDateStr(Calendar.getInstance(), "yyyyMMdd_HHmmss")).append(File.separator);

        File backupDir = new File(BACKUP_DIR, subdir.toString());
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        File storeFile = new File(appServerTemplate.getStorePath());
        File backupFile = new File(backupDir, storeFile.getName());

        return backupFile;
    }

    private AppServerInstance createAppServerInstance(String instanceOid,
            AppServerTemplate appServerTemplate, PhyServer phyServer, String startupCmd, Map params) {
        AppServerInstance asInstance = new AppServerInstance();
        asInstance.setOid(instanceOid);
        // ���ƻ�������
        MduUtils.copyProperties(asInstance, phyServer, appServerTemplate);
        
        asInstance.setStartupCmd(startupCmd);
        String stopCmd = VarTemplate.format(appServerTemplate.getStopCmd(), params);
        stopCmd = FilenameUtils.normalize(stopCmd);  // ���滯��
        asInstance.setStopCmd(stopCmd);
        // ��ʱ�޷�ȡ��PID������滻���滻�ı���
//        // ������ģ���ж�ȡ��Kill������
//        params.put("pid", pid);
        String killCmdTmpl = SystemConfiguration.getInstance().readString("mdu.killCmd",
                CloudUtils.getKillCmdTemplate());
        String killCmd = VarTemplate.format(killCmdTmpl, params, true);
        killCmd = FilenameUtils.normalize(killCmd);  // ���滯
        asInstance.setKillCmd(killCmd);
        return asInstance;
    }

    private void createParams(AppServerTemplate appServerTemplate, final Map params) {
        // JSON ��ʽ�Ĳ���ֵ,���磺{a:'b', b:'${workPath}\c', c:'${inc_}',d:'${workPath}\${_inc}', 
        // e:'${workPath}\${_random}'}
        String tmpParams = appServerTemplate.getParams();
        if (StringUtils.isNotEmpty(tmpParams)) {
            JsonBuilder jsonBuilder = new JsonBuilder();
            Map<String, Object> _params = (Map) jsonBuilder.parse(tmpParams);
            for (Map.Entry<String, Object> entry : _params.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                value = processParam(key, value, params);
                params.put(key, value);
            }
        }
    }

    private Object processParam(String name, Object value, final Map params) {
        if (value instanceof String) {
            String _value = (String) value;
            _value = VarParser.parse(_value, new CommandValueReader(params));
            return _value;
        }
        return value;
    }

    
}
