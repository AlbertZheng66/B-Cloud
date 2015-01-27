

package com.xt.bcloud.comm;

import java.io.Serializable;

/**
 * 用于描述一个资源的容量, 如果 CPU 的频率, 内存的大小等信息.
 * @author albert
 */
public class Capacity implements Serializable {

    /**
     * 最小内存（单位：M），默认是：64M。
     */
    private int minMem = 64;

    /**
     * 最大内存（单位：M），默认是：256M。
     */
    private int maxMem = 256;

    public Capacity() {
    }

    public int getMaxMem() {
        return maxMem;
    }

    public void setMaxMem(int maxMem) {
        this.maxMem = maxMem;
    }

    public int getMinMem() {
        return minMem;
    }

    public void setMinMem(int minMem) {
        this.minMem = minMem;
    }
}
