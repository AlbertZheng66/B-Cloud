

package com.xt.bcloud.bg;

import com.xt.core.db.pm.PersistenceManager;
import com.xt.core.service.IService;

/**
 * ���ڶ��ڡ�Ѳ�顱Ӧ�õĸ���ָ���Ƿ�������
 * @author albert
 */
public interface Inspectable extends IService {

    /**
     * ִ�м��Ķ�����
     * �˷�����Ҫ���д����쳣��
     * @param persistenceManager
     */
    public void excecute(/*PersistenceManager persistenceManager*/);

}
