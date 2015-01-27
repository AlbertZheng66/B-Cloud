
package com.xt.bcloud.td7;

import java.io.IOException;
import java.nio.ByteBuffer;


/**
 *
 * @author Albert
 */
public interface Parser {
    
//    /**
//     * ͷ�Ƿ��������
//     * @return 
//     */
//    public boolean isHeaderParsed(Buffers buffers);
    
    public Message parse(ByteBuffer buffer) throws IOException;
    
    /**
     * ��������EOF��ʱ��ǿ��ֹͣ���������ܽ����Ƿ����
     */
    public void end();
    
    /**
     * ��ǰ��Ϣ�Ƿ��������
     * @return 
     */
    public boolean finished();
    
    /**
     * �������һ����������Ϣ
     * @return 
     */
     public Message getLastMessage();
    
}
