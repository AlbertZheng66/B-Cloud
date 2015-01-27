package com.xt.bcloud.mdu.command;

import com.xt.bcloud.mdu.AppServerInstance;

/**
 *
 * @author Albert
 */
public class RestartCommand extends ServerInstanceCommand {

    @Override
    protected String[] getCmd(AppServerInstance asInstance) {
        return new String[] {asInstance.getStopCmd(), asInstance.getStartupCmd()};
    }
    
    @Override
    protected String getTitle() {
        return "ÖØÐÂÆô¶¯";
    }
}
