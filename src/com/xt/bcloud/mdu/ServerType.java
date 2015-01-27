
package com.xt.bcloud.mdu;

import com.xt.core.conv.impl.Ab;

/**
 * 描述当前服务器的类型。
 * @author albert
 */
public enum ServerType {

    /**
     * 任务分派器
     */
    @Ab("TD")
    TASK_DISPATCHER,

    /**
     * 应用服务器
     */
    @Ab("AS")
    APP_SERVER,
    
    /**
     * 消息中间件
     */
    @Ab("MS")
    MESSAGE_SERVER,
    
    /**
     * MDU 管理器
     */
    @Ab("MDU")
    MDU_MANAGER;
    

     @Override
    public String toString() {
        switch (this) {
            case TASK_DISPATCHER:
                return "任务分派器";
            case APP_SERVER:
                return "应用服务器";
            case MESSAGE_SERVER:
                return "消息中间件";
            case MDU_MANAGER:
                return "MDU 管理器";
        }
        return super.toString();
    }
}



