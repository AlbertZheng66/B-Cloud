
package com.xt.bcloud.bg.impl;

import com.xt.bcloud.bg.Inspectable;
import com.xt.core.db.pm.PersistenceManager;
import com.xt.gt.sys.SystemConfiguration;

/**
 * 当导出文件超过一定域值时(文件过多,或者文件过大)，可使用此“巡查”程序进行删除。
 * @author albert
 */
public class DumperInspector  extends AbstractInspector {

    /**
     * 最多的导出文件个数。
     */
    private final int maxCount = SystemConfiguration.getInstance().readInt("dumper.maxFileCount", 100);

    public void excecute() {
    }

}
