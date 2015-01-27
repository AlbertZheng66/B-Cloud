

package com.xt.bcloud.resource;

import com.xt.bcloud.worker.Cattle;
import com.xt.core.db.pm.IPOPersistenceManager;
import org.jdom.Element;

/**
 * �ڴ˷����ж����û����õķ���
 * @author albert
 */
public interface ServiceProvider {

    /**
     * ����һ�����ṩ������ص������ļ���
     */
    public void createConf(Element root, Cattle cattle,
            IPOPersistenceManager persistenceManager);

}
