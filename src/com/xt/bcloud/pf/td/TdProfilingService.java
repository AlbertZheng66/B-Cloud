
package com.xt.bcloud.pf.td;

import com.xt.bcloud.mdu.MduService;
import com.xt.bcloud.pf.AbstractProfilingService;
import com.xt.bcloud.pf.ProfilingException;
import com.xt.bcloud.pf.server.ServerProfilingInfo;
import com.xt.bcloud.pf.server.mbeans.MBeanNames;
import com.xt.bcloud.resource.TaskDispatcher;
import com.xt.core.log.LogWriter;
import com.xt.core.proc.impl.InjectService;
import com.xt.core.service.LocalMethod;
import com.xt.core.utils.CollectionUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

/**
 *
 * @author Albert
 */
public class TdProfilingService extends AbstractProfilingService {
    
    private static final long serialVersionUID = -8269595659916795195L;
    
    @InjectService
    protected transient MduService mduService;
    
    public TdProfilingService() {
    }

    public List<ServerProfilingInfo> listTaskDispatchers() {
        List<TaskDispatcher> taskDsipatchers = mduService.listTaskDispatchers();
        // FIXME: �޳���ͬ�ķ�������There might be several application servers running on the save server��
        taskDsipatchers = CollectionUtils.unique(taskDsipatchers, new Comparator<TaskDispatcher>() {
            public int compare(TaskDispatcher o1, TaskDispatcher o2) {
                if (o1 == null || o2 == null || 
                        o1.getIp() == null || o2.getIp() == null){
                    return -1;
                }
                // FIXME: �����ƱȽϻ�����IP�Ƚ�׼ȷ�أ� 
                return (o1.getIp().equals(o2.getIp()) ? 0 : 1);
            }
        });
        LogWriter.info(logger, "listServers serverInfos=", taskDsipatchers);
        List<ServerProfilingInfo> pis = new ArrayList(taskDsipatchers.size());
        for (Iterator<TaskDispatcher> it = taskDsipatchers.iterator(); it.hasNext();) {
            TaskDispatcher taskDispatcher = it.next();
            ServerProfilingInfo pi = new ServerProfilingInfo();
            pi.load(taskDispatcher);
            read(taskDispatcher, pi);            
            pis.add(pi);
        }
        return pis;
    }

    private void read(TaskDispatcher taskDispatcher, ServerProfilingInfo pi) {
        MBeanServerConnection conn = getConnection(taskDispatcher);
        try {
            ServerProfilingInfo profileInfo = (ServerProfilingInfo)conn.getAttribute(new ObjectName(MBeanNames.SERVER_PROFILING), "ServerProfilingInfo");
            pi.setCpu(profileInfo.getCpu());
            pi.setDisks(profileInfo.getDisks());
            pi.setNetwork(profileInfo.getNetwork());
            pi.setMemory(profileInfo.getMemory());
        } catch (Exception ex) {
           throw new ProfilingException(String.format("��ȡ������[%s]����Ϣʱ�����쳣��", taskDispatcher.getId()), ex);
        }
        
    }

    @LocalMethod
    public MduService getMduService() {
        return mduService;
    }

    @LocalMethod
    public void setMduService(MduService mduService) {
        this.mduService = mduService;
    }
}
