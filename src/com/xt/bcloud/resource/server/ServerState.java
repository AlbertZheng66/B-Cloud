package com.xt.bcloud.resource.server;

import com.xt.core.conv.impl.Ab;

/**
 * 描述当前服务器的状态。
 * @author albert
 */
public enum ServerState {

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
     * 系统资源已经超负荷运作
     */
    @Ab("O")
    OVERUSED,
    /**
     * 已经被暂停
     */
    @Ab("P")
    PAUSED,

    /**
     * 已经停止使用
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
            case OVERUSED:
                return "超负荷";
            case PAUSED:
                return "暂停";
            case STOPED:
                return "停止";
        }
        return super.toString();
    }
}
