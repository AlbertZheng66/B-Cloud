package com.xt.bcloud.resource.db;

import com.xt.core.db.pm.IPersistence;
import com.xt.gt.ui.table.ColumnInfo;
import java.util.Calendar;

/**
 * 记录单一数据库配置相关的信息.
 * @author albert
 */
public class DbSource implements IPersistence {
    
    private static final long serialVersionUID = 3319341908264581439L;

    /**
     * 内部编码
     */
    private String oid;
    /**
     * 数据库组的编码
     */
    private String groupOid;
    /**
     * 数据库名称（单一数据库）
     */
    @ColumnInfo(title="数据库名称")
    private String name;
    /**
     * 数据库所在的服务器地址
     */
    @ColumnInfo(title="服务器地址")
    private String ip;
    /**
     * 数据库使用的端口号
     */
    @ColumnInfo(title="端口号")
    private String port;
    /**
     * 描述信息
     */
    @ColumnInfo(title="描述信息")
    private String description;
    /**
     * 是否可用, Y/N
     */
    @ColumnInfo(title="是否可用")
    private boolean valid;

    /**
     * 主库，还是备库，Y/N
     */
    @ColumnInfo(title="是否是主库")
    private boolean master;
    
    /**
     * 当前状态（在用，闲置）
     */
    @ColumnInfo(title="当前状态")
    private String state;

    /**
     * 创建时间
     */
    @ColumnInfo(title="创建时间")
    private Calendar createTime;

    public DbSource() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DbSource other = (DbSource) obj;
        if ((this.oid == null) ? (other.oid != null) : !this.oid.equals(other.oid)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.oid != null ? this.oid.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder strBld = new StringBuilder();
        strBld.append(super.toString()).append("[");
        strBld.append("oid=").append(oid).append("; ");
        strBld.append("groupOid=").append(groupOid).append("; ");
        strBld.append("name=").append(name).append("; ");
        strBld.append("ip=").append(ip).append("; ");
        strBld.append("port=").append(port).append("; ");
        strBld.append("valid=").append(valid).append("; ");
        strBld.append("state=").append(state).append("; ");
        strBld.append("master=").append(master);
        strBld.append("]");
        return strBld.toString();
    }

    public Calendar getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Calendar createTime) {
        this.createTime = createTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGroupOid() {
        return groupOid;
    }

    public void setGroupOid(String groupOid) {
        this.groupOid = groupOid;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public boolean isMaster() {
        return master;
    }

    public boolean getMaster() {
        return master;
    }

    public void setMaster(boolean master) {
        this.master = master;
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

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean getValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
