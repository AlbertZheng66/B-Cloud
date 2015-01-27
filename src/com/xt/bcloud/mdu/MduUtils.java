
package com.xt.bcloud.mdu;

import java.util.Calendar;

/**
 *
 * @author Albert
 */
public class MduUtils {
    
    public static void copyProperties(AppServerInstance asInstance, PhyServer phyServer, AppServerTemplate asTemplate) {
        // 发布之后自动注册
        asInstance.setPhyServerOid(phyServer.getOid());
        asInstance.setTemplateOid(asTemplate.getOid());
        asInstance.setName(asTemplate.getName());
        asInstance.setServerType(asTemplate.getServerType());
        asInstance.setVersion(asTemplate.getVersion());
        asInstance.setInsertTime(Calendar.getInstance());
        asInstance.setStartupTime(Calendar.getInstance());
        asInstance.setValid(true);
        asInstance.setState(AppServerInstanceState.AVAILABLE);
        asInstance.setPhyServerOid(phyServer.getOid());
    }
    
}
