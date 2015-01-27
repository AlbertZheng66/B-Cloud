
package com.xt.bcloud.td7;

import java.nio.ByteBuffer;

/**
 * ��׼���ݵ����ӿڣ����ڵ���Http������Ӧ��������ϵͳ�����쳣ʱ��
 * @author Albert
 */
public interface Dumpable {
    
    public void setFilenamePattern(String fileNamePattern);
    
    public void open();
    
    /**
     * ����ǰ���ֽ�����д��Dumper�ļ�.
     * @param b
     */
    public void write(ByteBuffer[] b);
    
    /**
     * ����ǰ���ֽ�����д��Dumper�ļ�.
     * @param b
     */
    public void write(ByteBuffer b);

    /**
     * ��ǰ������������߳�ʱ.
     */
    public void close();
}
