package com.xt.bcloud.resource;

import com.xt.bcloud.app.App;
import com.xt.bcloud.app.AppInstance;
import com.xt.bcloud.app.AppInstanceState;
import com.xt.bcloud.app.AppVersion;
import com.xt.bcloud.comm.Capacity;
import com.xt.bcloud.comm.Constants;
import com.xt.bcloud.resource.server.ServerInfo;
import com.xt.bcloud.resource.server.ServerState;
import com.xt.bcloud.worker.Cattle;
import com.xt.core.db.pm.IPOPersistenceManager;
import com.xt.core.db.pm.Item;
import com.xt.core.exception.ServiceException;
import com.xt.core.exception.SystemException;
import com.xt.core.log.LogWriter;
import com.xt.core.proc.impl.IPOPersistenceAware;
import com.xt.core.service.IService;
import com.xt.core.service.LocalMethod;
import com.xt.core.utils.EnumUtils;
import com.xt.core.utils.SqlUtils;
import com.xt.proxy.Proxy;
import com.xt.proxy.ServiceFactory;
import com.xt.proxy.impl.http.stream.HttpStreamProxy;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.log4j.Logger;

/**
 *
 * @author albert
 */
public class ResourceService implements IService, IPOPersistenceAware {

    private final Logger logger = Logger.getLogger(ResourceService.class);
    /**
     * �־û�������ʵ��
     */
    private transient IPOPersistenceManager persistenceManager;

    public ResourceService() {
    }

    /**
     * ע��һ����Դ
     * @param resource
     * @return ע���Ƿ�ɹ������serverInfo ��ID�Ѿ����ڣ���ע�Ὣʧ�ܡ�
     */
    public boolean registerServer(ServerInfo serverInfo, Profile profile) {
        if (serverInfo == null) {
            throw new ResourceException("��������Ϣ����Ϊ�ա�");
        }
        LogWriter.info(logger, "registerServer serverInfo", serverInfo);
        LogWriter.info(logger, "profile", profile);
        // �־û��˷�����
        List list = persistenceManager.findAll(ServerInfo.class, "ID=? AND STATE!=?",
                SqlUtils.getParams(serverInfo.getId(), EnumUtils.toString(ServerState.STOPED)), null);
        if (!list.isEmpty()) {
            throw new ResourceException(String.format("������[%s]�Ѿ�ע�ᣬ��֪ͨϵͳ����ԱЭ�����������",
                    serverInfo));
        }
        serverInfo.setOid(UUID.randomUUID().toString());
        serverInfo.setValid(true);
        serverInfo.setState(ServerState.AVAILABLE);
        serverInfo.setInsertTime(Calendar.getInstance());
        serverInfo.setInvalidTime(Constants.INVALID_TIME);
        this.persistenceManager.insert(serverInfo);
        return true;
    }

    /**
     * ע��һ����Դ.
     * @param resource
     * @return ע���Ƿ�ɹ������serverInfo ��ID�Ѿ����ڣ���ע�Ὣʧ�ܡ�
     */
    public boolean unregisterServer(ServerInfo serverInfo) {
        LogWriter.info(logger, "��ʼע�������� serverInfo", serverInfo);

        // �־û��˷�����
        List<ServerInfo> serverInfos = persistenceManager.findAll(ServerInfo.class, "ID=?",
                SqlUtils.getParams(serverInfo.getId()), null);
        if (serverInfos.isEmpty()) {
            LogWriter.warn(logger, String.format("��ע���ķ�����[%s]δ��¼��", serverInfo));
            // throw new ResourceException(message);
            return false;
        }
        for (Iterator<ServerInfo> it0 = serverInfos.iterator(); it0.hasNext();) {
            ServerInfo serverInfo1 = it0.next();
            // �����ڱ�ע���ķ������ġ��������е�ʵ��������Ϊ��ֹͣ��
            List<AppInstance> intances = persistenceManager.findAll(AppInstance.class, "SERVER_OID=? AND STATE=?",
                    SqlUtils.getParams(serverInfo1.getOid(), EnumUtils.toString(AppInstanceState.RUNNING)), null);
            for (Iterator<AppInstance> it = intances.iterator(); it.hasNext();) {
                AppInstance appInstance = it.next();
                appInstance.setState(AppInstanceState.STOPED);
                persistenceManager.update(appInstance);
            }
        }



        for (Iterator<ServerInfo> it = serverInfos.iterator(); it.hasNext();) {
            ServerInfo _servInfo = it.next();
            LogWriter.warn(logger, String.format("ע���ķ�������¼[%s]��", _servInfo));
            persistenceManager.delete(_servInfo);
        }
        return true;
    }

    /**
     * ����һ����Դ����һͷţ��
     * @param capacity ָ��ţ������
     * @return
     */
    public Cattle applyFor(App app, AppVersion appVersion, String appInstanceOid, Capacity capacity) {
        LogWriter.info(logger, String.format("����ΪӦ��[%s](�汾Ϊ[%s])��������Ϊ[%s]����Դ��",
                app, appVersion, capacity));
        if (app == null) {
            throw new SystemException("������Ӧ�ò���Ϊ�ա�");
        }

        if (appVersion == null) {
            throw new SystemException("Ӧ�ð汾����Ϊ�ա�");
        }

        // ���δָ�����ã���ʹ�ñ�׼���á�
        if (capacity == null) {
            capacity = Constants.STANDARD_CAPACITY;
        }

        Cattle cattle = null;

        // ���Է��������10�����������ָ���Ĵ���֮��ϵͳ������ʧ����Ϣ��
        for (int i = 0; i < 10; i++) {
            // �ҵ��Ѿ�ע��ķ���������������ѡ��һ�����ʵķ�������
            ServerInfo serverInfo = findServer(app, capacity);
            if (serverInfo == null) {
                throw new ResourceException("�޿��õ���Դ");
            }

            LogWriter.info(logger, String.format("ΪӦ��[%s]�����˷�����[%s]����������[%s]����",
                    app, serverInfo, capacity));

            // ����Դ����������Դ
            String proxyUrl = String.format("http://%s:%d%s", serverInfo.getIp(),
                    serverInfo.getManagerPort(), serverInfo.getContextPath());
            Proxy proxy = new HttpStreamProxy(proxyUrl);

            ServerService serverManager = ServiceFactory.getInstance().getService(ServerService.class, proxy);
            cattle = serverManager.deploy(app, appVersion, appInstanceOid, capacity);

            // �ҵ�������
            if (cattle != null) {
                cattle.setServerOid(serverInfo.getOid());
                break;
            }
        }
        if (cattle == null) {
            // ����ʧ�ܣ���Ҫ���·��������
            LogWriter.warn(logger, String.format("ΪӦ��[%s]�����˷���������������[%s]��ʧ�ܡ�",
                    app, capacity));
        }

        return cattle;
    }

    /**
     * TODO: ���Ӧ�÷���ͬһ�����������������
     * @param app
     * @param capacity
     * @return
     */
    private ServerInfo findServer(App app, Capacity capacity) {
        List<ServerInfo> servers = getAvailableServers();
        if (servers.isEmpty()) {
            return null;
        }

        // ������������ʣ������
        if (servers.size() == 1) {
            return servers.get(0);
        }

        // TODO: ������֤ͬһ��Ӧ��ʹ�ò�ͬ�ķ�����
        Collections.sort(servers, new ServerComparator(app, capacity, getInstancesCount(app)));

        // ѡ�����ȼ���ߵķ�����
        ServerInfo selected = servers.get(0);
        LogWriter.info2(logger, "���������[%s]��", selected);
        return selected;

//        int index = new Random().nextInt(servers.size());
//        return servers.get(index);
    }

    private Map<String, Integer> getInstancesCount(App app) {
        final Map<String, Integer> ic = new HashMap();
        persistenceManager.query("SELECT SERVER_OID, COUNT(SERVER_OID) CNT FROM APP_INSTANCE WHERE APP_OID=? GROUP BY SERVER_OID",
                SqlUtils.getParams(app.getOid()), new Item() {

            public Object createObject(ResultSet rs) throws SQLException {
                ic.put(rs.getString("SERVER_OID"), rs.getInt("CNT"));
                return rs.getRow();
            }

            public boolean isPagination() {
                return false;
            }
        });
        return ic;
    }

    /**
     * ���ؿ��õķ�������Ϣ��
     * @return ��������Ϣ����
     */
    private List<ServerInfo> getAvailableServers() {
        List<ServerInfo> servers = persistenceManager.findAll(ServerInfo.class,
                "(STATE=? OR STATE=?)", 
                SqlUtils.getParams(EnumUtils.toString(ServerState.AVAILABLE),
                EnumUtils.toString(ServerState.USING)), null);
        return servers;
    }

    /**
     * ��ʾ��ǰ����ע��ķ�����.
     * @return
     */
    public List<ServerInfo> listServers() {
        List<ServerInfo> servers = persistenceManager.findAll(ServerInfo.class, "1=1", null, null);
        return servers;
    }
    
    /**
     * ��ʾ��ǰ����ע��ķ�����.
     * @return
     */
    public List<ServerInfo> listAvailableServers() {
        List<ServerInfo> servers = persistenceManager.findAll(ServerInfo.class, "state != ?",
                SqlUtils.getParams(ServerState.STOPED), null);
        return servers;
    }

    /**
     * ɾ��һ����������Ϣ��
     * ��������������ֹʱ������Ա����ͨ���˽ӿ�ɾ��һ���Ѿ�ע��ķ�������
     */
    public boolean removeServer(ServerInfo serverInfo) {
        if (serverInfo == null) {
            return false;
        }
        if (null == persistenceManager.findByPK(serverInfo)) {
            throw new ServiceException(String.format("������[%s]δע�ᡣ", serverInfo));
        }

        // ��Դ����ʹ��ʱ������ɾ��

        // TODO: ����Դ��ص�ʵ������Ҫɾ��?
        persistenceManager.execute("DELETE FROM APP_INSTANCE WHERE SERVER_OID=?",
                SqlUtils.getParams(serverInfo.getOid()));

        return persistenceManager.delete(serverInfo);
    }

    /**
     * ����ʹ����ͷţ
     * @param cattle
     */
    public void giveback(Cattle cattle) {
    }

    @LocalMethod
    public void setPersistenceManager(IPOPersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    @LocalMethod
    public IPOPersistenceManager getPersistenceManager() {
        return this.persistenceManager;
    }
}

/**
 * ����Ӧ�õ���Ϣ�������Է�������������.
 * @author albert
 */
class ServerComparator implements Comparator<ServerInfo> {

    private final App app;
    private final Capacity capacity;
    /**
     * ���������е�ʵ����������Ӧ�ã�����������ͬһ̨�������Ϸ���Ӧ�ã�
     */
    private final Map<String, Integer> instancesCount;

    public ServerComparator(App app, Capacity capacity, Map<String, Integer> instancesCount) {
        this.app = app;
        this.capacity = capacity;
        this.instancesCount = instancesCount;
    }

    public int compare(ServerInfo o1, ServerInfo o2) {
        String oid1 = o1.getOid();
        String oid2 = o2.getOid();
        int count1 = instancesCount.containsKey(oid1) ? instancesCount.get(oid1) : 0;  // ��������ʵ������
        //System.out.println("compare......count1=" +  count1);
        int count2 = instancesCount.containsKey(oid2) ? instancesCount.get(oid2) : 0;
        //System.out.println("compare......count2=" +  count2);
        return (count1 - count2);
    }
}
