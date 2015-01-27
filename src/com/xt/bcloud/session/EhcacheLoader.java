package com.xt.bcloud.session;

import com.xt.bcloud.app.App;
import com.xt.bcloud.app.AppVersion;
import com.xt.bcloud.comm.CloudUtils;
import com.xt.bcloud.comm.PortFactory;
import com.xt.bcloud.resource.ConfService;
import com.xt.bcloud.resource.GroupConf;
import com.xt.core.app.init.SystemLifecycle;
import com.xt.core.log.LogWriter;
import com.xt.gt.sys.SystemConfiguration;
import com.xt.proxy.Proxy;
import com.xt.proxy.ServiceFactory;
import java.io.InputStream;
import net.sf.ehcache.CacheManager;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * EhCache 装载器。
 * @author albert
 */
public class EhcacheLoader implements SystemLifecycle {

    private final Logger logger = Logger.getLogger(EhcacheLoader.class);
    private CacheManager manager;

    public EhcacheLoader() {
    }

    public void onInit() {
        Proxy proxy = CloudUtils.createArmProxy();
        ConfService confService = ServiceFactory.getInstance().getService(ConfService.class, proxy);
        String appOid = SystemConfiguration.getInstance().readString(EhcacheSessionProvider.APP_OID);
        String versionOid = SystemConfiguration.getInstance().readString(EhcacheSessionProvider.APP_VERSION_OID);
        String instanceOid = SystemConfiguration.getInstance().readString(EhcacheSessionProvider.APP_INSTANCE_OID);
        String cacheId = SystemConfiguration.getInstance().readString(EhcacheSessionProvider.EHCACHE_ID);
        if (StringUtils.isEmpty(appOid) || StringUtils.isEmpty(versionOid)) {
            throw new ClusterException("应用编码和版本编码都不能为空。");
        }
        App app = new App();
        app.setOid(appOid);
        AppVersion version = new AppVersion();
        version.setOid(versionOid);
        String host = CloudUtils.getLocalHostAddress();
        int port = PortFactory.getInstance().getPort();
        GroupConf groupConf = new GroupConf();
        groupConf.setBindAddr(host);
        groupConf.setBindPort(String.valueOf(port));
        groupConf.setGroupId(cacheId);
        groupConf.setEntityId(instanceOid);
        LogWriter.info2(logger, "为应用[%s]版本[%s]加载 Ehcache 的参数(组参数[%s])。", app, version, groupConf);
        InputStream conf = confService.readEhcache(app, version, groupConf);
//        try {
//            FileOutputStream fos = new FileOutputStream("e:\\EhcacheLoader-ehcacheConf-" + System.currentTimeMillis() + ".conf.xml");
//            IOHelper.i2o(conf, fos, true, true);
//        } catch (FileNotFoundException ex) {
//            ex.printStackTrace();
//        }
        manager = CacheManager.create(conf);
        // manager = CacheManager.create("E:\\work\\xthinker\\B-Cloud\\src\\files\\ehcache.xml");
    }

    public void onDestroy() {
        LogWriter.info2(logger, "正在关闭缓存管理器[%s]...", manager);
        if (manager != null) {
            try {
                manager.shutdown();
            } catch (Throwable t) {
                LogWriter.warn2(logger, t, "关闭 EhCache 缓存时出现异常。");
            }
        }
    }
}
