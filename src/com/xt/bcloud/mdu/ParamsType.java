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
     * 字符串
     */
    @Ab("Str")
    STRING,

    /**
     * 端口区间
     */
    @Ab("Pr")
    PORT_RANGE,

    /**
     * UUID 编码
     */
    @Ab("Oid")
    UUID,
    
    /**
     * 已经被暂停使用
     */
    @Ab("Inc")
    INCREMENT,
    
    /**
     * 随机字符串
     */
    @Ab("Rnd")
    RANDOM;

     @Override
    public String toString() {
        switch (this) {
            case STRING:
                return "字符串";
            case PORT_RANGE:
                return "端口区间";
            case RANDOM:
                return "随机字符串";
            case INCREMENT:
                return "增量";
            case UUID:
                return "UUID 编码";
        }
        return super.toString();
    }
    
}
