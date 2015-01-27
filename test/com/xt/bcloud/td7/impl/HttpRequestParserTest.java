/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xt.bcloud.td7.impl;

import com.xt.bcloud.td7.Message;
import com.xt.bcloud.td7.Utils;
import java.io.IOException;
import java.nio.ByteBuffer;
import junit.framework.TestCase;

/**
 *
 * @author Albert
 */
public class HttpRequestParserTest extends TestCase {
    
    public HttpRequestParserTest(String testName) {
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

    public void testParse() throws IOException {
        HttpRequestParser hrp = new HttpRequestParser();
        ByteBuffer byteBuffer = Utils.createBuffer(this, "td_request_no_content_length.dump");
        Message msg = hrp.parse(byteBuffer);
        assertNotNull(msg);
        hrp.end();
        assertTrue(hrp.finished());
    }
}
