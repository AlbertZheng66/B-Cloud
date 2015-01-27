
package com.xt.bcloud.worker;

/**
 *
 * @author albert
 */
public enum CattleState {
    NONE,      // 未定义

    STARTING,  // 正在启动

    IDLE,     // 空闲

    WORDING,  // 正在处理工作

    STOPING,  // 正在停止工作

    STOPED,   // 已经停止工作
}
