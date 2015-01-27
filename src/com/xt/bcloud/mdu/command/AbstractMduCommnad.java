/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xt.bcloud.mdu.command;

import com.xt.bcloud.comm.VarParser;
import com.xt.bcloud.mdu.AppServerTemplate;
import com.xt.bcloud.mdu.PhyServer;
import java.io.File;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Albert
 */
abstract public class AbstractMduCommnad extends Command {

    public AbstractMduCommnad() {
        super();
    }

    /**
     * ����·�����Բ��ý�����ʽ����·���к��б�����${_inc}�������������ʽ������·���к��б�����${_random}����
     * �ֻ������ڷ�ʽ����·���к��б�����${_date}����
     *
     * @param phyServer
     * @return
     */
    protected File getWorkPath(PhyServer phyServer,
            AppServerTemplate asTemplate, Map params) {
        String workPath = phyServer.getWorkPath();
        
        // ÿ�εĹ���ʱ�䶼��һ��
        workPath = FilenameUtils.concat(workPath, "${_time}");

        //�滻���еı���
        workPath = VarParser.parse(workPath, new CommandValueReader(params));

        File path = new File(workPath);
        if (!path.exists()) {
            path.mkdirs();
        }

        return path;
    }
}
