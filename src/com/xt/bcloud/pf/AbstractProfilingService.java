
package com.xt.bcloud.pf;

import com.xt.bcloud.pf.connector.RmiConnectorFactory;
import com.xt.bcloud.resource.ResourceService;
import com.xt.bcloud.resource.TaskDispatcher;
import com.xt.bcloud.resource.server.ServerInfo;
import com.xt.core.proc.impl.InjectService;
import com.xt.core.proc.impl.Injectable;
import com.xt.core.service.AbstractService;
import com.xt.core.service.LocalMethod;
import javax.management.MBeanServerConnection;

/**
 *
 * @author Albert
 */
abstract public class AbstractProfilingService extends AbstractService implements Injectable {
    private static final long serialVersionUID = -5377056766089132802L;
    
    
    @InjectService
    protected transient ResourceService resourceService;
    
    /**
     * 获得JMX连接信息
     *
     * @param serverInfo
     * @return
     */
    protected MBeanServerConnection getConnection(ServerInfo serverInfo) {
        MBeanServerConnection conn = RmiConnectorFactory.getInstance().getConnection(serverInfo.getIp(),
                serverInfo.getJmxRmiPort());
        return conn;
    }
    
    /**
     * 获得JMX连接信息
     *
     * @param serverInfo
     * @return
     */
    protected MBeanServerConnection getConnection(TaskDispatcher taskDispatcher) {
        MBeanServerConnection conn = RmiConnectorFactory.getInstance().getConnection(taskDispatcher.getIp(),
                taskDispatcher.getJmxRmiPort());
        return conn;
    }
    
    
    @LocalMethod
    public ResourceService getResourceService() {
        return resourceService;
    }

    @LocalMethod
    public void setResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }
    
}
