package com.xt.bcloud.bg.impl;

import com.xt.bcloud.comm.CloudUtils;
import com.xt.bcloud.mdu.*;
import com.xt.bcloud.resource.server.ServerInfo;
import com.xt.core.utils.SqlUtils;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * 此类用于检查物理服务器状态是否可用。
 *
 * @author Albert
 */
public class ServerInstanceInspector extends AbstractInspector {

    private final Logger logger = Logger.getLogger(ServerInstanceInspector.class);
    
//    /**
//     * 超出此间隔
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
                    // 检查他的心跳是否
                }
                
            } else {
                // FIMXE: 和ServerInfo(ServerStatusInspcetor)存在内在的天然联系,暂时在 ServerStatusInspcetor中处理
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
