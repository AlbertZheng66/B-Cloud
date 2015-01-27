
package com.xt.bcloud.mdu.command;

import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;

/**
 *
 * @author Albert
 */
public class VarProcessorTest extends TestCase {
    
    public VarProcessorTest(String testName) {
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
     * Test of generate method, of class VarProcessor.
     */
    public void testGenerate() {
        System.out.println("generate");
        String templateFileName = "E:/work/xthinker/B-Cloud/workspace/appAndResourceMgr/apache-tomcat-6.0.16/webapps/arMgr/WEB-INF/templates/tomcat.6.0.12/bin/catalina.bat.vm";
        String targetFileName = "e:/temp/catalina.bat";
        Map<String, Object> params = new HashMap();
        params.put("jmxPort", "28993");
        params.put("serverPort", "38001");
        params.put("serverRedirectPort", "38443");
        params.put("appServNo", "8");
        VarProcessor instance = new VarProcessor();
        instance.generate(templateFileName, targetFileName, params);
        
        // 2        
        templateFileName = "E:/work/xthinker/B-Cloud/workspace/appAndResourceMgr/apache-tomcat-6.0.16/webapps/arMgr/WEB-INF/templates/tomcat.6.0.12/conf/server.xml.vm";
        targetFileName = "e:/temp/server.xml";
        instance.generate(templateFileName, targetFileName, params);
        
        // 3
        templateFileName = "E:/work/xthinker/B-Cloud/workspace/appAndResourceMgr/apache-tomcat-6.0.16/webapps/arMgr/WEB-INF/templates/tomcat.6.0.12/webapps/serv1/WEB-INF/serv-config.xml.vm";
        targetFileName = "e:/temp/serv-config.xml";
        instance.generate(templateFileName, targetFileName, params);
        
    }
}
