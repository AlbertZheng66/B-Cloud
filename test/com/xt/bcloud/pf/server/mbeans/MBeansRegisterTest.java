/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xt.bcloud.pf.server.mbeans;

import com.xt.bcloud.pf.connector.RmiConnectorFactory;
import com.xt.bcloud.resource.server.ServerInfo;
import java.io.IOException;
import javax.management.*;
import junit.framework.TestCase;

/**
 *
 * @author Albert
 */
public class MBeansRegisterTest extends TestCase {
    
    private MBeanServerConnection conn;
    
    public MBeansRegisterTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setIp("127.0.0.1");
        serverInfo.setJmxRmiPort(26877);
        conn = getConnection(serverInfo);
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of onInit method, of class MBeansRegister.
     */
    public void testOnInit() {
        System.out.println("onInit");
        try {
            printMBeanInfo(MBeanNames.SERVER_MEMORY);
            printMBeanInfo(MBeanNames.SERVER_CPU);
            printMBeanInfo(MBeanNames.SERVER_FILE_SYSTEM);
            printMBeanInfo(MBeanNames.SERVER_NETWORK);
           
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private MBeanInfo printMBeanInfo(String objectName) throws Exception {
        MBeanInfo oi = conn.getMBeanInfo(new ObjectName(objectName));
        MBeanAttributeInfo[] attrs = oi.getAttributes();
        for (MBeanAttributeInfo attr : attrs) {
            Object value = conn.getAttribute(new ObjectName(objectName), attr.getName());
            System.out.println("value =" + value);
        }
//
        return oi;
    }
    
    private MBeanServerConnection getConnection(ServerInfo serverInfo) {
        MBeanServerConnection conn = RmiConnectorFactory.getInstance().getConnection(serverInfo.getIp(),
                serverInfo.getJmxRmiPort());
        return conn;
    }

    /**
     * Test of onDestroy method, of class MBeansRegister.
     */
    public void testOnDestroy() {
    }
}
