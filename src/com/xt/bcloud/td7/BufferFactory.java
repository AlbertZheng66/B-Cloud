
package com.xt.bcloud.td7;

import java.nio.ByteBuffer;

/**
 * �û�ͳһ������Դ��Buffer������ͻ��յĹ����ࡣ
 * @author Albert
 */
public class BufferFactory {
    /**
     * ȱʡ��ͷ�������Ĵ�С
     */
    public static final int DEFAULT_HEAD_BUFFER_SIZE = 1024;
    
    private static BufferFactory instance = new BufferFactory();
    
    private BufferFactory() {
        
    }
    
    static public BufferFactory getInstance() {
        return instance;
    }
    
    public ByteBuffer allocate(int capacity) {
        return ByteBuffer.allocate(capacity);
    }
    
    
    public ByteBuffer allocate() {
        return allocate(DEFAULT_HEAD_BUFFER_SIZE);
    }
    
    public ByteBuffer wrap(byte[] bytes) {
        return ByteBuffer.wrap(bytes);
    }
    
    public void dispose(ByteBuffer byteBuffer) {
        byteBuffer.clear();
    }
    
}
