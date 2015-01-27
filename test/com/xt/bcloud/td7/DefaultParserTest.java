
package com.xt.bcloud.td7;

import com.xt.bcloud.td7.impl.HttpRequestParser;
import com.xt.bcloud.td7.impl.AbstractHttpParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import junit.framework.TestCase;

/**
 *
 * @author Albert
 */
public class DefaultParserTest extends TestCase {
    
    public DefaultParserTest(String testName) {
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
     * Test of parse method, of class DefaultParser.
     */
    public void testParse_ByteBuffer() throws Exception {
        System.out.println("parse");
        ByteBuffer buffer = Utils.createBuffer(this, "http.request.formdata.txt");
        AbstractHttpParser instance = new HttpRequestParser();
        Message result = instance.parse(buffer);
        System.out.println("result=" + result);
        // assertEquals(expResult, result);
    }
    
    
}
