
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
     * ������ʱ��
     */
    @ColumnInfo(title = "������ʱ��")
    private long uptime;
    
    /**
     * �ڴ����Ϣ
     */
    @ColumnInfo(title = "�ڴ����Ϣ")
    private MemoryInfo memoryInfo;
    
    /**
     * CPU ��Ϣ
     */
    @ColumnInfo(title = "CPU ��Ϣ")
    private CpuInfo cpuInfo;
    
    /**
     * ��������Ϣ
     */
    @ColumnInfo(title = "��������Ϣ")
    private ClassLoadingInfo classLoadingInfo;
    
    /**
     * �߳���Ϣ
     */
    @ColumnInfo(title = "�߳���Ϣ")
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
