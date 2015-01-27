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
 * ���ڼ��汾��״̬��
 * @author albert
 */
public class AppVersionInspector extends AbstractInspector {

    private final Logger logger = Logger.getLogger(AppVersionInspector.class);

    public void excecute() {
        checkStopedVersion();
    }

    /**
     * ���汾��״̬,�����ǰ�汾Ϊ"����"����"����"����������ʵ�����У��������Ϊֹͣ��
     * ע�⣺��������ܺ��Զ�����ʵ���г�ͻ��
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
                LogWriter.warn2(logger, "�汾[%s]�����������е�ʵ����ϵͳ����״̬����Ϊ��ֹͣ����", appVersion);
                appVersion.setState(AppVersionState.STOPED);
                persistenceManager.update(appVersion);
            }
        }
    }
}
