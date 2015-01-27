package com.xt.bcloud.comm;

import com.xt.core.log.LogWriter;
import com.xt.gt.sys.SystemConfiguration;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.Logger;

/**
 * This class is used to send heat-beating singals to the server regularly.
 *
 * @author Albert
 */
public class HeartBeating {

    /**
     * ��־ʵ��
     */
    private final static Logger logger = Logger.getLogger(HeartBeating.class);
    /**
     * �������ķ��ͼ��
     */
    public final static int HEART_BEATING_INTERVAL = SystemConfiguration.getInstance().readInt("heatBeating.interval", 5000);
    /**
     * ���ڷ���������Ϣ�Ķ�ʱ��
     */
    private final Timer beatingTimer = new Timer(true);

    public HeartBeating() {
    }
    
    public void start(final Runnable runnable) {
        // ����������ʱ��
        beatingTimer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                if (runnable != null) {
                    runnable.run();
                }
            }
        },
                HEART_BEATING_INTERVAL, HEART_BEATING_INTERVAL);
    }

    public void cancel() {
        try {
            // 
            LogWriter.info2(logger, "ֹͣ��������", beatingTimer);
            beatingTimer.cancel();
        } catch (Throwable t) {
            LogWriter.info2(logger, "ֹͣ��������ʱ�����쳣", beatingTimer);
        }
    }
}
