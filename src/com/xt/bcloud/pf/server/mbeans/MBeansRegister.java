package com.xt.bcloud.pf.server.mbeans;

import com.xt.core.app.init.SystemLifecycle;
import com.xt.core.log.LogWriter;
import java.lang.management.ManagementFactory;
import javax.management.ObjectName;
import org.apache.log4j.Logger;

/**
 * The class is in charge of resitering all managed beans at the process of
 * starting of the server.
 *
 * @author Albert
 */
public class MBeansRegister implements SystemLifecycle {

    private final Logger logger = Logger.getLogger(MBeansRegister.class);

    public MBeansRegister() {
    }

    /**
     * register all beans here.
     */
    public void onInit() {
        try {
            LogWriter.info2(logger, "正在注册MBean[%s]", MBeanNames.SERVER_MEMORY);
            MemoryMBean memMBean = new Memory();
            ManagementFactory.getPlatformMBeanServer().registerMBean(memMBean, new ObjectName(MBeanNames.SERVER_MEMORY));


            LogWriter.info2(logger, "正在注册MBean[%s]", MBeanNames.SERVER_FILE_SYSTEM);
            FileSystemMBean fsMBean = new FileSystem();
            ManagementFactory.getPlatformMBeanServer().registerMBean(fsMBean, new ObjectName(MBeanNames.SERVER_FILE_SYSTEM));


            LogWriter.info2(logger, "正在注册MBean[%s]", MBeanNames.SERVER_CPU);
            ServerCpuMBean cpuMBean = new ServerCpu();
            ManagementFactory.getPlatformMBeanServer().registerMBean(cpuMBean, new ObjectName(MBeanNames.SERVER_CPU));

            LogWriter.info2(logger, "正在注册MBean[%s]", MBeanNames.SERVER_NETWORK);
            NetworkMBean networkMBean = new Network();
            ManagementFactory.getPlatformMBeanServer().registerMBean(networkMBean, new ObjectName(MBeanNames.SERVER_NETWORK));
            
             LogWriter.info2(logger, "正在注册MBean[%s]", MBeanNames.SERVER_PROFILING);
            ServerProfilingMBean serverProfilingMBean = new ServerProfiling();
            ManagementFactory.getPlatformMBeanServer().registerMBean(serverProfilingMBean, new ObjectName(MBeanNames.SERVER_PROFILING));
        } catch (Exception ex) {
            LogWriter.warn(logger, "注册MBean时出现异常。", ex);
        }
    }

    /**
     * remove all registered beans.
     */
    public void onDestroy() {
        try {
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(new ObjectName(MBeanNames.SERVER_CPU));
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(new ObjectName(MBeanNames.SERVER_FILE_SYSTEM));
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(new ObjectName(MBeanNames.SERVER_MEMORY));
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(new ObjectName(MBeanNames.SERVER_NETWORK));
        } catch (Exception ex) {
            LogWriter.warn(logger, "卸载MBean时出现异常。", ex);
        }
    }
}
