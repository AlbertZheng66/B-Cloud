
package com.xt.bcloud.td;

import com.xt.core.exception.SystemException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;


/**
 * 服务器通道工厂.
 * @author albert
 */
public class ServerChannelFactory {

    private static ServerChannelFactory instance = new ServerChannelFactory();

    private Map<ChannelKey, SocketChannel> cachedChannels = Collections.synchronizedMap(new HashMap());

    private ServerChannelFactory () {

    }

    static public ServerChannelFactory getInstance() {
        return instance;
    }

    synchronized public SocketChannel register (String host, int port, Selector selector, Object attachment) {
        if (StringUtils.isEmpty(host)) {
            throw new SystemException("主机地址不能为空.");
        }
        if (port < 0 || port > 65535) {
            throw new SystemException("端口号非法。");
        }
        if (selector == null || !selector.isOpen()) {
            throw new SystemException("选择器的状态错误。");
        }
        ChannelKey channelKey = new ChannelKey(host, port);
        SocketChannel channel = cachedChannels.get(channelKey);
        if (channel == null || !channel.isOpen()) {
            channel = open(host, port, selector, attachment);
        }        
        return channel;
        
    }

    private SocketChannel open(String host, int port, Selector selector, Object attachment) {
        SocketChannel channel = null;
        try {
            channel = SocketChannel.open();
            if (channel.isConnectionPending()) {
                channel.finishConnect();
            }
            channel.configureBlocking(false);

            // 注册转发通道
            channel.register(selector, SelectionKey.OP_CONNECT, attachment);

            // 连接到远端
            SocketAddress serverAddress = new InetSocketAddress(host, port);
            channel.connect(serverAddress);
            
            // 存放在缓存中
            ChannelKey channelKey = new ChannelKey(host, port);
            cachedChannels.put(channelKey, channel);
        } catch (Exception e) {
            // LogWriter.warn(logger, message);
        }
        return channel;
    }

}

class ChannelKey {
    String host;

    int port;

    public ChannelKey(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ChannelKey other = (ChannelKey) obj;
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
        int hash = 7;
        hash = 67 * hash + (this.host != null ? this.host.hashCode() : 0);
        hash = 67 * hash + this.port;
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder strBld = new StringBuilder();
        strBld.append(super.toString()).append("[");
        strBld.append("host=").append(host).append("; ");
        strBld.append("port=").append(port);
        strBld.append("]");
        return strBld.toString();
    }

}
