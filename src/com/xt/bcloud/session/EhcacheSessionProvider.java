package com.xt.bcloud.session;

import com.xt.bcloud.app.App;
import com.xt.bcloud.app.AppVersion;
import com.xt.bcloud.resource.ProviderHelper;
import com.xt.bcloud.resource.ResourceException;
import com.xt.bcloud.resource.ServiceProvider;
import com.xt.bcloud.worker.Cattle;
import com.xt.core.db.pm.IPOPersistenceManager;
import com.xt.core.exception.SystemException;
import com.xt.gt.jt.http.GreenTeaGeneralServlet;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.Element;
import static com.xt.gt.sys.SystemConfiguration.*;

/**
 * 给应用提供分布式 Session。
 * @author albert
 */
public class EhcacheSessionProvider implements ServiceProvider {

    /**
     * 自动在配置文件中设置 Cache 的ID。
     */
    public static final String EHCACHE_ID = "ehcachSession.cacheId";


    /**
     * 自动在配置文件中设置当前的应用版本编码。
     */
    public static final String APP_VERSION_OID = "ehcachSession.appVersionOid";


    /**
     * 自动在配置文件中设置当前的应用服务器实例的编码。
     */
    public static final String APP_INSTANCE_OID = "ehcachSession.appInstanceOid";

    /**
     * 自动在配置文件中设置当前的应用版本编码。
     */
    public static final String APP_OID = "ehcachSession.appOid";

    private final Logger logger = Logger.getLogger(EhcacheSessionProvider.class);

    public EhcacheSessionProvider() {
    }

    /**
     * 构建分配给应用的数据库资源。
     * @param root
     * @param app
     * @param version
     * @param persistenceManager
     */
    public void createConf(Element root, Cattle cattle, 
            IPOPersistenceManager persistenceManager) {
        if (cattle == null || cattle.getApp() == null || StringUtils.isEmpty(cattle.getApp().getOid())) {
            throw new ResourceException("应用及其编码信息不能为空。");
        }

        // 加入 分布式 Session 处理工厂
        /*
        <system>
        <param name="processorFactories" type="list">
        <data value="com.xt.bcloud.session.SessionProcessorFactory" />
        </param>
        </system>
         */
        Element system = root.getChild(CONF_FILE_TAG_SYSTEM);
        if (system == null) {
            system = new Element(CONF_FILE_TAG_SYSTEM);
            root.addContent(system);
        }



        Element processorFactories = ProviderHelper.getChild(system, ProviderHelper.TAG_PROCESSOR_FACTORIES);
        if (processorFactories == null) {
            processorFactories = ProviderHelper.createListNode(system, ProviderHelper.TAG_PROCESSOR_FACTORIES);
        }
        Element data = new Element(CONF_FILE_TAG_LIST_DATA);
        data.setAttribute(CONF_FILE_TAG_LIST_VALUE, ClusterSessionProcessorFactory.class.getName());
        processorFactories.addContent(data);

        // 加入当前“应用 OID”及其“版本 OID”
        /**
         * <param name="CURRENT_APP_OID"  value="99990000" />
         */
        final App app            = cattle.getApp();
        final AppVersion version = cattle.getAppVersion();
        String cacheId = getCacheId(persistenceManager, app, version);
        ProviderHelper.createSimpleNode(system, EHCACHE_ID,      cacheId);
        ProviderHelper.createSimpleNode(system, APP_OID,         app.getOid());
        ProviderHelper.createSimpleNode(system, APP_VERSION_OID, version.getOid());
        ProviderHelper.createSimpleNode(system, APP_INSTANCE_OID, cattle.getAppInstanceOid());

         // 加入当前“分布式Session的标记”
        /**
         * <param name="system.session.clustered"  value="true" />
         */
        ProviderHelper.createSimpleNode(system, GreenTeaGeneralServlet.SYSTEM_CLUSTERED_FLAG, "true");

         // 加入当前“分布式Session的实现类”
        /**
         * <param name="system.session.cluster.class"  value="EhcacheSession" />
         */
        ProviderHelper.createSimpleNode(system, GreenTeaGeneralServlet.SYSTEM_CLUSTER_SESSION_CLASS, EhcacheSession.class.getName());

        // 加入启动 Loader
        /*
        <param name="appLifecycles" type='list'>
        <data value="com.xt.bcloud.session.EhcacheLoader" />
        </param>
         */
        Element appLifecycles = ProviderHelper.getChild(system, "appLifecycles");
        if (appLifecycles == null) {
            appLifecycles = ProviderHelper.createListNode(system, "appLifecycles");
        }
        Element alData = new Element(CONF_FILE_TAG_LIST_DATA);
        alData.setAttribute(CONF_FILE_TAG_LIST_VALUE, EhcacheLoader.class.getName());
        appLifecycles.addContent(alData);
    }

    private String getCacheId(IPOPersistenceManager persistenceManager,
            App app, AppVersion version) {
        AppCacheService appCacheService = new AppCacheService();
        appCacheService.setPersistenceManager(persistenceManager);
        // return appUpgradeService.generateCacheId(app, version, null, false);
        String cacheId = appCacheService.getCacheId(version.getOid());
        if (StringUtils.isEmpty(cacheId)) {
            throw new ClusterException(String.format("应用[%s]版本[%s]的缓存尚未分配。", app.getOid(), version.getOid()));
        }
        return cacheId;
    }

   
}

