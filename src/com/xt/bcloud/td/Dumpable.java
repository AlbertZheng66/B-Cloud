
package com.xt.bcloud.td;

/**
 * �������ԭʼ���������Ӧ��ԭʼ�ӿڡ�
 * @author albert
 */
public interface Dumpable {

//    /**
//     * �����Ƿ����� ��
//     * @param enable
//     */
//    public void setEnable(boolean enable);

    /**
     * ����ǰ���ֽ�����(��������)д��Dumper�ļ�.
     * @param b
     */
    public void writReq(byte[] b);

    /**
     * ��ǰ������������߳�ʱ.
     */
    public void closeReq();

    /**
     * ����ǰ���ֽ����飨������Ӧ��д��Dumper�ļ�.
     * @param b
     */
    public void writRes(byte[] b);

    /**
     * ��ǰ����Ӧ�������߳�ʱ.
     */
    public void closeRes();

}
