/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xt.bcloud.mdu.command;

import com.xt.core.utils.VarTemplate;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Albert
 */
public class DeployCommandTest extends TestCase {

    public DeployCommandTest(String testName) {
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

    public void testStartCmd() {
        Map params = new HashMap();
        
        String startupCmd = VarTemplate.format("./exe.sh \"${workPath}/bin/startup.${suffix}\"", params, true);
        assertEquals("./exe.sh \"${workPath}/bin/startup.${suffix}\"", startupCmd);
        params.put("suffix", "sh");
        startupCmd = VarTemplate.format("./exe.sh \"${workPath}/bin/startup.${suffix}\"", params, true);
        assertEquals("./exe.sh \"${workPath}/bin/startup.sh\"", startupCmd);
        
        params.put("workPath", "/root/workspace");
        startupCmd = VarTemplate.format("./exe.sh \"${workPath}/bin/startup.${suffix}\"", params, true);
        assertEquals("./exe.sh \"/root/workspace/bin/startup.sh\"", startupCmd);
        
        startupCmd = FilenameUtils.normalize(startupCmd);
        assertEquals("exe.sh \"\\root\\workspace\\bin\\startup.sh\"", startupCmd);
    }
}
