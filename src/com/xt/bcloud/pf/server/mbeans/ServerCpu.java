/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xt.bcloud.pf.server.mbeans;

import com.xt.bcloud.pf.ProfilingException;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

/**
 *
 * @author Albert
 */
public class ServerCpu implements ServerCpuMBean {

    private final Sigar sigar = new Sigar();

    public ServerCpu() {
    }

    public CPUInfo getCpuInfo() {
        CPUInfo cpuInfo = new CPUInfo();
        try {
            cpuInfo.setCpuInfos(sigar.getCpuInfoList());
            cpuInfo.setCpuPercs(sigar.getCpuPercList());
            cpuInfo.setCpus(sigar.getCpuList());
        } catch (SigarException ex) {
            throw new ProfilingException("读取服务器 CPU 信息时出现异常。", ex);
        }
        return cpuInfo;
    }
}
