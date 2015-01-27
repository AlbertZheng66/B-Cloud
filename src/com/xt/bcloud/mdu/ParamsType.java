/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xt.bcloud.mdu;

import com.xt.core.conv.impl.Ab;

/**
 *
 * @author Albert
 */
public enum ParamsType {
    
    /**
     * �ַ���
     */
    @Ab("Str")
    STRING,

    /**
     * �˿�����
     */
    @Ab("Pr")
    PORT_RANGE,

    /**
     * UUID ����
     */
    @Ab("Oid")
    UUID,
    
    /**
     * �Ѿ�����ͣʹ��
     */
    @Ab("Inc")
    INCREMENT,
    
    /**
     * ����ַ���
     */
    @Ab("Rnd")
    RANDOM;

     @Override
    public String toString() {
        switch (this) {
            case STRING:
                return "�ַ���";
            case PORT_RANGE:
                return "�˿�����";
            case RANDOM:
                return "����ַ���";
            case INCREMENT:
                return "����";
            case UUID:
                return "UUID ����";
        }
        return super.toString();
    }
    
}
