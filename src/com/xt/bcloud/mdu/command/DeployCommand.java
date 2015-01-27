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
     * 是否应用服务器发布是否成功的最大等待时长，默认为：2分钟。
     */
    private final static int MAX_WAITING_TIME = SystemConfiguration.getInstance().readInt("mdu.maxWaitingTime", 2 * 60 * 1000); 
    
    /**
     * 是否应用服务器发布是否成功的测试间隔，默认为：5秒钟。
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
        LogWriter.info2(logger, "应用服务器[%s]部署成功。",
                appServerInstance);
        MduManager.getInstance().registerAppServerInstance(appServerInstance);
        LogWriter.info2(logger, "应用服务器[%s]注册成功。",
                appServerInstance);
        // 返回应用实例
        return appServerInstance;
    }

    /**
     * 部署一个应用服务器。可以同时部署多个，需要进行参数替换。
     */
    public AppServerInstance deploy(AppServerTemplate appServerTemplate) {
        // 计算发布路径
        PhyServer phyServer = MduManager.getInstance().getPhyServer();
        if (phyServer == null || appServerTemplate == null) {
            throw new MduException("资源信息不全");
        }
        LogWriter.info2(logger, "开始在物理服务器[%s]上部署应用服务器[%s]",
                phyServer, appServerTemplate);

        // 用于计算动态变量的参数
        final Map params = new HashMap();
        // 加入Java属性变量
        params.putAll(System.getProperties());
        
        // 加入系统参数
        params.putAll(SystemConfiguration.getInstance().getParams());
        // 加入物理服务器相关参数
        Pair[] props = BeanHelper.getProperties(phyServer);
        for (Pair pair : props) {
            Object _value = processParam(pair.getName(), pair.getValue(), params);
            params.put(pair.getName(), _value);
        }
        // 加入应用服务器实例ID（通过参数的方式写入变量）
        // TODO: 写入模板文件
        String instanceOid = UUID.randomUUID().toString();
        LogWriter.info2(logger, "应用服务器实例的OID[%s]", instanceOid);
        params.put(Constants.APP_SERVER_INSTANCE_OID, instanceOid);
        params.put(Constants.APP_SERVER_TEMPLATE_OID, appServerTemplate.getOid());
        createParams(appServerTemplate, params);

        File workPath = getWorkPath(phyServer, appServerTemplate, params);
        params.put("workPath", workPath.getAbsoluteFile());  // 因为和上面（10行）放到Params的参数值可能不一致。
        LogWriter.info2(logger, "应用服务器实的工作路径[%s]", workPath);


        // 从服务器下载发布包
        LogWriter.info2(logger, "开始下载资源包");
        InputStream is = makingService.downloadStream(appServerTemplate);
        // 原压缩包复制到备份路径
        File backupFile = createBackup(appServerTemplate);
        LogWriter.info2(logger, "将资源包复制到备份路径[%s]", backupFile.getAbsolutePath());
        try {
            FileOutputStream fos = new FileOutputStream(backupFile);
            long count = IOHelper.i2o(is, fos, true, true);
            if (count <= 0) {
                throw new MduException(String.format("模板文件不存在[%s]不存在。", appServerTemplate.getStorePath()));
                
            }
        } catch (FileNotFoundException ex) {
            throw new MduException(String.format("文件[%s]不存在。", backupFile.getAbsolutePath()), ex);
        }

        // 解压缩到发布路径
        LogWriter.info2(logger, "将资源包接压缩到工作路径[%s]", workPath);
        try {
            FileInputStream fis = new FileInputStream(backupFile.getAbsoluteFile());
            UnZip unzip = new UnZip();
            unzip.unZip(fis, workPath);
        } catch (FileNotFoundException ex) {
            throw new MduException(String.format("文件[%s]不存在。", backupFile.getAbsolutePath()), ex);
        }

        // 替换文件模板中的变量
        LogWriter.info2(logger, "开始替换文件模板中的变量");
        VarProcessorUtils.processVars(workPath, params);

        // 启动服务器
        System.out.println("启动命令模板=" + appServerTemplate.getStartupCmd());
        String startupCmd = VarTemplate.format(appServerTemplate.getStartupCmd(), params, true);  // 启动命令
        System.out.println("启动命令=" + startupCmd);
        startupCmd = FilenameUtils.normalize(startupCmd);  // 注意：正规化(会错误的正规话“./”,将命令“./exe.sh”变为“exe.sh”)
        LogWriter.info2(logger, "使用命令启动应用服务器实例[%s]", startupCmd);
        //TODO: 需要使用异步启动资源
        ProcessResult pi = CloudUtils.executeCommand(startupCmd);
//        if (pi.getExitVal() != 0) {
//            throw new MduException(String.format("执行命令[%s]错误。\n 返回码:%s"
//                    + " \n 错误信息：%s", startupCmd, pi.getExitVal(), pi.getOutput()));
//        }

        AppServerInstance asInstance = createAppServerInstance(instanceOid,
                appServerTemplate, phyServer, startupCmd, params);
        asInstance.setWorkPath(workPath.getAbsolutePath());
        waitForStartuping(asInstance);
        
        LogWriter.info2(logger, "构建应用服务器实例[%s]完成。", asInstance);
        return asInstance;
    }

    /**
     * 等待应用服务器启动
     * @param asInstance 
     */
    private void waitForStartuping(AppServerInstance asInstance) {
        //FIXME: 各种不同类型的服务器探测是否发布成功的方式不一样。
        // 目前：任务分派器直接返回
        if (asInstance.getServerType() == ServerType.TASK_DISPATCHER) {
            return;
        }
        boolean alive = false;
        
        long startTime = System.currentTimeMillis();  // 开始时间
        while (!alive 
                && ((System.currentTimeMillis() - startTime) < MAX_WAITING_TIME)
                && !Stoper.getInstance().isStoped()) {
            try {
                Thread.sleep(WAITING_INTERVAL); // 等待五秒
                ServerInfo serverInfo = mduService.getServerInfo(asInstance);
                LogWriter.info2(logger, "读取应用实例[%s]的服务器信息[%s]", asInstance, serverInfo);
                if (serverInfo != null) {
                    alive = CloudUtils.isAlive(serverInfo);
                }
                LogWriter.info2(logger, "服务器:isAlive[%s]:[%s]", asInstance, alive);
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
        // 复制基本属性
        MduUtils.copyProperties(asInstance, phyServer, appServerTemplate);
        
        asInstance.setStartupCmd(startupCmd);
        String stopCmd = VarTemplate.format(appServerTemplate.getStopCmd(), params);
        stopCmd = FilenameUtils.normalize(stopCmd);  // 正规化，
        asInstance.setStopCmd(stopCmd);
        // 此时无法取得PID，因此替换可替换的变量
//        // 从命令模板中读取“Kill”命令
//        params.put("pid", pid);
        String killCmdTmpl = SystemConfiguration.getInstance().readString("mdu.killCmd",
                CloudUtils.getKillCmdTemplate());
        String killCmd = VarTemplate.format(killCmdTmpl, params, true);
        killCmd = FilenameUtils.normalize(killCmd);  // 正规化
        asInstance.setKillCmd(killCmd);
        return asInstance;
    }

    private void createParams(AppServerTemplate appServerTemplate, final Map params) {
        // JSON 形式的参数值,例如：{a:'b', b:'${workPath}\c', c:'${inc_}',d:'${workPath}\${_inc}', 
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
