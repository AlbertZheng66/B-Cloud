
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
//     * ���������ķ�������
//     */
//    public static final String METHOD_HEART_BEAT = "heartBeat";

    public GroupConfService() {
    }

    /**
     * ����������Ƿ���Чʱ��ʹ��������ʽ���д����˷���������ָ��������ʱ�䡣
     * @param  entityId ��ʾ����������ı�ʶ
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
            LogWriter.warn2(logger, "���������[%s]�������Ѿ������ڡ�", entityId);
        }
    }

}
