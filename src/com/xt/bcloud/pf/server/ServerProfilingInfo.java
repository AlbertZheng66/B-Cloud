package com.xt.bcloud.pf.server;

import com.xt.gt.ui.table.ColumnInfo;

/**
 * 用于记录服务器相关的信息。
 * @author Albert
 */
public class ServerProfilingInfo extends com.xt.bcloud.pf.ProfilingInfo {
    
    private static final long serialVersionUID = 70397517290859148L;

    
    @ColumnInfo(title = "CPU 资源")
    private String cpu;
    
    @ColumnInfo(title = "内存占用")
    private String memory;
    
    @ColumnInfo(title = "名称")
    private String disks;
    
    @ColumnInfo(title = "IO 资源")
    private String network;

    public ServerProfilingInfo() {
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

  
    public String getDisks() {
        return disks;
    }

    public void setDisks(String disks) {
        this.disks = disks;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }


    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    @Override
    public String toString() {
        return "ProfilingInfo{" + "super=" + super.toString() + ", cpu=" + cpu + ", memory=" + memory + ", io=" + disks + ", network=" + network + '}';
    }
}
