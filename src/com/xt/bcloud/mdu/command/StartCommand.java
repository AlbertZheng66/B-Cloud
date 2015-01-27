
package com.xt.bcloud.mdu.command;

import com.xt.bcloud.mdu.AppServerInstance;

/**
 *
 * @author Albert
 */
public class StartCommand extends ServerInstanceCommand {

    public StartCommand() {
        super();
    }
    

    @Override
    protected String[] getCmd(AppServerInstance asInstance) {
        return new String[]{asInstance.getStartupCmd()};
    }

    @Override
    protected String getTitle() {
        return "Æô¶¯";
    }
    
}
