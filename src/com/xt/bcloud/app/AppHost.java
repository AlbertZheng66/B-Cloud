
package com.xt.bcloud.app;

import com.xt.core.db.pm.IPersistence;
import com.xt.gt.ui.table.ColumnInfo;

/**
 * 应用所服务的主机号
 * @author albert
 */
public class AppHost implements IPersistence {

    private static final long serialVersionUID = 1006760803123571299L;

    private String oid;

    private String appOid;

    @ColumnInfo(title="域名")
    private String host;

    @ColumnInfo(title="是否生效")
    private boolean valid = true;

    public AppHost() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AppHost other = (AppHost) obj;
        if ((this.oid == null) ? (other.oid != null) : !this.oid.equals(other.oid)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + (this.oid != null ? this.oid.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder strBld = new StringBuilder();
        strBld.append(super.toString()).append("[");
        strBld.append("oid=").append(oid).append("; ");
        strBld.append("appOid=").append(appOid).append("; ");
        strBld.append("host=").append(host).append("; ");
        strBld.append("valid=").append(valid);
        strBld.append("]");
        return strBld.toString();
    }

    public String getAppOid() {
        return appOid;
    }

    public void setAppOid(String appOid) {
        this.appOid = appOid;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public boolean getValid() {
        return this.valid;
    }

}
