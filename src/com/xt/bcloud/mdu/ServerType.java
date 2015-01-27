
package com.xt.bcloud.mdu;

import com.xt.core.conv.impl.Ab;

/**
 * ������ǰ�����������͡�
 * @author albert
 */
public enum ServerType {

    /**
     * ���������
     */
    @Ab("TD")
    TASK_DISPATCHER,

    /**
     * Ӧ�÷�����
     */
    @Ab("AS")
    APP_SERVER,
    
    /**
     * ��Ϣ�м��
     */
    @Ab("MS")
    MESSAGE_SERVER,
    
    /**
     * MDU ������
     */
    @Ab("MDU")
    MDU_MANAGER;
    

     @Override
    public String toString() {
        switch (this) {
            case TASK_DISPATCHER:
                return "���������";
            case APP_SERVER:
                return "Ӧ�÷�����";
            case MESSAGE_SERVER:
                return "��Ϣ�м��";
            case MDU_MANAGER:
                return "MDU ������";
        }
        return super.toString();
    }
}



