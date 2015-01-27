package com.xt.bcloud.session;



import com.xt.core.session.LocalSession;
import com.xt.proxy.LocalContext;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;
import net.sf.ehcache.CacheManager;

/**
 *
 * @author albert
 */
public class ClusterSessionTest extends TestCase {

    private LocalSession localSession = LocalSession.getInstance();

    public ClusterSessionTest(String testName) {
        super(testName);
        CacheManager.create("E:\\work\\xthinker\\B-Cloud\\src\\files\\ehcache.xml");
    }

    public void testGetAttribute() {
        EhcacheSession clusterSession = new EhcacheSession(localSession, new LocalContext());
        System.out.println("CurrentTime =" + System.currentTimeMillis());
        System.out.println("CreationTime=" + clusterSession.getCreationTime());
        clusterSession.setAttribute("attr1", "aaa");
        clusterSession.setAttribute("attr2", "bbb");
        clusterSession.setAttribute("attr6", "ccc");
        Object value = clusterSession.getAttribute("attr1");
        assertEquals("aaa", value);

        assertNull(clusterSession.getAttribute("attr222"));
    }

    public void testGetAttributeNames() {
        EhcacheSession clusterSession = new EhcacheSession(localSession, new LocalContext());
        System.out.println("CurrentTime =" + System.currentTimeMillis());
        clusterSession.setAttribute("attr1", "aaa");
        System.out.println("CreationTime=" + clusterSession.getCreationTime());
        //clusterSession.setAttribute("attr2", "aaa");
        for (Enumeration it = clusterSession.getAttributeNames(); it.hasMoreElements();) {
            Object object = it.nextElement();
            System.out.println("name=" + object);
        }
    }

    public void testGetCreationTime() {
        EhcacheSession clusterSession = new EhcacheSession(localSession, new LocalContext());
        System.out.println("CurrentTime =" + System.currentTimeMillis());
        try {
            Thread.sleep(5000);
            System.out.println("CreationTime=" + clusterSession.getCreationTime());
        } catch (InterruptedException ex) {
            Logger.getLogger(ClusterSessionTest1.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (Enumeration it = clusterSession.getAttributeNames(); it.hasMoreElements();) {
            Object object = it.nextElement();
            System.out.println("name=" + object);
        }

    }

    public void testGetId() {
    }

    public void testGetLastAccessedTime() {
        EhcacheSession clusterSession = new EhcacheSession(localSession, new LocalContext());
        long creationTime = clusterSession.getCreationTime();
        
        long lastAccessedTime = clusterSession.getLastAccessedTime();
        assertEquals(creationTime, lastAccessedTime);
        long currentTime = System.currentTimeMillis();
        clusterSession.setLastAccessedTime(currentTime);
        lastAccessedTime = clusterSession.getLastAccessedTime();
        assertNotSame(creationTime, lastAccessedTime);
        assertEquals(currentTime, lastAccessedTime);
    }

    public void testGetMaxInactiveInterval() {
    }

    public void testInvalidate() {
    }

    public void testRemoveAttribute() {
        EhcacheSession clusterSession = new EhcacheSession(localSession, new LocalContext());
        final String attr3 = "attr3";
        clusterSession.setAttribute(attr3,"aaa");
        assertEquals("aaa", clusterSession.getAttribute(attr3));
        clusterSession.removeAttribute(attr3);
        assertNull(clusterSession.getAttribute(attr3));
    }
    
    public static void main(String[] argv) {
        ClusterSessionTest1 test = new ClusterSessionTest1("test1");
        test.putAttributes();
    }
}
