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
 * ���ڴ����ڷֲ�ʽ������£�ϵͳʹ�õ� Session��
 * ͬһ��Ӧ��ʹ����ͬ�� Cache��Ӧ�õĸ��� Session ���ԡ���ͬ��Cache���ơ���Ϊ���֡�
 *
 * TODO: Cache �Ƿ������������?!
 * @author albert
 */
public class EhcacheSessionOld implements Session {

    /**
     * ���ڴ��Ӧ�������Ϣ�Ļ��棨��Ӧ��������صĻ���ʵ�������ռ���룩��
     */
    private static final String APP_CACHE_NAME = SystemConfiguration.getInstance().readString("clusterSession.appCacheName", "appsCache");
//    /**
//     * ����ģʽ�Ƿ���á���Ⱥ����ʽ
//     */
//    private boolean isClustered = SystemConfiguration.getInstance().readBoolean("clusterSession.isClustered", false);
    /**
     * ������Cacheʹ�õı����Ĵ���ʱ�䡣
     */
    private final static String CREATION_TIME_SUBFIX_IN_CACHE = "__CREATION_TIME";

    /**
     * ������Cacheʹ�õı����Ĵ���ʱ�䡣
     */
    private final static String LAST_ACCESSED_TIME_SUBFIX_IN_CACHE = "__LAST_ACCESSED_TIME";

    /**
     * ÿ��Ӧ�õ�����ʵ����Ҫ��֤ʹ��Ψһ�� Session Id��
     */
    private final static String APP_SESSION_ID_SUBFIX_IN_CACHE = "__APP_SESSION_ID";

    /**
     * �ڱ��ػ����л���� AppSessionID �����ơ�
     */
    private final static String APP_SESSION_ID_IN_LOCAL_SESSION = "APP_SESSION_ID_IN_LOCAL_SESSION";

    /**
     * �ڱ��ػ����л���� AppSessionID �����ơ�
     */
    private final static String CREATION_TIME_IN_LOCAL_SESSION = "CREATION_TIME_IN_LOCAL_SESSION";

    /**
     * ����װ�� Session��
     */
    private final Session wrappedSession;

    /**
     * ���ڱ�ʾ������ Cache �е�Session��ע�⣺һ��Ӧ��ʹ��һ����ͬ��Cache��
     */
    private final String appCacheId;

    /**
     * ͬһӦ�ö���ʹ��ͬһ�� Session ID(�軰������)
     */
    private final String appSessionId;

    /**
     * ���ݻ����ǰ׺
     */
    private final String dataPrefix;

    /**
     * ��ǰӦ�õı���
     */
    private final String appOid = SystemConfiguration.getInstance().readString(EhcacheSessionProvider.EHCACHE_ID);

    /**
     * Cluster
     */
    private final String clusterSessionId;

//    /**
//     * ��ǰӦ�ð汾�ı���
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
            throw new BadParameterException("�������Ʋ���Ϊ�ա�");
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
                // ������ʱ�仺���ڱ���
                creationTime = (Long) cache.get(ctName).getValue();
            } else {
                putAppCache(cache, CREATION_TIME_SUBFIX_IN_CACHE, creationTime);
            }
        } else {
            // ���ﶼû�е�ʱ�򣬵�һ�δ����Ķ���
            localCachedSessionId = CloudUtils.generateOid();
            putAppCache(cache, APP_SESSION_ID_SUBFIX_IN_CACHE, localCachedSessionId);

            // ��š�����ʱ�䡱
            putAppCache(cache, CREATION_TIME_SUBFIX_IN_CACHE, creationTime);

            // ���桰����ȡʱ�䡱
            putAppCache(cache, LAST_ACCESSED_TIME_SUBFIX_IN_CACHE, creationTime);

        }
        wrappedSession.setAttribute(APP_SESSION_ID_IN_LOCAL_SESSION, localCachedSessionId);
        wrappedSession.setAttribute(CREATION_TIME_IN_LOCAL_SESSION, creationTime);
        return localCachedSessionId;
    }

    private void putAppCache(Cache cache, String subfix, Serializable value) {
        // ���桰����ʱ�䡱
        String ctNameInCache = appOid + subfix;
        Element ctElem = new Element(ctNameInCache, value);
        cache.put(ctElem);
    }

    /**
     * ������Ӧ�� Session��
     * TODO: �Ƿ�Ҫ�ڱ���Session�л���Cache��
     * @param creation ������治���ڣ��Ƿ�Ҫ����
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
     * ע�⣺�˷�����������ķǳ��������������в�Ҫʹ�á�
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
                // ȥ��ǰ׺
                String strKey = (String) key;
                if (strKey.startsWith(dataPrefix)) {
                    attrNames.add(strKey.substring(dataPrefix.length()));
                }
            }
        }
        return Collections.enumeration(attrNames);
    }

    /**
     * �˴���ʱ��������ϵͳ�Ĵ���ʱ�䣬�����Ǳ�Session�Ĵ���ʱ�䡣
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
     * �˴���ʱ��������ϵͳ������ȡʱ�䣬�����Ǳ���Session������ȡʱ�䡣
     * @return
     */
    public long getLastAccessedTime() {
        //ʹ��ͳһ�� Session ����ʱ��
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
        // ������кʹ�Session��ص�����
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
     * @param name �������ƣ�����Ϊ�գ������׳��쳣��
     * @param value ֵ�����Ϊ�գ�Ч����removeAttribute��ͬ��
     * @throws BadParameterException 1. ��������Ϊ�գ� 2. ֵ�������л�
     */
    public void setAttribute(String name, Serializable value) {
        assertAttrName(name);
        String cacheName = getCacheName(name);
        if (value == null) {
            removeAttribute(cacheName);
            return;
        }
        Cache cache = getDataCache(true);
        // TODO: ��Ҫ�����Ƴ���?
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
