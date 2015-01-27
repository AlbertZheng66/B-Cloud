package com.xt.bcloud.session;

import com.xt.bcloud.comm.CloudUtils;
import com.xt.comm.Cleanable;
import com.xt.comm.CleanerManager;
import com.xt.core.exception.BadParameterException;
import com.xt.core.log.LogWriter;
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
import org.apache.log4j.Logger;

/**
 * 用于处理在分布式的情况下，系统使用的 Session。
 * 同一个应用使用相同的 Cache，应用的各个 Session 则以“不同的Cache名称”作为区分。
 * 
 * @TODO: Cache 是否存在锁的问题?!
 * @FIXME: 经常发现session 同步失效的情况，需要巡查程序同步进行处理。
 * 在每个Session中设置同步点。
 * @author albert
 */
public class EhcacheSession implements Session {

    private final Logger logger = Logger.getLogger(EhcacheSession.class);
    /**
     * 在缓存中保存的最大原始个数，可通过参数“session.ehcache.maxElementsInMemory”进行配置，默认为 10000。
     */
    private static final int maxElementsInMemory = SystemConfiguration.getInstance().readInt("session.ehcache.maxElementsInMemory", 10000);
    /**
     * 是否将缓存输出到硬盘，可通过参数“session.ehcache.overflowToDisk”进行配置，默认为 true。
     */
    private static final boolean overflowToDisk = SystemConfiguration.getInstance().readBoolean("session.ehcache.overflowToDisk", true);
    /**
     * Cache 的生存时间（秒），可通过参数“session.ehcache.timeToLiveSeconds”进行配置，默认为 1800 秒。
     */
    private static final int timeToLiveSeconds = SystemConfiguration.getInstance().readInt("session.ehcache.timeToLiveSeconds", 1800);
    private final int timeToIdleSeconds = SystemConfiguration.getInstance().readInt("session.ehcache.timeToIdleSeconds", 100);
    private final boolean external = SystemConfiguration.getInstance().readBoolean("session.ehcache.external", false);
    /**
     * 在本地缓存中缓存的本 Session 的创建时间。
     */
    private final static String CREATION_TIME_IN_LOCAL_SESSION = "CREATION_TIME_IN_LOCAL_SESSION";
    /**
     * 用于在 Cache 使用的保留的创建时间，key 为：[clusterSessionId] + "__CREATION_TIME"。
     */
    private final static String CREATION_TIME_SUBFIX_IN_CACHE = "__CREATION_TIME";
    /**
     * 用于在Cache使用的保留的最后访问时间，key 为：[clusterSessionId] + "__LAST_ACCESSED_TIME"。
     */
    private final static String LAST_ACCESSED_TIME_SUBFIX_IN_CACHE = "__LAST_ACCESSED_TIME";
    /**
     * 被包装的 Session。
     */
    private final Session wrappedSession;
    /**
     * Cluster 的Session ID，这个ID用于Session属性名称的前缀，即存放在Cache中的Session。
     */
    private final String clusterSessionId;
    
    /**
     * 应用数据存储的 Cache 的名称。注意：一个应用的某些特定版本应该使用一个相同的Cache。
     * 同一应用的不同版本应使用不同的 Cache.
     */
    private final String cacheId = SystemConfiguration.getInstance().readString(EhcacheSessionProvider.EHCACHE_ID);
    /**
     * 用于缓存的本地 Cache 实例。
     */
    private Cache cache;

    public EhcacheSession(Session wrappedSession, Context context) {
        this.wrappedSession = wrappedSession;
        this.clusterSessionId = getClusterSessionId(context);
        // 注册一个关闭监听器
        CleanerManager.getInstance().register(new Cleanable() {

            public void clean() {
                if (cache != null) {
                    LogWriter.info2(logger, "正在关闭缓存[%s]......", cache);
                    cache.getCacheManager().shutdown();
                    cache = null;
                }
            }
        });
    }

    /**
     * 尝试从 Cookie 中读取集群的 Session ID。
     * @param context 上下文实例
     * @return
     */
    private String getClusterSessionId(Context context) {
        String _clusterSessionId = null;
        if (context instanceof ServletContext) {
            ServletContext _context = (ServletContext) context;
            HttpServletRequest request = _context.getRequest();
            if (request != null && request.getCookies() != null
                    && request.getCookies().length > 0) {
                for (Cookie cookie : request.getCookies()) {
                    if (ClusterSessionProcessor.CLUSTER_SESSION_ID_NAME.equals(cookie.getName())) {
                        _clusterSessionId = cookie.getValue();
                        break;
                    }
                }
            }
        }
        LogWriter.debug2(logger, "应用缓存[%s]的当前 Cluster Session ID[%s]", cacheId, clusterSessionId);

        // 判断Session 是否仍然有效, Session 失效的话需要重新创建
        if (StringUtils.isNotEmpty(_clusterSessionId)) {
            String cacheName = _clusterSessionId + CREATION_TIME_SUBFIX_IN_CACHE;
            if (!exists(cacheName) /*|| Session过期*/) {
                _clusterSessionId = null;
            }
            LogWriter.debug2(logger, "应用缓存[%s]的当前 Cluster Session ID[%s]", cacheId, clusterSessionId);
        }

        if (StringUtils.isEmpty(_clusterSessionId)) {
            // 创建新的 Cluster Session
            _clusterSessionId = CloudUtils.generateOid();
            Long creationTime = System.currentTimeMillis();
            wrappedSession.setAttribute(CREATION_TIME_IN_LOCAL_SESSION, creationTime);
            String cacheName = _clusterSessionId + CREATION_TIME_SUBFIX_IN_CACHE;
            Element elem = new Element(cacheName, creationTime);
            Cache _cache = getCache(true);
            _cache.put(elem);
        }
        return _clusterSessionId;
    }

    /**
     * 判断指定的 Cache 值是否存在。
     * @param cacheName Cache 值的Key
     * @return
     */
    private boolean exists(String cacheName) {
        Cache _cache = getCache(false);
        return (_cache != null && _cache.isKeyInCache(cacheName));
    }

    private void assertAttrName(String name) throws BadParameterException {
        if (StringUtils.isEmpty(name)) {
            throw new BadParameterException("参数名称不能为空。");
        }
    }

    /**
     * 返回相应的 Session。
     * TODO: 是否要在本地Session中缓存Cache？
     * @param creation 如果缓存不存在，是否要创建
     * @return
     */
    private Cache getCache(boolean creation) {
        if (cache != null) {
            return cache;
        }
        CacheManager manager = CacheManager.getInstance();
        if (manager.cacheExists(cacheId)) {
            cache = manager.getCache(cacheId);
        } else if (creation) {
            cache = new Cache(cacheId, maxElementsInMemory, overflowToDisk, external, timeToLiveSeconds, timeToIdleSeconds);
            manager.addCache(cache);
        }
        return cache;
    }

    public Object getAttribute(String name) {
        assertAttrName(name);
        Cache _cache = getCache(false);
        if (_cache == null) {
            return null;
        }
        String _name = getCacheName(name);
        if (_cache.isKeyInCache(_name)) {
            Element elem = _cache.get(_name);
            return elem.getValue();
        }
        return null;
    }

    private String getCacheName(String name) {
        return clusterSessionId + name;
    }

    /**
     * 注意：此方法对性能损耗非常大，在生产环境中不要使用。
     * @return
     */
    public Enumeration<String> getAttributeNames() {
        Cache _cache = getCache(false);
        if (_cache == null) {
            return Collections.enumeration(Collections.EMPTY_LIST);
        }
        List<String> attrNames = new ArrayList();
        List keys = _cache.getKeys();
        for (Iterator iter = keys.iterator(); iter.hasNext();) {
            Object key = iter.next();
            if (key != null && key instanceof String) {
                // 去掉前缀
                String strKey = (String) key;
                if (strKey.startsWith(clusterSessionId)) {
                    attrNames.add(strKey.substring(clusterSessionId.length()));
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
        String elementName = clusterSessionId + CREATION_TIME_SUBFIX_IN_CACHE;
        Cache _cache = getCache(true);
        if (_cache.isKeyInCache(elementName)) {
            Element elem = _cache.get(elementName);
            Long creationTime = (Long) elem.getValue();
            // 在本地进行缓存
            wrappedSession.setAttribute(CREATION_TIME_IN_LOCAL_SESSION, creationTime);
            return creationTime;
        }
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
        String latName = clusterSessionId + LAST_ACCESSED_TIME_SUBFIX_IN_CACHE;
        Cache _cache = getCache(false);
        if (_cache == null || _cache.isKeyInCache(latName)) {
            Element elem = _cache.get(latName);
            return (Long) elem.getValue();
        }
        return -1;
    }

    public int getMaxInactiveInterval() {
        return wrappedSession.getMaxInactiveInterval();
    }

    public void invalidate() {
        // 清除所有和此Session相关的数据
        Cache _cache = getCache(false);
        if (_cache != null) {
            List keys = _cache.getKeys();
            for (Iterator iter = keys.iterator(); iter.hasNext();) {
                Object key = iter.next();
                if (key != null && (key instanceof String)) {
                    String strKey = (String) key;
                    if (strKey.startsWith(clusterSessionId)) {
                        _cache.remove(key);
                    }
                }
            }
            return;
        }
        wrappedSession.invalidate();
    }

    public void removeAttribute(String name) {
        assertAttrName(name);
        Cache _cache = getCache(false);
        if (_cache != null) {
            String cacheName = getCacheName(name);
            _cache.remove(cacheName);
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
        Cache _cache = getCache(true);
        // TODO: 需要先行移除吗?
//        if (_cache.isKeyInCache(cacheName)) {
//            _cache.remove(cacheName);
//        }
        Element elem = new Element(cacheName, value);
        _cache.put(elem);
    }

    public void setLastAccessedTime(long accessedTime) {
        Cache _cache = getCache(true);
        String latName = clusterSessionId + LAST_ACCESSED_TIME_SUBFIX_IN_CACHE;
        Element latElem = new Element(latName, accessedTime);
        _cache.put(latElem);
    }

    @Override
    public String toString() {
        StringBuilder strBld = new StringBuilder();
        strBld.append(super.toString()).append("[");
        strBld.append("clusterSessionId=").append(clusterSessionId).append("; ");
        strBld.append("appCacheId=").append(cacheId).append("; ");
        strBld.append("wrappedSession=").append(this.wrappedSession).append("; ");
        strBld.append("appCacheId=").append(cacheId);
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
