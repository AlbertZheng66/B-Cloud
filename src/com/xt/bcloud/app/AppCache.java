
package com.xt.bcloud.app;

import com.xt.core.db.pm.IPersistence;
import java.util.Calendar;

/**
 * 应用升级的相关信息.
 * @author albert
 */
public class AppCache  implements IPersistence {
    
    private static final long serialVersionUID = 4517977257353571422L;

    /**
     * 描述一个应用的内部标识。
     */
    private String oid;

    /**
     * 应用的内部编码
     */
    private String appOid;

    /**
     * 应用版本的内部编码
     */
    private String appVersionOid;

    /**
     * 原版本的编码
     */
    private String oldVersionOid;

    /**
     * Cache 的编码
     */
    private String cacheId;

    /**
     * 此应用是否可用, Y/N
     */
    private boolean valid;

    /**
     * 创建时间
     */
    private Calendar insertTime;

    public AppCache() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AppCache other = (AppCache) obj;
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
        strBld.append("appOid=").append(appOid).append("; ");
        strBld.append("appVersionOid=").append(appVersionOid).append("; ");
        strBld.append("oldVersionOid=").append(oldVersionOid).append("; ");
        strBld.append("sessionPrefix=").append(cacheId).append("; ");
        strBld.append("valid=").append(valid);
        strBld.append("]");
        return strBld.toString();
    }


    public Calendar getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Calendar insertTime) {
        this.insertTime = insertTime;
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

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getOldVersionOid() {
        return oldVersionOid;
    }

    public void setOldVersionOid(String oldVersionOid) {
        this.oldVersionOid = oldVersionOid;
    }

    public String getCacheId() {
        return cacheId;
    }

    public void setCacheId(String cacheId) {
        this.cacheId = cacheId;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

}
