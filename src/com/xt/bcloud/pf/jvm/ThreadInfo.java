package com.xt.bcloud.pf.jvm;

import java.io.Serializable;

/**
 * 线程信息
 *
 * @author Albert
 */
public class ThreadInfo implements Serializable {

    private static final long serialVersionUID = 5836204466157448039L;
    /**
     * 当前活动数
     */
    private int live = 0;
    /**
     * 最大活动数
     */
    private int livePeak = 0;
    /**
     * 后台线程数
     */
    private int deamon = 0;
    /**
     * 启动的总线程数
     */
    private long totalStarted = 0;

    public ThreadInfo() {
    }

    public void add(ThreadInfo threadInfo) {
        if (threadInfo == null) {
            return;
        }
        this.live         += threadInfo.live;
        this.livePeak     += threadInfo.livePeak;
        this.deamon       += threadInfo.deamon;
        this.totalStarted += threadInfo.totalStarted;
    }

    public int getDeamon() {
        return deamon;
    }

    public void setDeamon(int deamon) {
        this.deamon = deamon;
    }

    public int getLive() {
        return live;
    }

    public void setLive(int live) {
        this.live = live;
    }

    public int getLivePeak() {
        return livePeak;
    }

    public void setLivePeak(int livePeak) {
        this.livePeak = livePeak;
    }

    public long getTotalStarted() {
        return totalStarted;
    }

    public void setTotalStarted(long totalStarted) {
        this.totalStarted = totalStarted;
    }

    @Override
    public String toString() {
        return "TreadInfo{" + "live=" + live + ", livePeak=" + livePeak + ", deamon=" + deamon + ", totalStarted=" + totalStarted + '}';
    }
}
