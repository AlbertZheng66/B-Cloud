
package com.xt.bcloud.resource;

import com.xt.core.log.LogWriter;
import com.xt.core.service.AbstractService;
import com.xt.core.utils.SqlUtils;
import java.util.Calendar;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author albert
 */
public class GroupConfService extends AbstractService {
    
    private static final long serialVersionUID = -3592992896296908259L;

    private transient final Logger logger = Logger.getLogger(GroupConfService.class);

//    /**
//     * 心跳方法的方法名称
//     */
//    public static final String METHOD_HEART_BEAT = "heartBeat";

    public GroupConfService() {
    }

    /**
     * 在任务管理是否有效时，使用心跳方式进行处理。此方法将更新指定的最新时间。
     * @param  entityId 表示任务管理器的标识
     */
    public void heartBeat(String entityId) {
        if (StringUtils.isEmpty(entityId)) {
            return;
        }
        GroupConf groupConf = persistenceManager.findFirst(GroupConf.class, "ENTITY_ID=?", SqlUtils.getParams(entityId), null);
        if (groupConf != null) {
            groupConf.setLastUpdateTime(Calendar.getInstance());
            persistenceManager.update(groupConf);
        } else {
            LogWriter.warn2(logger, "任务管理器[%s]的数据已经不存在。", entityId);
        }
    }

}
