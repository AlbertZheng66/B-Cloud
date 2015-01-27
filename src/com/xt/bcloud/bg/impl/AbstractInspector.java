
package com.xt.bcloud.bg.impl;

import com.xt.bcloud.bg.Inspectable;
import com.xt.core.db.pm.IPOPersistenceManager;
import com.xt.core.proc.impl.IPOPersistenceAware;
import com.xt.core.service.LocalMethod;

/**
 *
 * @author albert
 */
abstract public class AbstractInspector implements Inspectable, IPOPersistenceAware{


    /**
     * 持久化文件实例
     */
    protected transient IPOPersistenceManager persistenceManager;

    public AbstractInspector() {
    }


    @LocalMethod
    public void setPersistenceManager(IPOPersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    @LocalMethod
    public IPOPersistenceManager getPersistenceManager() {
        return persistenceManager;
    }

}
