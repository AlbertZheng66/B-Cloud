package com.xt.bcloud.worker;

import com.xt.bcloud.comm.Load;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;

/**
 * ���㸺�صĽӿڡ�����ͨ�� JMX ��ʽ���㱾 JVM �ĸ��������
 * @author albert
 */
public class LoadCalculator {

    public LoadCalculator() {
    }

    /**
     * ���㵱ǰϵͳ�ĸ��ء�
     * @return
     */
    public Load calculate() {
        Load load = new Load();
        MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
//            MemoryUsage usage1 = bean.getHeapMemoryUsage();
//            load.setMemoryUsage(usage1.getMax());

        // ��ʱδ����߷�ʱ�ڵ��ڴ�ʹ����
        MemoryUsage memUsage2 = bean.getNonHeapMemoryUsage();
        load.setMemoryUsage(memUsage2.getMax());
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        double average = osBean.getSystemLoadAverage();
        load.setAverage(average);

        // com/sun/management/OperatingSystemMXBean ����������Ϣ����

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        // threadBean.getCurrentThreadCpuTime();
        load.setThreadCount(threadBean.getThreadCount());
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        load.setUpTime(runtimeBean.getUptime());
        return load;
    }
}
