
package com.xt.bcloud.resource;

import com.xt.bcloud.comm.UnZip;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import junit.framework.TestCase;

/**
 *
 * @author albert
 */
public class UnZipTest extends TestCase {
    
    public UnZipTest(String testName) {
        super(testName);
    }

    public void xtestUnZip() {
        UnZip unzip = new UnZip();
        unzip.unZip("E:/work/xthinker/B-Cloud/workspace/appAndResourceMgr/deployPackages/00001/tiger.0.1.war", new File("e:\\test"));
    }

    public void testUnZip2() throws FileNotFoundException {
        UnZip unzip = new UnZip();
        FileInputStream fis = new FileInputStream("E:/work/xthinker/B-Cloud/workspace/appAndResourceMgr/deployPackages/00001/tiger.0.1.war");
        unzip.unZip(fis, new File("e:\\test2"));
    }

}
