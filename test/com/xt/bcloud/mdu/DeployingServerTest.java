/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xt.bcloud.mdu;

import com.xt.bcloud.mdu.command.CommandServer;


/**
 *
 * @author Albert
 */
public class DeployingServerTest {
    
    /**
     * Test of startServer method, of class DeployingClient.
     */
    public static void main( String[] args ) {
        System.out.println("startServer");
        CommandServer instance = new CommandServer(12000);
        instance.startServer();
    }
}
