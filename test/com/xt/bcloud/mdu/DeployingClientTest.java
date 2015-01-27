
package com.xt.bcloud.mdu;

import com.xt.bcloud.mdu.command.UpdateCommand;
import com.xt.bcloud.mdu.command.CommandClient;
import junit.framework.TestCase;

/**
 *
 * @author Albert
 */
public class DeployingClientTest extends TestCase {
    
    public DeployingClientTest(String testName) {
        super(testName);
    }

    /**
     * Test of execute method, of class DeployingClient2.
     */
    public void testExecute() {
        System.out.println("execute");
        PhyServer phyServer = new PhyServer();
        phyServer.setIp("127.0.0.1");
        phyServer.setManagerPort(12000);
        CommandClient instance = new CommandClient();
        instance.execute(phyServer, new UpdateCommand());
       
    }
}
