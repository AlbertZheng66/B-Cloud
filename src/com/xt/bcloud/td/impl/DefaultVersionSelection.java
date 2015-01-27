

package com.xt.bcloud.td.impl;

import com.xt.bcloud.app.AppVersion;
import com.xt.bcloud.app.AppVersionState;
import com.xt.bcloud.td.VersionSelectable;
import com.xt.bcloud.td7.CookieReader;
import java.util.Iterator;
import java.util.Set;

/**
 * 缺省的版本选择器。
 * @author albert
 */
public class DefaultVersionSelection implements VersionSelectable {

    /**
     * 标识应用版本的 Cookie 名称。
     */
    public static final String VERSION_COOKIE_NAME = "x-app-verion";

    /**
     * 标识应用版本的 Cookie 名称。
     */
    public static final String TEST_VERSION_COOKIE_NAME = "x-app-test-verion";

    public AppVersion select(CookieReader cookieReader, AppVersion defaultVersion,
            Set<AppVersion> availableVersions) {
        /**
         * 只有一个供选版本时，不做版本选择。
         */
        if (availableVersions.size() <= 1) {
            return defaultVersion;
        }

        // 如果版本有特定的测试状态
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
            // 使用默认版本。
            return defaultVersion;
        }
        
        for (Iterator<AppVersion> it = availableVersions.iterator(); it.hasNext();) {
            AppVersion appVersion = it.next();
            // 用户指定了版本
            if (version.equals(appVersion.getVersion())) {
                return appVersion;
            }
        }

        // 用户指定的版本已经不再服务
        return defaultVersion;
    }

}
