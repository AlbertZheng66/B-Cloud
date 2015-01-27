

package com.xt.bcloud.resource;

import com.xt.bcloud.worker.Cattle;
import com.xt.core.db.pm.IPOPersistenceManager;
import org.jdom.Element;

/**
 * 在此服务中定义用户可用的服务。
 * @author albert
 */
public interface ServiceProvider {

    /**
     * 创建一个和提供服务相关的配置文件。
     */
    public void createConf(Element root, Cattle cattle,
            IPOPersistenceManager persistenceManager);

}
