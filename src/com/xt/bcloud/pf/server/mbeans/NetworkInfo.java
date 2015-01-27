/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xt.bcloud.pf.server.mbeans;

import java.io.Serializable;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.NetInterfaceStat;

/**
 *
 * @author Albert
 */
public class NetworkInfo implements Serializable {
    private static final long serialVersionUID = -7073946950895534340L;
    
    private NetInterfaceConfig config;  
    
    private NetInterfaceStat stat;

    public NetworkInfo() {
    }
    

    public NetInterfaceConfig getConfig() {
        return config;
    }

    public void setConfig(NetInterfaceConfig config) {
        this.config = config;
    }

    public NetInterfaceStat getStat() {
        return stat;
    }

    public void setStat(NetInterfaceStat stat) {
        this.stat = stat;
    }

    @Override
    public String toString() {
        return "NetworkInfo{" + "config=" + config + ", stat=" + stat + '}';
    }
    
    
}
