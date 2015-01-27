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
     * 工作路径可以采用渐增方式（即路径中含有变量：${_inc}）；或者随机方式，（即路径中含有变量：${_random}）；
     * 又或者日期方式（即路径中含有变量：${_date}）。
     *
     * @param phyServer
     * @return
     */
    protected File getWorkPath(PhyServer phyServer,
            AppServerTemplate asTemplate, Map params) {
        String workPath = phyServer.getWorkPath();
        
        // 每次的工作时间都不一样
        workPath = FilenameUtils.concat(workPath, "${_time}");

        //替换其中的变量
        workPath = VarParser.parse(workPath, new CommandValueReader(params));

        File path = new File(workPath);
        if (!path.exists()) {
            path.mkdirs();
        }

        return path;
    }
}
