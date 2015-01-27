package com.xt.bcloud.resource.server;

import com.xt.core.conv.impl.Ab;

/**
 * ������ǰ��������״̬��
 * @author albert
 */
public enum ServerState {

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
    /**
     * ϵͳ��Դ�Ѿ�����������
     */
    @Ab("O")
    OVERUSED,
    /**
     * �Ѿ�����ͣ
     */
    @Ab("P")
    PAUSED,

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
            case PAUSED:
                return "��ͣ";
            case STOPED:
                return "ֹͣ";
        }
        return super.toString();
    }
}
