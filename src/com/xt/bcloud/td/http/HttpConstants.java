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
     * 空格
     */
    public final static byte BLANK = ' ';
    
    private final static ByteBuffer BLANK_BUFFER = BufferFactory.getInstance().wrap((new byte[]{BLANK}));
    
    /**
     * 冒号
     */
    public final static byte COLON = ':';
    /**
     * Buffer 形式的冒号
     */
    private final static ByteBuffer COLON_BUFFER = BufferFactory.getInstance().wrap((new byte[]{COLON}));
    /**
     * 分号
     */
    public final static byte SEMI_COLON = ';';
    /**
     * 分隔符: 回车
     */
    public final static byte CARRAIGE_RETURN = 13;
    
     /**
     * 分隔符: 回车
     */
    public final static ByteBuffer CARRAIGE_RETURN_BUFFER  = BufferFactory.getInstance().wrap((new byte[]{CARRAIGE_RETURN}));
    
    /**
     * 连字号
     */
    public static final char HYPHEN = '-';
    /**
     * 等号
     */
    public static final char ASSIGNMENT = '=';
    /**
     * 分隔符: 换行
     */
    public final static byte LINE_FEED = 10;
    /**
     * HTTP 协议头采用的编码格式
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
