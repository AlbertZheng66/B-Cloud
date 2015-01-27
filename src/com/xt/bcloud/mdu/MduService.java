package com.xt.bcloud.mdu;

import com.xt.bcloud.comm.Constants;
import com.xt.bcloud.mdu.command.*;
import com.xt.bcloud.resource.TaskDispatcher;
import com.xt.bcloud.resource.server.ServerInfo;
import com.xt.bcloud.resource.server.ServerState;
import com.xt.core.log.LogWriter;
import com.xt.core.service.AbstractService;
import com.xt.core.utils.BeanHelper;
import com.xt.core.utils.CollectionUtils;
import com.xt.core.utils.RandomUtils;
import com.xt.core.utils.SqlUtils;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Albert
 */
public class MduService extends AbstractService {

    private static final long serialVersionUID = -5023906897590688066L;

    public MduService() {
    }

    /**
     * 注册一个物理服务器。
     *
     * @param phyServer
     */
    public void registerPhyServer(PhyServer phyServer) {
        LogWriter.info2(logger, "开始注册物理服务器实例[%s]", phyServer);
        String oid = phyServer.getOid();
        phyServer.setLastUpdatedTime(Calendar.getInstance());
        PhyServer old = (PhyServer) persistenceManager.findByPK(PhyServer.class, oid);
        LogWriter.info2(logger, "查看是否已经存在物理服务器实例[%s]的状态", old);
        if (old == null) {
            phyServer.setInsertTime(Calendar.getInstance());
            phyServer.setInvalidTime(Constants.INVALID_TIME); // 必须得有一个值。
            persistenceManager.insert(phyServer);
        } else {
            LogWriter.info2(logger, "更新物理服务器实例[%s]的状态", phyServer);
            phyServer.setValid(old.getValid());  // 不能自动更改物理服务器的失效状态
            persistenceManager.update(phyServer);
        }
    }

    public void disablePhyServer(PhyServer phyServer) {
        LogWriter.info2(logger, "让物理服务器实例[%s]暂时失效", phyServer);
        phyServer.setValid(false);
        persistenceManager.update(phyServer);
    }

    /**
     * 注册一个应用服务器实例
     */
    public boolean registerAppServerInstance(AppServerInstance appServerInstance) {
        LogWriter.info2(logger, "注册应用服务器实例[%s]", appServerInstance);
        if (appServerInstance == null || StringUtils.isEmpty(appServerInstance.getOid())) {
            LogWriter.warn2(logger, "应用服务器实例[%s]注册失败[实例或者主键为空]。", appServerInstance);
            return false;
        }
        AppServerInstance asInstance = persistenceManager.findByPK(AppServerInstance.class, appServerInstance.getOid());
        LogWriter.info2(logger, "已经查询到应用服务器实例[%s]", asInstance);
        if (asInstance == null || StringUtils.isEmpty(asInstance.getOid())) {
            return persistenceManager.insert(appServerInstance);
        }
        return false;
    }

    /**
     * 显示当前所有的应用服务器实例
     */
    public List<AppServerInstance> listAllAppServerInstances() {
        return listAppServerInstances(ServerType.APP_SERVER);
    }

    private List<AppServerInstance> listAppServerInstances(ServerType serverType) {
        LogWriter.info2(logger, "显示当前所有的应用服务器实例。");
        return persistenceManager.query(AppServerInstance.class,
                "SELECT * FROM APP_SERVER_INSTANCE WHERE SERVER_TYPE=? ORDER BY INSERT_TIME DESC",
                SqlUtils.getParams(serverType));
    }

    /**
     * 尝试获取应用服务器信息
     */
    public ServerInfo getServerInfo(AppServerInstance appServerInstance) {
        LogWriter.info2(logger, "尝试获取应用服务器信息[%s]", appServerInstance);
        if (appServerInstance == null
                || StringUtils.isEmpty(appServerInstance.getOid())) {
            return null;
        }
        String sql = "SELECT * FROM SERVER_INFO WHERE app_Server_Instance_Oid=?";
        List<ServerInfo> serverInfos = persistenceManager.query(ServerInfo.class, sql,
                SqlUtils.getParams(appServerInstance.getOid()));
        if (serverInfos.isEmpty()) {
            return null;
        } else {
            return serverInfos.get(0);
        }
    }

    /**
     * 注册一个应用服务器实例
     */
    public void updateAppServerInstance(AppServerInstance appServerInstance) {
        LogWriter.info2(logger, "更新应用服务器实例[%s]", appServerInstance);
        if (appServerInstance == null || StringUtils.isEmpty(appServerInstance.getOid())) {
            LogWriter.warn2(logger, "更新应用服务器实例[%s]失败[实例或者主键为空]。", appServerInstance);
            return;
        }
        registerAppServerInstance(appServerInstance);
        persistenceManager.update(appServerInstance);
    }

    /**
     * 显示所有应用服务模板
     */
    public List<AppServerTemplate> listAppServerTemplates() {
        LogWriter.info2(logger, "检索所有应用服务模板");
        List<AppServerTemplate> asTemplates = persistenceManager.findAll(AppServerTemplate.class);
        return asTemplates;
    }

    /**
     * 删除指定的应用服务模板
     */
    public void deleteAppServerTemplate(AppServerTemplate asTemplate) {
        LogWriter.info2(logger, "删除指定的应用服务模板[%s]", asTemplate);
        // FIXME: 校验
        persistenceManager.delete(asTemplate);
    }

    /**
     * 根据OID查找一个应用服务器实例
     */
    public AppServerInstance getAppServerInstance(String oid) {
        LogWriter.info2(logger, "查找一个应用服务器实例[%s]", oid);
        // 校验
        if (StringUtils.isEmpty(oid)) {
            LogWriter.warn2(logger, "应用服务器实例OID[%s]不能为空。", oid);
            return null;
        }
        AppServerInstance _instance = (AppServerInstance) persistenceManager.findByPK(AppServerInstance.class, oid);
        if (_instance == null) {
            LogWriter.info2(logger, "应用服务器实例[%s]不存在。", oid);
            return null;
        }
        AppServerInstance instance = new AppServerInstance();
        BeanHelper.copy(instance, _instance);
        return instance;
    }

    /**
     * 注册一个应用服务器实例
     */
    public List<AppServerInstance> listAppServerInstances(PhyServer phyServer) {
        LogWriter.info2(logger, "检索应用服务器实例列表[%s]", phyServer);
        // FIXME: 校验
        List<AppServerInstance> instances = persistenceManager.findAll(AppServerInstance.class,
                "PHY_SERVER_OID=?", SqlUtils.getParams(phyServer.getOid()), null);
        return instances;
    }

    /**
     * 物理服务器的心跳检查
     */
    public void psBeat(PhyServer phyServer) {
        LogWriter.info2(logger, "物理服务器实例[%s]的一次心跳", phyServer);
        if (phyServer == null || StringUtils.isEmpty(phyServer.getOid())) {
            return;
        }
        phyServer.setLastUpdatedTime(Calendar.getInstance());
        persistenceManager.update(phyServer);
    }

    /**
     * 任务管理器的心跳检查
     */
    public void tdBeat(TaskDispatcher taskDispatcher) {
        LogWriter.info2(logger, "物理服务器实例[%s]的一次心跳", taskDispatcher);
        if (taskDispatcher == null || StringUtils.isEmpty(taskDispatcher.getOid())) {
            return;
        }
        taskDispatcher.setLastUpdatedTime(Calendar.getInstance());
        persistenceManager.update(taskDispatcher);
    }

    public void unregisterPhyServer(PhyServer phyServer) {
        LogWriter.info2(logger, "注销物理服务器实例[%s]", phyServer);
        if (phyServer == null || StringUtils.isEmpty(phyServer.getOid())) {
            return;
        }
        phyServer.setLastUpdatedTime(Calendar.getInstance());
        phyServer.setState(PhyServerState.STOPED);
        persistenceManager.update(phyServer);
    }

    /**
     * 部署到有系统选定的服务器。
     */
    public void deployServerTemplate(DeployingParam param) {
        if (param == null || param.getAsTemplate() == null) {
            throw new MduException("参数不能为空。");
        }
        LogWriter.info2(logger, "部署应用服务器[%s]", param.getAsTemplate());
        List<PhyServer> selectedPhyServer;
        if (param.isRandomSelection()) {
            List phyServers = listAvailablePhyServers();
            selectedPhyServer = RandomUtils.select(phyServers, param.getCount());
        } else {
            selectedPhyServer = param.getPhyServers();
        }
        System.out.println("phyServer=" + selectedPhyServer);
        if (CollectionUtils.isEmpty(selectedPhyServer)) {
            throw new MduException("没有可用的服务器。");
        }
        for (Iterator<PhyServer> it = selectedPhyServer.iterator(); it.hasNext();) {
            PhyServer phyServer = it.next();
            deployToServer(phyServer, param.getAsTemplate());
        }

    }

    /**
     * 显示所有的物理服务器。
     */
    public List<PhyServer> listPhyServers() {
        LogWriter.info2(logger, "显示所有的物理服务器");
        return persistenceManager.findAll(PhyServer.class);
    }

    /**
     * 显示所有的可用的物理服务器。
     */
    public List<PhyServer> listAvailablePhyServers() {
        return persistenceManager.findAll(PhyServer.class, "valid=? and (state=? OR state=?)",
                SqlUtils.getParams(Boolean.TRUE, PhyServerState.AVAILABLE,
                PhyServerState.USING), null);
    }

    /**
     * 显示所有可用的服务器。 FIXME: 按版本号倒序
     */
    public List<AppServerTemplate> listAvailableAppServers(String type) {
        return persistenceManager.findAll(AppServerTemplate.class,
                "VALID=? AND SERVER_TYPE=?", SqlUtils.getParams(Boolean.TRUE, type),
                "INSERT_TIME DESC");
    }

    /**
     * 部署到指定服务器。
     */
    public void deployToServer(PhyServer phyServer, AppServerTemplate asTemplate) {
        LogWriter.info2(logger, "在物理服务器[%s]上部署应用服务器[%s]", phyServer, asTemplate);

        // System.out.println("json=" + asTemplate.getParams());
        CommandClient cc = new CommandClient();
        Command command = new DeployCommand();
        command.setParam(asTemplate);
        AppServerInstance asInstance = (AppServerInstance) cc.execute(phyServer, command);
        LogWriter.info2(logger, "在物理服务器[%s]上部署应用服务器[%s]成功，并产生应用服务器实例[%s]",
                phyServer, asTemplate, asInstance);
        if (asInstance == null) {
            throw new MduException("应用服务器的实例为空。");
        }
        MduUtils.copyProperties(asInstance, phyServer, asTemplate);
        registerAppServerInstance(asInstance);
    }
    
    /**
     * 对指定的服务器实例执行特定命令。
     */
    private void execute(AppServerInstance appServerInstance, 
            Command command, AppServerInstanceState state) {
        if (appServerInstance == null
                || StringUtils.isEmpty(appServerInstance.getPhyServerOid())) {
            throw new MduException("服务器实例或者其关联的物理服务器不能为空。");
        }
        PhyServer phyServer = persistenceManager.findByPK(PhyServer.class,
                appServerInstance.getPhyServerOid());
        if (phyServer == null) {
            throw new MduException(String.format("未找到物理服务器[%s]。",
                    appServerInstance.getPhyServerOid()));
        }
        LogWriter.info2(logger, "在物理服务器[%s]上执行如下命令[%s]", phyServer, command);

        CommandClient cc = new CommandClient();
        command.setParam(appServerInstance);
        Boolean result = (Boolean) cc.execute(phyServer, command);
        
        appServerInstance.setState(state);
        persistenceManager.update(appServerInstance);
    }
    
    /**
     * 停止一个指定的服务器实例。
     */
    public void stopServerInstance(AppServerInstance appServerInstance) {
        LogWriter.info2(logger, "停止服务实例[%s]", appServerInstance);
        Command command = new StopCommand();
        execute(appServerInstance, command, AppServerInstanceState.STOPED);
    }
    
    /**
     * 启动一个指定的服务器实例。
     */
    public void startServerInstance(AppServerInstance appServerInstance) {
        LogWriter.info2(logger, "启动服务实例[%s]", appServerInstance);
        Command command = new StartCommand();
        execute(appServerInstance, command, AppServerInstanceState.STOPED);
    }
    
    /**
     * 强制停止（Kill）一个指定的服务器实例。
     */
    public void killServerInstance(AppServerInstance appServerInstance) {
        LogWriter.info2(logger, "强制停止服务实例[%s]", appServerInstance);
        Command command = new KillCommand();
        execute(appServerInstance, command, AppServerInstanceState.STOPED);
    }

    /**
     * 注册应用服务器模板
     *
     * @param asTemplate
     * @return
     */
    public void updateAppServerTemplate(AppServerTemplate asTemplate) {
        LogWriter.info2(logger, "更新应用服务器模板[%s]", asTemplate);
        // FIXME: 校验
        persistenceManager.insert(asTemplate);
    }

    /**
     * 注册任务分派器（当TaskDispatcher启动时自动注册）
     *
     * @param asTemplate
     * @return
     */
    public void registerTaskDispatcher(TaskDispatcher taskDispatcher) {
        LogWriter.info2(logger, "注册任务分配器[%s]", taskDispatcher);
        if (taskDispatcher == null || StringUtils.isEmpty(taskDispatcher.getId())) {
            LogWriter.warn2(logger, "任务分配器[%s]机器ID不能为空。", taskDispatcher);
            return;
        }

        // 删除之前未注销的实例
        String ql = "delete from task_dispatcher where id=?";
        persistenceManager.execute(ql, SqlUtils.getParams(taskDispatcher.getId()));
        persistenceManager.insert(taskDispatcher);
    }

    /**
     * 注册任务分派器（当TaskDispatcher关闭时自动注销，或者由“任务巡检程序”自动注销）。
     *
     * @param asTemplate
     * @return
     */
    public void unregisterTaskDispatcher(TaskDispatcher taskDispatcher) {
        LogWriter.info2(logger, "更新应用服务器模板[%s]", taskDispatcher);
        // FIXME: 校验
        if (taskDispatcher != null) {
            persistenceManager.delete(taskDispatcher);
        }
    }

    /**
     * 显示当前所有的任务分配器实例。
     *
     * @param asTemplate
     * @return
     */
    public List<TaskDispatcher> listTaskDispatchers() {
        LogWriter.info2(logger, "显示当前所有的任务分配器实例。");
        String sql = "valid=? AND STATE != ?";
        return persistenceManager.findAll(TaskDispatcher.class, sql,
                SqlUtils.getParams("y", ServerState.STOPED), null);
    }

    /**
     * 显示当前所有的任务分配器实例。
     *
     * @param asTemplate
     * @return
     */
    public List<AppServerInstance> listTaskDispatcherServers() {
        LogWriter.info2(logger, "显示当前所有的任务分配器实例。");
        return listAppServerInstances(ServerType.TASK_DISPATCHER);
    }

    /**
     * 注册应用服务器模板
     *
     * @param asTemplate
     * @return
     */
    public AppServerTemplate getTaskDispatcherTemplate(String templateOid) {
        LogWriter.info2(logger, "获取任务分派器模板[%s]", templateOid);
        if (StringUtils.isEmpty(templateOid)) {
            return null;  // 返回一个最新模板
        }
        // 这是一个IPO对象，所有需要改为实体对象
        AppServerTemplate ipo = persistenceManager.findByPK(AppServerTemplate.class, templateOid);
        if (ipo == null) {
            return null;
        }
        AppServerTemplate ast = new AppServerTemplate();
        BeanHelper.copy(ast, ipo);
        return ast;
    }
}
