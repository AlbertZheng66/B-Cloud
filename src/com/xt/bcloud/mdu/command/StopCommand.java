
package com.xt.bcloud.mdu.command;

import com.xt.bcloud.comm.CloudUtils;
import com.xt.bcloud.mdu.AppServerInstance;
import com.xt.bcloud.mdu.MduException;
import com.xt.core.log.LogWriter;
import java.io.Serializable;
import org.apache.commons.lang.StringUtils;

/**
 * Í£Ö¹·þÎñÆ÷ÃüÁî
 * @author Albert
 */
public class StopCommand  extends ServerInstanceCommand {

    public StopCommand() {
        super();
    }
    

    @Override
    protected String[] getCmd(AppServerInstance asInstance) {
        return new String[]{asInstance.getStopCmd()};
    }

    @Override
    protected String getTitle() {
        return "Í£Ö¹";
    }
    
}
