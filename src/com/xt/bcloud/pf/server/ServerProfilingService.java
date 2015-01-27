package com.xt.bcloud.pf.server;

import com.xt.bcloud.pf.AbstractProfilingService;
import com.xt.bcloud.pf.ProfilingException;
import com.xt.bcloud.pf.server.mbeans.MBeanNames;
import com.xt.bcloud.resource.server.ServerInfo;
import com.xt.core.log.LogWriter;
import com.xt.core.utils.CollectionUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javax.management.*;
import org.apache.log4j.Logger;

/**
 * �����ṩ���������ܱ���ķ���
 *
 * @author Albert
 */
public class ServerProfilingService extends AbstractProfilingService {
    
    private static final long serialVersionUID = -4037429847579941128L;
    
    public ServerProfilingService() {
    }

    public List<ServerProfilingInfo> listServers() {
        List<ServerInfo> serverInfos = resourceService.listAvailableServers();
        // �޳���ͬ�ķ�������There might be several application servers running on the save server��
        serverInfos = CollectionUtils.unique(serverInfos, new Comparator<ServerInfo>() {
            public int compare(ServerInfo o1, ServerInfo o2) {
                if (o1 == null || o2 == null || 
                        o1.getIp() == null || o2.getIp() == null){
                    return -1;
                }
                // FIXME: �����ƱȽϻ�����IP�Ƚ�׼ȷ�أ� 
                return (o1.getIp().equals(o2.getIp()) ? 0 : 1);
            }
        });
        LogWriter.info(logger, "listServers serverInfos=", serverInfos);
        List<ServerProfilingInfo> pis = new ArrayList(serverInfos.size());
        for (Iterator<ServerInfo> it = serverInfos.iterator(); it.hasNext();) {
            ServerInfo serverInfo = it.next();
            ServerProfilingInfo pi = new ServerProfilingInfo();
            pi.load(serverInfo);
            read(serverInfo, pi);            
            pis.add(pi);
        }
        return pis;
    }

    private void read(ServerInfo serverInfo, ServerProfilingInfo pi) {
        MBeanServerConnection conn = getConnection(serverInfo);
        try {
            ServerProfilingInfo profileInfo = (ServerProfilingInfo)conn.getAttribute(new ObjectName(MBeanNames.SERVER_PROFILING), "ServerProfilingInfo");
            pi.setCpu(profileInfo.getCpu());
            pi.setDisks(profileInfo.getDisks());
            pi.setNetwork(profileInfo.getNetwork());
            pi.setMemory(profileInfo.getMemory());
        } catch (Exception ex) {
           throw new ProfilingException(String.format("��ȡ������[%s]����Ϣʱ�����쳣��", serverInfo.getId()), ex);
        }
        
    }

}
