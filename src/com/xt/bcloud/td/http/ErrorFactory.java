package com.xt.bcloud.td.http;

import com.xt.bcloud.comm.CloudUtils;
import com.xt.core.exception.SystemException;
import com.xt.core.log.LogWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import static com.xt.bcloud.td.http.HttpConstants.*;

/**
 *
 * @author albert
 */
public class ErrorFactory {

    private static final ErrorFactory instance = new ErrorFactory();

    private final Map<String, HttpError> errors;
    
    private static final Logger logger = Logger.getLogger(ErrorFactory.class);

    /****** 系统定义的异常消息 *****/
    /**
     * This response code indicates that the server detected a syntax error in the client's request.
     */
    public static final HttpError ERROR_400 = new HttpError("400", "Bad Request");
    /**
     * The result code is given along with the WWW-Authenticate header to
     * indicate that the request lacked proper authorization, and the client
     * should supply proper authorization when requesting this URL again.
     * See the description of the Authorization header for more information
     * on how authorization works in HTTP.
     */
    public static final HttpError ERROR_401 = new HttpError("401", "Unauthorized");
    /**
     * This code is not yet implemented in HTTP.
     */
    public static final HttpError ERROR_402 = new HttpError("402", "Payment Required");
    /**
     * The request was denied for a reason the server does not want to
     * (or has no means to) indicate to the client.
     */
    public static final HttpError ERROR_403 = new HttpError("403", "Forbidden");
    /**
     * The document at the specified URL does not exist.
     */
    public static final HttpError ERROR_404 = new HttpError("404", "Not Found");
    /**
     * This code is given with the Allow header and indicates that the method
     * used by the client is not supported for this URL.
     */
    public static final HttpError ERROR_405 = new HttpError("405", "Method Not Allowed");
    /**
     * The URL specified by the client exists, but not in a format preferred
     * by the client. Along with this code, the server provides
     * the Content-Language, Content-Encoding, and Content-type headers.
     */
    public static final HttpError ERROR_406 = new HttpError("406", "Not Acceptable");
    /**
     * The proxy server needs to authorize the request before forwarding it.
     * Used with the Proxy-Authenticate header.
     */
    public static final HttpError ERROR_407 = new HttpError("407", "Proxy Authentication Required");
    /**
     * This response code means the client did not produce a full request
     * within some predetermined time (usually specified in the server's
     * configuration), and the server is disconnecting the network connection.
     */
    public static final HttpError ERROR_408 = new HttpError("408", "Request Time-out");
    /**
     * This code indicates that the request conflicts with another request or
     * with the server's configuration. Information about the conflict should
     * be returned in the data portion of the reply. For example, this response
     * code could be given when a client's request would cause integrity
     * problems in a database.
     */
    public static final HttpError ERROR_409 = new HttpError("409", "Conflict");
    /**
     * This code indicates that the requested URL no longer exists
     * and has been permanently removed from the server.
     */
    public static final HttpError ERROR_410 = new HttpError("410", "Gone");
    /**
     * The server will not accept the request without a Content-length header supplied in the request.
     */
    public static final HttpError ERROR_411 = new HttpError("411", "Length Required");
    /**
     * The condition specified by one or more If... headers in the request evaluated to false.
     */
    public static final HttpError ERROR_412 = new HttpError("412", "Precondition Failed");
    /**
     * The server will not process the request because its entity-body is too large.
     */
    public static final HttpError ERROR_413 = new HttpError("413", "Request Entity Too Large");
    /**
     * The server will not process the request because its request URL is too large.
     */
    public static final HttpError ERROR_414 = new HttpError("414", "Request URL Too Long");
    /**
     * The server will not process the request because its entity-body is in an unsupported format. 
     */
    public static final HttpError ERROR_415 = new HttpError("415", "Unsupported Media Type");
    /**
     * The requested byte range is not available and is out of bounds.
     */
    public static final HttpError ERROR_416 = new HttpError("416", "Request Range Not Satisfiable");
    /**
     * The server is unable to meet the demands of the Expect header given by the client.
     */
    public static final HttpError ERROR_417 = new HttpError("417", "Expectation Failed");
    /**
     * This code indicates that a part of the server (for example, a CGI program)
     * has crashed or encountered a configuration error.
     */
    public static final HttpError ERROR_500 = new HttpError("500", "Internal Server Error");
    /**
     * This code indicates that the client requested an action that
     * cannot be performed by the server.
     */
    public static final HttpError ERROR_501 = new HttpError("501", "Not Implemented");
    /**
     * This code indicates that the server (or proxy) encountered invalid
     * responses from another server (or proxy).
     */
    public static final HttpError ERROR_502 = new HttpError("502", "Bad Gateway");
    /**
     * This code means that the service is temporarily unavailable,
     * but should be restored in the future. If the server knows
     * when it will be available again, a Retry-After header may also be supplied.
     */
    public static final HttpError ERROR_503 = new HttpError("503", "Service Unavailable");
    /**
     * This response is like 408 (Request Time-out) except that a gateway or proxy has timed out.
     */
    public static final HttpError ERROR_504 = new HttpError("504", "Gateway Time-out");

    /**
     * The server will not support the HTTP protocol version used in the request.
     */
    public static final HttpError ERROR_505 = new HttpError("505", "HTTP Version Not Supported");
    
    /**
     * There are errors caused by the servers.
     */
    public static final HttpError ERROR_506 = new HttpError("506", "Errors occured in the server");


    static {
    }

    /****** 系统定义的异常消息 *****/
    private ErrorFactory() {
        errors = new ConcurrentHashMap();
    }

    static private void init() {
        instance.errors.put("400", ERROR_400);
        instance.errors.put("401", ERROR_401);
        instance.errors.put("402", ERROR_402);
        instance.errors.put("403", ERROR_403);
        instance.errors.put("404", ERROR_404);
        instance.errors.put("405", ERROR_405);
        instance.errors.put("406", ERROR_406);
        instance.errors.put("407", ERROR_407);
        instance.errors.put("408", ERROR_408);
        instance.errors.put("409", ERROR_409);
        instance.errors.put("410", ERROR_410);
        instance.errors.put("411", ERROR_411);
        instance.errors.put("412", ERROR_412);
        instance.errors.put("413", ERROR_413);
        instance.errors.put("414", ERROR_414);
        instance.errors.put("415", ERROR_415);
        instance.errors.put("416", ERROR_416);
        instance.errors.put("417", ERROR_417);
        instance.errors.put("500", ERROR_500);
        instance.errors.put("501", ERROR_501);
        instance.errors.put("502", ERROR_502);
        instance.errors.put("503", ERROR_503);
        instance.errors.put("504", ERROR_504);
        instance.errors.put("505", ERROR_505);
    }

    static public ErrorFactory getInstance() {
        if (instance.errors.isEmpty()) {
            init();
        }
        return instance;
    }

    public HttpError create(String errorCode) {
        if (StringUtils.isEmpty(errorCode) || !errors.containsKey(errorCode)) {
            throw new SystemException(String.format("错误编码[%s]不能为空，或者不存在。", errorCode));
        }
        HttpError registered = errors.get(errorCode);
        try {
            HttpError error = (HttpError) registered.clone();
            return error;
        } catch (CloneNotSupportedException ex) {
            throw new SystemException(String.format("对象[%s]克隆失败。", registered), ex);
        }
    }

    static public byte[] getError(String errorCode) {
        return null;
    }

    static public void writeTo(OutputStream os, HttpException ex) {
        if (os == null || ex == null || ex.getError() == null) {
            LogWriter.warn2(logger, "输出错误信息时出现错误参数，os=%s; ex=%s。", os, ex);
            return;
        }
        try {
            HttpError error = ex.getError();
            // 消息体
            byte[] body = String.format("<html><head>"
                    + "<title>B-Cloud/0.1 - Error report</title></head>"
                    + "<body>" +
                    "    <h1>服务端处理产生错误[%s: %s]。</h1>" +
                    "    <h3>详细信息:</h3> %s " +
                    "</body></html>", error.getErrorCode(), error.getErrorMessage(),
                    error.getLocalMessage()).getBytes("utf-8");

            // 写响应行
            os.write(CloudUtils.toHeaderBytes(HttpMessage.PROTOCOL));
            os.write(BLANK);
            os.write(CloudUtils.toHeaderBytes(ex.getError().getErrorCode()));
            os.write(BLANK);
            os.write(CloudUtils.toHeaderBytes(ex.getError().getErrorMessage()));
            os.write(CRLF);

            // 写日期
            writeHeader(os, "Date", "Sun, 31 Jan 2010 00:48:28 GMT");

            // 写 Server信息
            writeHeader(os, "Server", "B-CLOUD/0.1(Java)");

            // 写Connection信息
            writeHeader(os, "Connection", "close");

            // 写Content type 信息
            writeHeader(os, "Content-Type", "text/html;charset=utf-8");

            // 消息体的长度: 1039
            writeHeader(os, "Content-Length", String.valueOf(body.length));

            // 消息头与消息体之间的空行。
            os.write(CRLF);

            os.write(body);
        } catch (IOException ex1) {
            LogWriter.warn2(logger, ex1, "输出错误信息时出现错。");
        }
    }

    private static void writeHeader(OutputStream os, String name, String value) throws IOException {
        os.write(CloudUtils.toHeaderBytes(name));
        os.write(COLON);
        os.write(BLANK);
        os.write(CloudUtils.toHeaderBytes(value));
        os.write(CRLF);
    }
}
