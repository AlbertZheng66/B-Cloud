
package com.xt.bcloud.resource;

/**
 * ������Դ��״̬��
 * @author albert
 */
public enum ResourceState {

    /**
     * ��������δʹ��
     */
    CREATED,  

    /**
     * ��������
     */
    RUNNING,  

    /**
     * ���ɴ������Ϊ������ϻ�������ԭ��
     */
    UNREACHABLE, 

    /**
     * ���ڻ��գ������õķ���������Ϣ����������Щ��Դ����ʹ��
     */
    REVOKING, 

    /**
     * �Ѿ��ջ�
     */
    WITHDRAWED,

    /**
     * �Ѿ�ֹͣ
     */
    STOPED,

}
