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
    
    // ��ǰ��������
    protected int rowIndex = 0;
    
    // ��ǰ��Ϣ�����������
    protected String transferEncoding;
    
    /**
     * ���ء�transferEncoding=chunk��ʱ��lastChunk ������
     */
    private int lastChunk = -1;
    /**
     * ���һ����chunk���Ĵ�С
     */
    private long chunkSize = -1;
    /**
     * ��Ϣ�ĳ���
     */
    protected int contentLength = 0;

    // ��ǰ���ڴ������(HTTP ͷ����)����(��ȡͷ��ÿһ��)
    protected final ByteArrayOutputStream rowContent = new ByteArrayOutputStream();
    
    /**
     * ��ǩ�������
     */
    protected final ByteArrayOutputStream bodyContent = new ByteArrayOutputStream();
    
    /**
     * ǰһ���Ƿ�Ϊ����
     */
    protected boolean prevRowEmpty = false;
    /**
     * ͷ��Ϣ�Ķ�ȡ�Ƿ����
     */
    protected boolean headEnd = false;
    /**
     * ԭʼ�� HTTP ����
     */
    private final ByteArrayOutputStream originalMessage = new ByteArrayOutputStream(1 * 1024);
    /**
     * ǰһ������ͷ
     */
    protected String prevHeaderName;
    /**
     * �Ѷ�ȡ����������ֽ��� body��
     */
    protected int bodyLength = 0;
    /**
     * �Ѿ���������Ϣ
     */
    // private final List<HttpMessage> messages = new ArrayList(1);
    /**
     * �Ƿ��ǡ�multipart/byteranges; boundary=THIS_STRING_SEPARATES�����͡�
     */
    private boolean isByteRanges = false;
    /**
     * "multipart/byteranges"���͵ķֽ��
     */
    private byte[] boundary = null;
    /**
     * ���±���������Ϣ
     */
    private HttpMessage lastMessage;
    /**
     * ��ʶһ����Ϣ���Ƿ��ԡ�chunked����ʽ���д���
     */
    private boolean isChunked = false;

    public HttpParser() {
    }

    /**
     * ��ʼ������
     */
    abstract HttpMessage begin();

    /**
     * HTTP ����ʼ
     * @param bodyBytes ��Ϣ����ֽ����������Ϣ��Ϊ�գ�����ֶ�Ϊ�ա�
     * @param originalBytes ԭʼ�ֽ�����������Ϣ�����Ϣ�����ڵ������ֽڣ�
     */
    abstract HttpMessage end(byte[] bodyBytes, byte[] originalBytes);

    /**
     * ����ͷһ��
     */
    abstract void setStartLine(String row);

    /**
     * ���ý������ͷ��Ϣ
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
                // ����: Content-type: multipart/byteranges; boundary=THIS_STRING_SEPARATES
                String[] segs = _value.split("[;]");
                for (int i = 0; i < segs.length; i++) {
                    String seg = segs[i].trim();
                    if (seg.startsWith("boundary")) {
                        String[] _fields = seg.split("[=]");
                        if (_fields.length < 1) {
                            // ȱ�١�boundary����
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
                    // ȱ�١�boundary����
                    throw new HttpException(ErrorFactory.ERROR_400);
                }
            }
        }
    }

    /**
     * ׷�ӽ������ͷ��Ϣ
     */
    abstract void appendHeader(String name, String value);

    /**
     * �ж�һ�������Ƿ����������(RFC-2616)
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
        // 1. 1XX, 204, 304����Ϣ��(�� response �д���)��

        // 2. ����Transfer-Encoding header��ͷ�����
        if (isChunked) {
            // ����Ѱ�ҡ�0CRLF�� �жϵķ�ʽΪ��1*("0") [ chunk-extension ] CRLF
            // ��OCRLF������Ӧ�ý���һ�����У���ʾ chunk ����
            if (lastChunk < 0) {
                int length = rowContent.size();
                // �жϴ����Ƿ�Ϊ��1*("0") [ chunk-extension ] CRLF��
                if (length > 2) {
                    byte[] content = rowContent.toByteArray();
                    if (content[0] == '0' && content[length - 2] == CARRAIGE_RETURN && content[length - 1] == LINE_FEED) {
                        lastChunk = this.rowIndex;
                    }
                }
            } else {
                int length = rowContent.size();
                // ��鵽һ������
                if (length == 2) {
                    byte[] content = rowContent.toByteArray();
                    if (content[0] == CARRAIGE_RETURN && content[1] == LINE_FEED) {
                        return true;
                    }
                } else if (length > 2) {
                    // �������������д��ڡ�0CRLF�����������ô������һ�в��ǿ��У����Դ��еġ�0CRLF��
                    lastChunk = -1;
                }
            }
            return false;  // �����ж�
        }

        // 3. ����contentLength�����(��"chunk"���ų�����)
        if ((!isChunked) && (!isByteRanges) && (bodyLength >= contentLength)) {
            return true;
        }

        // 4. ����"multipart/byteranges", ����������ʽ��--boundary--CRLF
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

        // 5. �������ӹرյ����

        return false;
    }

    /**
     *  ��Http ������н���
     */
    public List<HttpMessage> parse(final byte[] _bytes) throws IOException {
        if (_bytes == null || _bytes.length == 0) {
            return Collections.EMPTY_LIST;
        }
        final List<HttpMessage> messages = new ArrayList(1);

        // ���ڴ����HttpMessage�����������Ӧ��
        lastMessage = begin();  // ��ʼ����
        for (int i = 0; i < _bytes.length; i++) {
            byte b = _bytes[i];
            originalMessage.write(b);  // ����ԭʼ���룬���浽����������
            if (headEnd) {
                // ͷ��Ϣ������
                bodyContent.write(b);
                bodyLength++;
            }

            rowContent.write(b);  // ��¼һ�е���Ϣ(��Ϣͷ)�����浽�н���
            // for test
            // System.out.println("rowContent=" + new String(rowContent.toByteArray()));

            if (isMessageEnd()) {
                doMessageEnd(messages);
                continue;  // һ�����ӿ��ܷ����˶������(Pipeline �����)
            }

            if (b != LINE_FEED) {
                continue;
            }

            // ��ʼ��������(���еı�׼��ֻ��һ��)
            if (isEmptyRow(rowContent)) {
                // ���Կ�ͷ�Ŀ���
                /* In the interest of robustness, servers SHOULD ignore any empty line(s) received where a Request-Line is
                 * expected. In other words, if the server is reading the protocol stream at the beginning of a message and receives a
                 * CRLF first, it should ignore the CRLF.
                 */
                if (rowIndex <= 0) {
                    rowContent.reset();
                    continue;
                }

                // ĳЩ����£�ͷ����Ҳ���������������(û����Ϣ�壬������Ҫ�ٴ��ж�һ��)
                if (isMessageEnd()) {
                    doMessageEnd(messages);
                    continue;
                }
                // �ж�����Ϣ��ȡ����������ͷ��Ϣ��ȡ����
                // FIXME: �и�Bug, ��Ҫ�������ͷ����������磺û����ContentLength��
                headEnd = true;       // ͷ��Ϣ�Ѿ���ȡ����������һ�����оͽ�����
                prevRowEmpty = true;
            } else {
                prevRowEmpty = false;  // ���ÿ��м�¼

                if (!headEnd) {  // ͷ����������Ҫ��������
                    // ��ʼ����ͷ������
                    String content = new String(rowContent.toByteArray(), HTTP_HEADER_ENCODING);
                    if (rowIndex == 0) {
                        // ���󷽷���Request Methods ���� �����������ݡ�
                        setStartLine(content);
                    } else {
                        parseHeadField(content);
                    }
                }
            }  // end of isEmptyRow()
            rowContent.reset();    // ��������������
            rowIndex++;
        }  // end of while
        return messages;
    }

    /**
     * ���ش�HTTP���Ƿ������һ������һ��HTTP���ӿ��ܰ����������, ��Ҫͨ���ж�
     * ͷ����Connection:close�� �� ��Keep-Alive�� �����жϡ�
     * HTTP1.1 ������£�Ĭ���Ǵ򿪵ġ�
     * @return �˴����������Ƿ������
     */
    public boolean isConnectionClosed() {
        return (lastMessage != null) && "close".equals(lastMessage.getConnection());
    }

    /**
     * �ж�һ���м�¼�Ƿ��ǿ���(ֻ��һ�������ַ�)����һ���س���һ�������ַ���
     * @param _rowContent
     * @param b ��ǰ�ڴ�����ַ���
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
     * �������֮����Ҫ���еĴ�������
     * @param messages
     */
    private void doMessageEnd(final List<HttpMessage> messages) {
        // һ��������HTTP �����ԭʼ��Ϣ
        byte[] bodyBytes = new byte[bodyContent.size()];
        System.arraycopy(bodyContent.toByteArray(), 0, bodyBytes, 0, bodyBytes.length);
        byte[] originalBytes = new byte[originalMessage.size()];
        System.arraycopy(originalMessage.toByteArray(), 0, originalBytes, 0, originalBytes.length);
        lastMessage = end(bodyBytes, originalBytes); // ����һ����������Ϣ
        messages.add(lastMessage);
        reset(); // �������ñ���
    }

    /**
     * ���������������Ӧ��ͷ��ÿ�����ݡ�
     * @param content
     */
    private void parseHeadField(String content) {
        // HTTP/1.1 header field values can be folded onto multiple lines
        // if the continuation line begins with a space or horizontal tab.
        if (content.charAt(0) == ' ' || content.charAt(0) == 11) {
            // ׷�ӵ�ǰһ�е�ͷ��
            appendHeader(prevHeaderName, content);
        } else {
            // ��ȡ����ͷ��Request Headers �����������������ƺ͡��塱
            String[] segs = content.split("[:]", 2);
            if (segs.length > 1) {
                String name = segs[0];
                prevHeaderName = name;
                String value = segs[1];
                putHeader(name, value);
            } else if (segs.length == 1) {
                // ׷�ӵ�ǰһ�е�ͷ��
                appendHeader(prevHeaderName, content);
            } else {
                // throw new Exception();
            }
        }
    }

    /**
     * ��Ϣ���ñ�����Ϣ
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
     * �ж�һ��ͷ�Ƿ��������
     * @return
     */
    public boolean isHeadParsed() {
        return this.headEnd;
    }

    public HttpMessage getLastMessage() {
        return lastMessage;
    }
}

