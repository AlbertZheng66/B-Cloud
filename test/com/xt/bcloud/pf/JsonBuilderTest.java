/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xt.bcloud.pf;

import com.xt.bcloud.pf.server.ServerProfilingService;
import com.xt.bcloud.pf.server.ServerProfilingInfo;
import com.xt.bcloud.resource.server.ServerInfo;
import com.xt.bcloud.resource.server.ServerState;
import com.xt.comm.service.LocationService;
import com.xt.core.json.JsonBuilder;
import com.xt.proxy.event.Response;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author Albert
 */
public class JsonBuilderTest extends TestCase {

    public JsonBuilderTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of build method, of class JSonBuilder.
     */
    public void testResponse() {
        Response res = new Response();
        res.setRefParams(new Object[0]);
        ServerProfilingService sps = new ServerProfilingService();
        res.setServiceObject(sps);

        List<ServerProfilingInfo> pis = listServers();
        res.setReturnValue(pis);
        LocationService ls = new LocationService();
        System.out.println(ls.getURL(JsonBuilder.class.getName()));

        JsonBuilder instance = new JsonBuilder();
        // String expResult = \"\";
        String result = instance.build(res);
        System.out.println("result=" + result);

    }

    private List<ServerProfilingInfo> listServers() {
        List<ServerInfo> serverInfos = new ArrayList();
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setOid("11111");
        serverInfo.setId("aaaaaaaaa");
        serverInfo.setName("bbbbbbbbbbbbbbb");
        serverInfo.setState(ServerState.USING);
        serverInfos.add(serverInfo);
        List<ServerProfilingInfo> pis = new ArrayList(serverInfos.size());
        for (Iterator<ServerInfo> it = serverInfos.iterator(); it.hasNext();) {
            ServerInfo _serverInfo = it.next();
            ServerProfilingInfo pi = new ServerProfilingInfo();
            pi.setOid(_serverInfo.getOid());
            pi.setId(_serverInfo.getId());
            pi.setName(pi.getName());
            pi.setState(_serverInfo.getState());
            // read(serverInfo, pi);
            pis.add(pi);
        }
        return pis;
    }
}
