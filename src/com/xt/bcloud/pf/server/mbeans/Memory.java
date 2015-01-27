
package com.xt.bcloud.pf.server.mbeans;

import com.xt.bcloud.pf.ProfilingException;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.Swap;

/**
 *
 * @author Albert
 */
public class Memory implements MemoryMBean {
    
//    private Mem mem;  
//    
//   private Swap swap;

    private final Sigar sigar = new Sigar(); 
    
    public Memory() {
    }
    
    public Mem getMem() {
        try {
            return sigar.getMem();
        } catch (SigarException ex) {
            throw new ProfilingException("读取服务器内存时出现异常。", ex);
        }
    }

    public Swap getSwap() {
        try {
            return sigar.getSwap();
        } catch (SigarException ex) {
            throw new ProfilingException("读取服务器交换内存时出现异常。", ex);
        }
    }
//    
//    private void init() throws SigarException {  
//        Sigar sigar = new Sigar();  
//        mem = sigar.getMem();  
//        swap = sigar.getSwap();  
//    }  
    
}
