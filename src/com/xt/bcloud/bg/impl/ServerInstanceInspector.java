package com.xt.bcloud.bg.impl;

import com.xt.bcloud.comm.CloudUtils;
import com.xt.bcloud.mdu.*;
import com.xt.bcloud.resource.server.ServerInfo;
import com.xt.core.utils.SqlUtils;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * �������ڼ�����������״̬�Ƿ���á�
 *
 * @author Albert
 */
public class ServerInstanceInspector extends AbstractInspector {

    private final Logger logger = Logger.getLogger(ServerInstanceInspector.class);
    
//    /**
//     * �����˼��
//     */
//    private final static int interval = 2 * MduManager.HEART_BEATING_INTERVAL;

    public ServerInstanceInspector() {
    }

    public void excecute() {
        List<AppServerInstance> runningServers = persistenceManager.findAll(AppServerInstance.class,
                "valid = ?", SqlUtils.getParams("y"), null);
        long currentTime = System.currentTimeMillis();
        for (Iterator<AppServerInstance> it = runningServers.iterator(); it.hasNext();) {
            AppServerInstance asInstance = it.next();
            AppServerInstanceState asState = asInstance.getState();
            if (asState == AppServerInstanceState.STOPED) {
                if (asInstance.getServerType() == ServerType.APP_SERVER) {
                    // ������������Ƿ�
                }
                
            } else {
                // FIMXE: ��ServerInfo(ServerStatusInspcetor)�������ڵ���Ȼ��ϵ,��ʱ�� ServerStatusInspcetor�д���
                // CloudUtils.isAlive(null)live();
            }
        }
    }
    
    private boolean isAlive(AppServerInstance asInstance) {
        ServerInfo serverInfo = persistenceManager.findFirst(ServerInfo.class,
                "app_Server_Instance_Oid=?", SqlUtils.getParams(asInstance.getOid()), null);
        return CloudUtils.isAlive(serverInfo);
    }
}
