package com.xt.bcloud.mdu.command;

import com.xt.bcloud.comm.CloudUtils;
import com.xt.bcloud.mdu.AppServerInstance;
import com.xt.bcloud.mdu.MduException;
import com.xt.core.log.LogWriter;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;

/**
 * ����ִ�����з�����ʵ��������ģ�塣
 *
 * @author Albert
 */
abstract public class ServerInstanceCommand extends AbstractMduCommnad {

    public ServerInstanceCommand() {
    }

    @Override
    public Serializable execute() {
        LogWriter.info2(logger, "start updating server[%s]", getParam());
        AppServerInstance asInstance = (AppServerInstance) getParam();
        if (asInstance == null) {
            throw new MduException("������ʵ������Ϊ�ա�");
        }
        String title = getTitle();
        String[] cmds = getCmd(asInstance);
        for (int i = 0; i < cmds.length; i++) {
            String cmd = cmds[i];
            if (StringUtils.isEmpty(cmd)) {
                throw new MduException(String.format("ʵ��[%s]��[%s]����Ϊ�ա�", asInstance, title));
            }
            LogWriter.info2(logger, "start executing command[%s]", cmd);
            try {
                if (i < cmds.length - 1) {
                    // FIXME: ����ô֪����һ������ִ������
                    Thread.sleep(1000 * 60);  // �ȵ�1��������
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(ServerInstanceCommand.class.getName()).log(Level.SEVERE, null, ex);
            }
            CloudUtils.executeCommand(cmd);
        }
        return true;
    }

    abstract protected String[] getCmd(AppServerInstance asInstance);

    abstract protected String getTitle();
}
