/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.xt.bcloud.td;

import junit.framework.TestCase;

/**
 *
 * @author albert
 */
public class TaskDispatcherMgrGroupTest extends TestCase {
    
    public TaskDispatcherMgrGroupTest(String testName) {
        super(testName);
    }

    public void testGroup() throws InterruptedException {
        CattleManager tdmg = CattleManager.getInstance();
        tdmg.init();
        
        while(true) {
            Thread.sleep(200);
        }
    }


}
