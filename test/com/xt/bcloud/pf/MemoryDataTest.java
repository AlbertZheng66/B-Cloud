/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xt.bcloud.pf;

import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.Swap;

/**
 *
 * @author Albert
 */
public class MemoryDataTest {
    private Mem mem;  
    
    private Swap swap;  
  
    public MemoryDataTest() {  
    }  
  
    public void populate(Sigar sigar) throws SigarException {  
        mem = sigar.getMem();  
        swap = sigar.getSwap();  
    }  
  
    public static MemoryDataTest gather(Sigar sigar) throws SigarException {  
        MemoryDataTest data = new MemoryDataTest();  
        data.populate(sigar);  
        return data;  
    }

    @Override
    public String toString() {
        return "MemoryDataTest{" + "mem=" + mem + ", swap=" + swap + '}';
    }
    
    
      
    public static void main(String[] args) throws Exception {  
        Sigar sigar = new Sigar();  
        MemoryDataTest memData = MemoryDataTest.gather(sigar);  
        System.out.println(memData);  
    }  
    
}
