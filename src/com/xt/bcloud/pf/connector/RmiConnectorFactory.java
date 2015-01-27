package com.xt.bcloud.pf.connector;

import com.xt.core.log.LogWriter;
import com.xt.core.utils.VarTemplate;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.apache.log4j.Logger;

/**
 *
 * @author Albert
 */
public class RmiConnectorFactory {
    
    private final Logger logger = Logger.getLogger(RmiConnectorFactory.class);

    /**
     * 远程地址的模板
     */
    private final static String URL_TEMPLATE = "service:jmx:rmi:///jndi/rmi://${host}:${port}/jmxrmi";
    /**
     * 工厂实例
     */
    private static final RmiConnectorFactory instance = new RmiConnectorFactory();
    /**
     * 是否自动关闭RMI连接
     */
    private boolean autoClose = true;
    /**
     * 最大空闲期（单位：秒）
     */
    private int duration = 30;
    /**
     * 连接器集合（用于自动关闭连接）
     */
    private Map<Key, Value> connectors = Collections.synchronizedMap(new HashMap());

    private RmiConnectorFactory() {
    }

    public static RmiConnectorFactory getInstance() {
        return instance;
    }

    synchronized public MBeanServerConnection getConnection(String host, int port) {
        try {
            JMXConnector jmxc = getConnector(host, port);
            MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
            return mbsc;
        } catch (IOException ex) {
            Map params = createParams(host, port);
            String strUrl = VarTemplate.format(URL_TEMPLATE, params);
            throw new RmiException(String.format("访问 RMI 地址[%s]出现 IO 异常。", strUrl), ex);
        }
    }

    private JMXConnector getConnector(String host, int port) {
        String strUrl = null;
        try {
            // 检查缓存
            Key key = new Key(host, port);
            if (connectors.containsKey(key)) {
                Value value = connectors.get(key);
                JMXConnector connector = value.getConnector();
                if (!test(connector)) {
                    // 当前连接已经中断，将其失效
                    connectors.remove(key);
                } else {
                    value.setLastAccessTime(System.currentTimeMillis());
                    return connector;
                }
            }
            Map params = createParams(host, port);
            strUrl = VarTemplate.format(URL_TEMPLATE, params);
            JMXServiceURL url = new JMXServiceURL(strUrl);
            JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
            // 放入缓存
            connectors.put(key, new Value(jmxc, System.currentTimeMillis()));
            return jmxc;

        } catch (MalformedURLException ex) {
            throw new RmiException(String.format("RMI 地址[%s]解析错误。", strUrl), ex);
        } catch (IOException ex) {
            throw new RmiException(String.format("访问 RMI 地址[%s]出现 IO 异常。", strUrl), ex);
        }
    }

    /**
     * 关闭RMI的远程连接
     *
     * @param host
     * @param port
     */
    synchronized public void close(String host, int port) {
        Key key = new Key(host, port);
        if (connectors.containsKey(key)) {
            Value value = connectors.remove(key);
            try {
                value.getConnector().close();
            } catch (IOException ex) {
                Map params = createParams(host, port);
                String strUrl = VarTemplate.format(URL_TEMPLATE, params);
                LogWriter.error(logger, String.format("关闭服务器连接[%s]出现 IO 异常。", strUrl), ex);
            }
        }
    }
    
    /**
     * 测试当前连接是否可用
     */
    private boolean test(JMXConnector connector) {
        try {
            connector.getMBeanServerConnection();
        } catch (IOException ex) {
            LogWriter.warn2(logger, ex, "连接已经被关闭!");
            return false;
        }
        return true;
    }

    private Map createParams(String host, int port) {
        Map params = new HashMap();
        params.put("host", host);
        params.put("port", String.valueOf(port));
        return params;
    }
}

class Key {

    private final String host;
    private final int port;

    public Key(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Key other = (Key) obj;
        if ((this.host == null) ? (other.host != null) : !this.host.equals(other.host)) {
            return false;
        }
        if (this.port != other.port) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + (this.host != null ? this.host.hashCode() : 0);
        hash = 71 * hash + this.port;
        return hash;
    }

    @Override
    public String toString() {
        return "Key{" + "host=" + host + ", port=" + port + '}';
    }
}

class Value {

    /**
     * 缓存的连接
     */
    private final JMXConnector connector;
    /**
     * 开始时间
     */
    private final long startTime;
    /**
     * 最后一次访问时间
     */
    private long lastAccessTime;

    public Value(JMXConnector connector, long startTime) {
        this.connector = connector;
        this.startTime = startTime;
        this.lastAccessTime = startTime;
    }

    public JMXConnector getConnector() {
        return connector;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return (obj == this);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }
}
