package com.xt.bcloud.app;

import com.xt.core.conv.impl.Ab;

/**
 * 描述一个应用版本的状态。
 * @author albert
 */
public enum AppVersionState {

    /**
     * 已经注册，但是还没有发布的版本
     */
    @Ab("RE")
    REGISTERED,

    /**
     * 正在运行(且至少有一个版本正在运行，如果系统只有一个版本在运行，且处于测试状态，
     * 此时应用的状态为“注册”或者“停止”状态)。
     */
    @Ab("R")
    RUNNING,

    /**
     * 此版本正处于测试阶段运行，只对某些“特殊用户”
     * （注：应通过 URL 进行标识或者“Cookie”进行标识）提供服务。
     * 这种情况适用于为“程序员”提供发布验证的阶段。
     */
    @Ab("T")
    TESTING,

    /**
     * 此版本正在升级的过程当中（应用可正常访问，不会对用户造成影响）。
     * TODO: 这个状态是否有问题，需要仔细考虑！！
     */
    @Ab("U")
    UPGRADING,

    /**
     * 处于暂停状态（注意：此时属于此应用的所有实例仍在运行）。
     * 可以将整个应用设置为暂停状态，也可能是因为应用版本的运行
     * 状态而影响应用的状态（比如：一个应用只有一个当前运行的版本，
     * 且只有一个实例，如果升级此应用版本，则整个应用处于暂停状态（这种方式有待仔细考虑）。）
     */
    @Ab("P")
    PAUSED,

    /**
     * 此应用已经停止提供服务，系统将停止此应用的所有运行实例。
     */
    @Ab("S")
    STOPED;
    
//    /**
//     * 此应用已经被移除，系统将删除此应有的所有相关配置。
//     */
//    @Ab("RM")
//    REMOVED;

    @Override
    public String toString() {
        switch (this) {
            case REGISTERED:
                return "注册";
            case RUNNING:
                return "运行";
            case PAUSED:
                return "暂停";
            case STOPED:
                return "停止";
//            case REMOVED:
//                return "移除";
            case TESTING:
                return "测试";
            case UPGRADING:
                return "升级";
        }
        return super.toString();
    }
}
