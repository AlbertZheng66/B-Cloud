
package com.xt.bcloud.td.http;

import com.xt.bcloud.comm.CloudUtils;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import static com.xt.bcloud.td.http.HttpConstants.*;

/**
 * HTTP ��Ϣ, �� HTTP �������Ӧ�Ĺ�������, ��װ�����ǹ��õ����Ժͷ�����
 * @author albert
 */
public class HttpMessage {
    
    public static final String PROTOCOL = "HTTP/1.1";

    /**
     * ȫ��Ψһ��OID
     */
    private final String oid = CloudUtils.generateOid();

    /**
     * ��Ϣ�Ĵ���ʱ��
     */
    private final long createdTime = System.nanoTime();

    /**
     * ����ĳ���
     */
    protected int contentLength = 0;

    /**
     * ͷ��Ϣ��Headers��
     */
    protected final Map<String, String> headers = new LinkedHashMap();

    /**
     * ԭʼ����(����������Ÿ�ֵ)
     */
    protected byte[] originalMessage;

    /**
     * ��Ϣ��
     */
    protected byte[] messageBody;

    /**
     * ��¼ͷ�ġ�Connection����Ϣ��
     */
    private String connection;

    public HttpMessage() {
    }

    public void putHeader(String name, String value) {
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(value)) {
            // LogWriter.warn(logger, "����������Ϊ��");
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
            // LogWriter.warn(logger, "ͷ����������Ϊ��");
        }
        if (!headers.containsKey(name)) {
            // LogWriter.warn(logger, String.format("ͷ����[%s]����", name));
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
