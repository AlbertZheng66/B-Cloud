
package com.xt.bcloud.mdu;

import com.xt.core.conv.impl.Ab;

/**
 * ������ǰ�����������״̬��
 * @author albert
 */
public enum PhyServerState {

    /**
     * ���õģ���δ����ʵ����
     */
    @Ab("A")
    AVAILABLE,

    /**
     * ����ʹ�ã��Ѿ�����ʵ����
     */
    @Ab("U")
    USING,
    
//    /**
//     * ��ǰ�в��ɷ��ʣ�����ȷ���Ƿ�ʧЧ��
//     */
//    @Ab("D")
//    UNREACHABLE,
    
    /**
     * ϵͳ��Դ�Ѿ�����������
     */
    @Ab("O")
    OVERUSED,
    
//    /**
//     * �Ѿ�����ͣ
//     */
//    @Ab("P")
//    PAUSED,

    /**
     * �Ѿ�ֹͣʹ��
     */
    @Ab("S")
    STOPED;

     @Override
    public String toString() {
        switch (this) {
            case AVAILABLE:
                return "����";
            case USING:
                return "����";
            case OVERUSED:
                return "������";
//            case PAUSED:
//                return "��ͣ";
            case STOPED:
                return "ֹͣ";
        }
        return super.toString();
    }
}

