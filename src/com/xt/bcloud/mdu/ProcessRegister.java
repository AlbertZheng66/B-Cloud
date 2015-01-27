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
 * �����ؽ�����Ϣ��MDU���������б���
 *
 * @author Albert
 */
public class ProcessRegister implements SystemLifecycle {

    final Logger logger = Logger.getLogger(ProcessRegister.class);

    public ProcessRegister() {
    }

    public void onInit() {
        String pid = getPid();
        LogWriter.info2(logger, "��������ע������PID:[%s]", pid);
        ProcessInfo pi = createProcessInfo(pid);
        if (StringUtils.isEmpty(pi.getAppServerInstanceOid())) {
            // ���Ǵ�MDU�����ķ����������޷���֤��OID��ϵͳ��ֱ�ӷ���
            LogWriter.info2(logger, "�˽���[pid]��MDU������", pid);
            return;
        }
        ProcessCommand command = new ProcessCommand();
        command.setParam(pi);
        PhyServer phyServer = createPhyServer();
        CommandClient instance = new CommandClient();
        LogWriter.info2(logger, "�����������[%s]��������[%s]", phyServer, command);
        instance.execute(phyServer, command);
        // TODO: ���ע��������δ��������ٳ��Խ��м��δ���
    }

    private PhyServer createPhyServer() {
        PhyServer phyServer = new PhyServer();
        phyServer.setIp("127.0.0.1");  // ֻ�򱾵ص�MDU����������ע��
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

        LogWriter.info2(logger, "����������Ϣ[%s]", pi);
        return pi;
    }

    

    private String getPid() throws NumberFormatException {
        String pid = System.getProperty("pid");
        if (pid == null) {
            String name = ManagementFactory.getRuntimeMXBean().getName();
            String[] Ids = name.split("@");
            pid = Ids[0];
        }
        LogWriter.info2(logger, "��ȡPID[%s]", pid);
        return String.valueOf(pid);
    }

    public void onDestroy() {
    }
}
