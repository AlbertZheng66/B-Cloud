/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xt.bcloud.comm;

import junit.framework.TestCase;

/**
 *
 * @author Albert
 */
public class VarParserTest extends TestCase {
    
    public VarParserTest(String testName) {
        super(testName);
    }
    
   

    /**
     * Test of parse method, of class VarParser.
     */
    public void testParse() {
        System.out.println("parse");
        String value = "{a:'b', b:'${workPath}/c', c:'${_inc}',d:'${workPath}\\${_inc}', e:'${workPath}\\${_random}'}";
        ValueReader reader = new ValueReader() {

                public String readValue(String name) {
                    if ("_random".equals(name)) {
                        // 产生一个随机数
                        return "123";
                    } else if (name.matches("^_range\\[\\d+,\\d+\\]$")) {
                        // 在特定范围内产生一个字
                        return "range";
                    } else if ("_inc".equals(name)) {
                        // 从属性中读出初始值，然后加一
                        return "9";
                    } else {
                        
                    }
                    return "${" + name + "?}";
                }
        };
        String expResult = "";
        String result = VarParser.parse(value, reader);
        // assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        
        System.out.println("result=" + result);
    }
}
