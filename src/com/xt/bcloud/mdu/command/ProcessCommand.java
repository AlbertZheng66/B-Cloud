
package com.xt.bcloud.mdu.command;

import com.xt.bcloud.mdu.MduManager;
import java.io.Serializable;

/**
 * This class can be used to report the process infomation to the MDU Manager.
 * @author Albert
 */
public class ProcessCommand extends Command {
    
    public final static String PROCESS = "process";

    public ProcessCommand() {
        this.name = PROCESS;
    }

    @Override
    public Serializable execute() {
        ProcessInfo process = (ProcessInfo)this.param;
        MduManager.getInstance().updateProcessInfo(process);
        return true;
    }
    
}
