
package com.xt.bcloud.td;

import com.xt.bcloud.app.AppVersion;
import com.xt.bcloud.td7.CookieReader;
import java.util.Set;

/**
 * �汾ѡ��ӿڣ���Ӧ���ж���汾ʱ������ȷ����ǰӦ��Ӧʹ�õİ汾��
 * @author albert
 */
public interface VersionSelectable {

    public AppVersion select(CookieReader cookieReader,
            AppVersion defaultVersion, Set<AppVersion> availableVersions);
    

}
