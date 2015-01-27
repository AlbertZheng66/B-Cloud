package com.xt.bcloud.bg.impl;

import com.xt.bcloud.mdu.AppServerInstance;
import com.xt.bcloud.mdu.AppServerInstanceState;
import com.xt.bcloud.resource.EchoService;
import com.xt.bcloud.resource.server.ServerInfo;
import com.xt.bcloud.resource.server.ServerState;
import com.xt.core.log.LogWriter;
import com.xt.core.utils.EnumUtils;
import com.xt.core.utils.SqlUtils;
import com.xt.proxy.Proxy;
import com.xt.proxy.ServiceFactory;
import com.xt.proxy.impl.http.stream.HttpStreamProxy;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * ������Ѳ�����ڼ��������Ƿ�ɷ��ʣ�������ɷ��ʣ�����ӷ�������Դ����ɾ������������Ϊ�����ã���
 * @author albert
 */
public class ServerStatusInspector extends AbstractInspector {

    private final Logger logger = Logger.getLogger(ServerStatusInspector.class);

    public ServerStatusInspector() {
    }

    public void excecute() {
        List<ServerInfo> list = persistenceManager.findAll(ServerInfo.class, "(STATE IS NULL OR STATE<>?)",
                SqlUtils.getParams(EnumUtils.toString(ServerState.STOPED)), null);
        if (list.isEmpty()) {
            LogWriter.info(logger, "��ǰ���������вŷ�������");
            return;
        }
        for (Iterator<ServerInfo> it = list.iterator(); it.hasNext();) {
            ServerInfo serverInfo = it.next();
            LogWriter.info2(logger, "��������[%s]�Ƿ����......", serverInfo);
            if (!isAlive(serverInfo)) {
                LogWriter.info2(logger, "������[%s]�Ѿ������ã���������Ϊ��ֹͣ��״̬��", serverInfo);
                serverInfo.setState(ServerState.STOPED);
                serverInfo.setInvalidTime(Calendar.getInstance());
                persistenceManager.update(serverInfo);
                
                // ����AppServerInstance��״̬
                if (StringUtils.isNotEmpty(serverInfo.getAppServerInstanceOid())) {
                    AppServerInstance asInstance = persistenceManager.findByPK(AppServerInstance.class,
                            serverInfo.getAppServerInstanceOid());
                    if (asInstance != null) {
                        asInstance.setState(AppServerInstanceState.STOPED);
                        persistenceManager.update(asInstance);
                    }
                }
            }
        }
    }

    /**
     * ���������Ƿ��������С�
     * @param serverInfo
     * @return
     */
    private boolean isAlive(ServerInfo serverInfo) {
        if (serverInfo == null) {
            return true;
        }
        // ����Դ����������Դ
        String proxyUrl = String.format("http://%s:%d%s", serverInfo.getIp(),
                serverInfo.getManagerPort(), serverInfo.getContextPath());
        Proxy proxy = new HttpStreamProxy(proxyUrl);
        try {
            EchoService echoService = ServiceFactory.getInstance().getService(EchoService.class, proxy);
            if (null != echoService.echo("Hi")) {
                return true;
            }
        } catch (Throwable t) {
            // ignored
        }
        return false;
    }
}
