package com.xt.bcloud.pf.server.mbeans;

import java.io.Serializable;
import org.hyperic.sigar.Cpu;
import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.CpuPerc;

/**
 *
 * @author Albert
 */
public class CPUInfo implements Serializable {
    
    private static final long serialVersionUID = -2759730327399077860L;

    private CpuInfo[] cpuInfos;
    private CpuPerc[] cpuPercs;
    private Cpu[] cpus;

    public CPUInfo() {
    }

    public CpuInfo[] getCpuInfos() {
        return cpuInfos;
    }

    public void setCpuInfos(CpuInfo[] cpuInfos) {
        this.cpuInfos = cpuInfos;
    }

    public CpuPerc[] getCpuPercs() {
        return cpuPercs;
    }

    public void setCpuPercs(CpuPerc[] cpuPercs) {
        this.cpuPercs = cpuPercs;
    }

    public Cpu[] getCpus() {
        return cpus;
    }

    public void setCpus(Cpu[] cpus) {
        this.cpus = cpus;
    }

    @Override
    public String toString() {
        return "CPUInfo{" + "cpuInfos=" + cpuInfos + ", cpuPercs=" + cpuPercs + ", cpus=" + cpus + '}';
    }

    
}
