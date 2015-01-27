

package com.xt.bcloud.worker;

import com.xt.bcloud.comm.ServerThread;

/**
 * 此类用于控制服务器的启动,停止.
 * @author albert
 */
public class JettyServerController  implements ServerController {

    private Cattle cattle;

    private String resourceBase;  // 应用的发布路径

    public JettyServerController() {
    }
    
    public void init(Cattle cattle, String resourceBase) {
        this.cattle = cattle;
        this.resourceBase = resourceBase;
    }

    /**
     * 启动一头牛
     * @param cattle
     */
    public void start() {
//        // 服务器暂时用线程方式启动
//        ServerThread st = new ServerThread(cattle.getPort(), cattle.getContextPath(), resourceBase);
//
//        new Thread(st).start();
    }

    public void stop (boolean forcefully) {

    }

    synchronized public void restart() {

    }

}
