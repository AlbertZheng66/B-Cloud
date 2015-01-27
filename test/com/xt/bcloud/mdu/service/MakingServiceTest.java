/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xt.bcloud.mdu.service;

import com.xt.bcloud.mdu.ParamsVO;
import com.xt.bcloud.mdu.AppServerTemplate;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author Albert
 */
public class MakingServiceTest extends TestCase {
    
    public MakingServiceTest(String testName) {
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

    public void testLoad() {
        MakingService service = new MakingService();
        AppServerTemplate asTemplate = new AppServerTemplate();
        asTemplate.setParams("{'jmxPort':'${_port[20000,29999]}','serverPort':'${_port[30000,39999]}','serverRedirectPort': '${_port[40000,49999]}', 'appServNo':'${_inc[appServNo]}'}");
        List<ParamsVO> params = service.loadParams(asTemplate);
        System.out.println("params=" + params);
    }
}
