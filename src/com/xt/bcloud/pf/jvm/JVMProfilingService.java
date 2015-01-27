package com.xt.bcloud.pf.jvm;

import com.xt.bcloud.pf.AbstractProfilingService;
import com.xt.bcloud.app.App;
import com.xt.bcloud.app.AppInstance;
import com.xt.bcloud.pf.connector.RmiConnectorFactory;
import com.xt.bcloud.resource.ResourceService;
import com.xt.bcloud.resource.server.ServerInfo;
import com.xt.core.exception.ServiceException;
import com.xt.core.log.LogWriter;
import com.xt.core.proc.impl.InjectService;
import com.xt.core.proc.impl.Injectable;
import com.xt.core.service.AbstractService;
import java.io.IOException;
import java.lang.management.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.management.MBeanServerConnection;
import org.apache.log4j.Logger;

/**
 * 用于提供服务器性能报告的服务。
 *
 * @author Albert
 */
public class JVMProfilingService extends AbstractProfilingService {
    private static final long serialVersionUID = -27110947620289909L;

    private final Logger logger = Logger.getLogger(JVMProfilingService.class);
    
    @InjectService
    private transient ResourceService resourceService;

    public JVMProfilingService() {
    }

    public List<JVMProfilingInfo> listServers() {
        List<ServerInfo> serverInfos = resourceService.listAvailableServers();
        LogWriter.info(logger, "listServers serverInfos=", serverInfos);
        List<JVMProfilingInfo> pis = new ArrayList(serverInfos.size());

        for (Iterator<ServerInfo> it = serverInfos.iterator(); it.hasNext();) {
            ServerInfo serverInfo = it.next();
            JVMProfilingInfo profilingInfo = new JVMProfilingInfo();
            profilingInfo.load(serverInfo);
            MBeanServerConnection conn = getConnection(serverInfo);
            
            long uptime = readUptime(conn, serverInfo); 
            profilingInfo.setUptime(uptime);
            
            // 类信息
            ClassLoadingInfo classLoadingInfo = readClassesInfo(conn, serverInfo);
            profilingInfo.setClassLoadingInfo(classLoadingInfo);
            
            // CPU 信息
            CpuInfo cpuInfo = readCpuInfo(conn, serverInfo);
            profilingInfo.setCpuInfo(cpuInfo);
            
            // 内存信息
            MemoryInfo memoryInfo = readMemoryInfo(conn, serverInfo);
            profilingInfo.setMemoryInfo(memoryInfo);
            
            // 线程信息
            ThreadInfo threadInfo = readThreadInfo(conn, serverInfo);
            profilingInfo.setThreadInfo(threadInfo);

            pis.add(profilingInfo);
        }
        return pis;
    }

    

    private MemoryInfo readMemoryInfo(MBeanServerConnection conn, ServerInfo serverInfo) {
        MemoryInfo memoryInfo = new MemoryInfo();
        try {
            MemoryMXBean mbean = ManagementFactory.getPlatformMXBean(conn, MemoryMXBean.class);
            memoryInfo.setHeapMemoryUsage(convertMemoryUsage(mbean.getHeapMemoryUsage()));
            memoryInfo.setNonHeapMemoryUsage(convertMemoryUsage(mbean.getNonHeapMemoryUsage()));
        } catch (IOException ex) {
            throw new ServiceException(String.format("读取服务器[%s]端口[%d]的内存信息时出现异常。",
                    serverInfo.getIp(), serverInfo.getJmxRmiPort()), ex);
        }
        return memoryInfo;
    }

    private List<MemoryPoolInfo> readMemoryPoolInfo(MBeanServerConnection conn, ServerInfo serverInfo) {
        List<MemoryPoolInfo> memInfos = new ArrayList();
        try {
            List<MemoryPoolMXBean> xb = ManagementFactory.getPlatformMXBeans(conn, MemoryPoolMXBean.class);
            for (Iterator<MemoryPoolMXBean> it = xb.iterator(); it.hasNext();) {
                MemoryPoolMXBean mbean = it.next();
                MemoryPoolInfo memoryInfo = new MemoryPoolInfo();
                memoryInfo.setName(mbean.getName());
                memoryInfo.setType(mbean.getType());
                memoryInfo.setPeakUsage(convertMemoryUsage(mbean.getPeakUsage()));
                memoryInfo.setUsage(convertMemoryUsage(mbean.getUsage()));
                memInfos.add(memoryInfo);
            }
        } catch (IOException ex) {
            throw new ServiceException(String.format("读取服务器[%s]端口[%d]的内存信息时出现异常。",
                    serverInfo.getIp(), serverInfo.getJmxRmiPort()), ex);
        }
        return memInfos;
    }

    private long readUptime(MBeanServerConnection conn, ServerInfo serverInfo) {
        try {
            RuntimeMXBean mbean = ManagementFactory.getPlatformMXBean(conn, RuntimeMXBean.class);
            return mbean.getUptime();
        } catch (IOException ex) {
            throw new ServiceException(String.format("读取服务器[%s]端口[%d]的内存信息时出现异常。",
                    serverInfo.getIp(), serverInfo.getJmxRmiPort()), ex);
        }
    }

    private CpuInfo readCpuInfo(MBeanServerConnection conn, ServerInfo serverInfo) {
        CpuInfo cpuInfo = new CpuInfo();
        try {

            OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(conn, OperatingSystemMXBean.class);
            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                com.sun.management.OperatingSystemMXBean mxbean =
                        (com.sun.management.OperatingSystemMXBean) osBean;
                cpuInfo.setProcessCpuLoad(mxbean.getProcessCpuLoad());
                cpuInfo.setProcessCpuTime(mxbean.getProcessCpuTime());
                cpuInfo.setSystemCpuLoad(mxbean.getSystemCpuLoad());
                cpuInfo.setTotalSwapSpaceSize(mxbean.getTotalSwapSpaceSize());
            }

        } catch (IOException ex) {
            throw new ServiceException(String.format("读取服务器[%s]端口[%d]的内存信息时出现异常。",
                    serverInfo.getIp(), serverInfo.getJmxRmiPort()), ex);
        }
        return cpuInfo;
    }

    private ThreadInfo readThreadInfo(MBeanServerConnection conn, ServerInfo serverInfo) {
        ThreadInfo threadInfo = new ThreadInfo();
        try {
            ThreadMXBean mbean = ManagementFactory.getPlatformMXBean(conn, ThreadMXBean.class);
            threadInfo.setDeamon(mbean.getDaemonThreadCount());
            threadInfo.setLive(mbean.getThreadCount());
            threadInfo.setLivePeak(mbean.getPeakThreadCount());
            threadInfo.setTotalStarted(mbean.getTotalStartedThreadCount());
        } catch (IOException ex) {
            throw new ServiceException(String.format("读取服务器[%s]端口[%d]的内存信息时出现异常。",
                    serverInfo.getIp(), serverInfo.getJmxRmiPort()), ex);
        }
        return threadInfo;
    }

    private ClassLoadingInfo readClassesInfo(MBeanServerConnection conn, ServerInfo serverInfo) {
        ClassLoadingInfo classInfo = new ClassLoadingInfo();
        try {
            ClassLoadingMXBean mbean = ManagementFactory.getPlatformMXBean(conn, ClassLoadingMXBean.class);
            // threadInfo.setSharedLoaded(mbean.get)Deamon(mbean.getDaemonThreadCount());
            classInfo.setTotalLoaded(mbean.getLoadedClassCount());
            classInfo.setTotalUnloaded(mbean.getUnloadedClassCount());
            classInfo.setTotalLoadedClassCount(mbean.getTotalLoadedClassCount());
        } catch (IOException ex) {
            throw new ServiceException(String.format("读取服务器[%s]端口[%d]的内存信息时出现异常。",
                    serverInfo.getIp(), serverInfo.getJmxRmiPort()), ex);
        }
        return classInfo;
    }

    public List<MemoryPoolInfo> listMemoryInfo(App app) {
        return Collections.EMPTY_LIST;
    }

    public List<MemoryPoolInfo> listMemoryInfoOfInstance(App app, AppInstance instance) {
        String serverOid = instance.getServerOid();
        ServerInfo serverInfo = getServerInfo(serverOid);
        if (serverInfo == null) {
            throw new ServiceException(String.format("服务器资源[%s]未找到。", serverOid));
        }
        if (serverInfo.getJmxRmiPort() < 0) {
            LogWriter.warn2(logger, String.format("服务器资源[%s]未定义 RMI 端口。", serverOid));
            return Collections.EMPTY_LIST;
        }
        List<MemoryPoolInfo> memInfos = new ArrayList();
        MBeanServerConnection conn = getConnection(serverInfo);
        try {
            List<MemoryPoolMXBean> xb = ManagementFactory.getPlatformMXBeans(conn, MemoryPoolMXBean.class);
            for (Iterator<MemoryPoolMXBean> it = xb.iterator(); it.hasNext();) {
                MemoryPoolMXBean mbean = it.next();
                MemoryPoolInfo memoryInfo = new MemoryPoolInfo();
                memoryInfo.setName(mbean.getName());
                memoryInfo.setType(mbean.getType());
//                memoryInfo.setPeakUsage(convertMemoryUsage(mbean.getPeakUsage()));
//                memoryInfo.setUsage(convertMemoryUsage(mbean.getUsage()));
                memInfos.add(memoryInfo);
            }
        } catch (IOException ex) {
            throw new ServiceException("数据库出错", ex);
        }
        return memInfos;
    }

    private MemoryUsage convertMemoryUsage(java.lang.management.MemoryUsage mu) {
        return new MemoryUsage(mu.getInit(), mu.getUsed(), mu.getCommitted(), mu.getMax());
    }

    public ServerInfo getServerInfo(String serverOid) {
        ServerInfo serverInfo = (ServerInfo) getPersistenceManager().findByPK(ServerInfo.class, serverOid);
        return serverInfo;
    }

    public ResourceService getResourceService() {
        return resourceService;
    }

    public void setResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }
    
    
}
