
package com.xt.bcloud.pf.jvm;

import java.io.Serializable;

/**
 *
 * @author Albert
 */
public class MemoryInfo implements Serializable {
    private static final long serialVersionUID = -1408625702409794863L;
    
    private MemoryUsage heapMemoryUsage;
    
    private MemoryUsage nonHeapMemoryUsage;

    public MemoryInfo() {
    }
    
    public void add(MemoryInfo memoryInfo ){
        if (memoryInfo == null) {
            return;
        }
        if (heapMemoryUsage == null) {
            heapMemoryUsage = memoryInfo.heapMemoryUsage;
        } else {
            heapMemoryUsage.add(memoryInfo.heapMemoryUsage);
        }
        
        if (nonHeapMemoryUsage == null) {
            nonHeapMemoryUsage = memoryInfo.nonHeapMemoryUsage;
        } else {
            nonHeapMemoryUsage.add(memoryInfo.nonHeapMemoryUsage);
        }
    }

    public MemoryUsage getHeapMemoryUsage() {
        return heapMemoryUsage;
    }

    public void setHeapMemoryUsage(MemoryUsage heapMemoryUsage) {
        this.heapMemoryUsage = heapMemoryUsage;
    }

    public MemoryUsage getNonHeapMemoryUsage() {
        return nonHeapMemoryUsage;
    }

    public void setNonHeapMemoryUsage(MemoryUsage nonHeapMemoryUsage) {
        this.nonHeapMemoryUsage = nonHeapMemoryUsage;
    }

    @Override
    public String toString() {
        return "MemoryInfo{" + "heapMemoryUsage=" + heapMemoryUsage + ", nonHeapMemoryUsage=" + nonHeapMemoryUsage + '}';
    }
    
}
