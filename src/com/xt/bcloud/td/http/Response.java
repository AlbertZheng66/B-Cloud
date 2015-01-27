package com.xt.bcloud.td.http;

import com.xt.bcloud.comm.CloudUtils;
import com.xt.core.exception.BadParameterException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import static com.xt.bcloud.td.http.HttpConstants.*;

/**
 *
 * @author albert
 */
public class Response extends HttpMessage {

    /**
     * ״̬�У�Status Line��
     */
    private String statusLine;
    // private final Request request;
    private List<Cookie> cookies;

    public Response() {
    }

    public String getStatusLine() {
        return statusLine;
    }

    public void setStatusLine(String statusLine) {
        this.statusLine = statusLine;
    }

    /**
     * ���ͻ�������һ��Cookie�������������cookie������Ϊ�գ����׳� BadParameterException��
     * @param cookie
     */
    synchronized public void addCookie(Cookie cookie) {
        validate(cookie);
        if (cookies == null) {
            cookies = new ArrayList<Cookie>();
        }
        cookies.add(cookie);
    }

    /**
     * У��һ��Cookie�Ƿ�Ϸ���
     */
    private void validate(Cookie cookie) {
        if (cookie == null || StringUtils.isEmpty(cookie.getName())) {
            throw new BadParameterException("Cookie �������ƶ�����Ϊ�ա�");
        }
    }

    public Iterator<Cookie> getCookies() {
        if (cookies == null) {
            List<Cookie> empty = Collections.emptyList();
            return empty.iterator();
        }
        return cookies.iterator();
    }

    /**
     * �����е� Cookies ��Ϣ�����������С�
     * Set-Cookie: name=value [; expires=date] [; path=path] [; domain=domain] [; secure]
     * @param os
     */
    private void writeTo(OutputStream os) throws IOException {
        for (Iterator<Cookie> it = getCookies(); it.hasNext();) {
            Cookie cookie = it.next();
            os.write(CloudUtils.toHeaderBytes("Set-Cookie2"));
            os.write(COLON);
            os.write(BLANK);

            // д����
            writeField(os, cookie.getName(), cookie.getValue(), false);

            // д����ʱ�� ��expires��

            // д���ʱ��
            writeField(os, "Max-Age", String.valueOf(cookie.getMaxAge()), true);

            // д Version
            writeField(os, "Version", String.valueOf(cookie.getVersion()), true);

            // дDomain
            if (StringUtils.isNotEmpty(cookie.getDomain())) {
                writeField(os, "Domain", String.valueOf(cookie.getDomain()), true);
            }

            // д Path
            if (StringUtils.isNotEmpty(cookie.getPath())) {
                writeField(os, "Path", String.valueOf(cookie.getPath()), true);
            }

            // д Comment
            if (StringUtils.isNotEmpty(cookie.getComment())) {
                writeField(os, "Comment", String.valueOf(cookie.getComment()), true);
            }
            
            // д Secure
            if (cookie.getSecure()) {
                writeField(os, "Secure", "true", true);
            }
        }

    }

    private void writeField(OutputStream os, String name, String value, boolean prefix) throws IOException {
        if (prefix) {
            os.write(SEMI_COLON);
        }
        os.write(CloudUtils.toHeaderBytes(name));
        os.write(ASSIGNMENT);
        os.write(CloudUtils.toHeaderBytes(value));
        os.write(BLANK);
    }
}
