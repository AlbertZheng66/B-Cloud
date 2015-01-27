
package com.xt.bcloud.mdu;

import java.io.Serializable;

/**
 *
 * @author Albert
 */
public class ParamsVO implements Serializable {
    
    private static final long serialVersionUID = -6789515504636576584L;
    
    private String name;
    
    private ParamsType type;
    
    private String from;
    
    private String to;

    public ParamsVO() {
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public ParamsType getType() {
        return type;
    }

    public void setType(ParamsType type) {
        this.type = type;
    }

   
    @Override
    public String toString() {
        return "ParamsVO{" + "name=" + name + ", type=" + type + ", from=" + from + ", to=" + to + '}';
    }
    
}
