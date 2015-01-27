
package com.xt.bcloud.worker;

/**
 *
 * @author albert
 */
public interface ServerController {

    /**
     * 初始服务器接口。
     * @param cattle
     * @param resourceBase
     */
    public void init(Cattle cattle, String resourceBase);

     /**
     * 启动服务器接口。
     * @param cattle
     */
    public void start();

    /**
     * 停止服务器接口
     * @param forcefully 是否强制退出（即终止当前正在处理业务逻辑）。
     */
    public void stop (boolean forcefully);

    /**
     * 重新启动服务器。
     */
    public void restart();

}
