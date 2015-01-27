
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
     * ע��һͷţ
     * @param cattle
     */
    public synchronized  void register (Cattle cattle, ServerController serverController) {
        if (serverController == null || cattle == null) {
            return;
        }
        this.usingCattles.put(cattle, serverController);
    }

    /**
     * ���ݷ���������ҵ���Ӧ�Ŀ�����
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
     * �Ƿ�һͷţ
     * @param cattle
     */
    public synchronized  void release (ServerController serverController) {
        if (serverController == null) {
            return;
        }
        this.usingCattles.remove(serverController);
    }

}
