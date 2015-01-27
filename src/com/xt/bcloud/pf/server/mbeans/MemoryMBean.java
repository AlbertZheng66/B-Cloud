
package com.xt.bcloud.pf.server.mbeans;

import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Swap;

/**
 *
 * @author Albert
 */
public interface MemoryMBean {
     
     public Mem getMem();  
     
     public Swap getSwap();  
}
