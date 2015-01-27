
package com.xt.bcloud.mdu;

import com.xt.core.conv.impl.Ab;

/**
 *
 * @author Albert
 */

public enum AppServerInstanceState {

    /**
     * 可用的（尚未分配实例）
     */
    @Ab("A")
    AVAILABLE,

    /**
     * 正在使用（已经分配实例）
     */
    @Ab("U")
    USING,
    
    /**
     * 已经被暂停使用
     */
    @Ab("P")
    PAUSED,

    /**
     * 已经停止
     */
    @Ab("S")
    STOPED;

     @Override
    public String toString() {
        switch (this) {
            case AVAILABLE:
                return "可用";
            case USING:
                return "在用";
            case PAUSED:
                return "暂停";
            case STOPED:
                return "停止";
        }
        return super.toString();
    }
}


