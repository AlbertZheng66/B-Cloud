
package com.xt.bcloud.mdu.command;

import java.io.Serializable;

/**
 *
 * @author Albert
 */
public class OkCommand extends Command{
    

    public OkCommand() {
        this.name = CommandFactory.OK;
    }

    @Override
    public Serializable execute() {
        return param;
    }
    
}

