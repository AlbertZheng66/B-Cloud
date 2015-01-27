
package com.xt.bcloud.resource;

/**
 * 描述资源的状态。
 * @author albert
 */
public enum ResourceState {

    /**
     * 创建但是未使用
     */
    CREATED,  

    /**
     * 正在运行
     */
    RUNNING,  

    /**
     * 不可达（可能因为网络故障或者其他原因）
     */
    UNREACHABLE, 

    /**
     * 正在回收（向在用的发出回收消息），但是有些资源正在使用
     */
    REVOKING, 

    /**
     * 已经收回
     */
    WITHDRAWED,

    /**
     * 已经停止
     */
    STOPED,

}
