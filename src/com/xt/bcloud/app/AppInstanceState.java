

package com.xt.bcloud.app;

import com.xt.core.conv.impl.Ab;

/**
 * 标识了运行的实例的状态。
 * @author albert
 */

public enum AppInstanceState {

    /**
     * 正在运行。
     */
    @Ab("R")
    RUNNING,

//    /**
//     * 此版本正处于失效状态，（后台进程已经怀疑其不可用）。
//     */
//    @Ab("I")
//    INVALID,

    /**
     * 此实例已经停止提供服务。
     */
    @Ab("S")
    STOPED,
            ;

    @Override
    public String toString() {
        switch (this) {
            case RUNNING:
                return "运行";
            case STOPED:
                return "停止";
        }
        return super.toString();
    }

}
