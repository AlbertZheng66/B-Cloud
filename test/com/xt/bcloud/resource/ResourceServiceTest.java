
package com.xt.bcloud.resource;

import com.xt.bcloud.comm.CloudUtils;
import com.xt.bcloud.resource.server.ServerInfo;
import junit.framework.TestCase;

/**
 *
 * @author albert
 */
public class ResourceServiceTest extends TestCase {
    
    public ResourceServiceTest(String testName) {
        super(testName);
    }

    public void testRegisterServer() {
        ResourceService rs = CloudUtils.createResourceService();
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setId("rs_test");
        serverInfo.setIp("127.0.0.1");
        serverInfo.setManagerPort(8080);
        serverInfo.setContextPath("appMgr");
        serverInfo.setName("≤‚ ‘°£°£°£");
        rs.registerServer(serverInfo, null);
    }

    public void testApplyFor() {
    }

    public void testGiveback() {
    }

}
