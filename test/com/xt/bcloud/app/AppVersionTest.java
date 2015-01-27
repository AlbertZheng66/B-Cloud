
package com.xt.bcloud.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author albert
 */
public class AppVersionTest extends TestCase {
    
    public AppVersionTest(String testName) {
        super(testName);
    }

    /**
     * 对排序算法进行测试
     */
    public void testCompareTo() {
        List<AppVersion> list = new ArrayList();
        AppVersion v3 = new AppVersion();
        v3.setVersion("1.5");
        list.add(v3);
        AppVersion v1 = new AppVersion();
        v1.setVersion("0.2");
        list.add(v1);
        AppVersion v2 = new AppVersion();
        v2.setVersion("1.2");
        list.add(v2);
        assertEquals(v3, list.get(0));
        assertEquals(v2, list.get(2));
        Collections.sort(list);
        System.out.println("sorted = " + list);
        assertEquals(v3, list.get(2));
        assertEquals(v2, list.get(1));
    }

}
