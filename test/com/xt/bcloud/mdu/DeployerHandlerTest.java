/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xt.bcloud.mdu;

import com.xt.bcloud.mdu.command.CommandHandler;
import junit.framework.TestCase;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

/**
 *
 * @author Albert
 */
public class DeployerHandlerTest extends TestCase {
    
    public DeployerHandlerTest(String testName) {
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
     * Test of messageReceived method, of class DeployerHandler.
     */
    public void xtestMessageReceived() throws Exception {
        System.out.println("messageReceived");
        IoSession session = null;
        Object message = "update?{aaa='aa'}";
        CommandHandler instance = new CommandHandler();
        instance.messageReceived(session, message);
        
        message = "update";
        instance = new CommandHandler();
        instance.messageReceived(session, message);
        
         message = "aaa";
        instance = new CommandHandler();
        instance.messageReceived(session, message);
    }
   

}
