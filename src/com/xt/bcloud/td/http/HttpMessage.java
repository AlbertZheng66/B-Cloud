
package com.xt.bcloud.td.http;

import com.xt.bcloud.comm.CloudUtils;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import static com.xt.bcloud.td.http.HttpConstants.*;

/**
 * HTTP 消息, 是 HTTP 请求和响应的公共基类, 封装了他们公用的属性和方法。
 * @author albert
 */
public class HttpMessage {
    
    public static final String PROTOCOL = "HTTP/1.1";

    /**
     * 全局唯一的OID
     */
    private final String oid = CloudUtils.generateOid();

    /**
     * 消息的创建时间
     */
    private final long createdTime = System.nanoTime();

    /**
     * 请求的长度
     */
    protected int contentLength = 0;

    /**
     * 头信息（Headers）
     */
    protected final Map<String, String> headers = new LinkedHashMap();

    /**
     * 原始请求(解析结束后才赋值)
     */
    protected byte[] originalMessage;

    /**
     * 消息体
     */
    protected byte[] messageBody;

    /**
     * 记录头的“Connection”信息。
     */
    private String connection;

    public HttpMessage() {
    }

    public void putHeader(String name, String value) {
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(value)) {
            // LogWriter.warn(logger, "参数名不能为空");
            return;
        }
        String _name  = name.trim();
        headers.put(name, value);
        if ("Content-Length".equals(_name)) {
            this.contentLength = Integer.parseInt(value.trim());
        } else if ("Connection".equals(_name)) {
            this.connection = value;
        }
    }

    public void appendHeader(String name, String value) {
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(value)) {
            // LogWriter.warn(logger, "头参数名不能为空");
        }
        if (!headers.containsKey(name)) {
            // LogWriter.warn(logger, String.format("头参数[%s]错误。", name));
            return;
        }
        StringBuilder newValue = new StringBuilder(headers.get(name));
        newValue.append(new String(new byte[]{CARRAIGE_RETURN}, Charset.forName(HTTP_HEADER_ENCODING)));
        newValue.append(value);
        headers.put(name, newValue.toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HttpMessage other = (HttpMessage) obj;
        if ((this.oid == null) ? (other.oid != null) : !this.oid.equals(other.oid)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + (this.oid != null ? this.oid.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder strBld = new StringBuilder();
        strBld.append(super.toString()).append("[");
        strBld.append("uuid=").append(oid).append("; ");
        strBld.append("]");
        return strBld.toString();
    }

    public String getOid() {
        return oid;
    }

    public int getContentLength() {
        return contentLength;
    }

    public byte[] getOriginalMessage() {
        return originalMessage;
    }

    public void setOriginalMessage(byte[] originalMessage) {
        this.originalMessage = originalMessage;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getConnection() {
        return connection;
    }

    public byte[] getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(byte[] messageBody) {
        this.messageBody = messageBody;
    }

}
