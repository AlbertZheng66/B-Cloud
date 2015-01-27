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
     * 日志实例
     */
    private final static Logger logger = Logger.getLogger(HeartBeating.class);
    /**
     * 心跳包的发送间隔
     */
    public final static int HEART_BEATING_INTERVAL = SystemConfiguration.getInstance().readInt("heatBeating.interval", 5000);
    /**
     * 用于发送心跳信息的定时器
     */
    private final Timer beatingTimer = new Timer(true);

    public HeartBeating() {
    }
    
    public void start(final Runnable runnable) {
        // 启动心跳定时器
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
            LogWriter.info2(logger, "停止心跳程序", beatingTimer);
            beatingTimer.cancel();
        } catch (Throwable t) {
            LogWriter.info2(logger, "停止心跳程序时出现异常", beatingTimer);
        }
    }
}
