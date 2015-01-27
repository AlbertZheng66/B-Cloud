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
 * �������ڼ�����Ŀ�У�
 *     1. ʵ���ĸ����Ƿ���ʣ�������Сʵ����������Ҫ�Զ�����һЩ����ʵ����
 *     2. �����Ѿ�ֹͣ���еķ��������Ƿ�ֹͣ������ʵ����Ȼ���ڣ�
 *     3. ��������Ϣ�Ѿ���ע������Ҫֹͣ�������е�ʵ������ɾ��ʵ����Ϣ��
 *     4. ���ʵ����״̬�Ƿ���ȷ(Ӧ�û��ߴ˰汾�Ѿ�ֹͣ��ʵ����������״̬�ȵ�)
 *     5. ���ʵ���Ѿ�ֹͣ���߱�ɾ��������Ӧ������ϢҲӦ�ñ�ɾ��
 * @author albert
 */
public class AppInstanceInspector extends AbstractInspector implements Injectable {

    private final Logger logger = Logger.getLogger(AppInstanceInspector.class);
    /**
     * �Ƿ���ʵ������������ͨ��������inspector.checkInstanceCount���������ã�Ĭ��Ϊ��false��
     */
    private final boolean isCheckInstanceCount = SystemConfiguration.getInstance().readBoolean("inspector.checkInstanceCount", false);

    /**
     * ������������������ڣ���ͨ��������taskDispater.timeToLive���������ã���λΪ���롱��Ĭ��Ϊ��300��
     */
    private final int timeToLiveOfTaskDispater = SystemConfiguration.getInstance().readInt("taskDispater.timeToLive", 300);

    /**
     * �������������ӳ�ʱ�䣬���ڴ�ʱ���ڣ�����������������Ϣ����ͨ��������inspector.delayOfStartup���������ã���λΪ���롱��Ĭ��Ϊ��120��
     */
    private final int delayOfStartup = SystemConfiguration.getInstance().readInt("inspector.delayOfStartup", 120);

    @InjectService
    private AppService appService;

    public AppInstanceInspector() {
    }

    public void excecute() {
        checkStopedServer(persistenceManager);
        checkInstanceCount(persistenceManager);
        // TODO: ʵ���Ѿ�ֹͣ�����������������֪��ʵ���Ƿ����
        checkStopedVersion(persistenceManager);
        checkUnavailableGroupConf(persistenceManager);
    }

    /**
     * ����汾�Ѿ�ֹͣ����Ҫ����Ƿ�����Ҫֹͣ��ʵ����
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
     * ʵ���ĸ����Ƿ���ʣ�������Сʵ����������Ҫ�Զ�����һЩ����ʵ����
     */
    private void checkInstanceCount(PersistenceManager persistenceManager) {
        if (!isCheckInstanceCount) {
            return;
        }
        List<PublishInfo> publishInfos = readPublishInfos(persistenceManager);  // �������еİ汾�ķ�����Ϣ

        for (Iterator<PublishInfo> it = publishInfos.iterator(); it.hasNext();) {
            PublishInfo pb = it.next();
            // if (pb.getAutoScale())  // TODO: �����Ƿ��Զ����������
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
     * �����Ѿ�ֹͣ���еķ��������Ƿ�ֹͣ������ʵ����Ȼ���ڣ�
     * @param persistenceManager
     */
    private void checkStopedServer(PersistenceManager persistenceManager) {
        List<AppInstance> instances = persistenceManager.findAll(AppInstance.class, "(STATE IS NULL OR STATE<>?)",
                SqlUtils.getParams(EnumUtils.toString(AppInstanceState.STOPED)), null);
        if (instances.isEmpty()) {
            return;
        }
        Set<String> runningServers = new HashSet();  // �������еķ�����
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
                LogWriter.info2(logger, "������[%s]�Ѿ�ֹͣ����Ӧ��ʵ��[%s]����Ϊ��ͣ״̬��", instance.getServerOid(), instance);
                instance.setState(AppInstanceState.STOPED);
                instance.setShutdownTime(Calendar.getInstance());
                persistenceManager.update(instance);
                // TODO: �費��Ҫֹͣʵ���أ�
            }
        }
        persistenceManager.commit();
    }

    /**
     * ���ʵ���Ѿ�ֹͣ���߱�ɾ��������Ӧ������ϢҲӦ�ñ�ɾ��
     * @param persistenceManager
     */
    private void checkUnavailableGroupConf(PersistenceManager persistenceManager) {
        // ���ȼ���Ѿ�ֹͣʵ��
        List params = SqlUtils.getParams(CattleManager.TASK_DISPATCHER_PREFIX.length(),
                CattleManager.TASK_DISPATCHER_PREFIX);
        // �ӳ��������ٽ��д���
        String where = String.format(" WHERE (INSERT_TIME+INTERVAL %d SECOND)<now() AND SUBSTRING(ENTITY_ID, 1, ?) != ? AND ", delayOfStartup);
        String sql = "DELETE FROM GROUP_CONF " + where + " (ENTITY_ID NOT IN(SELECT OID FROM APP_INSTANCE))";
        LogWriter.info2(logger, "���ʧЧ������Ϣ��sql=[%s], ����=[%s]", sql, params);
        persistenceManager.execute(sql, params);

        // �������������������һ��ʱ����û��������Ӧ������Ϊ���Ѿ�������
        sql = String.format("DELETE FROM GROUP_CONF WHERE (INSERT_TIME+INTERVAL %d SECOND)<now() AND (LAST_UPDATE_TIME+INTERVAL %d SECOND)<now() AND SUBSTRING(ENTITY_ID, 1, ?) = ?  ",
                delayOfStartup, timeToLiveOfTaskDispater);
        LogWriter.info2(logger, "���ʧЧ�������������Ϣ��sql=[%s], ����=[%s]", sql, params);
        persistenceManager.execute(sql, params);

        // ɾ���Ѿ���ֹͣʵ������������Ϣ
        params.add(EnumUtils.toString(AppInstanceState.STOPED));
        sql = "DELETE FROM GROUP_CONF " + where + " (ENTITY_ID IN(SELECT OID FROM APP_INSTANCE WHERE STATE=?))";
        persistenceManager.execute(sql, params);
        LogWriter.info2(logger, "���ʧЧ������Ϣ��sql=[%s], ����=[%s]", sql, params);

    }

    /**
     * ������õĻ���
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
