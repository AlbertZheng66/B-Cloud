/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xt.bcloud.mdu.command;

import com.xt.bcloud.comm.ValueReader;
import com.xt.bcloud.comm.VarParser;
import java.util.HashMap;
import junit.framework.TestCase;

/**
 *
 * @author Albert
 */
public class CommandValueReaderTest extends TestCase {
    
    public CommandValueReaderTest(String testName) {
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
     * Test of readValue method, of class CommandValueReader.
     */
    public void testReadValue() {
        System.out.println("readValue");
        System.out.println("parse");
        String value = "{a:'b', b:'${workPath}/c', c:'${_inc}',d:'${workPath}\\${_inc}',"
                + " e:'${workPath}\\${_random}', port=${_port[10000,20000]}},range=${_range[1000,2000]}},${_date}, ${_time}";
        ValueReader reader = new CommandValueReader(new HashMap());
        String result = VarParser.parse(value, reader);
        
        System.out.println("result=" + result);
        
        value = "{jmxPort:\"${_port[20000,29999]}\", serverPort:\"${_port[30000,39999]}\", serverRedirectPort:\"${_port[40000,49999]}\", appServNo:\"${_inc[appServNo]}\"";
        result = VarParser.parse(value, reader);
        
        System.out.println("result=" + result);
    }
}
