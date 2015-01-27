
package com.xt.bcloud.mdu.command;

import java.io.Serializable;

/**
 *
 * @author Albert
 */
public class ProcessInfo implements Serializable {
    
    private static final long serialVersionUID = -3450487050069996445L;
    
    /**
     * ���̺�
     */
    private String pid;
    
    
    
    /**
     * Ӧ�÷�����ʵ����Ψһ���
     */
    private String appServerInstanceOid;
    
    /**
     * JMX ͨ���õĶ˿ںš�
     */
    private int jmxRmiPort;
    
     /**
     * �Զ��� ID ��ʶ��
     */
    private String id;
    
    /**
     * ���������ƣ��������ƣ����ڹ���
     */
    private String name;
    
    /**
     * �����õĶ˿ںš�
     */
    private int managerPort;
    
    /**
     * �����õ������ġ�
     */
    private String contextPath;
    
    /**
     * ���ص�IP��ַ
     */
    private String ip;

    public ProcessInfo() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getJmxRmiPort() {
        return jmxRmiPort;
    }

    public void setJmxRmiPort(int jmxRmiPort) {
        this.jmxRmiPort = jmxRmiPort;
    }

    public int getManagerPort() {
        return managerPort;
    }

    public void setManagerPort(int managerPort) {
        this.managerPort = managerPort;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getAppServerInstanceOid() {
        return appServerInstanceOid;
    }

    public void setAppServerInstanceOid(String appServerInstanceOid) {
        this.appServerInstanceOid = appServerInstanceOid;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
    
    

    @Override
    public String toString() {
        return "ProcessInfo{" + "pid=" + pid + ", appServerInstanceOid=" + appServerInstanceOid + ", jmxRmiPort=" + jmxRmiPort + ", id=" + id + ", name=" + name + ", managerPort=" + managerPort + '}';
    }

    
}
