package com.xt.bcloud.resource.db;

import com.xt.core.db.pm.IPersistence;
import java.util.Calendar;

/**
 *
 * @author albert
 */
public class DbSourceInstance implements IPersistence {

    private static final long serialVersionUID = 7056255325674027728L;

    /**
     * 内部编码
     */
    private String oid;

    /**
     * 应用编码
     */
    private String appOid;

    /**
     * 应用版本编码
     */
    private String appVersionOid;

    /**
     * 数据库组的编码
     */
    private String dbGroupOid;

    /**
     * 描述信息
     */
    private String description;
    /**
     * 是否可用, Y/N
     */
    private boolean valid;
    /**
     * 当前状态（在用，闲置）
     */
    private String state;
    /**
     * 创建时间
     */
    private Calendar createTime;

    public DbSourceInstance() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DbSourceInstance other = (DbSourceInstance) obj;
        if ((this.oid == null) ? (other.oid != null) : !this.oid.equals(other.oid)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + (this.oid != null ? this.oid.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder strBld = new StringBuilder();
        strBld.append(super.toString()).append("[");
        strBld.append("oid=").append(oid).append("; ");
        strBld.append("appOid=").append(appOid).append("; ");
        strBld.append("appVersionOid=").append(appVersionOid).append("; ");
        strBld.append("dbGroupOid=").append(dbGroupOid).append("; ");
        strBld.append("description=").append(description).append("; ");
        strBld.append("valid=").append(valid).append("; ");
        strBld.append("state=").append(state);
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

    public Calendar getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Calendar createTime) {
        this.createTime = createTime;
    }

    public String getDbGroupOid() {
        return dbGroupOid;
    }

    public void setDbGroupOid(String dbGroupOid) {
        this.dbGroupOid = dbGroupOid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }


}
