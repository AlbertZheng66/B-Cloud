
package com.xt.bcloud.td7;

import java.io.IOException;
import java.nio.ByteBuffer;


/**
 *
 * @author Albert
 */
public interface Parser {
    
//    /**
//     * 头是否解析结束
//     * @return 
//     */
//    public boolean isHeaderParsed(Buffers buffers);
    
    public Message parse(ByteBuffer buffer) throws IOException;
    
    /**
     * 当读到“EOF”时，强制停止解析，不管解析是否结束
     */
    public void end();
    
    /**
     * 当前消息是否解析结束
     * @return 
     */
    public boolean finished();
    
    /**
     * 返回最后一个解析的消息
     * @return 
     */
     public Message getLastMessage();
    
}
