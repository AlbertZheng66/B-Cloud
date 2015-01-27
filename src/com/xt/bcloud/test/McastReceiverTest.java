/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.xt.bcloud.test;

/**
 * 测试地址及端口是否可用(接收端)。
 * @author albert
 */
public class McastReceiverTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String[] _args = {"-mcast_addr", "224.10.10.10", "-port", "5555"};
        org.jgroups.tests.McastReceiverTest.main(_args);
    }

}
