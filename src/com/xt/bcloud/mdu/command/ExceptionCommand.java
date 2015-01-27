
package com.xt.bcloud.mdu.command;

import java.io.Serializable;

/**
 *
 * @author Albert
 */
public class ExceptionCommand extends Command{
    
    public ExceptionCommand() {
        this.name = CommandFactory.CLIENT_EXCEPTION;
    }

    @Override
    public Serializable execute() {
        return param;
    }
    
    
    
}

