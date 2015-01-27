

package com.xt.bcloud.td;

import com.xt.comm.quartz.AbstractQuartzService;
import org.apache.log4j.Logger;

/**
 * 用心跳的方式表示任务管理器仍然健康（存在）。
 * @author albert
 */
public class TaskManagerHeartBeatService extends AbstractQuartzService {


    private final Logger logger = Logger.getLogger(TaskManagerHeartBeatService.class);

    @Override
    public void run() {

    }

}
