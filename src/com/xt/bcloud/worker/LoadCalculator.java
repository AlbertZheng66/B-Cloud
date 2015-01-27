package com.xt.bcloud.worker;

import com.xt.bcloud.comm.Load;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;

/**
 * 计算负载的接口。此类通过 JMX 方式计算本 JVM 的负载情况。
 * @author albert
 */
public class LoadCalculator {

    public LoadCalculator() {
    }

    /**
     * 就算当前系统的负载。
     * @return
     */
    public Load calculate() {
        Load load = new Load();
        MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
//            MemoryUsage usage1 = bean.getHeapMemoryUsage();
//            load.setMemoryUsage(usage1.getMax());

        // 暂时未计算高峰时期的内存使用率
        MemoryUsage memUsage2 = bean.getNonHeapMemoryUsage();
        load.setMemoryUsage(memUsage2.getMax());
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        double average = osBean.getSystemLoadAverage();
        load.setAverage(average);

        // com/sun/management/OperatingSystemMXBean 这个类里的信息更多

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        // threadBean.getCurrentThreadCpuTime();
        load.setThreadCount(threadBean.getThreadCount());
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        load.setUpTime(runtimeBean.getUptime());
        return load;
    }
}
