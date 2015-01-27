/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.xt.bcloud.comm;

import junit.framework.TestCase;

/**
 *
 * @author albert
 */
public class PortFactoryTest extends TestCase {
    
    public PortFactoryTest(String testName) {
        super(testName);
    }

    public void testGetInstance() {
    }

    public void testRegister() {
    }

    public void testGetPort() {
    }

    public void testIsValid() {
        PortFactory pf = PortFactory.getInstance();
        for (int i = 0; i < 65536; i++) {
            if (pf.isValid(i)) {
                System.out.println("¶Ë¿Ú" + i + "¿ÉÒÔ¡£");
            }

        }
    }

    public void testGiveBack() {
    }

}
