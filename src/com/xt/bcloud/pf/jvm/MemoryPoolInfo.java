
package com.xt.bcloud.pf.jvm;

import java.io.Serializable;
import java.lang.management.MemoryType;

/**
 *
 * @author Albert
 */
public class MemoryPoolInfo implements Serializable {
    
    private MemoryUsage peakUsage;
    
    private MemoryUsage usage;
    
    private MemoryType type;
    
    private String name;

    public MemoryPoolInfo() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MemoryUsage getPeakUsage() {
        return peakUsage;
    }

    public void setPeakUsage(MemoryUsage peakUsage) {
        this.peakUsage = peakUsage;
    }

    public MemoryType getType() {
        return type;
    }

    public void setType(MemoryType type) {
        this.type = type;
    }

    public MemoryUsage getUsage() {
        return usage;
    }

    public void setUsage(MemoryUsage usage) {
        this.usage = usage;
    }

}
