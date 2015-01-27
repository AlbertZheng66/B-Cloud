package com.xt.bcloud.resource;

import com.xt.core.app.init.SystemLifecycle;
import com.xt.core.log.LogWriter;
import org.apache.log4j.Logger;

/**
 * 服务器注册程序，用于在应用启动时，自动向资源管理注册自身服务，并在服务器关闭时自动注销。
 *
 * @author albert
 */
public class ServerRegister implements SystemLifecycle {

    private final Logger logger = Logger.getLogger(ServerRegister.class);
    
    public ServerRegister() {
    }
    
    public void onInit() {
        try {
            ServerManager.getInstance().register(null);
        } catch (Throwable t) {
            LogWriter.warn2(logger, t, "注册失败。");
        }
    }

    public void onDestroy() {
        ServerManager.getInstance().unregister();
    }
}
