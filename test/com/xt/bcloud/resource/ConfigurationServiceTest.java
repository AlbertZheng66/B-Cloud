
package com.xt.bcloud.resource;

import com.xt.bcloud.app.App;
import com.xt.bcloud.app.AppVersion;
import com.xt.bcloud.worker.Cattle;
import com.xt.core.utils.IOHelper;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import junit.framework.TestCase;

/**
 *
 * @author albert
 */
public class ConfigurationServiceTest extends TestCase {
    
    public ConfigurationServiceTest(String testName) {
        super(testName);
    }

    public void testReadParams() throws FileNotFoundException {
        ConfService cs = new ConfService();
        InputStream is = cs.readSystemParams(new Cattle("aa", new App(), new AppVersion(), "aa", "127.0.0.1", 8080));
        FileOutputStream fos = new FileOutputStream("e:\\params.xml");
        IOHelper.i2o(is, fos, true, true);
    }

}
