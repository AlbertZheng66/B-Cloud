
package com.xt.bcloud.resource.server;

import com.xt.bcloud.resource.Profile;
import com.xt.core.db.pm.IPersistence;
import com.xt.gt.ui.table.ColumnInfo;
import java.util.Calendar;

/**
 *
 * @author albert
 */
public class ServerInfo implements IPersistence {
    
    private static final long serialVersionUID = 3469993927685961658L;

    /**
     * 唯一标识
     */
    private String oid;
    
    /**
     * 唯一标识
     */
    private String appServerInstanceOid;

    /**
     * 自定义 ID 标识。
     */
    @ColumnInfo(title="自定义标识")
    private String id;

    /**
     * 服务器名称（机器名称，便于管理）
     */
    @ColumnInfo(title="名称")
    private String name;

    /**
     * 服务器的IP 地址
     */
    @ColumnInfo(title="IP 地址")
    private String ip = "127.0.0.1";

    /**
     * 管理用的端口号。
     */
    @ColumnInfo(title="管理端口号")
    private int managerPort;
    
    /**
     * JMX 通信用的端口号。
     */
    @ColumnInfo(title="管理端口号")
    private int jmxRmiPort;

    /**
     * 应用的上下文路径
     */
    @ColumnInfo(title="上下文路径")
    private String contextPath;

    /**
     * 创建时间
     */
    @ColumnInfo(title="创建时间")
    private Calendar insertTime;

    /**
     * 是否可用(默认为可用)
     */
    @ColumnInfo(title="是否可用")
    private boolean valid = true;

    /**
     * 服务器的相关配置信息
     */
    private Profile profile;

    /**
     * 失效时间
     */
    private Calendar invalidTime;

    /**
     * 当前状态
     */
    @ColumnInfo(title="当前状态")
    private ServerState state;

    public ServerInfo() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ServerInfo other = (ServerInfo) obj;
        if ((this.oid == null) ? (other.oid != null) : !this.oid.equals(other.oid)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this.oid != null ? this.oid.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder strBld = new StringBuilder();
        strBld.append(super.toString()).append("[");
        strBld.append("oid=").append(oid).append("; ");
        strBld.append("id=").append(id).append("; ");
        strBld.append("name=").append(name).append("; ");
        strBld.append("ip=").append(ip).append("; ");
        strBld.append("state=").append(state).append("; ");
        strBld.append("managerPort=").append(managerPort).append("; ");
        strBld.append("jmxRmiPort=").append(jmxRmiPort).append("; ");
        strBld.append("profile=").append(profile);
        strBld.append("]");
        return strBld.toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
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

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public Calendar getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Calendar insertTime) {
        this.insertTime = insertTime;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public Calendar getInvalidTime() {
        return invalidTime;
    }

    public void setInvalidTime(Calendar invalidTime) {
        this.invalidTime = invalidTime;
    }

    public ServerState getState() {
        return state;
    }

    public void setState(ServerState state) {
        this.state = state;
    }

    public int getJmxRmiPort() {
        return jmxRmiPort;
    }

    public void setJmxRmiPort(int jmxRmiPort) {
        this.jmxRmiPort = jmxRmiPort;
    }

    public String getAppServerInstanceOid() {
        return appServerInstanceOid;
    }

    public void setAppServerInstanceOid(String appServerInstanceOid) {
        this.appServerInstanceOid = appServerInstanceOid;
    }
    
    

}
