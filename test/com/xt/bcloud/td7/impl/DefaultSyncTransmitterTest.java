/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xt.bcloud.td7.impl;

import com.xt.bcloud.td7.Message;
import com.xt.bcloud.td7.Request;
import com.xt.bcloud.td7.Response;
import com.xt.bcloud.td7.SyncTransmitter;
import com.xt.bcloud.td7.Utils;
import java.io.IOException;
import java.nio.ByteBuffer;
import junit.framework.TestCase;

/**
 *
 * @author Albert
 */
public class DefaultSyncTransmitterTest extends TestCase {

    public DefaultSyncTransmitterTest(String testName) {
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

    public void testSend() throws IOException {
        // 将消息进行转发，并解析相应的响应请求
        SyncTransmitter transmitter = TransmitterFactory.getInstance().getTransmitter("127.0.0.1",
                28057);
        HttpRequestParser hrp = new HttpRequestParser();
        ByteBuffer byteBuffer = Utils.createBuffer(this, "td_request_no_content_length.dump");
        Request msg = (Request)hrp.parse(byteBuffer);
        msg.setContextPath("/demo/");
        Response response = transmitter.send(msg);
        Utils.print(response.getOriginalBytes());
        System.out.println("res=" + response);
    }
}
