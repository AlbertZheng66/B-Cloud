
package com.xt.bcloud.session;

import com.xt.core.session.LocalSession;
import com.xt.proxy.LocalContext;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.ehcache.CacheManager;

/**
 *
 * @author albert
 */
public class ClusterSessionTest2 {
    private static CacheManager manager;

    private LocalSession localSession = LocalSession.getInstance();
    
    public ClusterSessionTest2(String testName) {
         manager = CacheManager.create("E:\\work\\xthinker\\B-Cloud\\src\\files\\ehcache.2.xml");
        //manager = CacheManager.create("E:\\ehcacheConf-1267948408976.conf.xml");
    }
    

    public void printAttributeNames() {
        EhcacheSession clusterSession = new EhcacheSession(localSession, new LocalContext());
        
        System.out.println("CreationTime=" + clusterSession.getCreationTime());
        //clusterSession.setAttribute("attr2", "aaa");
        for (Enumeration it = clusterSession.getAttributeNames(); it.hasMoreElements();) {
            Object object = it.nextElement();
            System.out.println("name=" + object);
        }

    }
    public static void main(String[] argv) {
        ClusterSessionTest2 test = new ClusterSessionTest2("test2");
        try {
            System.out.print("press any key to continue:");
            System.in.read();
        } catch (IOException ex) {
            Logger.getLogger(ClusterSessionTest2.class.getName()).log(Level.SEVERE, null, ex);
        }
        test.printAttributeNames();
        System.out.println("end............");
        // manager.shutdown();
    }


}
