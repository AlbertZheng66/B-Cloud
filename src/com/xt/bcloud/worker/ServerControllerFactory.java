
package com.xt.bcloud.worker;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author albert
 */
public class ServerControllerFactory {

    private final static ServerControllerFactory instance = new ServerControllerFactory();

    private final Map<Cattle, ServerController> usingCattles = new HashMap();

    private ServerControllerFactory() {
    }

    public static ServerControllerFactory getInstance() {
        return instance;
    }

    /**
     * 注册一头牛
     * @param cattle
     */
    public synchronized  void register (Cattle cattle, ServerController serverController) {
        if (serverController == null || cattle == null) {
            return;
        }
        this.usingCattles.put(cattle, serverController);
    }

    /**
     * 根据分配的任务找到对应的控制器
     * @param cattle
     * @return
     */
    synchronized public ServerController find(Cattle cattle) {
        if (cattle == null) {
            return null;
        }
        return this.usingCattles.get(cattle);
    }

    synchronized public ServerController find(String cattleId) {
        if (cattleId == null) {
            return null;
        }
        for (Iterator<Cattle> it = usingCattles.keySet().iterator(); it.hasNext();) {
            Cattle cattle = it.next();
            if (cattleId.equals(cattle.getId())) {
                return find(cattle);
            }
        }
        return null;
    }

    /**
     * 是否一头牛
     * @param cattle
     */
    public synchronized  void release (ServerController serverController) {
        if (serverController == null) {
            return;
        }
        this.usingCattles.remove(serverController);
    }

}
