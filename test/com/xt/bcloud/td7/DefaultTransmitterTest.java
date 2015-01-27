package com.xt.bcloud.td7;

import com.xt.bcloud.td7.impl.HttpRequestParser;
import com.xt.bcloud.td7.impl.AbstractHttpParser;
import com.xt.bcloud.td7.impl.DefaultSyncTransmitter;
import com.xt.bcloud.td7.impl.DumperFactory;
import com.xt.bcloud.test.PrintServer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import junit.framework.TestCase;

/**
 *
 * @author Albert
 */
public class DefaultTransmitterTest extends TestCase {

    public DefaultTransmitterTest(String testName) {
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
     * Test of open method, of class DefaultTransmitter.
     */
    public void xtestOpen() {
        System.out.println("open");
        DefaultSyncTransmitter instance = null;
        boolean expResult = false;
        boolean result = instance.open();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of send method, of class DefaultTransmitter.
     */
    public void xtestSend() throws Exception {
        System.out.println("send");
        ByteBuffer buffer = Utils.createBuffer(this, "http.request.formdata.txt");
        AbstractHttpParser instance = new HttpRequestParser();
        Request request = (Request) instance.parse(buffer);

        DefaultSyncTransmitter trasmitter = new DefaultSyncTransmitter("127.0.0.1", PrintServer.port);
        trasmitter.open();
        Response result = trasmitter.send(request);
        trasmitter.end();
        // assertEquals(expResult, result);
    }
    
    /**
     * Test of send method, of class DefaultTransmitter.
     */
    public void xtestSendImage() throws Exception {
        System.out.println("sendImage");
        ByteBuffer buffer = Utils.createBuffer(this, "http.request.image.txt");
        AbstractHttpParser instance = new HttpRequestParser();
        Request request = (Request) instance.parse(buffer);
        request.setContextPath("/demo/images/tiger.gif");
       
        final int port = 23199; // 服务器端口
        

        DefaultSyncTransmitter trasmitter = new DefaultSyncTransmitter("www.bc_demo1.com", port);
        trasmitter.open();
        Response response = trasmitter.send(request);
        DumperFactory.getInstance().write("DefaultTransmitterTest",
                new InetSocketAddress("127.0.0.1", port), response.getOriginalBytes().getBuffers());
        Utils.print(response.getOriginalBytes());
    }
    
     /**
     * Test of send method, of class DefaultTransmitter.
     */
    public void testSendImage() throws Exception {
        System.out.println("sendImage");
        ByteBuffer buffer = Utils.createBuffer(this, "http.request.js.txt");
        AbstractHttpParser instance = new HttpRequestParser();
        Request request = (Request) instance.parse(buffer);
        request.setContextPath("/demo" + request.getContextPath());
       
        final int port = 20139; // 服务器端口
        

        DefaultSyncTransmitter trasmitter = new DefaultSyncTransmitter("www.bc_demo1.com", port);
        trasmitter.open();
        Response response = trasmitter.send(request);
        DumperFactory.getInstance().write("DefaultTransmitterTest",
                new InetSocketAddress("127.0.0.1", port), response.getOriginalBytes().getBuffers());
        Utils.print(response.getOriginalBytes());
    }

    /**
     * Test of end method, of class DefaultTransmitter.
     */
    public void xtestEnd() {
        System.out.println("end");
        DefaultSyncTransmitter instance = null;
        instance.end();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
