/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xt.bcloud.pf;

import org.hyperic.sigar.Cpu;
import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

/**
 *
 * @author Albert
 */
public class CpuInfoTest {
    private CpuInfo info;  
    private CpuPerc perc;  
    private Cpu timer;  
  
    public CpuInfoTest() {  
    }  
  
    public void populate(Sigar sigar) throws SigarException {  
        info = sigar.getCpuInfoList()[0];  
        perc = sigar.getCpuPerc();  
        timer = sigar.getCpu();  
    }

    @Override
    public String toString() {
        
        return "SigarTest{" + "info=" + info + ", perc=" + perc + ", timer=" + timer + '}';
    }
  
    
  
    public static void main(String[] args) throws Exception {  
        System.out.println("java.library.path=" + System.getProperty("java.library.path")); 
        
        Sigar sigar = new Sigar();  
        CpuInfoTest st = new CpuInfoTest();
        st.populate(sigar);  
        System.out.println(st);  
    }  
}
