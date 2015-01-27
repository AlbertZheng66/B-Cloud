
package com.xt.bcloud.pf.jvm;

import java.io.Serializable;

/**
 *
 * @author Albert
 */
public class ClassLoadingInfo implements Serializable {
    private static final long serialVersionUID = 693151945130291914L;
    
    /**
     * ��������
     */
    private int totalLoaded;
    
    /**
     * ���������
     */
    private int sharedLoaded;
    
    /**
     * ��ж����
     */
    private long totalUnloaded;
    
    /**
     * �����Ѽ��ص��������
     */
    private long totalLoadedClassCount;

    public ClassLoadingInfo() {
    }
    
    
    public void add(ClassLoadingInfo classLoadingInfo) {
        if (classLoadingInfo == null) {
            return;
        }
        this.totalLoaded           += classLoadingInfo.totalLoaded;
        this.sharedLoaded          += classLoadingInfo.sharedLoaded;
        this.totalUnloaded         += classLoadingInfo.totalUnloaded;
        this.totalLoadedClassCount += classLoadingInfo.totalLoadedClassCount;
    }

    public int getSharedLoaded() {
        return sharedLoaded;
    }

    public void setSharedLoaded(int sharedLoaded) {
        this.sharedLoaded = sharedLoaded;
    }

    public int getTotalLoaded() {
        return totalLoaded;
    }

    public void setTotalLoaded(int totalLoaded) {
        this.totalLoaded = totalLoaded;
    }

    public long getTotalUnloaded() {
        return totalUnloaded;
    }

    public void setTotalUnloaded(long totalUnloaded) {
        this.totalUnloaded = totalUnloaded;
    }

    public long getTotalLoadedClassCount() {
        return totalLoadedClassCount;
    }

    public void setTotalLoadedClassCount(long totalLoadedClassCount) {
        this.totalLoadedClassCount = totalLoadedClassCount;
    }

    @Override
    public String toString() {
        return "ClassLoadingInfo{" + "totalLoaded=" + totalLoaded + ", sharedLoaded=" + sharedLoaded + ", totalUnloaded=" + totalUnloaded + ", totalLoadedClassCount=" + totalLoadedClassCount + '}';
    }
    
    
    
}
