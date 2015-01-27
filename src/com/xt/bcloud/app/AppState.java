

package com.xt.bcloud.app;

import com.xt.core.conv.impl.Ab;

/**
 * 描述一个应用的状态。
 * @author albert
 */
public enum AppState {
//    /**
//     * 尚未注册的程序(只是一个表示状态，在程序中不应出现)
//     */
//    NONE,        

    /**
     * 已经注册，但是还没有发布程序版本
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
     * 处于暂停状态（注意：此时属于此应用的所有实例仍在运行）。
     * 可以将整个应用设置为暂停状态，也可能是因为应用版本的运行
     * 状态而影响应用的状态（比如：一个应用只有一个当前运行的版本，
     * 且只有一个实例，如果升级此应用版本，则整个应用处于暂停状态（这种方式有待仔细考虑）。）
     */
    @Ab("P")
    PAUSED,

//   /**
//     * 将处于暂停状态的实例设置为“运行”状态。
//     */
//    @Ab("RS")
//    RESUMED,

    /**
     * 此应用已经停止提供服务，系统将停止此应用的所有运行实例。
     */
    @Ab("S")
    STOPED,
    
    /**
     * 应用处于测试状态
     */
    @Ab("T")
    TESTING,

    /**
     * 此应用已经被移除，系统将删除此应有的所有相关配置。
     */
    @Ab("RM")
    REMOVED;

    @Override
    public String toString() {
        switch (this) {
            case REGISTERED:
                return "注册";
            case RUNNING:
                return "运行";
            case PAUSED:
                return "暂停";
            case TESTING:
                return "测试";
            case STOPED:
                return "停止";
            case REMOVED:
                return "移除";
        }
        return super.toString();
    }
}

