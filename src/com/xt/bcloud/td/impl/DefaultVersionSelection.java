

package com.xt.bcloud.td.impl;

import com.xt.bcloud.app.AppVersion;
import com.xt.bcloud.app.AppVersionState;
import com.xt.bcloud.td.VersionSelectable;
import com.xt.bcloud.td7.CookieReader;
import java.util.Iterator;
import java.util.Set;

/**
 * ȱʡ�İ汾ѡ������
 * @author albert
 */
public class DefaultVersionSelection implements VersionSelectable {

    /**
     * ��ʶӦ�ð汾�� Cookie ���ơ�
     */
    public static final String VERSION_COOKIE_NAME = "x-app-verion";

    /**
     * ��ʶӦ�ð汾�� Cookie ���ơ�
     */
    public static final String TEST_VERSION_COOKIE_NAME = "x-app-test-verion";

    public AppVersion select(CookieReader cookieReader, AppVersion defaultVersion,
            Set<AppVersion> availableVersions) {
        /**
         * ֻ��һ����ѡ�汾ʱ�������汾ѡ��
         */
        if (availableVersions.size() <= 1) {
            return defaultVersion;
        }

        // ����汾���ض��Ĳ���״̬
        String testVersion = cookieReader.getCookieValue(TEST_VERSION_COOKIE_NAME);
        if (testVersion != null) {
            for (Iterator<AppVersion> it = availableVersions.iterator(); it.hasNext();) {
                AppVersion appVersion = it.next();
                if (appVersion.getState() == AppVersionState.TESTING) {
                    if (testVersion.equals(appVersion.getVersion())) {
                        return appVersion;
                    }
                }
            }

        }
        String version = cookieReader.getCookieValue(VERSION_COOKIE_NAME);
        if (version == null) {
            // ʹ��Ĭ�ϰ汾��
            return defaultVersion;
        }
        
        for (Iterator<AppVersion> it = availableVersions.iterator(); it.hasNext();) {
            AppVersion appVersion = it.next();
            // �û�ָ���˰汾
            if (version.equals(appVersion.getVersion())) {
                return appVersion;
            }
        }

        // �û�ָ���İ汾�Ѿ����ٷ���
        return defaultVersion;
    }

}
