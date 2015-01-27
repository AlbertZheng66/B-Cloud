
package com.xt.bcloud.pf.jvm;

import java.io.Serializable;

/**
 *
 * @author Albert
 */
public class MemoryUsage implements Serializable {
    private static final long serialVersionUID = 5297944840993829379L;
    private long init = 0;
    private long used = 0;
    private long committed = 0;
    private long max = 0;

    public MemoryUsage() {
    }
    

    public MemoryUsage(long init, long used, long committed, long max) {
        this.init = init;
        this.used = used;
        this.committed = committed;
        this.max = max;
    }
    
    public void add(MemoryUsage memoryUsage) {
        if (memoryUsage == null) {
            return;
        }
        this.init      += memoryUsage.init;
        this.used      += memoryUsage.used;
        this.committed += memoryUsage.committed;
        this.max       += memoryUsage.max;
    }

    public void setCommitted(long committed) {
        this.committed = committed;
    }

    public void setInit(long init) {
        this.init = init;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public void setUsed(long used) {
        this.used = used;
    }

    

    public long getCommitted() {
        return committed;
    }

    public long getInit() {
        return init;
    }

    public long getMax() {
        return max;
    }

    public long getUsed() {
        return used;
    }

    @Override
    public String toString() {
        return "MemoryUsage{" + "init=" + init + ", used=" + used + ", committed=" + committed + ", max=" + max + '}';
    }
    
}
