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
 * 服务器巡查用于检测服务器是否可访问，如果不可访问，则将其从服务器资源表中删除（或者设置为不可用）。
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
            LogWriter.info(logger, "当前无正在运行才服务器。");
            return;
        }
        for (Iterator<ServerInfo> it = list.iterator(); it.hasNext();) {
            ServerInfo serverInfo = it.next();
            LogWriter.info2(logger, "检查服务器[%s]是否可用......", serverInfo);
            if (!isAlive(serverInfo)) {
                LogWriter.info2(logger, "服务器[%s]已经不可用，将其设置为“停止”状态。", serverInfo);
                serverInfo.setState(ServerState.STOPED);
                serverInfo.setInvalidTime(Calendar.getInstance());
                persistenceManager.update(serverInfo);
                
                // 更新AppServerInstance的状态
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
     * 检查服务器是否正在运行。
     * @param serverInfo
     * @return
     */
    private boolean isAlive(ServerInfo serverInfo) {
        if (serverInfo == null) {
            return true;
        }
        // 向资源工厂申请资源
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
