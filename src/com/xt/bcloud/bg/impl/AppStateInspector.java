
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
 * Ӧ�ó���״̬���������Ҫ������������
 * 1. �����ǰӦ����Ӧ��ʵ�����ڡ����С�״̬������״̬����Ϊ�����С���
 * 2. �����ǰӦ��Ӧ��ʵ���޴��ڡ����С����д��ڡ����ԡ�״̬������״̬����Ϊ�����ԡ���
 * 3. ���ޡ����С������ޡ����ԡ�״̬��������Ϊ��ֹͣ��״̬��
 * @author albert
 */
public class AppStateInspector extends AbstractInspector {

    private final Logger logger = Logger.getLogger(AppStateInspector.class);

    public void excecute() {
        checkStopedApp();
    }

    /**
     * �����ǰӦ�����ް汾���ڡ����С����ߡ����ԡ�״̬������״̬����Ϊ��ֹͣ����
     */
    private void checkStopedApp() {
//        List<App> runningApps = persistenceManager.findAll(App.class, "STATE = ?",
//                SqlUtils.getParams(EnumUtils.toString(AppState.RUNNING)), null);
        List<App> runningApps = persistenceManager.findAll(App.class, "valid = ?",
                SqlUtils.getParams("y"), null);
        for (Iterator<App> it = runningApps.iterator(); it.hasNext();) {
            App app = it.next();
            // ��ǰ�������е�ʵ��
            long runningCount = persistenceManager.count(AppVersion.class, "STATE IN (?) AND APP_OID=?",
                SqlUtils.getParams(EnumUtils.toString(AppVersionState.RUNNING), app.getOid()));
            if (runningCount > 0) {
                if (app.getState() != AppState.RUNNING) {
                    changeState(app, AppState.RUNNING);
                }
                continue;
            }
            // ��鵱ǰӦ���Ƿ��ڲ���״̬
            long testingCount = persistenceManager.count(AppVersion.class, "STATE IN (?) AND APP_OID=?",
                SqlUtils.getParams(EnumUtils.toString(AppVersionState.TESTING), app.getOid()));
            if (testingCount > 0) {
                if (app.getState() != AppState.TESTING) {
                    changeState(app, AppState.TESTING);
                }
                continue;
            }
            if (app.getState() != AppState.STOPED) {
                LogWriter.info2(logger, "Ӧ��[%s]���޴������кͲ���״̬��ʵ������������Ϊ��ֹͣ��״̬��", app);
                changeState(app, AppState.STOPED);
            }
        }
    }

    private void changeState(App app, AppState state) throws PersistenceException {
        app.setState(state);
        persistenceManager.update(app);
    }

}
