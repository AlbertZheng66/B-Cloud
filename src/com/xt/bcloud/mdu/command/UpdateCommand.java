
package com.xt.bcloud.mdu.command;

import com.xt.core.log.LogWriter;
import java.io.Serializable;
import org.apache.log4j.Logger;

/**
 *
 * @author Albert
 */
public class UpdateCommand extends Command{
    
    public final static String UPDATE = "update";
    
    public UpdateCommand() {
        this.name = UPDATE;
    }

    @Override
    public Serializable execute() {
        LogWriter.info2(logger, "start updating server[%s]", getParam());
        return true;
    }
    
    /**
     * 更新一个应用服务器。
     */
    public void update(boolean relocatable) {
        // 在临时目录构建新的应用服务器
        // 停止当前服务器
    }    
}
