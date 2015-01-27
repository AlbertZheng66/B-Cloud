
package com.xt.bcloud.comm;

import com.xt.bcloud.app.AppService;
import com.xt.bcloud.td.CattleManager;
import com.xt.core.exception.BadParameterException;
import org.apache.commons.lang.StringUtils;

/**
 * 服务定位器，用于查找服务所在的位置。
 * @author albert
 */
public class ServiceLocator {

    /**
     * 唯一实例
     */
    private final static ServiceLocator instance = new ServiceLocator();

    private ServiceLocator() {
    }

    public static ServiceLocator getInstance() {
        return instance;
    }

    /**
     * 根据资源的名称查找一个服务。
     * @param name 资源的名称
     * @return 服务的位置
     */
    public String find(String name) {
        if (StringUtils.isEmpty(name)) {
            throw new BadParameterException("资源的名称不能为空。");
        }
        if (name.equals(AppService.APP_MGR_URL)) {
            String proxyUrl = String.format("http://127.0.0.1:%d%s", AppService.APP_MGR_PORT,
                AppService.APP_MGR_CONTEXT_PATH);
            return proxyUrl;
        } else if (CattleManager.TASK_DISPATCHERS_GROUP_CONFIG.equals(name)) {
            return "E:\\work\\xthinker\\B-Cloud\\src\\files\\task_dispatchers_group.xml";
        }
        
        return null;
    }

}
