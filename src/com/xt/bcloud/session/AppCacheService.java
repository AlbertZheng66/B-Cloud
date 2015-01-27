package com.xt.bcloud.session;

import com.xt.bcloud.app.App;
import com.xt.bcloud.app.AppCache;
import com.xt.bcloud.app.AppVersion;
import com.xt.bcloud.comm.CloudUtils;
import com.xt.core.log.LogWriter;
import com.xt.core.service.AbstractService;
import com.xt.core.utils.SqlUtils;
import java.util.Calendar;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author albert
 */
public class AppCacheService extends AbstractService {
    
    private static final long serialVersionUID = -1369365940859450258L;

    private final Logger logger = Logger.getLogger(AppCacheService.class);

    public AppCacheService() {
    }

    public AppCache getAppCache(String versionOid) {
        LogWriter.info2(logger, "获取版本[%s]的缓存", versionOid);
        AppCache appCache = persistenceManager.findFirst(AppCache.class,
                "APP_VERSION_OID=?", SqlUtils.getParams(versionOid), null);
        return appCache;
    }

    public String getCacheId(String versionOid) {
        AppCache appCache = getAppCache(versionOid);
        if (appCache == null || StringUtils.isEmpty(appCache.getCacheId())) {
            return null;
        } else {
            return appCache.getCacheId();
        }
    }

    /**
     * 清除指定版本的更新记录.
     * @param versionOid
     */
    public void clear(String versionOid) {
        if (versionOid != null) {
            persistenceManager.execute("DELETE FROM APP_CACHE WHERE APP_VERSION_OID=?",
                    SqlUtils.getParams(versionOid));
            persistenceManager.commit();
        }
    }

    public String saveCacheId(String appOid, String versionOid, String oldVersionOid, String cacheId) {
        AppCache newAppCache = new AppCache();
        newAppCache.setOid(CloudUtils.generateOid());
        newAppCache.setAppOid(appOid);
        newAppCache.setAppVersionOid(versionOid);
        newAppCache.setInsertTime(Calendar.getInstance());
        newAppCache.setOldVersionOid(oldVersionOid);
        if (StringUtils.isEmpty(cacheId)) {
            cacheId = createCacheId();
        }
        newAppCache.setCacheId(cacheId);
        persistenceManager.insert(newAppCache);
        persistenceManager.commit();
        return cacheId;
    }

    public String generateCacheId(App app, AppVersion version) {
        String cacheId = null;
        AppCache appCache = persistenceManager.findFirst(AppCache.class,
                "APP_VERSION_OID=?",
                SqlUtils.getParams(version.getOid()), null);
        if (appCache == null) {
            cacheId = createCacheId();
            LogWriter.warn2(logger, "未找到应用[%s]版本[%s]的升级信息，系统自动产生 Cache Id[%s]。",
                    app, version, cacheId);
            saveCacheId(app.getOid(), version.getOid(), null, cacheId);
            persistenceManager.commit();
        } else {
            cacheId = appCache.getCacheId();
        }
        return cacheId;
    }

    private String createCacheId() {
        return "cache_" + CloudUtils.generateOid();
    }
}
