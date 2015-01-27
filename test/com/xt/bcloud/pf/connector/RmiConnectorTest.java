/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xt.bcloud.pf.connector;

import junit.framework.TestCase;

/**
 *
 * @author Albert
 */
public class RmiConnectorTest extends TestCase {
    
    public RmiConnectorTest(String testName) {
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
     * Test of getClient method, of class RmiConnector.
     */
    public void testGetClient() {
        System.out.println("getClient");
        RmiConnector instance = new RmiConnector();
        RmiConnector expResult = null;
        RmiConnector result = instance.getClient();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }
}
