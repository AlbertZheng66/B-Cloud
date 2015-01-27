package com.xt.bcloud.td7;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 *
 * @author Albert
 */
public class Buffers {

    /**
     * �����ÿ��Buffer������
     */
    private int capacity = 1024;
    
    private ByteBuffer[] buffers = new ByteBuffer[0];

    public Buffers() {
    }

    public void put(byte[] bytes, int index, int count) {
        if (buffers.length == 0) {
            extend();
        }
        ByteBuffer last = buffers[buffers.length - 1];
        // �����Ҫ�������������е�����������չ����
        int len = count;
        int remaining = last.remaining();
        while (len > remaining) {
            last.put(bytes, index, remaining);  // ����ǰ���һ�������
            last = extend();
            index += remaining;
            len -= remaining;
        }
        if (len > 0) {
            last.put(bytes, index, len);
        }
    }

    /**
     * ������Buffer����Ϊ���ɶ���״̬
     */
    public void flip() {
        for (int i = 0; i < buffers.length; i++) {
            ByteBuffer byteBuffer = buffers[i];
            byteBuffer.flip();
        }
    }

    public ByteBuffer[] getBuffers() {
        return buffers;
    }
    
    public long getLimit() {
        long limit = 0;
        for (int i = 0; i < buffers.length; i++) {
            ByteBuffer byteBuffer = buffers[i];
            limit += byteBuffer.limit();
        }
        return limit;
    }
    
    public byte[] toBytes() {
        long limit = getLimit();
        // FIXME: �����Ϣ����4G�����ܳ������⡣
        byte[] bytes = new byte[(int)limit];
        int index = 0;
        for (int i = 0; i < buffers.length; i++) {
            ByteBuffer byteBuffer = buffers[i];
            byte[] _b = byteBuffer.array();
            System.arraycopy(_b, 0, bytes, index, _b.length);
            index += _b.length;
        }
        return bytes;
    }

    private ByteBuffer extend() {
        return extend0(BufferFactory.getInstance().allocate(capacity));
    }

    private ByteBuffer extend0(ByteBuffer buffer) {
        ByteBuffer[] newBuffers = new ByteBuffer[buffers.length + 1];
        System.arraycopy(buffers, 0, newBuffers, 0, buffers.length);
        newBuffers[buffers.length] = buffer;
        buffers = newBuffers;
        return buffer;
    }
    
    public void print(OutputStream os) throws IOException {
        for (int i = 0; i < buffers.length; i++) {
            ByteBuffer byteBuffer = buffers[i];
            os.write(byteBuffer.array());
        }
    }

    /**
     * �ͷŵ����е�Buffer
     */
    public void dispose() {
        for (int i = 0; i < buffers.length; i++) {
            ByteBuffer byteBuffer = buffers[i];
            BufferFactory.getInstance().dispose(byteBuffer);
        }
        buffers = new ByteBuffer[0];
    }

    public void append(ByteBuffer buffer) {
        if (buffer != null) {
            extend0(buffer);
        }
    }

    public void setBuffers(ByteBuffer[] buffers) {
        this.buffers = buffers;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}
