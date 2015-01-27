
package com.xt.bcloud.bg.impl;

import com.xt.bcloud.app.App;
import com.xt.bcloud.app.AppState;
import com.xt.bcloud.app.AppVersion;
import com.xt.bcloud.app.AppVersionState;
import com.xt.core.db.pm.PersistenceException;
import com.xt.core.log.LogWriter;
import com.xt.core.utils.EnumUtils;
import com.xt.core.utils.SqlUtils;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * 应用程序状态检查器，主要负责如下任务：
 * 1. 如果当前应用有应用实例处于“运行”状态，则将其状态设置为“运行”；
 * 2. 如果当前应用应用实例无处于“运行”，有处于“测试”状态，则将其状态设置为“测试”；
 * 3. 既无“运行”，又无“测试”状态，则设置为“停止”状态。
 * @author albert
 */
public class AppStateInspector extends AbstractInspector {

    private final Logger logger = Logger.getLogger(AppStateInspector.class);

    public void excecute() {
        checkStopedApp();
    }

    /**
     * 如果当前应用已无版本处于“运行”或者“测试”状态，则将其状态设置为“停止”。
     */
    private void checkStopedApp() {
//        List<App> runningApps = persistenceManager.findAll(App.class, "STATE = ?",
//                SqlUtils.getParams(EnumUtils.toString(AppState.RUNNING)), null);
        List<App> runningApps = persistenceManager.findAll(App.class, "valid = ?",
                SqlUtils.getParams("y"), null);
        for (Iterator<App> it = runningApps.iterator(); it.hasNext();) {
            App app = it.next();
            // 当前正在运行的实例
            long runningCount = persistenceManager.count(AppVersion.class, "STATE IN (?) AND APP_OID=?",
                SqlUtils.getParams(EnumUtils.toString(AppVersionState.RUNNING), app.getOid()));
            if (runningCount > 0) {
                if (app.getState() != AppState.RUNNING) {
                    changeState(app, AppState.RUNNING);
                }
                continue;
            }
            // 检查当前应用是否处于测试状态
            long testingCount = persistenceManager.count(AppVersion.class, "STATE IN (?) AND APP_OID=?",
                SqlUtils.getParams(EnumUtils.toString(AppVersionState.TESTING), app.getOid()));
            if (testingCount > 0) {
                if (app.getState() != AppState.TESTING) {
                    changeState(app, AppState.TESTING);
                }
                continue;
            }
            if (app.getState() != AppState.STOPED) {
                LogWriter.info2(logger, "应用[%s]已无处于运行和测试状态的实例，将其设置为“停止”状态。", app);
                changeState(app, AppState.STOPED);
            }
        }
    }

    private void changeState(App app, AppState state) throws PersistenceException {
        app.setState(state);
        persistenceManager.update(app);
    }

}
