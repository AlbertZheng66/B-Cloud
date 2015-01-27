
package com.xt.bcloud.pf;

import com.xt.bcloud.pf.jvm.MemoryUsage;
import com.xt.bcloud.pf.jvm.MemoryPoolInfo;
import com.xt.bcloud.app.App;
import com.xt.bcloud.app.AppInstance;
import com.xt.bcloud.app.AppService;
import com.xt.bcloud.pf.connector.RmiConnectorFactory;
import com.xt.bcloud.resource.server.ServerInfo;
import com.xt.core.exception.ServiceException;
import com.xt.core.log.LogWriter;
import com.xt.core.proc.impl.InjectService;
import com.xt.core.service.AbstractService;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.management.MBeanServerConnection;
import org.apache.log4j.Logger;

/**
 *
 * @author Albert
 * @deprecated 
 */
public class PerfilingService extends AbstractService {
    
    private final Logger logger = Logger.getLogger(PerfilingService.class);
    
    @InjectService
    private AppService appService;

    public PerfilingService() {
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
        MBeanServerConnection conn = RmiConnectorFactory.getInstance().getConnection(serverInfo.getIp(), serverInfo.getJmxRmiPort());
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
            throw new ServiceException("数据库出错", ex);
        }
        return memInfos;
    }
    
    private MemoryUsage convertMemoryUsage(java.lang.management.MemoryUsage mu) {
        return new MemoryUsage(mu.getInit(), mu.getUsed(), mu.getCommitted(), mu.getMax());
    }
    
    public ServerInfo getServerInfo(String serverOid) {
        ServerInfo serverInfo = (ServerInfo)getPersistenceManager().findByPK(ServerInfo.class, serverOid);
        return serverInfo;
    }

    public AppService getAppService() {
        return appService;
    }

    public void setAppService(AppService appService) {
        this.appService = appService;
    }
    
    
}