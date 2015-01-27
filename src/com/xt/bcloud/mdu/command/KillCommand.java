
package com.xt.bcloud.mdu.command;

import com.xt.bcloud.mdu.AppServerInstance;

/**
 *
 * @author Albert
 */
public class KillCommand extends ServerInstanceCommand {

    public KillCommand() {
        super();
    }   

    @Override
    protected String[] getCmd(AppServerInstance asInstance) {
        return new String[]{asInstance.getKillCmd()};
    }

    @Override
    protected String getTitle() {
        return "Ç¿ÖÆÍ£Ö¹";
    }
    
}
