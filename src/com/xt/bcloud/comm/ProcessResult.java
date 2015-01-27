
package com.xt.bcloud.comm;

import java.io.Serializable;

/**
 *
 * @author Albert
 */
public class ProcessResult implements Serializable {
    
    private static final long serialVersionUID = 172911450182523609L;
    
    private String pid;
    
    private int exitVal;
    
    private String output;

    public ProcessResult() {
    }

    public int getExitVal() {
        return exitVal;
    }

    public void setExitVal(int exitVal) {
        this.exitVal = exitVal;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    @Override
    public String toString() {
        return "ProcessInfo{" + "pid=" + pid + ", exitVal=" + exitVal + ", output=" + output + '}';
    }
}
