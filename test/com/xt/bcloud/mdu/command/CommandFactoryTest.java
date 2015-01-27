/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xt.bcloud.mdu.command;

import com.xt.bcloud.mdu.AppServerTemplate;
import junit.framework.TestCase;

/**
 *
 * @author Albert
 */
public class CommandFactoryTest extends TestCase {
    
    public CommandFactoryTest(String testName) {
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
     * Test of build method, of class CommandFactory.
     */
    public void testBuild() {
        System.out.println("build");
        Command command = new DeployCommand();
        AppServerTemplate asTemplate = new AppServerTemplate();
        asTemplate.setOid("1111111111");
        asTemplate.setParams("{jmxPort:'${_port[20000,29999]}', serverPort:'${_port[30000,39999]}', serverRedirectPort:'${_port[40000,49999]}', appServNo:'${_inc[appServNo]}'}");
        // asTemplate.setStorePath("");
        command.setParam(asTemplate);
        CommandFactory instance = CommandFactory.getInstance();
        String result = instance.build(command);
        System.out.println("result=" + result);
    }

    /**
     * Test of parse method, of class CommandFactory.
     */
    public void testParse() {
        System.out.println("parse");
        String commandStr = "deploy?{'__className':'com.xt.bcloud.mdu.AppServerTemplate','valid':false,'oid':'1111111111','fileSize':0,'params':{'jmxPort':'${_port[20000,29999]}','serverPort':'${_port[30000,39999]}','serverRedirectPort':'${_port[40000,49999]}','appServNo':'${_inc[appServNo]}'}}";
        CommandFactory instance = CommandFactory.getInstance();
        Command result = instance.parse(commandStr);
        System.out.println("result=" + result);
    }
}
