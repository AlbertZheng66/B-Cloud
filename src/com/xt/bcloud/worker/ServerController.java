
package com.xt.bcloud.worker;

/**
 *
 * @author albert
 */
public interface ServerController {

    /**
     * ��ʼ�������ӿڡ�
     * @param cattle
     * @param resourceBase
     */
    public void init(Cattle cattle, String resourceBase);

     /**
     * �����������ӿڡ�
     * @param cattle
     */
    public void start();

    /**
     * ֹͣ�������ӿ�
     * @param forcefully �Ƿ�ǿ���˳�������ֹ��ǰ���ڴ���ҵ���߼�����
     */
    public void stop (boolean forcefully);

    /**
     * ����������������
     */
    public void restart();

}
