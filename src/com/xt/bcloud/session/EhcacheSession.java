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
 * ���ڴ����ڷֲ�ʽ������£�ϵͳʹ�õ� Session��
 * ͬһ��Ӧ��ʹ����ͬ�� Cache��Ӧ�õĸ��� Session ���ԡ���ͬ��Cache���ơ���Ϊ���֡�
 * 
 * @TODO: Cache �Ƿ������������?!
 * @FIXME: ��������session ͬ��ʧЧ���������ҪѲ�����ͬ�����д���
 * ��ÿ��Session������ͬ���㡣
 * @author albert
 */
public class EhcacheSession implements Session {

    private final Logger logger = Logger.getLogger(EhcacheSession.class);
    /**
     * �ڻ����б�������ԭʼ��������ͨ��������session.ehcache.maxElementsInMemory���������ã�Ĭ��Ϊ 10000��
     */
    private static final int maxElementsInMemory = SystemConfiguration.getInstance().readInt("session.ehcache.maxElementsInMemory", 10000);
    /**
     * �Ƿ񽫻��������Ӳ�̣���ͨ��������session.ehcache.overflowToDisk���������ã�Ĭ��Ϊ true��
     */
    private static final boolean overflowToDisk = SystemConfiguration.getInstance().readBoolean("session.ehcache.overflowToDisk", true);
    /**
     * Cache ������ʱ�䣨�룩����ͨ��������session.ehcache.timeToLiveSeconds���������ã�Ĭ��Ϊ 1800 �롣
     */
    private static final int timeToLiveSeconds = SystemConfiguration.getInstance().readInt("session.ehcache.timeToLiveSeconds", 1800);
    private final int timeToIdleSeconds = SystemConfiguration.getInstance().readInt("session.ehcache.timeToIdleSeconds", 100);
    private final boolean external = SystemConfiguration.getInstance().readBoolean("session.ehcache.external", false);
    /**
     * �ڱ��ػ����л���ı� Session �Ĵ���ʱ�䡣
     */
    private final static String CREATION_TIME_IN_LOCAL_SESSION = "CREATION_TIME_IN_LOCAL_SESSION";
    /**
     * ������ Cache ʹ�õı����Ĵ���ʱ�䣬key Ϊ��[clusterSessionId] + "__CREATION_TIME"��
     */
    private final static String CREATION_TIME_SUBFIX_IN_CACHE = "__CREATION_TIME";
    /**
     * ������Cacheʹ�õı�����������ʱ�䣬key Ϊ��[clusterSessionId] + "__LAST_ACCESSED_TIME"��
     */
    private final static String LAST_ACCESSED_TIME_SUBFIX_IN_CACHE = "__LAST_ACCESSED_TIME";
    /**
     * ����װ�� Session��
     */
    private final Session wrappedSession;
    /**
     * Cluster ��Session ID�����ID����Session�������Ƶ�ǰ׺���������Cache�е�Session��
     */
    private final String clusterSessionId;
    
    /**
     * Ӧ�����ݴ洢�� Cache �����ơ�ע�⣺һ��Ӧ�õ�ĳЩ�ض��汾Ӧ��ʹ��һ����ͬ��Cache��
     * ͬһӦ�õĲ�ͬ�汾Ӧʹ�ò�ͬ�� Cache.
     */
    private final String cacheId = SystemConfiguration.getInstance().readString(EhcacheSessionProvider.EHCACHE_ID);
    /**
     * ���ڻ���ı��� Cache ʵ����
     */
    private Cache cache;

    public EhcacheSession(Session wrappedSession, Context context) {
        this.wrappedSession = wrappedSession;
        this.clusterSessionId = getClusterSessionId(context);
        // ע��һ���رռ�����
        CleanerManager.getInstance().register(new Cleanable() {

            public void clean() {
                if (cache != null) {
                    LogWriter.info2(logger, "���ڹرջ���[%s]......", cache);
                    cache.getCacheManager().shutdown();
                    cache = null;
                }
            }
        });
    }

    /**
     * ���Դ� Cookie �ж�ȡ��Ⱥ�� Session ID��
     * @param context ������ʵ��
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
        LogWriter.debug2(logger, "Ӧ�û���[%s]�ĵ�ǰ Cluster Session ID[%s]", cacheId, clusterSessionId);

        // �ж�Session �Ƿ���Ȼ��Ч, Session ʧЧ�Ļ���Ҫ���´���
        if (StringUtils.isNotEmpty(_clusterSessionId)) {
            String cacheName = _clusterSessionId + CREATION_TIME_SUBFIX_IN_CACHE;
            if (!exists(cacheName) /*|| Session����*/) {
                _clusterSessionId = null;
            }
            LogWriter.debug2(logger, "Ӧ�û���[%s]�ĵ�ǰ Cluster Session ID[%s]", cacheId, clusterSessionId);
        }

        if (StringUtils.isEmpty(_clusterSessionId)) {
            // �����µ� Cluster Session
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
     * �ж�ָ���� Cache ֵ�Ƿ���ڡ�
     * @param cacheName Cache ֵ��Key
     * @return
     */
    private boolean exists(String cacheName) {
        Cache _cache = getCache(false);
        return (_cache != null && _cache.isKeyInCache(cacheName));
    }

    private void assertAttrName(String name) throws BadParameterException {
        if (StringUtils.isEmpty(name)) {
            throw new BadParameterException("�������Ʋ���Ϊ�ա�");
        }
    }

    /**
     * ������Ӧ�� Session��
     * TODO: �Ƿ�Ҫ�ڱ���Session�л���Cache��
     * @param creation ������治���ڣ��Ƿ�Ҫ����
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
     * ע�⣺�˷�����������ķǳ��������������в�Ҫʹ�á�
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
                // ȥ��ǰ׺
                String strKey = (String) key;
                if (strKey.startsWith(clusterSessionId)) {
                    attrNames.add(strKey.substring(clusterSessionId.length()));
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
        String elementName = clusterSessionId + CREATION_TIME_SUBFIX_IN_CACHE;
        Cache _cache = getCache(true);
        if (_cache.isKeyInCache(elementName)) {
            Element elem = _cache.get(elementName);
            Long creationTime = (Long) elem.getValue();
            // �ڱ��ؽ��л���
            wrappedSession.setAttribute(CREATION_TIME_IN_LOCAL_SESSION, creationTime);
            return creationTime;
        }
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
        // ������кʹ�Session��ص�����
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
        Cache _cache = getCache(true);
        // TODO: ��Ҫ�����Ƴ���?
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
