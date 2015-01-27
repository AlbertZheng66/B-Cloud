package com.xt.bcloud.mdu.command;

import java.io.Serializable;
import org.apache.log4j.Logger;

/**
 *
 * @author Albert
 */
abstract public class Command {

    protected final transient Logger logger = Logger.getLogger(this.getClass());
    
    /**
     * the name of the command
     */
    protected String name;
    protected Serializable param;

    public Command() {
    }

    abstract public Serializable execute();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Serializable getParam() {
        return param;
    }

    public void setParam(Serializable param) {
        this.param = param;
    }

    @Override
    public String toString() {
        return "Command{" + "name=" + name + ", param=" + param + '}';
    }
}
