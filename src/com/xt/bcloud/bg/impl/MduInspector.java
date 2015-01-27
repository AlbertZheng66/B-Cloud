
package com.xt.bcloud.bg.impl;

import com.xt.bcloud.comm.HeartBeating;
import com.xt.bcloud.mdu.PhyServer;
import com.xt.bcloud.mdu.PhyServerState;
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
public class MduInspector extends AbstractInspector {

    private final Logger logger = Logger.getLogger(MduInspector.class);
    
    /**
     * �����˼��
     */
    private final static int interval = 2 * HeartBeating.HEART_BEATING_INTERVAL;
    
    public MduInspector() {
    }    

    public void excecute() {
        List<PhyServer> runningServers = persistenceManager.findAll(PhyServer.class);
        long currentTime = System.currentTimeMillis();
        for (Iterator<PhyServer> it = runningServers.iterator(); it.hasNext();) {
            PhyServer phyServer = it.next();
            if (!phyServer.isValid()) {
                continue;
            }
            Calendar lastUpdatedTime = phyServer.getLastUpdatedTime();
            if (lastUpdatedTime == null) {
                LogWriter.warn2(logger, "���������[%s]δ�������ʱ��", lastUpdatedTime);
                continue;
            }
            if ((currentTime - lastUpdatedTime.getTimeInMillis()) < interval) {
                // ����ʵ��
                continue;
            }
            if (PhyServerState.STOPED == phyServer.getState()) {
                 // ����Ƿ��Ѿ�����
                 //CloudUtils.isisAlive(phyServer);
            } else {
                // ���·�����״̬->ֹͣ��
                LogWriter.warn2(logger, "�����������[%s]��״̬��[%s]��Ϊ[%s]",
                        phyServer, phyServer.getState(), PhyServerState.STOPED);
                phyServer.setState(PhyServerState.STOPED);
                persistenceManager.update(phyServer);
            } 
        }
    }    
}
