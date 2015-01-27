
package com.xt.bcloud.td7.impl;

import com.xt.bcloud.comm.CloudUtils;
import com.xt.bcloud.td.http.HttpConstants;
import com.xt.bcloud.td7.AsyncTransmitter;
import com.xt.bcloud.td7.AsyncTransmitter.Callable;
import com.xt.bcloud.td7.Request;
import com.xt.bcloud.td7.Response;
import com.xt.bcloud.td7.Utils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;

/**
 *
 * @author Albert
 */
public class DefaultAsyncTransmitterTest extends TestCase {

    public DefaultAsyncTransmitterTest(String testName) {
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
     * Test of asyncSend method, of class DefaultAsyncTransmitter.
     */
    public void testAsyncSend() throws Exception {
//        System.out.print("|");
//        Utils.print(HttpConstants.blank());
//        Utils.print(HttpConstants.blank());
//        Utils.print(HttpConstants.blank());
//        Utils.print(HttpConstants.blank());
//        Utils.print(HttpConstants.blank());
//        Utils.print(HttpConstants.blank());
//        Utils.print(HttpConstants.blank());
//        Utils.print(HttpConstants.blank());
//        Utils.print(HttpConstants.blank());
//        Utils.print(HttpConstants.blank());
//        Utils.print(HttpConstants.blank());
//        Utils.print(HttpConstants.blank());
//        Utils.print(HttpConstants.blank());
//        Utils.print(HttpConstants.blank());
//        Utils.print(HttpConstants.blank());
//        Utils.print(HttpConstants.blank());
//        System.out.println("|");
        // 注意：测试前需要先启动资源管理的应用服务器
        AsyncTransmitter transmitter = TransmitterFactory.getInstance().getAsyncTransmitter("127.0.0.1",
                58080);
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
