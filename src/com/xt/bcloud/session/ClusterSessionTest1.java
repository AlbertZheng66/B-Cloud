package com.xt.bcloud.session;

import com.xt.core.session.LocalSession;
import com.xt.proxy.LocalContext;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.ehcache.CacheManager;

/**
 *
 * @author albert
 */
public class ClusterSessionTest1 {

    private LocalSession localSession = LocalSession.getInstance();
    private static CacheManager manager;

    public ClusterSessionTest1(String testName) {
         manager = CacheManager.create("E:\\work\\xthinker\\B-Cloud\\src\\files\\ehcache.1.xml");
        //manager = CacheManager.create("E:\\ehcacheConf-1267948408976.conf.xml");
    }

    public void putAttributes() {
        EhcacheSession clusterSession = new EhcacheSession(localSession, new LocalContext());
        System.out.println("CurrentTime =" + System.currentTimeMillis());
        System.out.println("CreationTime=" + clusterSession.getCreationTime());
        clusterSession.setAttribute("attr1", "aaa");
        clusterSession.setAttribute("attr2", "bbb");
        clusterSession.setAttribute("attr6", "ccc");
        Object value = clusterSession.getAttribute("attr1");
    }

    public static void main(String[] argv) {
        ClusterSessionTest1 test = new ClusterSessionTest1("test1");
        try {
            System.out.print("press an key to continue:");
            System.in.read();
            test.putAttributes();
            System.out.print("press 'q' to quit:");
            int q = 0;
            while (q != 'q') {
                q = System.in.read();
            }
            //manager.shutdown();
        } catch (IOException ex) {
            Logger.getLogger(ClusterSessionTest2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
