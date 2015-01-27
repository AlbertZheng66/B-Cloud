
package com.xt.bcloud.bg.impl;


import com.xt.bcloud.comm.HeartBeating;
import com.xt.bcloud.resource.TaskDispatcher;
import com.xt.bcloud.resource.server.ServerState;
import com.xt.core.log.LogWriter;
import com.xt.core.utils.SqlUtils;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * 检查所有 MDU 的状态。
 * 1. 如果更新时间长时间未更改，将其设置为"停止"
 * FIXME:如果处于其他状态，是否也要检查。
 * @author Albert
 */
public class TdInspector extends AbstractInspector {

    private final Logger logger = Logger.getLogger(TdInspector.class);
    
    /**
     * 超出此间隔
     */
    private final static int interval = 2 * HeartBeating.HEART_BEATING_INTERVAL;
    
    public TdInspector() {
    }    

    public void excecute() {
        List<TaskDispatcher> runningServers = persistenceManager.findAll(TaskDispatcher.class);
        long currentTime = System.currentTimeMillis();
        for (Iterator<TaskDispatcher> it = runningServers.iterator(); it.hasNext();) {
            TaskDispatcher td = it.next();
            if (!td.isValid()) {
                continue;
            }
            Calendar lastUpdatedTime = td.getLastUpdatedTime();
            if (lastUpdatedTime == null) {
                LogWriter.warn2(logger, "任务分配服务器[%s]未定义更新时间", lastUpdatedTime);
                continue;
            }
            if ((currentTime - lastUpdatedTime.getTimeInMillis()) < interval) {
                // 正常实例
                continue;
            }
            if (ServerState.STOPED == td.getState()) {
                 // 检查是否已经可用
                 //CloudUtils.isisAlive(phyServer);
            } else {
                // 更新服务器状态->停止。
                LogWriter.warn2(logger, "任务分配服务器[%s]的状态由[%s]改为[%s]",
                        td, td.getState(), ServerState.STOPED);
                td.setState(ServerState.STOPED);
                persistenceManager.update(td);
            } 
        }
    }    
}

