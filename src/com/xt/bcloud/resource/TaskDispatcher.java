
package com.xt.bcloud.resource;

import com.xt.bcloud.resource.server.ServerState;
import com.xt.core.db.pm.IPersistence;
import com.xt.gt.ui.table.ColumnInfo;
import java.util.Calendar;

/**
 *
 * @author Albert
 */
public class TaskDispatcher  implements IPersistence {
    
    private static final long serialVersionUID = 8628734067289042387L;
    
     /**
     * 唯一标识
     */
    private String oid;
    
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
     * 失效时间
     */
    private Calendar invalidTime;

    /**
     * 当前状态
     */
    @ColumnInfo(title="当前状态")
    private ServerState state;
    
    /**
     * 模板编码
     */
    @ColumnInfo(title="模板编码")
    private String templateId;
    
    /**
     * 最后一次更新时间
     */
    @ColumnInfo(title="最后一次更新时间")
    private Calendar lastUpdatedTime;

    public TaskDispatcher() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TaskDispatcher other = (TaskDispatcher) obj;
        if ((this.oid == null) ? (other.oid != null) : !this.oid.equals(other.oid)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.oid != null ? this.oid.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "TaskDispatcher{" + "oid=" + oid + ", id=" + id + ", name=" + name + ", ip=" + ip + ", managerPort=" + managerPort + ", jmxRmiPort=" + jmxRmiPort + ", insertTime=" + insertTime + ", valid=" + valid + ", invalidTime=" + invalidTime + ", state=" + state + '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public ServerState getState() {
        return state;
    }

    public void setState(ServerState state) {
        this.state = state;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public Calendar getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(Calendar lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }
    
    
}
