/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.xt.gt.demo.po;

import com.xt.core.conv.impl.Ab;

/**
 *
 * @author albert
 */
public enum AddressType {
    /**
     * 办公地址
     */
    @Ab(value="O")
    OFFICE,

    /**
     * 家庭地址
     */
    @Ab(value="H")
    HOME,

    /**
     * 不确定的地址
     */
    @Ab(value="N")
    NONE

}
