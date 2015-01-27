package com.xt.bcloud.pf.app;

import com.xt.bcloud.pf.jvm.ClassLoadingInfo;
import com.xt.bcloud.pf.jvm.CpuInfo;
import com.xt.bcloud.pf.jvm.MemoryInfo;
import com.xt.bcloud.pf.jvm.ThreadInfo;
import com.xt.gt.ui.table.ColumnInfo;
import java.io.Serializable;

/**
 *
 * @author Albert
 */
public class AppProfilingInfo implements Serializable {

    private static final long serialVersionUID = 2000029891334067287L;
    /**
     * ����һ��Ӧ�õ��ڲ���ʶ��
     */
    private String oid;
    /**
     * һ���ı�ʶһ��Ӧ��
     */
    @ColumnInfo(title = "Ӧ�ñ���")
    private String id;
    /**
     * Ӧ������
     */
    @ColumnInfo(title = "����")
    private String name;
    /**
     * Ӧ�ð汾����
     */
    private String appVersion;
    /**
     * ʵ������
     */
    private int instanceCount = 0;
    /**
     * ������ʱ��
     */
    @ColumnInfo(title = "������ʱ��")
    private long uptime = 0;
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

    public AppProfilingInfo() {
    }

    public void add(long uptime) {
        this.uptime = Math.max(this.uptime, uptime);
    }

    public void add(ClassLoadingInfo classLoadingInfo) {
        if (this.classLoadingInfo == null) {
            this.classLoadingInfo = classLoadingInfo;
        } else {
            this.classLoadingInfo.add(classLoadingInfo);
        }
    }

    public void add(MemoryInfo memoryInfo) {
        if (this.memoryInfo == null) {
            this.memoryInfo = memoryInfo;
        } else {
            this.memoryInfo.add(memoryInfo);
        }
    }

    public void add(CpuInfo cpuInfo) {
        if (this.cpuInfo == null) {
            this.cpuInfo = cpuInfo;
        } else {
            this.cpuInfo.add(cpuInfo);
        }
    }

    public void add(ThreadInfo threadInfo) {
        if (this.threadInfo == null) {
            this.threadInfo = threadInfo;
        } else {
            this.threadInfo.add(threadInfo);
        }
    }

    public void addInstanceCount() {
        this.instanceCount++;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getInstanceCount() {
        return instanceCount;
    }

    public void setInstanceCount(int instanceCount) {
        this.instanceCount = instanceCount;
    }

    public MemoryInfo getMemoryInfo() {
        return memoryInfo;
    }

    public void setMemoryInfo(MemoryInfo memoryInfo) {
        this.memoryInfo = memoryInfo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public ThreadInfo getThreadInfo() {
        return threadInfo;
    }

    public void setThreadInfo(ThreadInfo threadInfo) {
        this.threadInfo = threadInfo;
    }

    public long getUptime() {
        return uptime;
    }

    public void setUptime(long uptime) {
        this.uptime = uptime;
    }
}
