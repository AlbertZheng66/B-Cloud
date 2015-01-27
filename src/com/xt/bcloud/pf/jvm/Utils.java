
package com.xt.bcloud.pf.jvm;

/**
 *
 * @author Albert
 */
public class Utils {
    
    public final static MemoryInfo EMPTY_MEMORY_INFO = new MemoryInfo();
    
    
    public final static CpuInfo EMPTY_CPU_INFO = new CpuInfo();
    
    
    public final static ThreadInfo EMPTY_THREAD_INFO = new ThreadInfo();
    
    
    public final static ClassLoadingInfo EMPTY_CLASS_LOADING_INFO = new ClassLoadingInfo();
    
    static {
        EMPTY_MEMORY_INFO.setHeapMemoryUsage(new MemoryUsage());
        EMPTY_MEMORY_INFO.setNonHeapMemoryUsage(new MemoryUsage());
    }
    
}
