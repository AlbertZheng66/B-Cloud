
package com.xt.bcloud.td7;

import com.xt.bcloud.comm.CloudUtils;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

/**
 * 接受到的一个HTTP请求，注意：已经转换为标准的HTTP协议。
 * @author Albert
 */
abstract public class Message implements CookieReader{
    
    public static final String PROTOCOL = "HTTP/1.1";

    /**
     * 全局唯一的OID
     */
    protected final String oid = CloudUtils.generateOid();

    /**
     * 消息的创建时间
     */
    private final long createdTime = System.nanoTime();

    /**
     * 请求的长度
     */
    protected int contentLength = 0;

    /**
     * 记录头的“Connection”信息。
     */
    private String connection;
    
    
    protected Map<String, String> cookies = new LinkedHashMap(5);
    
    protected final Header header = new Header();
    
    protected final Buffers body = new Buffers();
    
    /**
     * 所有原始数据的Buffer。
     */
    protected Buffers originalBytes;

    public Message() {
    }
    
    public void putHeader(String name, String value) {
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(value)) {
            // LogWriter.warn(logger, "参数名不能为空");
            return;
        }
        String _name  = name.trim();
        header.fields.put(name, value);
        if ("Content-Length".equals(_name)) {
            this.contentLength = Integer.parseInt(value.trim());
        } else if ("Connection".equals(_name)) {
            this.connection = value;
        }
    }
    
    public void appendHeader(String name, String value) {
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(value)) {
            return;
        }
        if (header.fields.containsKey(name)) {
            StringBuilder strBld = new StringBuilder(header.fields.get(name));
            strBld.append(value);
            header.fields.put(name, strBld.toString());
        } else {
            putHeader(name, value);
        }
    }

    public void appendBodyBuffer(ByteBuffer buffer) {
        if (buffer != null) {
            this.body.append(buffer);
        }
    }
    
    public void appendBuffers(Buffers buffers) {
        if (buffers != null) {
            this.body.setBuffers(buffers.getBuffers());
        }
    }

    public Header getHeader() {
        return header;
    }

    public String getConnection() {
        return connection;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public String getOid() {
        return oid;
    }

    public Buffers getOriginalBytes() {
        return originalBytes;
    }

    public void setOriginalBytes(Buffers originalBytes) {
        this.originalBytes = originalBytes;
    }

    public Buffers getBody() {
        return body;
    }
    
}
