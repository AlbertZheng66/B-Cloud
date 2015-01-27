
package com.xt.bcloud.pf.jvm;

import java.io.Serializable;

/**
 *
 * @author Albert
 */
public class CpuInfo implements Serializable {
    private static final long serialVersionUID = 660357275030986670L;
    
    
//    
//    /**
//     * 系统使用率
//     */
//    private int sysUsage;
//    
//    /**
//     * 用户使用率
//     */
//    private int userUsage;
//    
//    /**
//     * 空闲使用率
//     */
//    private int idle;
//    
//    /**
//     * 等待
//     */
//    private int wait;
    
//    private long committedVirtualMemorySize;

    private long totalSwapSpaceSize = 0;

//    private long freeSwapSpaceSize;

    private long processCpuTime = 0;

//    private long freePhysicalMemorySize;

//    private long totalPhysicalMemorySize;

    private double systemCpuLoad = 0;

    private double processCpuLoad = 0;

    public CpuInfo() {
    }
    
    public void add(CpuInfo cpuInfo) {
        if (cpuInfo == null) {
            return;
        }
        this.totalSwapSpaceSize += cpuInfo.totalSwapSpaceSize;
        this.processCpuTime     += cpuInfo.processCpuTime;
        this.systemCpuLoad      += cpuInfo.systemCpuLoad;
        this.processCpuLoad     += cpuInfo.processCpuLoad;
    }

    public double getProcessCpuLoad() {
        return processCpuLoad;
    }

    public void setProcessCpuLoad(double processCpuLoad) {
        this.processCpuLoad = processCpuLoad;
    }

    public long getProcessCpuTime() {
        return processCpuTime;
    }

    public void setProcessCpuTime(long processCpuTime) {
        this.processCpuTime = processCpuTime;
    }

    public double getSystemCpuLoad() {
        return systemCpuLoad;
    }

    public void setSystemCpuLoad(double systemCpuLoad) {
        this.systemCpuLoad = systemCpuLoad;
    }

    public long getTotalSwapSpaceSize() {
        return totalSwapSpaceSize;
    }

    public void setTotalSwapSpaceSize(long totalSwapSpaceSize) {
        this.totalSwapSpaceSize = totalSwapSpaceSize;
    }

    @Override
    public String toString() {
        return "CpuInfo{" + "totalSwapSpaceSize=" + totalSwapSpaceSize + ", processCpuTime=" + processCpuTime + ", systemCpuLoad=" + systemCpuLoad + ", processCpuLoad=" + processCpuLoad + '}';
    }

}
