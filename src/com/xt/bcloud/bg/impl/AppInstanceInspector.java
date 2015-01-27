package com.xt.bcloud.bg.impl;

import com.xt.bcloud.app.App;
import com.xt.bcloud.app.AppInstance;
import com.xt.bcloud.app.AppInstanceState;
import com.xt.bcloud.app.AppService;
import com.xt.bcloud.app.AppVersion;
import com.xt.bcloud.app.AppVersionState;
import com.xt.bcloud.app.PublishInfo;
import com.xt.bcloud.resource.GroupConf;
import com.xt.bcloud.resource.server.ServerInfo;
import com.xt.bcloud.resource.server.ServerState;
import com.xt.bcloud.td.CattleManager;
import com.xt.core.db.pm.PersistenceManager;
import com.xt.core.log.LogWriter;
import com.xt.core.proc.impl.InjectService;
import com.xt.core.proc.impl.Injectable;
import com.xt.core.utils.EnumUtils;
import com.xt.core.utils.SqlUtils;
import com.xt.gt.sys.SystemConfiguration;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 * 此类用于检查的项目有：
 *     1. 实例的个数是否合适（低于最小实例个数，需要自动申请一些其他实例）
 *     2. 有无已经停止运行的服务器（非法停止，但是实例仍然存在）
 *     3. 服务器信息已经被注销，需要停止正在运行的实例，并删除实例信息。
 *     4. 检查实例的状态是否正确(应用或者此版本已经停止，实例处于运行状态等等)
 *     5. 如果实例已经停止或者被删除，其相应的组信息也应该被删除
 * @author albert
 */
public class AppInstanceInspector extends AbstractInspector implements Injectable {

    private final Logger logger = Logger.getLogger(AppInstanceInspector.class);
    /**
     * 是否检测实例的数量，可通过参数“inspector.checkInstanceCount”进行配置，默认为：false。
     */
    private final boolean isCheckInstanceCount = SystemConfiguration.getInstance().readBoolean("inspector.checkInstanceCount", false);

    /**
     * 任务管理器的生命周期，可通过参数“taskDispater.timeToLive”进行配置，单位为“秒”，默认为：300。
     */
    private final int timeToLiveOfTaskDispater = SystemConfiguration.getInstance().readInt("taskDispater.timeToLive", 300);

    /**
     * 服务器启动的延迟时间，即在此时段内，不检查其组的配置信息。可通过参数“inspector.delayOfStartup”进行配置，单位为“秒”，默认为：120。
     */
    private final int delayOfStartup = SystemConfiguration.getInstance().readInt("inspector.delayOfStartup", 120);

    @InjectService
    private AppService appService;

    public AppInstanceInspector() {
    }

    public void excecute() {
        checkStopedServer(persistenceManager);
        checkInstanceCount(persistenceManager);
        // TODO: 实例已经停止，但是任务管理器不知道实例是否可用
        checkStopedVersion(persistenceManager);
        checkUnavailableGroupConf(persistenceManager);
    }

    /**
     * 如果版本已经停止，需要检查是否有需要停止的实例。
     * @param persistenceManager
     */
    private void checkStopedVersion(PersistenceManager persistenceManager) {
        String sql = "SELECT I.* FROM APP_INSTANCE I JOIN APP_VERSION V ON I.APP_VERSION_OID=V.OID WHERE V.STATE=? AND I.STATE=?";
        List<AppInstance> runningInstances = persistenceManager.query(AppInstance.class,
                sql, SqlUtils.getParams(EnumUtils.toString(AppVersionState.STOPED),
                EnumUtils.toString(AppInstanceState.RUNNING)));
        for (Iterator<AppInstance> it = runningInstances.iterator(); it.hasNext();) {
            AppInstance appInstance = it.next();
            appService.stopInstance(appInstance);
        }
    }

    /**
     * 实例的个数是否合适（低于最小实例个数，需要自动申请一些其他实例）
     */
    private void checkInstanceCount(PersistenceManager persistenceManager) {
        if (!isCheckInstanceCount) {
            return;
        }
        List<PublishInfo> publishInfos = readPublishInfos(persistenceManager);  // 正在运行的版本的发布信息

        for (Iterator<PublishInfo> it = publishInfos.iterator(); it.hasNext();) {
            PublishInfo pb = it.next();
            // if (pb.getAutoScale())  // TODO: 考虑是否自动调整的情况
            App app = new App();
            app.setOid(pb.getAppOid());
            AppVersion version = new AppVersion();
            version.setOid(pb.getAppVersionOid());
            appService.alterCapacity(app, version, pb);
        }
    }

    private List<PublishInfo> readPublishInfos(PersistenceManager persistenceManager) {
        return persistenceManager.query(PublishInfo.class, "SELECT * FROM APP_VERSION A, PUBLISH_INFO P WHERE A.OID=P.APP_VERSION_OID AND A.STATE=?",
                SqlUtils.getParams(AppVersionState.RUNNING));
    }

    /**
     * 有无已经停止运行的服务器（非法停止，但是实例仍然存在）
     * @param persistenceManager
     */
    private void checkStopedServer(PersistenceManager persistenceManager) {
        List<AppInstance> instances = persistenceManager.findAll(AppInstance.class, "(STATE IS NULL OR STATE<>?)",
                SqlUtils.getParams(EnumUtils.toString(AppInstanceState.STOPED)), null);
        if (instances.isEmpty()) {
            return;
        }
        Set<String> runningServers = new HashSet();  // 正在运行的服务器
        List<ServerInfo> serverInfos = persistenceManager.findAll(ServerInfo.class, "(STATE IS NULL OR STATE<>?)",
                SqlUtils.getParams(EnumUtils.toString(ServerState.STOPED)), null);
        for (Iterator<ServerInfo> it = serverInfos.iterator(); it.hasNext();) {
            ServerInfo si = it.next();
            runningServers.add(si.getOid());
        }
        for (Iterator<AppInstance> it = instances.iterator(); it.hasNext();) {
            AppInstance instance = it.next();
            if (instance.getServerOid() != null
                    && !runningServers.contains(instance.getServerOid())) {
                LogWriter.info2(logger, "服务器[%s]已经停止，将应用实例[%s]设置为暂停状态。", instance.getServerOid(), instance);
                instance.setState(AppInstanceState.STOPED);
                instance.setShutdownTime(Calendar.getInstance());
                persistenceManager.update(instance);
                // TODO: 需不需要停止实例呢？
            }
        }
        persistenceManager.commit();
    }

    /**
     * 如果实例已经停止或者被删除，其相应的组信息也应该被删除
     * @param persistenceManager
     */
    private void checkUnavailableGroupConf(PersistenceManager persistenceManager) {
        // 首先检查已经停止实例
        List params = SqlUtils.getParams(CattleManager.TASK_DISPATCHER_PREFIX.length(),
                CattleManager.TASK_DISPATCHER_PREFIX);
        // 延迟两分钟再进行处理
        String where = String.format(" WHERE (INSERT_TIME+INTERVAL %d SECOND)<now() AND SUBSTRING(ENTITY_ID, 1, ?) != ? AND ", delayOfStartup);
        String sql = "DELETE FROM GROUP_CONF " + where + " (ENTITY_ID NOT IN(SELECT OID FROM APP_INSTANCE))";
        LogWriter.info2(logger, "检查失效的组信息，sql=[%s], 参数=[%s]", sql, params);
        persistenceManager.execute(sql, params);

        // 检查任务管理器，如果在一定时间内没有心跳反应，则认为其已经不存在
        sql = String.format("DELETE FROM GROUP_CONF WHERE (INSERT_TIME+INTERVAL %d SECOND)<now() AND (LAST_UPDATE_TIME+INTERVAL %d SECOND)<now() AND SUBSTRING(ENTITY_ID, 1, ?) = ?  ",
                delayOfStartup, timeToLiveOfTaskDispater);
        LogWriter.info2(logger, "检查失效的任务管理器信息，sql=[%s], 参数=[%s]", sql, params);
        persistenceManager.execute(sql, params);

        // 删除已经被停止实例的组配置信息
        params.add(EnumUtils.toString(AppInstanceState.STOPED));
        sql = "DELETE FROM GROUP_CONF " + where + " (ENTITY_ID IN(SELECT OID FROM APP_INSTANCE WHERE STATE=?))";
        persistenceManager.execute(sql, params);
        LogWriter.info2(logger, "检查失效的组信息，sql=[%s], 参数=[%s]", sql, params);

    }

    /**
     * 清除无用的缓存
     */
    private void checkUnavailableCache() {

    }

    public AppService getAppService() {
        return appService;
    }

    public void setAppService(AppService appService) {
        this.appService = appService;
    }
}
