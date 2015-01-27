

package com.xt.bcloud.app;

import com.xt.core.db.pm.IPOPersistenceManager;
import com.xt.core.proc.impl.IPOPersistenceAware;
import com.xt.core.service.IService;
import com.xt.core.service.LocalMethod;
import com.xt.core.utils.SqlUtils;
import java.util.List;

/**
 *
 * @author albert
 */
public class AppHostService  implements IService, IPOPersistenceAware {

     /**
     * 持久化文件实例
     */
    private transient IPOPersistenceManager persistenceManager;

    public AppHostService() {
    }

    public List<AppHost> list(App app) {
        if (app == null || app.getOid() == null) {
            throw new AppException("应用及其编码不能为空。");
        }
        return persistenceManager.findAll(AppHost.class, "APP_OID=?", SqlUtils.getParams(app.getOid()), null);
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
