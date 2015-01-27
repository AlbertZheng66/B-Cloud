package com.xt.bcloud.td.http;

import com.xt.bcloud.comm.CloudUtils;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import static com.xt.bcloud.td.http.HttpConstants.*;
import com.xt.bcloud.td7.CookieReader;

/**
 *
 * @author albert
 */
public class Request extends HttpMessage implements CookieReader{

    private final Logger logger = Logger.getLogger(Request.class);

    /**
     * 每个请求都产生一个UUID，便于错误的最终和审核
     */
    private final String uuid = CloudUtils.generateOid();
    
    /**
     * 客户端的IP地址（Host）
     */
    private String host;

    /**
     * 客户端的端口号
     */
    private int port = 80;
    /**
     * 请求方法（Request Methods），是原始的字符串，如：GET /gt_demo/ HTTP/1.1
     */
    private String requestMethod;
    /**
     * 计算应用的上下文(绝对路径）
     */
    private String contextPath;
    /**
     * 请求方法的名称：POST, GET, DELETE, TRACE, HEADER, OPTION 等
     * RFC 2616 的规范定义
     *  Method         = "OPTIONS"                ; Section 9.2
    | "GET"                    ; Section 9.3
    | "HEAD"                   ; Section 9.4
    | "POST"                   ; Section 9.5
    | "PUT"                    ; Section 9.6
    | "DELETE"                 ; Section 9.7
    | "TRACE"                  ; Section 9.8
    | "CONNECT"                ; Section 9.9
    | extension-method
    extension-method = token
     */
    private String methodName;

    /**
     * 请求的版本
     */
    private String version;

    private Map<String, String> cookies = new HashMap(5);

    public Request() {
    }

    /**
     * 解析“请求行”的各个部分信息
     * RFC 2616: Request-Line   = Method SP Request-URI SP HTTP-Version CRLF
     * @param requestMethod
     */
    private void parseMethod(String requestLine) {
        if (requestLine == null) {
            return;
        }
//        byte[] bytes = null;
//        try {
//            bytes = requestLine.getBytes("ASCII");
//        } catch (UnsupportedEncodingException ex) {
//            logger.warn(String.format("不支持的字符集[%s]。", "ASCII"), ex);
//            return;
//        }

//            // 空格作为分隔符
        String[] segs = requestLine.split(" ");
        if (segs.length > 0) {
            this.methodName = segs[0];
        }
        if (segs.length > 1) {
            // absoluate path
            this.contextPath = segs[1];
        }
        if (segs.length > 2) {
            this.version = segs[2];
        }
    }

    @Override
    public void putHeader(String name, String value) {
        super.putHeader(name, value);
        if ("Host".equals(name)) {
            String _value = value == null ? "" : value;
            String address = _value.trim();
            String[] segs = address.split("[:]");
            this.host = segs[0];
            if (segs.length > 1) {
                this.port = Integer.parseInt(segs[1]);
            }
        } else if ("Cookie".equals(name)) {
            parseCookies(value);
        }
    }

    /**
     * 解析请求头的 Cookie 内容
     * TODO: 处理字符转义的情况
     * @param value
     */
    private void parseCookies(String value) {
        // 处理Cookie
        if (StringUtils.isEmpty(value)) {
            return;
        }
        String[] _cookies = value.trim().split("[;]"); //
        for (int i = 0; i < _cookies.length; i++) {
            String[] cookie = _cookies[i].split("[=]");
            if (cookie.length > 1) {
                String cName = cookie[0];
                StringBuilder cValue = new StringBuilder(cookie[1]);
                if (cValue.charAt(0) == DOUBLE_QUOTATION) {
                    cValue.deleteCharAt(0);
                }
                if (cValue.charAt(cValue.length() - 1) == DOUBLE_QUOTATION) {
                    cValue = cValue.deleteCharAt(cValue.length() - 1);
                }
                this.cookies.put(cName, cValue.toString());
            } else {
                // warning and error
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder strBld = new StringBuilder();
        strBld.append(super.toString()).append("[");
        strBld.append("uuid=").append(uuid).append("; ");
        strBld.append("host=").append(host).append("; ");
        strBld.append("contextPath=").append(contextPath).append("; ");
        strBld.append("requestMethod=").append(requestMethod).append("; ");
        strBld.append("cookies=").append(cookies);
        strBld.append("]");
        return strBld.toString();
    }



    public String getHost() {
        return host;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public int getPort() {
        return port;
    }

    public void setRequestMethod(String requestMethod) {
        parseMethod(requestMethod);
        this.requestMethod = requestMethod;
    }

    /**
     * 返回指定Cookie的值，如果此值不存在，则返回空。
     * @param name
     * @return
     */
    public String getCookieValue(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        return cookies.get(name);
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getVersion() {
        return version;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

}
