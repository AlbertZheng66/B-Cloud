/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xt.bcloud.td7;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;

/**
 *
 * @author Albert
 */
public class TaskDispatcher7Test extends TestCase {
    
    public TaskDispatcher7Test(String testName) {
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
     * Test of main method, of class TaskDispatcher7.
     */
    public void testMain() {
        System.out.println("main");
        ByteBuffer buffer = Utils.createBuffer(this, "http.request.formdata.txt");
        
        SocketAddress sa = new InetSocketAddress("127.0.0.1", 4900);
        try {
            buffer.flip();
            SocketChannel channel = SocketChannel.open();
            channel.connect(sa);
            channel.write(buffer);
            //-------------------- start to read
            System.out.println("start to read..............");
            buffer = BufferFactory.getInstance().allocate(2048);
            channel.read(buffer);
            buffer.flip();
            Utils.print(buffer);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
