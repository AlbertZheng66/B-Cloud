package com.xt.bcloud.session;

import com.xt.bcloud.comm.CloudUtils;
import com.xt.core.exception.BadParameterException;
import com.xt.core.session.Session;
import com.xt.gt.jt.http.ServletContext;
import com.xt.gt.sys.SystemConfiguration;
import com.xt.proxy.Context;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.commons.lang.StringUtils;

/**
 * 用于处理在分布式的情况下，系统使用的 Session。
 * 同一个应用使用相同的 Cache，应用的各个 Session 则以“不同的Cache名称”作为区分。
 *
 * TODO: Cache 是否存在锁的问题?!
 * @author albert
 */
public class EhcacheSessionOld implements Session {

    /**
     * 用于存放应用相关信息的缓存（和应用数据相关的缓存实现命名空间隔离）。
     */
    private static final String APP_CACHE_NAME = SystemConfiguration.getInstance().readString("clusterSession.appCacheName", "appsCache");
//    /**
//     * 运行模式是否采用“集群”方式
//     */
//    private boolean isClustered = SystemConfiguration.getInstance().readBoolean("clusterSession.isClustered", false);
    /**
     * 用于在Cache使用的保留的创建时间。
     */
    private final static String CREATION_TIME_SUBFIX_IN_CACHE = "__CREATION_TIME";

    /**
     * 用于在Cache使用的保留的创建时间。
     */
    private final static String LAST_ACCESSED_TIME_SUBFIX_IN_CACHE = "__LAST_ACCESSED_TIME";

    /**
     * 每个应用的所有实例都要保证使用唯一的 Session Id。
     */
    private final static String APP_SESSION_ID_SUBFIX_IN_CACHE = "__APP_SESSION_ID";

    /**
     * 在本地缓存中缓存的 AppSessionID 的名称。
     */
    private final static String APP_SESSION_ID_IN_LOCAL_SESSION = "APP_SESSION_ID_IN_LOCAL_SESSION";

    /**
     * 在本地缓存中缓存的 AppSessionID 的名称。
     */
    private final static String CREATION_TIME_IN_LOCAL_SESSION = "CREATION_TIME_IN_LOCAL_SESSION";

    /**
     * 被包装的 Session。
     */
    private final Session wrappedSession;

    /**
     * 用于表示存在于 Cache 中的Session，注意：一个应用使用一个相同的Cache。
     */
    private final String appCacheId;

    /**
     * 同一应用都是使用同一个 Session ID(昏话！！！)
     */
    private final String appSessionId;

    /**
     * 数据缓存的前缀
     */
    private final String dataPrefix;

    /**
     * 当前应用的编码
     */
    private final String appOid = SystemConfiguration.getInstance().readString(EhcacheSessionProvider.EHCACHE_ID);

    /**
     * Cluster
     */
    private final String clusterSessionId;

//    /**
//     * 当前应用版本的编码
//     */
//    private final String appVersionOid = SystemConfiguration.getInstance().readString(EhcacheSessionProvider.CURRENT_APP_VERSION_OID);

    public EhcacheSessionOld(Session wrappedSession, Context context) {
        this.wrappedSession = wrappedSession;
        this.clusterSessionId = getClusterSessionId(context);

        appSessionId = getAppSessionId();
        dataPrefix = appSessionId + "-";

        // appCacheId = "cache-" + app.getOid();
        // for testing.....
        appCacheId = "appsCache";
    }

    private String getClusterSessionId(Context context) {
        String _clusterSessionId = null;
        if (context instanceof ServletContext) {
            ServletContext _context = (ServletContext) context;
            HttpServletRequest request = _context.getRequest();
            for (Cookie cookie : request.getCookies()) {
                if (ClusterSessionProcessor.CLUSTER_SESSION_ID_NAME.equals(cookie.getName())) {
                    _clusterSessionId = cookie.getValue();
                    break;
                }
            }
        }
        if (StringUtils.isEmpty(_clusterSessionId)) {
            _clusterSessionId = CloudUtils.generateOid();
        }
        return _clusterSessionId;
    }

    private void assertAttrName(String name) throws BadParameterException {
        if (StringUtils.isEmpty(name)) {
            throw new BadParameterException("参数名称不能为空。");
        }
    }

    private String getAppSessionId() {
        String localCachedSessionId = (String) wrappedSession.getAttribute(APP_SESSION_ID_IN_LOCAL_SESSION);
        if (StringUtils.isNotEmpty(localCachedSessionId)) {
            return localCachedSessionId;
        }
        long creationTime = System.currentTimeMillis();
        String asiNameInCache = appOid + APP_SESSION_ID_SUBFIX_IN_CACHE;
        Cache cache = getAppCache();
        if (cache.isKeyInCache(asiNameInCache)) {
            localCachedSessionId = (String) cache.get(asiNameInCache).getValue();
            String ctName = appOid + LAST_ACCESSED_TIME_SUBFIX_IN_CACHE;
            if (cache.isKeyInCache(ctName)) {
                // 将创建时间缓存在本地
                creationTime = (Long) cache.get(ctName).getValue();
            } else {
                putAppCache(cache, CREATION_TIME_SUBFIX_IN_CACHE, creationTime);
            }
        } else {
            // 哪里都没有的时候，第一次创建的对象
            localCachedSessionId = CloudUtils.generateOid();
            putAppCache(cache, APP_SESSION_ID_SUBFIX_IN_CACHE, localCachedSessionId);

            // 存放“创建时间”
            putAppCache(cache, CREATION_TIME_SUBFIX_IN_CACHE, creationTime);

            // 缓存“最后存取时间”
            putAppCache(cache, LAST_ACCESSED_TIME_SUBFIX_IN_CACHE, creationTime);

        }
        wrappedSession.setAttribute(APP_SESSION_ID_IN_LOCAL_SESSION, localCachedSessionId);
        wrappedSession.setAttribute(CREATION_TIME_IN_LOCAL_SESSION, creationTime);
        return localCachedSessionId;
    }

    private void putAppCache(Cache cache, String subfix, Serializable value) {
        // 缓存“创建时间”
        String ctNameInCache = appOid + subfix;
        Element ctElem = new Element(ctNameInCache, value);
        cache.put(ctElem);
    }

    /**
     * 返回相应的 Session。
     * TODO: 是否要在本地Session中缓存Cache？
     * @param creation 如果缓存不存在，是否要创建
     * @return
     */
    private Cache getDataCache(boolean creation) {
        CacheManager manager = CacheManager.getInstance();
        Cache cache = null;
        if (manager.cacheExists(appCacheId)) {
            cache = manager.getCache(appCacheId);
        } else if (creation) {
            cache = new Cache(appCacheId, 20, true, false, 100, 100);
            manager.addCache(cache);
        }
        return cache;
    }

    private Cache getAppCache() {
        CacheManager manager = CacheManager.getInstance();
        Cache cache = null;
        if (manager.cacheExists(APP_CACHE_NAME)) {
            cache = manager.getCache(APP_CACHE_NAME);
        } else {
            cache = new Cache(APP_CACHE_NAME, 20, false, false, 100, 100);
            manager.addCache(cache);
        }
        return cache;
    }

    private String getCacheName(String name) {
        return dataPrefix + name;
    }

    public Object getAttribute(String name) {
        assertAttrName(name);
        Cache cache = getDataCache(false);
        if (cache == null) {
            return null;
        }
        String _name = getCacheName(name);
        if (cache.isKeyInCache(_name)) {
            Element elem = cache.get(_name);
            return elem.getValue();
        }
        return null;
    }

    /**
     * 注意：此方法对性能损耗非常大，在生产环境中不要使用。
     * @return
     */
    public Enumeration<String> getAttributeNames() {
        Cache cache = getDataCache(false);
        if (cache == null) {
            return Collections.enumeration(Collections.EMPTY_LIST);
        }
        List<String> attrNames = new ArrayList();
        List keys = cache.getKeys();
        for (Iterator iter = keys.iterator(); iter.hasNext();) {
            Object key = iter.next();
            if (key != null && key instanceof String) {
                // 去掉前缀
                String strKey = (String) key;
                if (strKey.startsWith(dataPrefix)) {
                    attrNames.add(strKey.substring(dataPrefix.length()));
                }
            }
        }
        return Collections.enumeration(attrNames);
    }

    /**
     * 此创建时间是整体系统的创建时间，而不是本Session的创建时间。
     * @return
     */
    public long getCreationTime() {
        Object value = wrappedSession.getAttribute(CREATION_TIME_IN_LOCAL_SESSION);
        if (value != null && value instanceof Long) {
            return (Long) value;
        }
//        String ctName = app.getOid() + CREATION_TIME_SUBFIX_IN_CACHE;
//        Cache cache = getAppCache();
//        if (cache.isKeyInCache(ctName)) {
//            Element elem = cache.get(ctName);
//            return (Long) elem.getValue();
//        }
        return -1;
    }

    public String getId() {
        return clusterSessionId;
    }

    /**
     * 此创建时间是整体系统的最后存取时间，而不是本地Session的最后存取时间。
     * @return
     */
    public long getLastAccessedTime() {
        //使用统一的 Session 创建时间
        String latName = appOid + LAST_ACCESSED_TIME_SUBFIX_IN_CACHE;
        Cache cache = getAppCache();
        if (cache.isKeyInCache(latName)) {
            Element elem = cache.get(latName);
            return (Long) elem.getValue();
        }
        return -1;
    }

    public int getMaxInactiveInterval() {
        return wrappedSession.getMaxInactiveInterval();
    }

    public void invalidate() {
        // 清除所有和此Session相关的数据
        Cache cache = getDataCache(false);
        if (cache != null) {
            List keys = cache.getKeys();
            for (Iterator iter = keys.iterator(); iter.hasNext();) {
                Object key = iter.next();
                if (key != null && key instanceof String) {
                    String strKey = (String) key;
                    if (strKey.startsWith(dataPrefix)) {
                        cache.remove(key);
                    }
                }
            }
            return;
        }
        wrappedSession.invalidate();
    }

    public void removeAttribute(String name) {
        assertAttrName(name);
        Cache cache = getDataCache(false);
        if (cache != null) {
            String cacheName = getCacheName(name);
            cache.remove(cacheName);
        }
    }

    /**
     *
     * @param name 参数名称，不能为空，否则抛出异常。
     * @param value 值，如果为空，效果和removeAttribute相同。
     * @throws BadParameterException 1. 参数名称为空； 2. 值不能序列化
     */
    public void setAttribute(String name, Serializable value) {
        assertAttrName(name);
        String cacheName = getCacheName(name);
        if (value == null) {
            removeAttribute(cacheName);
            return;
        }
        Cache cache = getDataCache(true);
        // TODO: 需要先行移除吗?
        if (cache.isKeyInCache(cacheName)) {
            cache.remove(cacheName);
        }
        Element elem = new Element(cacheName, value);
        cache.put(elem);
    }

    public void setLastAccessedTime(long accessedTime) {
        Cache cache = getAppCache();
        String latName = appOid + LAST_ACCESSED_TIME_SUBFIX_IN_CACHE;
        Element latElem = new Element(latName, accessedTime);
        cache.put(latElem);
    }

    @Override
    public String toString() {
        StringBuilder strBld = new StringBuilder();
        strBld.append(super.toString()).append("[");
        strBld.append("appSessionId=").append(appSessionId).append("; ");
        strBld.append("appCacheId=").append(appCacheId).append("; ");
        strBld.append("wrappedSession=").append(this.wrappedSession).append("; ");
        strBld.append("appCacheId=").append(appCacheId).append("; ");
        strBld.append("dataPrefix=").append(dataPrefix);
        strBld.append("]");
        return strBld.toString();
    }

    public boolean isClustered() {
        return true;
    }

    public boolean isValid() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
