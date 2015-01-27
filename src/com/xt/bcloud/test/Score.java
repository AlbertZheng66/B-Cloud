

package com.xt.bcloud.test;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author albert
 */
public class Score {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
            MemoryUsage usage1 = bean.getHeapMemoryUsage();
            MemoryUsage memUsage2 = bean.getNonHeapMemoryUsage();
            System.out.println("bean=" + bean);
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            osBean.getSystemLoadAverage();
            // com/sun/management/OperatingSystemMXBean 这个类里的信息更多

            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            threadBean.getCurrentThreadCpuTime();
            threadBean.getThreadCount();
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
            runtimeBean.getUptime();
            // mbsc.
        } catch (Exception ex) {
            Logger.getLogger(Score.class.getName()).log(Level.SEVERE, null, ex);
       }
       // mbsc.
    }

}
