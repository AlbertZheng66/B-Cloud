
package com.xt.bcloud.worker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 用于管理和查找当前已经注册的实例。
 * @author albert
 */
public class RanchManager {

    private final static RanchManager instance = new RanchManager();

    private List<Ranch> registeredRanchs = new ArrayList();

    private RanchManager() {

    }

    static public RanchManager getInstance() {
        return instance;
    }

    synchronized  public void register(Ranch ranch) {
        if (ranch != null && !registeredRanchs.contains(ranch)) {
            registeredRanchs.add(ranch);
        }
    }

    synchronized  public void unregister(Ranch ranch) {
        if (ranch != null && registeredRanchs.contains(ranch)) {
            registeredRanchs.remove(ranch);
        }
    }

    synchronized  public Ranch find(String cattleId) {
        if (cattleId == null) {
            return null;
        }
        for (Iterator<Ranch> it = registeredRanchs.iterator(); it.hasNext();) {
            Ranch ranch = it.next();
            if (ranch.getManagedCattle() != null
                    && cattleId.equals(ranch.getManagedCattle().getId())) {
                return ranch;
            }
        }
        return null;
    }

}
