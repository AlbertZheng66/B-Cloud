/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xt.bcloud.pf;

import java.util.ArrayList;
import java.util.List;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

/**
 *
 * @author Albert
 */
public class NetInterfaceDataTest {
    
    private NetInterfaceConfig config;  
    private NetInterfaceStat stat;  
    private long rxbps;  
    private long txbps;  
  
    public NetInterfaceDataTest() {}  
  
    public void populate(Sigar sigar, String name)  
        throws SigarException {  
  
        config = sigar.getNetInterfaceConfig(name);  
  
        try {  
              
            long start = System.currentTimeMillis();  
            NetInterfaceStat statStart = sigar.getNetInterfaceStat(name);  
            long rxBytesStart = statStart.getRxBytes();  
            long txBytesStart = statStart.getTxBytes();  
            Thread.sleep(1000);  
            long end = System.currentTimeMillis();  
            NetInterfaceStat statEnd = sigar.getNetInterfaceStat(name);  
            long rxBytesEnd = statEnd.getRxBytes();  
            long txBytesEnd = statEnd.getTxBytes();  
              
            rxbps = (rxBytesEnd - rxBytesStart)*8/(end-start)*1000;  
            txbps = (txBytesEnd - txBytesStart)*8/(end-start)*1000;  
            stat = sigar.getNetInterfaceStat(name);  
        } catch (SigarException e) {  
              
        } catch (Exception e) {  
              
        }  
    }  
  
    public static NetInterfaceDataTest gather(Sigar sigar, String name)  
        throws SigarException {  
      
        NetInterfaceDataTest data = new NetInterfaceDataTest();  
        data.populate(sigar, name);  
        return data;  
    }  
  
    public NetInterfaceConfig getConfig() {  
        return config;  
    }  
  
    public NetInterfaceStat getStat() {  
        return stat;  
    }  
      
      
      
    public long getRxbps() {  
        return rxbps;  
    }  
  
    public long getTxbps() {  
        return txbps;  
    }

    @Override
    public String toString() {
        return "NetInterfaceDataTest{" + "config=" + config + ", stat=" + stat + ", rxbps=" + rxbps + ", txbps=" + txbps + '}';
    }
  
    public static void main(String[] args) throws Exception {  
        Sigar sigar = new Sigar();  
        String[] netIfs = sigar.getNetInterfaceList();  
        List netIfList = new ArrayList();  
        for ( String name:netIfs ) {  
            NetInterfaceDataTest netIfData1 = NetInterfaceDataTest.gather(sigar, name);  
            netIfList.add(netIfData1);  
        }   
        System.out.println(netIfList);  
    }  
}
