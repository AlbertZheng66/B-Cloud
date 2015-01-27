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
     * 持久化管理器实例
     */
    private transient IPOPersistenceManager persistenceManager;

    public ResourceService() {
    }

    /**
     * 注册一个资源
     * @param resource
     * @return 注册是否成功，如果serverInfo 的ID已经存在，则注册将失败。
     */
    public boolean registerServer(ServerInfo serverInfo, Profile profile) {
        if (serverInfo == null) {
            throw new ResourceException("服务器信息不能为空。");
        }
        LogWriter.info(logger, "registerServer serverInfo", serverInfo);
        LogWriter.info(logger, "profile", profile);
        // 持久化此服务器
        List list = persistenceManager.findAll(ServerInfo.class, "ID=? AND STATE!=?",
                SqlUtils.getParams(serverInfo.getId(), EnumUtils.toString(ServerState.STOPED)), null);
        if (!list.isEmpty()) {
            throw new ResourceException(String.format("服务器[%s]已经注册，请通知系统管理员协助检查此情况。",
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
     * 注销一个资源.
     * @param resource
     * @return 注册是否成功，如果serverInfo 的ID已经存在，则注册将失败。
     */
    public boolean unregisterServer(ServerInfo serverInfo) {
        LogWriter.info(logger, "开始注销服务器 serverInfo", serverInfo);

        // 持久化此服务器
        List<ServerInfo> serverInfos = persistenceManager.findAll(ServerInfo.class, "ID=?",
                SqlUtils.getParams(serverInfo.getId()), null);
        if (serverInfos.isEmpty()) {
            LogWriter.warn(logger, String.format("待注销的服务器[%s]未记录。", serverInfo));
            // throw new ResourceException(message);
            return false;
        }
        for (Iterator<ServerInfo> it0 = serverInfos.iterator(); it0.hasNext();) {
            ServerInfo serverInfo1 = it0.next();
            // 将属于被注销的服务器的“正在运行的实例”设置为“停止”
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
            LogWriter.warn(logger, String.format("注销的服务器记录[%s]。", _servInfo));
            persistenceManager.delete(_servInfo);
        }
        return true;
    }

    /**
     * 申请一个资源（买一头牛）
     * @param capacity 指定牛的能力
     * @return
     */
    public Cattle applyFor(App app, AppVersion appVersion, String appInstanceOid, Capacity capacity) {
        LogWriter.info(logger, String.format("正在为应用[%s](版本为[%s])申请容量为[%s]的资源。",
                app, appVersion, capacity));
        if (app == null) {
            throw new SystemException("发布的应用不能为空。");
        }

        if (appVersion == null) {
            throw new SystemException("应用版本不能为空。");
        }

        // 如果未指定配置，则使用标准配置。
        if (capacity == null) {
            capacity = Constants.STANDARD_CAPACITY;
        }

        Cattle cattle = null;

        // 尝试分配次数：10，即分配过来指定的次数之后，系统将返回失败消息。
        for (int i = 0; i < 10; i++) {
            // 找到已经注册的服务器，并在其中选择一个合适的服务器。
            ServerInfo serverInfo = findServer(app, capacity);
            if (serverInfo == null) {
                throw new ResourceException("无可用的资源");
            }

            LogWriter.info(logger, String.format("为应用[%s]分配了服务器[%s]（计算能力[%s]）。",
                    app, serverInfo, capacity));

            // 向资源工厂申请资源
            String proxyUrl = String.format("http://%s:%d%s", serverInfo.getIp(),
                    serverInfo.getManagerPort(), serverInfo.getContextPath());
            Proxy proxy = new HttpStreamProxy(proxyUrl);

            ServerService serverManager = ServiceFactory.getInstance().getService(ServerService.class, proxy);
            cattle = serverManager.deploy(app, appVersion, appInstanceOid, capacity);

            // 找到即返回
            if (cattle != null) {
                cattle.setServerOid(serverInfo.getOid());
                break;
            }
        }
        if (cattle == null) {
            // 部署失败，需要重新分配服务器
            LogWriter.warn(logger, String.format("为应用[%s]分配了服务器（计算能力[%s]）失败。",
                    app, capacity));
        }

        return cattle;
    }

    /**
     * TODO: 多个应该访问同一个服务器该如果处理
     * @param app
     * @param capacity
     * @return
     */
    private ServerInfo findServer(App app, Capacity capacity) {
        List<ServerInfo> servers = getAvailableServers();
        if (servers.isEmpty()) {
            return null;
        }

        // 计算分配表，计算剩余容量
        if (servers.size() == 1) {
            return servers.get(0);
        }

        // TODO: 尽量保证同一个应用使用不同的服务器
        Collections.sort(servers, new ServerComparator(app, capacity, getInstancesCount(app)));

        // 选择优先级最高的服务器
        ServerInfo selected = servers.get(0);
        LogWriter.info2(logger, "分配服务器[%s]。", selected);
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
     * 返回可用的服务器信息。
     * @return 服务器信息集合
     */
    private List<ServerInfo> getAvailableServers() {
        List<ServerInfo> servers = persistenceManager.findAll(ServerInfo.class,
                "(STATE=? OR STATE=?)", 
                SqlUtils.getParams(EnumUtils.toString(ServerState.AVAILABLE),
                EnumUtils.toString(ServerState.USING)), null);
        return servers;
    }

    /**
     * 显示当前所有注册的服务器.
     * @return
     */
    public List<ServerInfo> listServers() {
        List<ServerInfo> servers = persistenceManager.findAll(ServerInfo.class, "1=1", null, null);
        return servers;
    }
    
    /**
     * 显示当前所有注册的服务器.
     * @return
     */
    public List<ServerInfo> listAvailableServers() {
        List<ServerInfo> servers = persistenceManager.findAll(ServerInfo.class, "state != ?",
                SqlUtils.getParams(ServerState.STOPED), null);
        return servers;
    }

    /**
     * 删除一个服务器信息。
     * 当服务器意外终止时，管理员可以通过此接口删除一个已经注册的服务器。
     */
    public boolean removeServer(ServerInfo serverInfo) {
        if (serverInfo == null) {
            return false;
        }
        if (null == persistenceManager.findByPK(serverInfo)) {
            throw new ServiceException(String.format("服务器[%s]未注册。", serverInfo));
        }

        // 资源正在使用时，不能删除

        // TODO: 和资源相关的实例都需要删除?
        persistenceManager.execute("DELETE FROM APP_INSTANCE WHERE SERVER_OID=?",
                SqlUtils.getParams(serverInfo.getOid()));

        return persistenceManager.delete(serverInfo);
    }

    /**
     * 不再使用这头牛
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
 * 根据应用的信息和容量对服务器进行排序.
 * @author albert
 */
class ServerComparator implements Comparator<ServerInfo> {

    private final App app;
    private final Capacity capacity;
    /**
     * 服务器运行的实例个数本的应用（尽量避免在同一台服务器上发布应用）
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
        int count1 = instancesCount.containsKey(oid1) ? instancesCount.get(oid1) : 0;  // 服务器的实例个数
        //System.out.println("compare......count1=" +  count1);
        int count2 = instancesCount.containsKey(oid2) ? instancesCount.get(oid2) : 0;
        //System.out.println("compare......count2=" +  count2);
        return (count1 - count2);
    }
}
