
package com.xt.bcloud.worker;

/**
 * 向任务分配器注册消息或者传递负载时，定义的操作类型。
 * @author albert
 */
public enum Operator {

    /**
     * 注册一头牛
     */
    REGISTER,

    /**
     * 当前消息由协调人发出。
     */
    COORDINATOR,

    /**
     * 将一头牛设置为不可用
     */
    SUSPECT,

    /**
     * 取消一头牛
     */
    UNREGISTER,

    /**
     * 负载信息(单个任务管理器的负载信息)
     */
    LOAD,

    /**
     * 由协调者发送的整体负载信息。
     */
    RANCH_LOAD,

    /**
     * 将一个应用标识为“停止服务”
     */
    APP_STOP,

    /**
     * 将一个应用标识为“暂停服务”
     */
    APP_PAUSE,

    /**
     * 将一个已经暂停的应用标识为“可用”
     */
    APP_RESTART,

    /**
     * 设置应用的默认版本。
     */
    APP_SET_DEFAULT_VERSION,

    /**
     * 当任务管理器启动时，或者丢失了所有的“服务器实例”的信息，可以使用此消息要求同步。
     * 当“服务器实例管理程序（Ranch）”收到此消息时，将会发回本地的负载消息。
     */
    TASK_SYN,
}
