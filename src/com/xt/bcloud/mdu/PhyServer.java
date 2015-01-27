
package com.xt.bcloud.mdu;

import com.xt.core.db.pm.IPersistence;
import com.xt.gt.ui.table.ColumnInfo;
import java.util.Calendar;

/**
 * 封装了一个物理服务器的相关信息。
 * @author Albert
 */
public class PhyServer  implements IPersistence {
    
    private static final long serialVersionUID = 5721666662957147036L;
    
     /**
     * 唯一标识
     */
    private String oid;

//    /**
//     * 自定义 ID 标识。
//     */
//    @ColumnInfo(title="自定义标识")
//    private String id;

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
    
//    /**
//     * 服务器的 MAC 地址
//     */
//    @ColumnInfo(title="IP 地址")
//    private String macAddress;
    
    /**
     * 操作系统名称
     */
    @ColumnInfo(title="操作系统名称")
    private String osName;
    
    /**
     * 操作系统版本
     */
    @ColumnInfo(title="操作系统版本")
    private String osVersion;

    /**
     * 管理用的端口号。
     */
    @ColumnInfo(title="管理端口号")
    private int managerPort;
    
    /**
     * 工作路径，用于发布应用的路径
     */
    @ColumnInfo(title="工作路径")
    private String workPath;
    
    
    /**
     * 当前运行路径
     */
    @ColumnInfo(title="当前运行路径")
    private String userPath;
    
    /**
     * 临时目录，用于重启应用等操作的临时目录
     */
    @ColumnInfo(title="临时目录")
    private String tempPath;
    

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
     * 最后一次更新时间
     */
    @ColumnInfo(title="最后一次更新时间")
    private Calendar lastUpdatedTime;

    /**
     * 失效时间
     */
    @ColumnInfo(title="lastUpdatedTime")
    private Calendar invalidTime;

    /**
     * 当前状态
     */
    @ColumnInfo(title="当前状态")
    private PhyServerState state;
    

    public PhyServer() {
    }

    public Calendar getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Calendar insertTime) {
        this.insertTime = insertTime;
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

    public Calendar getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(Calendar lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
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

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public PhyServerState getState() {
        return state;
    }

    public void setState(PhyServerState state) {
        this.state = state;
    }

    public String getTempPath() {
        return tempPath;
    }

    public void setTempPath(String tempPath) {
        this.tempPath = tempPath;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    
    
    public String getUserPath() {
        return userPath;
    }

    public void setUserPath(String userDir) {
        this.userPath = userDir;
    }

    public boolean isValid() {
        return valid;
    }
    
    
    public boolean getValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
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
        final PhyServer other = (PhyServer) obj;
        if ((this.oid == null) ? (other.oid != null) : !this.oid.equals(other.oid)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + (this.oid != null ? this.oid.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "PhyServer{" + "oid=" + oid + ", name=" + name + ", ip=" + ip + ", workPath=" + workPath + ", userDir=" + userPath + ", tempPath=" + tempPath + ", osName=" + osName + ", managerPort=" + managerPort + ", insertTime=" + insertTime + ", valid=" + valid + ", lastUpdatedTime=" + lastUpdatedTime + ", invalidTime=" + invalidTime + ", state=" + state + '}';
    }
    
    
    
    
}
