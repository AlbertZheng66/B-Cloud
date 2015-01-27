package com.xt.bcloud.td.http;

import com.xt.bcloud.comm.CloudUtils;
import com.xt.core.log.LogWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import static com.xt.bcloud.td.http.HttpConstants.*;

/**
 *
 * @author albert
 */
abstract public class HttpParser {

    private final Logger logger = Logger.getLogger(HttpParser.class);    
    
    // 当前解析的行
    protected int rowIndex = 0;
    
    // 当前消息的请求传输编码
    protected String transferEncoding;
    
    /**
     * 记载“transferEncoding=chunk”时，lastChunk 的行数
     */
    private int lastChunk = -1;
    /**
     * 最后一个“chunk”的大小
     */
    private long chunkSize = -1;
    /**
     * 消息的长度
     */
    protected int contentLength = 0;

    // 当前正在处理的行(HTTP 头部分)内容(读取头的每一行)
    protected final ByteArrayOutputStream rowContent = new ByteArrayOutputStream();
    
    /**
     * 标签体的内容
     */
    protected final ByteArrayOutputStream bodyContent = new ByteArrayOutputStream();
    
    /**
     * 前一行是否为空行
     */
    protected boolean prevRowEmpty = false;
    /**
     * 头信息的读取是否结束
     */
    protected boolean headEnd = false;
    /**
     * 原始的 HTTP 请求
     */
    private final ByteArrayOutputStream originalMessage = new ByteArrayOutputStream(1 * 1024);
    /**
     * 前一个请求头
     */
    protected String prevHeaderName;
    /**
     * 已读取的请求体的字节数 body。
     */
    protected int bodyLength = 0;
    /**
     * 已经解析的消息
     */
    // private final List<HttpMessage> messages = new ArrayList(1);
    /**
     * 是否是“multipart/byteranges; boundary=THIS_STRING_SEPARATES”类型。
     */
    private boolean isByteRanges = false;
    /**
     * "multipart/byteranges"类型的分界符
     */
    private byte[] boundary = null;
    /**
     * 最新被解析的消息
     */
    private HttpMessage lastMessage;
    /**
     * 标识一个消息体是否以“chunked”方式进行传输
     */
    private boolean isChunked = false;

    public HttpParser() {
    }

    /**
     * 开始处理方法
     */
    abstract HttpMessage begin();

    /**
     * HTTP 请求开始
     * @param bodyBytes 消息体的字节数，如果消息体为空，则此字段为空。
     * @param originalBytes 原始字节数（包括消息体和消息体在内的所有字节）
     */
    abstract HttpMessage end(byte[] bodyBytes, byte[] originalBytes);

    /**
     * 设置头一行
     */
    abstract void setStartLine(String row);

    /**
     * 放置解析后的头信息
     */
    protected void putHeader(String name, String value) {
        if (name == null || value == null) {
            return;
        }
        String _name = name.trim();
        if (_name.equalsIgnoreCase("Transfer-Encoding")) {
            transferEncoding = value.trim();
            if (!transferEncoding.equals("identity")) {
                isChunked = true;
            }
        } else if ("Content-Length".equals(_name)) {
            String _value = (StringUtils.isEmpty(value) ? "0" : value);
            this.contentLength = Integer.parseInt(_value.trim());
        } else if ("Content-Type".equals(_name)) {
            String _value = value.trim();
            if (_value.startsWith("multipart/")) {
                this.isByteRanges = true;
                // 样例: Content-type: multipart/byteranges; boundary=THIS_STRING_SEPARATES
                String[] segs = _value.split("[;]");
                for (int i = 0; i < segs.length; i++) {
                    String seg = segs[i].trim();
                    if (seg.startsWith("boundary")) {
                        String[] _fields = seg.split("[=]");
                        if (_fields.length < 1) {
                            // 缺少“boundary”域
                            throw new HttpException(ErrorFactory.ERROR_400);
                        }
                        try {
                            this.boundary = CloudUtils.toHeaderBytes(_fields[1].trim());
                            LogWriter.info2(logger, "this.boundary=%s", this.boundary);
                        } catch (UnsupportedEncodingException ignored) {
                            // LogWriter.
                        }
                    }
                }
                if (boundary == null || boundary.length == 0) {
                    // 缺少“boundary”域
                    throw new HttpException(ErrorFactory.ERROR_400);
                }
            }
        }
    }

    /**
     * 追加解析后的头信息
     */
    abstract void appendHeader(String name, String value);

    /**
     * 判断一个请求是否结束，根据(RFC-2616)
     * The following rules describe how to correctly determine the length and end
     * of an entity body in several different circumstances. The rules should be applied in order;
     * the first match applies.
     * 1.       If a particular HTTP message type is not allowed to have a body,
     * ignore the Content-Length header for body calculations. The Content-Length
     * headers are informational in this case and do not describe the actual body length.
     * (Na?ve HTTP applications can get in trouble if they assume Content-Length always means there is a body).
     * The most important example is the HEAD response. The HEAD method requests
     * that a server send the headers that would have been returned by an equivalent GET request,
     * but no body. Because a GET response would send back a Content-Length header,
     * so will the HEAD response?but unlike the GET response, the HEAD response will not have a body.
     * 1XX, 204, and 304 responses also can have informational Content-Length headers
     * but no entity body. Messages that forbid entity bodies must terminate
     * at the first empty line after the headers, regardless of which entity header fields are present.
     * 2.       If a message contains a Transfer-Encoding header
     * (other than the default HTTP "identity" encoding),
     * the entity will be terminated by a special pattern called a "zero-byte chunk,"
     * unless the message is terminated first by closing the connection.
     * 3.       If a message has a Content-Length header (and the message type
     * allows entity bodies), the Content-Length value contains the body length,
     * unless there is a non-identity Transfer-Encoding header.
     * If a message is received with both a Content-Length header field
     * and a non-identity Transfer-Encoding header field, you must ignore
     * the Content-Length, because the transfer encoding will change the way entity
     * bodies are represented and transferred (and probably the number of bytes transmitted).
     * 4.       If the message uses the "multipart/byteranges" media type and the
     * entity length is not otherwise specified (in the Content-Length header),
     * each part of the multipart message will specify its own size.
     * This multipart type is the only entity body type that self-delimits
     * its own size, so this media type must not be sent unless the sender knows the recipient can parse it.
     * Because a Range header might be forwarded by a more primitive proxy that
     * does not understand multipart/byteranges, the sender must delimit the
     * message using methods 1, 3, or 5 in this section if it isn't sure the receiver
     * understands the self- delimiting format.
     * 5.       If none of the above rules match, the entity ends when the
     * connection closes. In practice, only servers can use connection close to
     * indicate the end of a message. Clients can't close the connection to
     * signal the end of client messages, because that would leave no way for
     * the server to send back a response.[5] The client could do a half close
     * of just its output connection, but many server applications aren't
     * designed to handle this situation and will interpret a half close as
     * the client disconnecting from the server. Connection management was never
     * well specified in HTTP. See Chapter 4 for more details.
     * To be compatible with HTTP/1.0 applications, any HTTP/1.1 request
     * that has an entity body also must include a valid Content-Length header field
     * (unless the server is known to be HTTP/1.1-compliant). The HTTP/1.1 specification
     * counsels that if a request contains a body and no Content-Length,
     * the server should send a 400 Bad Request response if it cannot determine
     * the length of the message, or a 411 Length Required response if it wants
     * to insist on receiving a valid Content-Length. For compatibility with HTTP/1.0 applications,
     * HTTP/1.1 requests containing an entity body must include
     * a valid Content-Length header field, unless the server is known to be HTTP/1.1-compliant.
     * If a request contains a body without a Content-Length,
     * the server should respond with 400 Bad Request if it cannot determine
     * the length of the message, or with 411 Length Required if it wants to insist
     * on receiving a valid Content-Length.
     * @return
     */
    protected boolean isMessageEnd() {
        if (!headEnd) {
            return false;
        }
        // 1. 1XX, 204, 304无消息体(在 response 中处理)。

        // 2. 处理“Transfer-Encoding header”头的情况
        if (isChunked) {
            // 首先寻找“0CRLF” 判断的范式为：1*("0") [ chunk-extension ] CRLF
            // “OCRLF”后面应该紧跟一个空行，表示 chunk 结束
            if (lastChunk < 0) {
                int length = rowContent.size();
                // 判断此行是否为“1*("0") [ chunk-extension ] CRLF”
                if (length > 2) {
                    byte[] content = rowContent.toByteArray();
                    if (content[0] == '0' && content[length - 2] == CARRAIGE_RETURN && content[length - 1] == LINE_FEED) {
                        lastChunk = this.rowIndex;
                    }
                }
            } else {
                int length = rowContent.size();
                // 检查到一个空行
                if (length == 2) {
                    byte[] content = rowContent.toByteArray();
                    if (content[0] == CARRAIGE_RETURN && content[1] == LINE_FEED) {
                        return true;
                    }
                } else if (length > 2) {
                    // 假设在数据中有存在“0CRLF”的情况，那么他的下一行不是空行，忽略此行的“0CRLF”
                    lastChunk = -1;
                }
            }
            return false;  // 快速中断
        }

        // 3. 处理contentLength的情况(将"chunk"的排除在外)
        if ((!isChunked) && (!isByteRanges) && (bodyLength >= contentLength)) {
            return true;
        }

        // 4. 处理"multipart/byteranges", 结束符的形式：--boundary--CRLF
        if (isByteRanges && rowContent.size() == boundary.length + 6) {
            byte[] content = rowContent.toByteArray();
            if (content[0] == HYPHEN && content[1] == HYPHEN
                    && content[content.length - 4] == HYPHEN
                    && content[content.length - 3] == HYPHEN
                    && content[content.length - 2] == CARRAIGE_RETURN
                    && content[content.length - 1] == LINE_FEED) {
                for (int i = 0; i < boundary.length; i++) {
                    if (content[i + 2] != boundary[i]) {
                        return false;
                    }
                }
                return true;
            }
        }

        // 5. 处理连接关闭的情况

        return false;
    }

    /**
     *  对Http 请求进行解析
     */
    public List<HttpMessage> parse(final byte[] _bytes) throws IOException {
        if (_bytes == null || _bytes.length == 0) {
            return Collections.EMPTY_LIST;
        }
        final List<HttpMessage> messages = new ArrayList(1);

        // 正在处理的HttpMessage（请求或者响应）
        lastMessage = begin();  // 开始处理
        for (int i = 0; i < _bytes.length; i++) {
            byte b = _bytes[i];
            originalMessage.write(b);  // 保存原始输入，保存到解析结束。
            if (headEnd) {
                // 头信息结束后
                bodyContent.write(b);
                bodyLength++;
            }

            rowContent.write(b);  // 记录一行的信息(信息头)，保存到行结束
            // for test
            // System.out.println("rowContent=" + new String(rowContent.toByteArray()));

            if (isMessageEnd()) {
                doMessageEnd(messages);
                continue;  // 一个连接可能发送了多个请求(Pipeline 的情况)
            }

            if (b != LINE_FEED) {
                continue;
            }

            // 开始处理数据(空行的标准：只有一个)
            if (isEmptyRow(rowContent)) {
                // 忽略开头的空行
                /* In the interest of robustness, servers SHOULD ignore any empty line(s) received where a Request-Line is
                 * expected. In other words, if the server is reading the protocol stream at the beginning of a message and receives a
                 * CRLF first, it should ignore the CRLF.
                 */
                if (rowIndex <= 0) {
                    rowContent.reset();
                    continue;
                }

                // 某些情况下，头结束也代表着请求结束了(没有消息体，所以需要再次判断一下)
                if (isMessageEnd()) {
                    doMessageEnd(messages);
                    continue;
                }
                // 判断是消息读取结束，还是头信息读取结束
                // FIXME: 有个Bug, 需要处理错误头的情况（比如：没设置ContentLength）
                headEnd = true;       // 头信息已经读取结束（遇到一个空行就结束）
                prevRowEmpty = true;
            } else {
                prevRowEmpty = false;  // 重置空行记录

                if (!headEnd) {  // 头结束后不再需要后续处理
                    // 开始解析头部数据
                    String content = new String(rowContent.toByteArray(), HTTP_HEADER_ENCODING);
                    if (rowIndex == 0) {
                        // 请求方法（Request Methods ）， 不关心起内容。
                        setStartLine(content);
                    } else {
                        parseHeadField(content);
                    }
                }
            }  // end of isEmptyRow()
            rowContent.reset();    // 重新设置行数据
            rowIndex++;
        }  // end of while
        return messages;
    }

    /**
     * 返回此HTTP的是否是最后一个请求。一个HTTP连接可能包含多个请求, 需要通过判断
     * 头：“Connection:close” 和 “Keep-Alive” 进行判断。
     * HTTP1.1 的情况下，默认是打开的。
     * @return 此次连接请求是否结束。
     */
    public boolean isConnectionClosed() {
        return (lastMessage != null) && "close".equals(lastMessage.getConnection());
    }

    /**
     * 判断一个行记录是否是空行(只有一个换行字符)或者一个回车加一个换行字符。
     * @param _rowContent
     * @param b 当前在处理的字符。
     * @return
     */
    protected boolean isEmptyRow(ByteArrayOutputStream _rowContent) {
        byte[] content = _rowContent.toByteArray();
        return (content.length == 1 && content[0] == LINE_FEED)
                || (content.length == 2
                && content[0] == CARRAIGE_RETURN
                && content[1] == LINE_FEED);
    }

    /**
     * 请求结束之后需要进行的处理工作。
     * @param messages
     */
    private void doMessageEnd(final List<HttpMessage> messages) {
        // 一个完整的HTTP 请求的原始信息
        byte[] bodyBytes = new byte[bodyContent.size()];
        System.arraycopy(bodyContent.toByteArray(), 0, bodyBytes, 0, bodyBytes.length);
        byte[] originalBytes = new byte[originalMessage.size()];
        System.arraycopy(originalMessage.toByteArray(), 0, originalBytes, 0, originalBytes.length);
        lastMessage = end(bodyBytes, originalBytes); // 处理一个完整的消息
        messages.add(lastMessage);
        reset(); // 重新设置变量
    }

    /**
     * 解析（请求或者响应）头的每行数据。
     * @param content
     */
    private void parseHeadField(String content) {
        // HTTP/1.1 header field values can be folded onto multiple lines
        // if the continuation line begins with a space or horizontal tab.
        if (content.charAt(0) == ' ' || content.charAt(0) == 11) {
            // 追加到前一行的头上
            appendHeader(prevHeaderName, content);
        } else {
            // 读取请求头（Request Headers ），解析出参数名称和“体”
            String[] segs = content.split("[:]", 2);
            if (segs.length > 1) {
                String name = segs[0];
                prevHeaderName = name;
                String value = segs[1];
                putHeader(name, value);
            } else if (segs.length == 1) {
                // 追加到前一行的头上
                appendHeader(prevHeaderName, content);
            } else {
                // throw new Exception();
            }
        }
    }

    /**
     * 信息设置标题信息
     */
    public void reset() {
        prevRowEmpty = false;
        headEnd = false;
        rowIndex = 0;
        contentLength = 0;
        transferEncoding = null;
        prevHeaderName = null;
        bodyLength = 0;
        originalMessage.reset();
        bodyContent.reset();
        rowContent.reset();
    }

    /**
     * 判断一下头是否解析结束
     * @return
     */
    public boolean isHeadParsed() {
        return this.headEnd;
    }

    public HttpMessage getLastMessage() {
        return lastMessage;
    }
}

