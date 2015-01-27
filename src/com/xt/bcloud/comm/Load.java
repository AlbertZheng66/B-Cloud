
package com.xt.bcloud.comm;

import java.io.Serializable;

/**
 * 描述一个资源或者一头"牛"的负载.
 * @author albert
 */
public class Load  implements Serializable {

    private float cpuUsage = -1;         // CPU 的资源占用率(当前)

    private float memoryUsage = -1;      // 内存的资源占用率(当前)，非峰值的最大

    private long upTime = -1;            // 已经运行时间（时间）

    private int responseTime = -1;      // 平均响应时间（毫秒）

    private double average = -1;         // the system load average for the last minute

    private int threadCount = -1;       // 当前虚拟机的线程数

    private float level  = 1;      // 机器性能的加权数（经验值）

    private long statTime = -1;      // 统计时间

    public Load() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Load other = (Load) obj;
        return other == this;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder strBld = new StringBuilder();
        strBld.append(super.toString()).append("[");
        strBld.append("cpuUsage=").append(cpuUsage).append("; ");
        strBld.append("memoryUsage=").append(memoryUsage).append("; ");
        strBld.append("upTime=").append(upTime).append("; ");
        strBld.append("threadCount=").append(threadCount).append("; ");
        strBld.append("average=").append(average).append("; ");
        strBld.append("statTime=").append(statTime);
        strBld.append("]");
        return strBld.toString();
    }

    public float getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(float cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public float getLevel() {
        return level;
    }

    public void setLevel(float level) {
        this.level = level;
    }

    public float getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(float memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public int getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(int responseTime) {
        this.responseTime = responseTime;
    }

    public long getStatTime() {
        return statTime;
    }

    public void setStatTime(long statTime) {
        this.statTime = statTime;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public long getUpTime() {
        return upTime;
    }

    public void setUpTime(long upTime) {
        this.upTime = upTime;
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }
}
