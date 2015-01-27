/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.xt.bcloud.worker;

import com.xt.bcloud.test.Cattle2;
import junit.framework.TestCase;

/**
 *
 * @author albert
 */
public class Cattle2Test extends TestCase {
    
    public Cattle2Test(String testName) {
        super(testName);
    }

    public void testCreate() {
        Cattle2 cattle = new Cattle2();
        cattle.create();
    }

}
