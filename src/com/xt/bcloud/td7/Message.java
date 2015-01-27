
package com.xt.bcloud.td7;

import com.xt.bcloud.comm.CloudUtils;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

/**
 * ���ܵ���һ��HTTP����ע�⣺�Ѿ�ת��Ϊ��׼��HTTPЭ�顣
 * @author Albert
 */
abstract public class Message implements CookieReader{
    
    public static final String PROTOCOL = "HTTP/1.1";

    /**
     * ȫ��Ψһ��OID
     */
    protected final String oid = CloudUtils.generateOid();

    /**
     * ��Ϣ�Ĵ���ʱ��
     */
    private final long createdTime = System.nanoTime();

    /**
     * ����ĳ���
     */
    protected int contentLength = 0;

    /**
     * ��¼ͷ�ġ�Connection����Ϣ��
     */
    private String connection;
    
    
    protected Map<String, String> cookies = new LinkedHashMap(5);
    
    protected final Header header = new Header();
    
    protected final Buffers body = new Buffers();
    
    /**
     * ����ԭʼ���ݵ�Buffer��
     */
    protected Buffers originalBytes;

    public Message() {
    }
    
    public void putHeader(String name, String value) {
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(value)) {
            // LogWriter.warn(logger, "����������Ϊ��");
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
