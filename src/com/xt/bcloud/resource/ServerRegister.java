package com.xt.bcloud.resource;

import com.xt.core.app.init.SystemLifecycle;
import com.xt.core.log.LogWriter;
import org.apache.log4j.Logger;

/**
 * ������ע�����������Ӧ������ʱ���Զ�����Դ����ע��������񣬲��ڷ������ر�ʱ�Զ�ע����
 *
 * @author albert
 */
public class ServerRegister implements SystemLifecycle {

    private final Logger logger = Logger.getLogger(ServerRegister.class);
    
    public ServerRegister() {
    }
    
    public void onInit() {
        try {
            ServerManager.getInstance().register(null);
        } catch (Throwable t) {
            LogWriter.warn2(logger, t, "ע��ʧ�ܡ�");
        }
    }

    public void onDestroy() {
        ServerManager.getInstance().unregister();
    }
}
