
package com.xt.bcloud.mdu.command;

import com.xt.core.log.LogWriter;
import com.xt.core.utils.DateUtils;
import java.io.Serializable;
import java.util.Calendar;
import org.apache.log4j.Logger;

/**
 * 测试服务器是否可用
 * @author Albert
 */
public class HeatBeatingCommand  extends Command {
    
    public final static String HEART_BEAT = "heartBeat";
    
    public HeatBeatingCommand() {
        this.name = HEART_BEAT;
    }

    @Override
    public Serializable execute() {
        LogWriter.info2(logger, "testing the client[%s]", DateUtils.toDateTimeStr(Calendar.getInstance()));
        return true;
    }
}
