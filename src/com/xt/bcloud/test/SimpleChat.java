package com.xt.bcloud.test;

import org.jgroups.JChannel;

/**
 *
 * @author albert
 */
public class SimpleChat {

    JChannel channel;
    String user_name = System.getProperty("user.name", "n/a");

    private void start() throws Exception {
        channel = new JChannel();
        channel.connect("ChatCluster");
    }

    public static void main(String[] args) throws Exception {
        new SimpleChat().start();
    }
}
