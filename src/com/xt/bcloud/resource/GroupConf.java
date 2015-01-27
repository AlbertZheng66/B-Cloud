

package com.xt.bcloud.resource;

import com.xt.core.db.pm.IPersistence;
import java.util.Calendar;

/**
 * 和组（JGroup）相关的配置信息。
 * @author albert
 */
public class GroupConf  implements IPersistence {
    
    private static final long serialVersionUID = -123268768338070553L;

    /**
     * 内部编码
     */
    private String oid;

    /**
     * 实体的标识符，用于唯一的标识一个使用Group的对象.
     */
    private String entityId;

    /**
     * 组编码（表示一个唯一的 Group ）
     */
    private String groupId;

    /**
     * 组通信的绑定地址
     */
    private String bindAddr;
    /**
     * 组通信占用的端口
     */
    private String bindPort;
    /**
     * 插入时间
     */
    private Calendar insertTime;

    /**
     * 最后一次更新时间
     */
    private Calendar lastUpdateTime;

    public GroupConf() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GroupConf other = (GroupConf) obj;
        if ((this.oid == null) ? (other.oid != null) : !this.oid.equals(other.oid)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + (this.oid != null ? this.oid.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder strBld = new StringBuilder();
        strBld.append(super.toString()).append("[");
        strBld.append("oid=").append(oid).append("; ");
        strBld.append("entityId=").append(entityId).append("; ");
        strBld.append("groupId=").append(groupId).append("; ");
        strBld.append("bindAddr=").append(bindAddr).append("; ");
        strBld.append("bindPort=").append(bindPort);
        strBld.append("lastUpdateTime=").append(lastUpdateTime);
        strBld.append("]");
        return strBld.toString();
    }

    public String getBindAddr() {
        return bindAddr;
    }

    public void setBindAddr(String bindAddr) {
        this.bindAddr = bindAddr;
    }

    public String getBindPort() {
        return bindPort;
    }

    public void setBindPort(String bindPort) {
        this.bindPort = bindPort;
    }

    public Calendar getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Calendar insertTime) {
        this.insertTime = insertTime;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Calendar getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Calendar lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    

}
