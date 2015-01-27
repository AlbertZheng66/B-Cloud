
package com.xt.bcloud.pf.jvm;

import com.xt.bcloud.pf.ProfilingInfo;
import com.xt.gt.ui.table.ColumnInfo;

/**
 *
 * @author Albert
 */
public class JVMProfilingInfo extends ProfilingInfo {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 已运行时间
     */
    @ColumnInfo(title = "已运行时间")
    private long uptime;
    
    /**
     * 内存堆信息
     */
    @ColumnInfo(title = "内存堆信息")
    private MemoryInfo memoryInfo;
    
    /**
     * CPU 信息
     */
    @ColumnInfo(title = "CPU 信息")
    private CpuInfo cpuInfo;
    
    /**
     * 加载类信息
     */
    @ColumnInfo(title = "加载类信息")
    private ClassLoadingInfo classLoadingInfo;
    
    /**
     * 线程信息
     */
    @ColumnInfo(title = "线程信息")
    private ThreadInfo threadInfo;

    public JVMProfilingInfo() {
    }

    public ClassLoadingInfo getClassLoadingInfo() {
        return classLoadingInfo;
    }

    public void setClassLoadingInfo(ClassLoadingInfo classLoadingInfo) {
        this.classLoadingInfo = classLoadingInfo;
    }

    

    public CpuInfo getCpuInfo() {
        return cpuInfo;
    }

    public void setCpuInfo(CpuInfo cpuInfo) {
        this.cpuInfo = cpuInfo;
    }

    public MemoryInfo getMemoryInfo() {
        return memoryInfo;
    }

    public void setMemoryInfo(MemoryInfo memoryInfo) {
        this.memoryInfo = memoryInfo;
    }

    public long getUptime() {
        return uptime;
    }

    public void setUptime(long uptime) {
        this.uptime = uptime;
    }

    public ThreadInfo getThreadInfo() {
        return threadInfo;
    }

    public void setThreadInfo(ThreadInfo threadInfo) {
        this.threadInfo = threadInfo;
    }

    @Override
    public String toString() {
        return "JVMProfilingInfo{" + "uptime=" + uptime + ", memoryInfo=" + memoryInfo + ", cpuInfo=" + cpuInfo + ", classLoadingInfo=" + classLoadingInfo + ", threadInfo=" + threadInfo + '}';
    }
    
}
