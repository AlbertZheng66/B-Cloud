

package com.xt.bcloud.bg;

import com.xt.core.db.pm.PersistenceManager;
import com.xt.core.service.IService;

/**
 * 用于定期“巡查”应用的各项指标是否正常。
 * @author albert
 */
public interface Inspectable extends IService {

    /**
     * 执行检查的动作。
     * 此方法需要自行处理异常。
     * @param persistenceManager
     */
    public void excecute(/*PersistenceManager persistenceManager*/);

}
