
package com.xt.bcloud.app;

import com.xt.core.db.pm.IPersistence;
import java.util.Calendar;

/**
 * Ӧ�������������Ϣ.
 * @author albert
 */
public class AppCache  implements IPersistence {
    
    private static final long serialVersionUID = 4517977257353571422L;

    /**
     * ����һ��Ӧ�õ��ڲ���ʶ��
     */
    private String oid;

    /**
     * Ӧ�õ��ڲ�����
     */
    private String appOid;

    /**
     * Ӧ�ð汾���ڲ�����
     */
    private String appVersionOid;

    /**
     * ԭ�汾�ı���
     */
    private String oldVersionOid;

    /**
     * Cache �ı���
     */
    private String cacheId;

    /**
     * ��Ӧ���Ƿ����, Y/N
     */
    private boolean valid;

    /**
     * ����ʱ��
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
