
package com.xt.bcloud.comm;

import com.xt.bcloud.app.AppService;
import com.xt.bcloud.td.CattleManager;
import com.xt.core.exception.BadParameterException;
import org.apache.commons.lang.StringUtils;

/**
 * ����λ�������ڲ��ҷ������ڵ�λ�á�
 * @author albert
 */
public class ServiceLocator {

    /**
     * Ψһʵ��
     */
    private final static ServiceLocator instance = new ServiceLocator();

    private ServiceLocator() {
    }

    public static ServiceLocator getInstance() {
        return instance;
    }

    /**
     * ������Դ�����Ʋ���һ������
     * @param name ��Դ������
     * @return �����λ��
     */
    public String find(String name) {
        if (StringUtils.isEmpty(name)) {
            throw new BadParameterException("��Դ�����Ʋ���Ϊ�ա�");
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
