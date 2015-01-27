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
     * ע��һ�������������
     *
     * @param phyServer
     */
    public void registerPhyServer(PhyServer phyServer) {
        LogWriter.info2(logger, "��ʼע�����������ʵ��[%s]", phyServer);
        String oid = phyServer.getOid();
        phyServer.setLastUpdatedTime(Calendar.getInstance());
        PhyServer old = (PhyServer) persistenceManager.findByPK(PhyServer.class, oid);
        LogWriter.info2(logger, "�鿴�Ƿ��Ѿ��������������ʵ��[%s]��״̬", old);
        if (old == null) {
            phyServer.setInsertTime(Calendar.getInstance());
            phyServer.setInvalidTime(Constants.INVALID_TIME); // �������һ��ֵ��
            persistenceManager.insert(phyServer);
        } else {
            LogWriter.info2(logger, "�������������ʵ��[%s]��״̬", phyServer);
            phyServer.setValid(old.getValid());  // �����Զ����������������ʧЧ״̬
            persistenceManager.update(phyServer);
        }
    }

    public void disablePhyServer(PhyServer phyServer) {
        LogWriter.info2(logger, "�����������ʵ��[%s]��ʱʧЧ", phyServer);
        phyServer.setValid(false);
        persistenceManager.update(phyServer);
    }

    /**
     * ע��һ��Ӧ�÷�����ʵ��
     */
    public boolean registerAppServerInstance(AppServerInstance appServerInstance) {
        LogWriter.info2(logger, "ע��Ӧ�÷�����ʵ��[%s]", appServerInstance);
        if (appServerInstance == null || StringUtils.isEmpty(appServerInstance.getOid())) {
            LogWriter.warn2(logger, "Ӧ�÷�����ʵ��[%s]ע��ʧ��[ʵ����������Ϊ��]��", appServerInstance);
            return false;
        }
        AppServerInstance asInstance = persistenceManager.findByPK(AppServerInstance.class, appServerInstance.getOid());
        LogWriter.info2(logger, "�Ѿ���ѯ��Ӧ�÷�����ʵ��[%s]", asInstance);
        if (asInstance == null || StringUtils.isEmpty(asInstance.getOid())) {
            return persistenceManager.insert(appServerInstance);
        }
        return false;
    }

    /**
     * ��ʾ��ǰ���е�Ӧ�÷�����ʵ��
     */
    public List<AppServerInstance> listAllAppServerInstances() {
        return listAppServerInstances(ServerType.APP_SERVER);
    }

    private List<AppServerInstance> listAppServerInstances(ServerType serverType) {
        LogWriter.info2(logger, "��ʾ��ǰ���е�Ӧ�÷�����ʵ����");
        return persistenceManager.query(AppServerInstance.class,
                "SELECT * FROM APP_SERVER_INSTANCE WHERE SERVER_TYPE=? ORDER BY INSERT_TIME DESC",
                SqlUtils.getParams(serverType));
    }

    /**
     * ���Ի�ȡӦ�÷�������Ϣ
     */
    public ServerInfo getServerInfo(AppServerInstance appServerInstance) {
        LogWriter.info2(logger, "���Ի�ȡӦ�÷�������Ϣ[%s]", appServerInstance);
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
     * ע��һ��Ӧ�÷�����ʵ��
     */
    public void updateAppServerInstance(AppServerInstance appServerInstance) {
        LogWriter.info2(logger, "����Ӧ�÷�����ʵ��[%s]", appServerInstance);
        if (appServerInstance == null || StringUtils.isEmpty(appServerInstance.getOid())) {
            LogWriter.warn2(logger, "����Ӧ�÷�����ʵ��[%s]ʧ��[ʵ����������Ϊ��]��", appServerInstance);
            return;
        }
        registerAppServerInstance(appServerInstance);
        persistenceManager.update(appServerInstance);
    }

    /**
     * ��ʾ����Ӧ�÷���ģ��
     */
    public List<AppServerTemplate> listAppServerTemplates() {
        LogWriter.info2(logger, "��������Ӧ�÷���ģ��");
        List<AppServerTemplate> asTemplates = persistenceManager.findAll(AppServerTemplate.class);
        return asTemplates;
    }

    /**
     * ɾ��ָ����Ӧ�÷���ģ��
     */
    public void deleteAppServerTemplate(AppServerTemplate asTemplate) {
        LogWriter.info2(logger, "ɾ��ָ����Ӧ�÷���ģ��[%s]", asTemplate);
        // FIXME: У��
        persistenceManager.delete(asTemplate);
    }

    /**
     * ����OID����һ��Ӧ�÷�����ʵ��
     */
    public AppServerInstance getAppServerInstance(String oid) {
        LogWriter.info2(logger, "����һ��Ӧ�÷�����ʵ��[%s]", oid);
        // У��
        if (StringUtils.isEmpty(oid)) {
            LogWriter.warn2(logger, "Ӧ�÷�����ʵ��OID[%s]����Ϊ�ա�", oid);
            return null;
        }
        AppServerInstance _instance = (AppServerInstance) persistenceManager.findByPK(AppServerInstance.class, oid);
        if (_instance == null) {
            LogWriter.info2(logger, "Ӧ�÷�����ʵ��[%s]�����ڡ�", oid);
            return null;
        }
        AppServerInstance instance = new AppServerInstance();
        BeanHelper.copy(instance, _instance);
        return instance;
    }

    /**
     * ע��һ��Ӧ�÷�����ʵ��
     */
    public List<AppServerInstance> listAppServerInstances(PhyServer phyServer) {
        LogWriter.info2(logger, "����Ӧ�÷�����ʵ���б�[%s]", phyServer);
        // FIXME: У��
        List<AppServerInstance> instances = persistenceManager.findAll(AppServerInstance.class,
                "PHY_SERVER_OID=?", SqlUtils.getParams(phyServer.getOid()), null);
        return instances;
    }

    /**
     * ������������������
     */
    public void psBeat(PhyServer phyServer) {
        LogWriter.info2(logger, "���������ʵ��[%s]��һ������", phyServer);
        if (phyServer == null || StringUtils.isEmpty(phyServer.getOid())) {
            return;
        }
        phyServer.setLastUpdatedTime(Calendar.getInstance());
        persistenceManager.update(phyServer);
    }

    /**
     * ������������������
     */
    public void tdBeat(TaskDispatcher taskDispatcher) {
        LogWriter.info2(logger, "���������ʵ��[%s]��һ������", taskDispatcher);
        if (taskDispatcher == null || StringUtils.isEmpty(taskDispatcher.getOid())) {
            return;
        }
        taskDispatcher.setLastUpdatedTime(Calendar.getInstance());
        persistenceManager.update(taskDispatcher);
    }

    public void unregisterPhyServer(PhyServer phyServer) {
        LogWriter.info2(logger, "ע�����������ʵ��[%s]", phyServer);
        if (phyServer == null || StringUtils.isEmpty(phyServer.getOid())) {
            return;
        }
        phyServer.setLastUpdatedTime(Calendar.getInstance());
        phyServer.setState(PhyServerState.STOPED);
        persistenceManager.update(phyServer);
    }

    /**
     * ������ϵͳѡ���ķ�������
     */
    public void deployServerTemplate(DeployingParam param) {
        if (param == null || param.getAsTemplate() == null) {
            throw new MduException("��������Ϊ�ա�");
        }
        LogWriter.info2(logger, "����Ӧ�÷�����[%s]", param.getAsTemplate());
        List<PhyServer> selectedPhyServer;
        if (param.isRandomSelection()) {
            List phyServers = listAvailablePhyServers();
            selectedPhyServer = RandomUtils.select(phyServers, param.getCount());
        } else {
            selectedPhyServer = param.getPhyServers();
        }
        System.out.println("phyServer=" + selectedPhyServer);
        if (CollectionUtils.isEmpty(selectedPhyServer)) {
            throw new MduException("û�п��õķ�������");
        }
        for (Iterator<PhyServer> it = selectedPhyServer.iterator(); it.hasNext();) {
            PhyServer phyServer = it.next();
            deployToServer(phyServer, param.getAsTemplate());
        }

    }

    /**
     * ��ʾ���е������������
     */
    public List<PhyServer> listPhyServers() {
        LogWriter.info2(logger, "��ʾ���е����������");
        return persistenceManager.findAll(PhyServer.class);
    }

    /**
     * ��ʾ���еĿ��õ������������
     */
    public List<PhyServer> listAvailablePhyServers() {
        return persistenceManager.findAll(PhyServer.class, "valid=? and (state=? OR state=?)",
                SqlUtils.getParams(Boolean.TRUE, PhyServerState.AVAILABLE,
                PhyServerState.USING), null);
    }

    /**
     * ��ʾ���п��õķ������� FIXME: ���汾�ŵ���
     */
    public List<AppServerTemplate> listAvailableAppServers(String type) {
        return persistenceManager.findAll(AppServerTemplate.class,
                "VALID=? AND SERVER_TYPE=?", SqlUtils.getParams(Boolean.TRUE, type),
                "INSERT_TIME DESC");
    }

    /**
     * ����ָ����������
     */
    public void deployToServer(PhyServer phyServer, AppServerTemplate asTemplate) {
        LogWriter.info2(logger, "�����������[%s]�ϲ���Ӧ�÷�����[%s]", phyServer, asTemplate);

        // System.out.println("json=" + asTemplate.getParams());
        CommandClient cc = new CommandClient();
        Command command = new DeployCommand();
        command.setParam(asTemplate);
        AppServerInstance asInstance = (AppServerInstance) cc.execute(phyServer, command);
        LogWriter.info2(logger, "�����������[%s]�ϲ���Ӧ�÷�����[%s]�ɹ���������Ӧ�÷�����ʵ��[%s]",
                phyServer, asTemplate, asInstance);
        if (asInstance == null) {
            throw new MduException("Ӧ�÷�������ʵ��Ϊ�ա�");
        }
        MduUtils.copyProperties(asInstance, phyServer, asTemplate);
        registerAppServerInstance(asInstance);
    }
    
    /**
     * ��ָ���ķ�����ʵ��ִ���ض����
     */
    private void execute(AppServerInstance appServerInstance, 
            Command command, AppServerInstanceState state) {
        if (appServerInstance == null
                || StringUtils.isEmpty(appServerInstance.getPhyServerOid())) {
            throw new MduException("������ʵ��������������������������Ϊ�ա�");
        }
        PhyServer phyServer = persistenceManager.findByPK(PhyServer.class,
                appServerInstance.getPhyServerOid());
        if (phyServer == null) {
            throw new MduException(String.format("δ�ҵ����������[%s]��",
                    appServerInstance.getPhyServerOid()));
        }
        LogWriter.info2(logger, "�����������[%s]��ִ����������[%s]", phyServer, command);

        CommandClient cc = new CommandClient();
        command.setParam(appServerInstance);
        Boolean result = (Boolean) cc.execute(phyServer, command);
        
        appServerInstance.setState(state);
        persistenceManager.update(appServerInstance);
    }
    
    /**
     * ֹͣһ��ָ���ķ�����ʵ����
     */
    public void stopServerInstance(AppServerInstance appServerInstance) {
        LogWriter.info2(logger, "ֹͣ����ʵ��[%s]", appServerInstance);
        Command command = new StopCommand();
        execute(appServerInstance, command, AppServerInstanceState.STOPED);
    }
    
    /**
     * ����һ��ָ���ķ�����ʵ����
     */
    public void startServerInstance(AppServerInstance appServerInstance) {
        LogWriter.info2(logger, "��������ʵ��[%s]", appServerInstance);
        Command command = new StartCommand();
        execute(appServerInstance, command, AppServerInstanceState.STOPED);
    }
    
    /**
     * ǿ��ֹͣ��Kill��һ��ָ���ķ�����ʵ����
     */
    public void killServerInstance(AppServerInstance appServerInstance) {
        LogWriter.info2(logger, "ǿ��ֹͣ����ʵ��[%s]", appServerInstance);
        Command command = new KillCommand();
        execute(appServerInstance, command, AppServerInstanceState.STOPED);
    }

    /**
     * ע��Ӧ�÷�����ģ��
     *
     * @param asTemplate
     * @return
     */
    public void updateAppServerTemplate(AppServerTemplate asTemplate) {
        LogWriter.info2(logger, "����Ӧ�÷�����ģ��[%s]", asTemplate);
        // FIXME: У��
        persistenceManager.insert(asTemplate);
    }

    /**
     * ע���������������TaskDispatcher����ʱ�Զ�ע�ᣩ
     *
     * @param asTemplate
     * @return
     */
    public void registerTaskDispatcher(TaskDispatcher taskDispatcher) {
        LogWriter.info2(logger, "ע�����������[%s]", taskDispatcher);
        if (taskDispatcher == null || StringUtils.isEmpty(taskDispatcher.getId())) {
            LogWriter.warn2(logger, "���������[%s]����ID����Ϊ�ա�", taskDispatcher);
            return;
        }

        // ɾ��֮ǰδע����ʵ��
        String ql = "delete from task_dispatcher where id=?";
        persistenceManager.execute(ql, SqlUtils.getParams(taskDispatcher.getId()));
        persistenceManager.insert(taskDispatcher);
    }

    /**
     * ע���������������TaskDispatcher�ر�ʱ�Զ�ע���������ɡ�����Ѳ������Զ�ע������
     *
     * @param asTemplate
     * @return
     */
    public void unregisterTaskDispatcher(TaskDispatcher taskDispatcher) {
        LogWriter.info2(logger, "����Ӧ�÷�����ģ��[%s]", taskDispatcher);
        // FIXME: У��
        if (taskDispatcher != null) {
            persistenceManager.delete(taskDispatcher);
        }
    }

    /**
     * ��ʾ��ǰ���е����������ʵ����
     *
     * @param asTemplate
     * @return
     */
    public List<TaskDispatcher> listTaskDispatchers() {
        LogWriter.info2(logger, "��ʾ��ǰ���е����������ʵ����");
        String sql = "valid=? AND STATE != ?";
        return persistenceManager.findAll(TaskDispatcher.class, sql,
                SqlUtils.getParams("y", ServerState.STOPED), null);
    }

    /**
     * ��ʾ��ǰ���е����������ʵ����
     *
     * @param asTemplate
     * @return
     */
    public List<AppServerInstance> listTaskDispatcherServers() {
        LogWriter.info2(logger, "��ʾ��ǰ���е����������ʵ����");
        return listAppServerInstances(ServerType.TASK_DISPATCHER);
    }

    /**
     * ע��Ӧ�÷�����ģ��
     *
     * @param asTemplate
     * @return
     */
    public AppServerTemplate getTaskDispatcherTemplate(String templateOid) {
        LogWriter.info2(logger, "��ȡ���������ģ��[%s]", templateOid);
        if (StringUtils.isEmpty(templateOid)) {
            return null;  // ����һ������ģ��
        }
        // ����һ��IPO����������Ҫ��Ϊʵ�����
        AppServerTemplate ipo = persistenceManager.findByPK(AppServerTemplate.class, templateOid);
        if (ipo == null) {
            return null;
        }
        AppServerTemplate ast = new AppServerTemplate();
        BeanHelper.copy(ast, ipo);
        return ast;
    }
}
