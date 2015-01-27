/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xt.bcloud.comm;

import com.xt.bcloud.mdu.MduService;
import com.xt.bcloud.resource.ResourceService;
import com.xt.proxy.Proxy;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import junit.framework.TestCase;

/**
 *
 * @author Albert
 */
public class CloudUtilsTest extends TestCase {
    
    public CloudUtilsTest(String testName) {
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
     * Test of executeCommand method, of class CloudUtils.
     */
    public void testExecuteCommand() {
        System.out.println("executeCommand");
        String cmd = "set";
        ProcessResult result = CloudUtils.executeCommand(cmd);
        System.out.println("pid=" + result);
    }

    /**
     * Test of getComputerName method, of class CloudUtils.
     */
    public void xtestGetComputerName() {
        System.out.println("getComputerName");
        String expResult = "";
        String result = CloudUtils.getComputerName();
        assertEquals(expResult, result);
        
    }

}
