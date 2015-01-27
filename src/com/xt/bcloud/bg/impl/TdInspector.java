
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
 * ������� MDU ��״̬��
 * 1. �������ʱ�䳤ʱ��δ���ģ���������Ϊ"ֹͣ"
 * FIXME:�����������״̬���Ƿ�ҲҪ��顣
 * @author Albert
 */
public class TdInspector extends AbstractInspector {

    private final Logger logger = Logger.getLogger(TdInspector.class);
    
    /**
     * �����˼��
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
                LogWriter.warn2(logger, "������������[%s]δ�������ʱ��", lastUpdatedTime);
                continue;
            }
            if ((currentTime - lastUpdatedTime.getTimeInMillis()) < interval) {
                // ����ʵ��
                continue;
            }
            if (ServerState.STOPED == td.getState()) {
                 // ����Ƿ��Ѿ�����
                 //CloudUtils.isisAlive(phyServer);
            } else {
                // ���·�����״̬->ֹͣ��
                LogWriter.warn2(logger, "������������[%s]��״̬��[%s]��Ϊ[%s]",
                        td, td.getState(), ServerState.STOPED);
                td.setState(ServerState.STOPED);
                persistenceManager.update(td);
            } 
        }
    }    
}

