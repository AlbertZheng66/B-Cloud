
package com.xt.bcloud.mdu.command;

import java.io.Serializable;

/**
 *
 * @author Albert
 */
public class ProcessInfo implements Serializable {
    
    private static final long serialVersionUID = -3450487050069996445L;
    
    /**
     * 进程号
     */
    private String pid;
    
    
    
    /**
     * 应用服务器实例的唯一编号
     */
    private String appServerInstanceOid;
    
    /**
     * JMX 通信用的端口号。
     */
    private int jmxRmiPort;
    
     /**
     * 自定义 ID 标识。
     */
    private String id;
    
    /**
     * 服务器名称（机器名称，便于管理）
     */
    private String name;
    
    /**
     * 管理用的端口号。
     */
    private int managerPort;
    
    /**
     * 管理用的上下文。
     */
    private String contextPath;
    
    /**
     * 本地的IP地址
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
