
package com.xt.bcloud.mdu;

import java.util.List;

/**
 * FIXME: ������һ����Χ��ѡ���������������������������ܵȵ�������
 * @author Albert
 */
public class DeployingParam {
    /**
     * Ӧ�÷�����ģ��
     */
    private AppServerTemplate asTemplate;
    
    /**
     * �Ƿ����ѡ�����������
     */
    private boolean randomSelection;
    
    /**
     * ���ѡ��������ĸ���
     */
    private int count; 
    
    /**
     * �û�ѡ�������������
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
