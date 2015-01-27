
package com.xt.bcloud.mdu;

import com.xt.core.db.pm.IPersistence;
import java.util.Calendar;

/**
 * ����ǰ�Ѿ�ע���Ӧ�÷�����ʵ����
 * @author Albert
 */
public class AppServerInstance implements IPersistence {
    
    private static final long serialVersionUID = 1046983683641959355L;
    
    private String oid;
    
    /**
     * ʹ�õ�ģ��ID������AppServerTemplate��OID
     */
    private String templateOid;
    
    /**
     * ���������ID������PhyServer��OID
     */
    private String phyServerOid;
    
    /**
     * Ӧ�÷�����������(Tomcat, Jetty)
     */
    private String name;
    
    /**
     * �汾��
     */
    private String version;
    
    /**
     * Ӧ�÷����������ͣ�(TD)TASK_DISPATCHER, APP_SERER(AS),JMS,MDU
     */
    private ServerType serverType;
    
//    /**
//     * ��������
//     */
//    private String params;
    
    /**
     * ��������
     */
    private String startupCmd;
    
    /**
     * ֹͣ����
     */
    private String stopCmd;
    
    /**
     * ǿ��ֹͣ����
     */
    private String killCmd;
    
    /**
     * ����ʱ��
     */
    private Calendar startupTime;
    
    /**
     * �Ƿ�����Ϊ��Ч
     */
    private boolean valid;
    
    private Calendar insertTime;
    
    /**
     * ����Ŀ¼
     */
    private String workPath;
    
    private AppServerInstanceState state;

    public AppServerInstance() {
    }

    public Calendar getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Calendar insertTime) {
        this.insertTime = insertTime;
    }

    public String getKillCmd() {
        return killCmd;
    }

    public void setKillCmd(String killCmd) {
        this.killCmd = killCmd;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getPhyServerOid() {
        return phyServerOid;
    }

    public void setPhyServerOid(String phyServerOid) {
        this.phyServerOid = phyServerOid;
    }

    public String getStartupCmd() {
        return startupCmd;
    }

    public void setStartupCmd(String startupCmd) {
        this.startupCmd = startupCmd;
    }

    public Calendar getStartupTime() {
        return startupTime;
    }

    public void setStartupTime(Calendar startupTime) {
        this.startupTime = startupTime;
    }

    public AppServerInstanceState getState() {
        return state;
    }

    public void setState(AppServerInstanceState state) {
        this.state = state;
    }

    public String getStopCmd() {
        return stopCmd;
    }

    public void setStopCmd(String stopCmd) {
        this.stopCmd = stopCmd;
    }

    public String getTemplateOid() {
        return templateOid;
    }

    public void setTemplateOid(String templateOid) {
        this.templateOid = templateOid;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
    
    public boolean getValid() {
        return valid;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public ServerType getServerType() {
        return serverType;
    }

    public void setServerType(ServerType serverType) {
        this.serverType = serverType;
    }

    public String getWorkPath() {
        return workPath;
    }

    public void setWorkPath(String workPath) {
        this.workPath = workPath;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AppServerInstance other = (AppServerInstance) obj;
        if ((this.oid == null) ? (other.oid != null) : !this.oid.equals(other.oid)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.oid != null ? this.oid.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "AppServerInstance{" + "oid=" + oid + 
                ", templateOid=" + templateOid + ", phyServerOid="
                + phyServerOid + ", name=" + name + ", version=" 
                + version + ", startupCmd=" + startupCmd 
                + ", stopCmd=" + stopCmd + ", killCmd=" 
                + killCmd + ", startupTime=" + startupTime
                + ", valid=" + valid + ", insertTime="
                + insertTime + ", state=" + state + ", serverType=" + serverType + '}';
    }
    
}
