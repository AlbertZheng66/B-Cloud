package com.xt.bcloud.app;

import com.xt.core.db.pm.IPersistence;
import com.xt.gt.ui.table.ColumnInfo;
import java.util.Calendar;

/**
 * 分配给应用的具体事例，和 Cattle 的概念类似，只不过此类主要用于持久化之用。
 * @author albert
 */
public class AppInstance implements IPersistence {

    private static final long serialVersionUID = -2753839720424337361L;


    /**
     *  内部编码
     */
    private String oid;

    /**
     * 服务器编码
     */
    @ColumnInfo(title="服务器编码")
    private String serverOid;

    /**
     * 当前实例的编码（句柄）
     */
    private String cattleOid;

    /**
     * 应用编码
     */
    private String appOid;
    /**
     * 应用版本编码
     */
    private String appVersionOid;
    /**
     * 服务器地址
     */
    @ColumnInfo(title="服务器地址")
    private String ip;
    /**
     * 占用端口号
     */
    @ColumnInfo(title="占用端口号")
    private String port;
    /**
     * 管理上下文
     */
    @ColumnInfo(title="管理上下文")
    private String contextPath;
    /**
     * 是否可用, Y/N
     */
    @ColumnInfo(title="是否可用")
    private boolean valid;
    /**
     * 启动时间
     */
    @ColumnInfo(title="启动时间")
    private Calendar startupTime;
    
    /**
     * 当前状态
     */
    @ColumnInfo(title="当前状态")
    private AppInstanceState state;

//    /**
//     * 组通信的绑定地址
//     */
//    private String bindAddr;
//
//
//    /**
//     * 组通信的绑定地址
//     */
//    private String bindPort;

    /**
     * 关闭时间
     */
    private Calendar shutdownTime;

    /**
     * 失效时间（自动巡视程序将对服务器进行探测，如果不能响应，则将其设置为失效）
     */
    private Calendar invalidTime;

     /**
     * 关闭时间
     */
    @ColumnInfo(title="发布路径")
    private String deployPath;

    public AppInstance() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AppInstance other = (AppInstance) obj;
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
        StringBuilder strBld = new StringBuilder();
        strBld.append(super.toString()).append("[");
        strBld.append("oid=").append(oid).append("; ");
        strBld.append("serverOid=").append(serverOid).append("; ");
        strBld.append("appOid=").append(appOid).append("; ");
        strBld.append("appVersionOid=").append(appVersionOid).append("; ");
        strBld.append("startupTime=").append(startupTime).append("; ");
        strBld.append("deployPath=").append(deployPath).append("; ");
        strBld.append("ip=").append(ip);
        strBld.append("]");
        return strBld.toString();
    }

    public String getAppOid() {
        return appOid;
    }

    public void setAppOid(String appOid) {
        this.appOid = appOid;
    }

    public String getAppVersionOid() {
        return appVersionOid;
    }

    public void setAppVersionOid(String appVersionOid) {
        this.appVersionOid = appVersionOid;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public Calendar getInvalidTime() {
        return invalidTime;
    }

    public void setInvalidTime(Calendar invalidTime) {
        this.invalidTime = invalidTime;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getServerOid() {
        return serverOid;
    }

    public void setServerOid(String serverOid) {
        this.serverOid = serverOid;
    }

    public Calendar getStartupTime() {
        return startupTime;
    }

    public void setStartupTime(Calendar startupTime) {
        this.startupTime = startupTime;
    }

    public AppInstanceState getState() {
        return state;
    }

    public void setState(AppInstanceState state) {
        this.state = state;
    }

    public boolean getValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getCattleOid() {
        return cattleOid;
    }

    public void setCattleOid(String cattleOid) {
        this.cattleOid = cattleOid;
    }

//    public String getBindAddr() {
//        return bindAddr;
//    }
//
//    public void setBindAddr(String bindAddr) {
//        this.bindAddr = bindAddr;
//    }
//
//    public String getBindPort() {
//        return bindPort;
//    }
//
//    public void setBindPort(String bindPort) {
//        this.bindPort = bindPort;
//    }

    public Calendar getShutdownTime() {
        return shutdownTime;
    }

    public void setShutdownTime(Calendar shutdownTime) {
        this.shutdownTime = shutdownTime;
    }

    public String getDeployPath() {
        return deployPath;
    }

    public void setDeployPath(String deployPath) {
        this.deployPath = deployPath;
    }
}
