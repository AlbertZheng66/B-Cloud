
package com.xt.bcloud.worker;

import com.xt.bcloud.app.App;
import com.xt.bcloud.app.AppVersion;
import junit.framework.TestCase;

/**
 *
 * @author albert
 */
public class RanchTest extends TestCase {
    
    public RanchTest(String testName) {
        super(testName);
    }

    public void testRegister() throws InterruptedException {
        App app = new App();
        app.setOid("test-001");
        app.setId("app-0001");
        app.addHost("www.sina.com");
        app.addHost("www.google.com");
        Cattle cattle = new Cattle(String.valueOf(System.nanoTime()), app, new AppVersion(), "aaa", "localhost", 8088);
        Ranch ranch = new Ranch(cattle);
        new Thread(ranch).start();
        while(true) {
            Thread.sleep(200);
        }
    }
}
