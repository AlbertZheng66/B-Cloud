package com.xt.bcloud.bg.impl;

import com.xt.bcloud.app.AppInstanceState;
import com.xt.bcloud.app.AppVersion;
import com.xt.bcloud.app.AppVersionState;
import com.xt.core.log.LogWriter;
import com.xt.core.utils.EnumUtils;
import com.xt.core.utils.SqlUtils;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * 用于检查版本的状态。
 * @author albert
 */
public class AppVersionInspector extends AbstractInspector {

    private final Logger logger = Logger.getLogger(AppVersionInspector.class);

    public void excecute() {
        checkStopedVersion();
    }

    /**
     * 检查版本的状态,如果当前版本为"运行"或者"测试"，但是已无实例运行，则将其更改为停止。
     * 注意：这个检查可能和自动启动实例有冲突。
     */
    private void checkStopedVersion() {
        List<AppVersion> versions = persistenceManager.findAll(AppVersion.class, "STATE IN (?,?)",
                SqlUtils.getParams(EnumUtils.toString(AppVersionState.RUNNING),
                EnumUtils.toString(AppVersionState.TESTING)), null);
        for (Iterator<AppVersion> it = versions.iterator(); it.hasNext();) {
            AppVersion appVersion = it.next();
            int count = persistenceManager.queryInt("SELECT COUNT(1) FROM APP_INSTANCE WHERE STATE=? AND APP_VERSION_OID=?",
                    SqlUtils.getParams(EnumUtils.toString(AppInstanceState.RUNNING), appVersion.getOid()));
            if (count < 1) {
                LogWriter.warn2(logger, "版本[%s]已无正在运行的实例，系统将其状态设置为“停止”。", appVersion);
                appVersion.setState(AppVersionState.STOPED);
                persistenceManager.update(appVersion);
            }
        }
    }
}
