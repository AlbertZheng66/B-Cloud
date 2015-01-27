package com.xt.bcloud.td.http;

import com.xt.bcloud.td7.BufferFactory;
import java.nio.ByteBuffer;

/**
 *
 * @author Albert
 */
public class HttpConstants {

    public final static byte[] CRLF = {13, 10};
    
    private final static ByteBuffer CRLF_BUFFER = BufferFactory.getInstance().wrap(CRLF);
    
    /**
     * �ո�
     */
    public final static byte BLANK = ' ';
    
    private final static ByteBuffer BLANK_BUFFER = BufferFactory.getInstance().wrap((new byte[]{BLANK}));
    
    /**
     * ð��
     */
    public final static byte COLON = ':';
    /**
     * Buffer ��ʽ��ð��
     */
    private final static ByteBuffer COLON_BUFFER = BufferFactory.getInstance().wrap((new byte[]{COLON}));
    /**
     * �ֺ�
     */
    public final static byte SEMI_COLON = ';';
    /**
     * �ָ���: �س�
     */
    public final static byte CARRAIGE_RETURN = 13;
    
     /**
     * �ָ���: �س�
     */
    public final static ByteBuffer CARRAIGE_RETURN_BUFFER  = BufferFactory.getInstance().wrap((new byte[]{CARRAIGE_RETURN}));
    
    /**
     * ���ֺ�
     */
    public static final char HYPHEN = '-';
    /**
     * �Ⱥ�
     */
    public static final char ASSIGNMENT = '=';
    /**
     * �ָ���: ����
     */
    public final static byte LINE_FEED = 10;
    /**
     * HTTP Э��ͷ���õı����ʽ
     */
    public static final String HTTP_HEADER_ENCODING = "ISO-8859-1";
    public static final String DEFAULT_CHARACTER_ENCODING = "ISO-8859-1";
    public static final char DOUBLE_QUOTATION = '\"';
    
    public static ByteBuffer colon() {
        return markOrReset(COLON_BUFFER);
    }
    
    public static ByteBuffer cr() {
        return markOrReset(CARRAIGE_RETURN_BUFFER);
    }
    
    public static ByteBuffer crlf() {
        return markOrReset(CRLF_BUFFER);
    }
        
    public static ByteBuffer blank() {
        return markOrReset(BLANK_BUFFER);
    }
    
    public static ByteBuffer markOrReset(ByteBuffer byteBuffer) {
        if (byteBuffer == null) {
            return byteBuffer;
        }
        if (byteBuffer.hasRemaining()) {
            byteBuffer.mark();
        } else {
            byteBuffer.reset();
        }
        return byteBuffer;
    }
}
