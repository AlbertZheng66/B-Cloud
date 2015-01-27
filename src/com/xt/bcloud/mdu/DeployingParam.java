
package com.xt.bcloud.mdu;

import java.util.List;

/**
 * FIXME: 可以在一定范围内选择物理服务器（根据物理机的性能等等条件）
 * @author Albert
 */
public class DeployingParam {
    /**
     * 应用服务器模板
     */
    private AppServerTemplate asTemplate;
    
    /**
     * 是否随机选择物理服务器
     */
    private boolean randomSelection;
    
    /**
     * 随机选择服务器的个数
     */
    private int count; 
    
    /**
     * 用户选择物理机的数量
     */
    private List<PhyServer> phyServers;

    public DeployingParam() {
    }

    public AppServerTemplate getAsTemplate() {
        return asTemplate;
    }

    public void setAsTemplate(AppServerTemplate asTemplate) {
        this.asTemplate = asTemplate;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<PhyServer> getPhyServers() {
        return phyServers;
    }

    public void setPhyServers(List<PhyServer> phyServers) {
        this.phyServers = phyServers;
    }

    public boolean isRandomSelection() {
        return randomSelection;
    }

    public void setRandomSelection(boolean randomSelection) {
        this.randomSelection = randomSelection;
    }

    @Override
    public String toString() {
        return "DeployingParam{" + "asTemplate=" + asTemplate + ", randomSelection=" + randomSelection + ", count=" + count + ", phyServers=" + phyServers + '}';
    }
    
}
