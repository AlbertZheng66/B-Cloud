package com.xt.bcloud.mdu;

import com.xt.bcloud.comm.Constants;
import com.xt.bcloud.mdu.command.CommandClient;
import com.xt.bcloud.mdu.command.ProcessCommand;
import com.xt.bcloud.mdu.command.ProcessInfo;
import com.xt.bcloud.resource.ServerManager;
import com.xt.bcloud.resource.server.ServerInfo;
import com.xt.core.app.init.SystemLifecycle;
import com.xt.core.log.LogWriter;
import java.lang.management.ManagementFactory;
import java.util.prefs.Preferences;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * 将本地进程信息向MDU管理器进行报告
 *
 * @author Albert
 */
public class ProcessRegister implements SystemLifecycle {

    final Logger logger = Logger.getLogger(ProcessRegister.class);

    public ProcessRegister() {
    }

    public void onInit() {
        String pid = getPid();
        LogWriter.info2(logger, "启动进程注册器，PID:[%s]", pid);
        ProcessInfo pi = createProcessInfo(pid);
        if (StringUtils.isEmpty(pi.getAppServerInstanceOid())) {
            // 不是从MDU启动的服务器可能无法查证此OID，系统将直接返回
            LogWriter.info2(logger, "此进程[pid]非MDU启动。", pid);
            return;
        }
        ProcessCommand command = new ProcessCommand();
        command.setParam(pi);
        PhyServer phyServer = createPhyServer();
        CommandClient instance = new CommandClient();
        LogWriter.info2(logger, "向命令服务器[%s]发送命令[%s]", phyServer, command);
        instance.execute(phyServer, command);
        // TODO: 如果注册出错该如何处理，后面再尝试进行几次处理
    }

    private PhyServer createPhyServer() {
        PhyServer phyServer = new PhyServer();
        phyServer.setIp("127.0.0.1");  // 只向本地的MDU服务器进行注册
        int port = getMduPort();
        phyServer.setManagerPort(port);
        return phyServer;
    }

    private int getMduPort() {
        String portStr = Preferences.userRoot().get(MduManager.MDU_MANAGERMANAGER_PORT, null);
        if (StringUtils.isEmpty(portStr)) {
            return MduManager.managerPort;
        } else {
            return Integer.parseInt(portStr);
        }
    }

    private ProcessInfo createProcessInfo(String pid) {
        ServerInfo serverInfo = ServerManager.getInstance().getServerInfo();
        ProcessInfo pi = new ProcessInfo();
        pi.setPid(pid);
        pi.setJmxRmiPort(serverInfo.getJmxRmiPort());
        pi.setIp(serverInfo.getIp());
        pi.setContextPath(serverInfo.getContextPath());
        pi.setId(serverInfo.getId());
        pi.setManagerPort(serverInfo.getManagerPort());
        pi.setName(serverInfo.getName());
        pi.setAppServerInstanceOid(serverInfo.getAppServerInstanceOid());

        LogWriter.info2(logger, "构建进程信息[%s]", pi);
        return pi;
    }

    

    private String getPid() throws NumberFormatException {
        String pid = System.getProperty("pid");
        if (pid == null) {
            String name = ManagementFactory.getRuntimeMXBean().getName();
            String[] Ids = name.split("@");
            pid = Ids[0];
        }
        LogWriter.info2(logger, "获取PID[%s]", pid);
        return String.valueOf(pid);
    }

    public void onDestroy() {
    }
}
