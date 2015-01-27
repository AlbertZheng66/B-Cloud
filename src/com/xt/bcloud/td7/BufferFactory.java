
package com.xt.bcloud.td7;

import java.nio.ByteBuffer;

/**
 * 用户统一控制资源（Buffer）分配和回收的工厂类。
 * @author Albert
 */
public class BufferFactory {
    /**
     * 缺省的头缓冲区的大小
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
