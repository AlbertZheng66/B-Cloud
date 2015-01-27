
package com.xt.bcloud.mdu;

import com.xt.core.conv.impl.Ab;

/**
 *
 * @author Albert
 */

public enum AppServerInstanceState {

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
     * �Ѿ�����ͣʹ��
     */
    @Ab("P")
    PAUSED,

    /**
     * �Ѿ�ֹͣ
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
            case PAUSED:
                return "��ͣ";
            case STOPED:
                return "ֹͣ";
        }
        return super.toString();
    }
}


