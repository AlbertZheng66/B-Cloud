
package com.xt.bcloud.td;

import com.xt.bcloud.app.AppVersion;
import com.xt.bcloud.td7.CookieReader;
import java.util.Set;

/**
 * 版本选择接口，当应用有多个版本时，用于确定当前应用应使用的版本。
 * @author albert
 */
public interface VersionSelectable {

    public AppVersion select(CookieReader cookieReader,
            AppVersion defaultVersion, Set<AppVersion> availableVersions);
    

}
