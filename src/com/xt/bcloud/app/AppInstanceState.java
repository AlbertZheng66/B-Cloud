

package com.xt.bcloud.app;

import com.xt.core.conv.impl.Ab;

/**
 * ��ʶ�����е�ʵ����״̬��
 * @author albert
 */

public enum AppInstanceState {

    /**
     * �������С�
     */
    @Ab("R")
    RUNNING,

//    /**
//     * �˰汾������ʧЧ״̬������̨�����Ѿ������䲻���ã���
//     */
//    @Ab("I")
//    INVALID,

    /**
     * ��ʵ���Ѿ�ֹͣ�ṩ����
     */
    @Ab("S")
    STOPED,
            ;

    @Override
    public String toString() {
        switch (this) {
            case RUNNING:
                return "����";
            case STOPED:
                return "ֹͣ";
        }
        return super.toString();
    }

}
