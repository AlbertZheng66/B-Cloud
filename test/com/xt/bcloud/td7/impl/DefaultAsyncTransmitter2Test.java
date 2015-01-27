 
package com.xt.bcloud.td7.impl;

import com.xt.bcloud.td7.AsyncTransmitter;
import com.xt.bcloud.td7.AsyncTransmitter.Callable;
import com.xt.bcloud.td7.Request;
import com.xt.bcloud.td7.Response;
import com.xt.bcloud.td7.Utils;
import java.nio.ByteBuffer;
import junit.framework.TestCase;

/**
 *
 * @author Albert
 */
public class DefaultAsyncTransmitter2Test extends TestCase {
    
    public DefaultAsyncTransmitter2Test(String testName) {
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
     * Test of asyncSend method, of class DefaultAsyncTransmitter2.
     */
    public void testAsyncSend() throws Exception {
        System.out.println("asyncSend");
                // 注意：测试前需要先启动资源管理的应用服务器
        AsyncTransmitter transmitter =new DefaultAsyncTransmitter2("127.0.0.1",
                58080);
        transmitter.open();
        ByteBuffer buffer = Utils.createBuffer(this, "http.request.formdata.txt");
        HttpRequestParser parser = new HttpRequestParser();
        Request request = (Request) parser.parse(buffer);
        final Thread main = Thread.currentThread();
        transmitter.asyncSend(request, new Callable() {

            public void execute(Response response) {
                for (ByteBuffer b : response.getBody().getBuffers()) {
                    Utils.print(b);
                }
                main.interrupt();
            }

            public void handle(Throwable t) {
                t.printStackTrace(System.out);
                main.interrupt();
            }
        });
        try {

            Thread.sleep(10000000000000000l);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
