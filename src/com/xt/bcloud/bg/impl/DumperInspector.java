
package com.xt.bcloud.bg.impl;

import com.xt.bcloud.bg.Inspectable;
import com.xt.core.db.pm.PersistenceManager;
import com.xt.gt.sys.SystemConfiguration;

/**
 * �������ļ�����һ����ֵʱ(�ļ�����,�����ļ�����)����ʹ�ôˡ�Ѳ�顱�������ɾ����
 * @author albert
 */
public class DumperInspector  extends AbstractInspector {

    /**
     * ���ĵ����ļ�������
     */
    private final int maxCount = SystemConfiguration.getInstance().readInt("dumper.maxFileCount", 100);

    public void excecute() {
    }

}
