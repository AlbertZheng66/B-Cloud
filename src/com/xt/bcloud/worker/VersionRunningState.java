
package com.xt.bcloud.worker;

/**
 * 当前版本的运行状态，任务管理器可以根据此状态决定将请求发给哪个版本的服务器。
 * @author albert
 */
public enum VersionRunningState {

    /**
     * 当前的缺省版本，如果请求未定义版本，则默认使用此版本。
     */
    DEFAULT,

    /**
     * 在用的，表示与缺省版本共同存在的版本。
     */
    USING,

    /**
     * 不再使用的, 表示此版本不再使用，当前应用的Session全部结束后，
     * 将停止此版本的运行。
     */
    UNUSED,

}
