
package com.xt.bcloud.app;

/**
 * 这个枚举类用于定义应用的升级模式。
 * @author albert
 */
public enum UpdateMode {

    /**
     * 优雅的升级，即将虚拟服务器逐个升级，并保证已登录用户平滑的切换到新系统。
     * 这种方式要求新老系统在方法调用的接口上保持一致。
     */
    GRACEFUL,

    /**
     * 逐步的, 即已经登录的用户将使用原有系统; 新登录用户使用新系统进行操作,
     * 在一定时间之后, 由管理员负责关闭老系统.应用管理器将申请多个新的应用实例.
     * 这种方式在新老系统不完全一致时使用此方法.
     */
    PROGRESSIVE,

    /**
     * 休克法, 即将所有请求都停止执行, 并关闭老版本的服务器（同时清除其Session信息）,
     * 用户再次发起的请求都将直接访问
     * 新版本. 这种方式一般用于老系统发生了严重的问题,不宜继续使用的情况下.
     */
    SHOCKED,

    /**
     * 自定义的升级方式，即由用户来确定升级的算法，比如：按地区或者所在单位逐步升级。
     */
    CUSTOMIZED;

    @Override
    public String toString() {
        switch(this) {
            case GRACEFUL:
                return "优雅";
            case PROGRESSIVE:
                return "逐步";
            case SHOCKED:
                return "休克";
            case CUSTOMIZED:
                return "自定义";
        }
        return super.toString();
    }
}
