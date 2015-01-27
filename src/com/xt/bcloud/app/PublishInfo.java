
package com.xt.bcloud.app;

import com.xt.core.db.pm.IPersistence;
import com.xt.gt.sys.SystemConfiguration;
import com.xt.gt.ui.table.ColumnInfo;
import java.util.Calendar;

/**
 * 发布应用时需要的相关信息.
 * @author albert
 */
public class PublishInfo implements IPersistence {

    private static final long serialVersionUID = -6586378478603869298L;

    /**
     * 缺省的最小服务器数量，可通过参数“publishInfo.minServers”进行设置，默认为 1.
     */
    private final static int DEFAULT_MIN_SERVERS = SystemConfiguration.getInstance().readInt("publishInfo.minServers", 1);

     /**
     * 缺省的初始化服务器数量，可通过参数“publishInfo.initialServers”进行设置，默认为 1.
     */
    private final static int DEFAULT_INITIAL_SERVERS = SystemConfiguration.getInstance().readInt("publishInfo.initialServers", 1);


    /**
     * 缺省的最小服务器数量，可通过参数“publishInfo.minServers”进行设置，默认为 10.
     */
    private final static int DEFAULT_MAX_SERVERS = SystemConfiguration.getInstance().readInt("publishInfo.maxServers", 10);
    
    /**
     * 唯一标识
     */
    private String oid;

    /**
     * 与其关联的应用OID。
     */
    private String appOid;

    /**
     * 与其关联的应用的版本号 OID。
     */
    private String appVersionOid;

    /**
     * 初始启动的服务器数量
     */
    @ColumnInfo(title="初始化服务器数量")
    private int initialServers = DEFAULT_INITIAL_SERVERS;
    
    /**
     * 最小启动的服务器数量, 如果系统小于此数量, 将标识为失败。
     */
    @ColumnInfo(title="最小服务器数量")
    private int minServers = DEFAULT_MIN_SERVERS;

    /**
     * 最大启动的服务器数量
     */
    @ColumnInfo(title="最大服务器数量")
    private int maxServers = DEFAULT_MAX_SERVERS;

    /**
     * 是否自动升级（当容器负载达到一定限度，并且启动实例已经超过定义的“最大数量”时，
     * 系统将自动扩展服务器的数量）
     */
    private boolean autoScale = true;

    // TODO: 最大连接数，超时时间等也需要在此设置。

    /**
     * 创建时间
     */
    @ColumnInfo(title=" 创建时间")
    private Calendar insertTime;

    public PublishInfo() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PublishInfo other = (PublishInfo) obj;
        if ((this.oid == null) ? (other.oid != null) : !this.oid.equals(other.oid)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.oid != null ? this.oid.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder strBld = new StringBuilder();
        strBld.append(super.toString()).append("[");
        strBld.append("oid=").append(oid).append("; ");
        strBld.append("appOid=").append(appOid).append("; ");
        strBld.append("initialServers=").append(initialServers).append("; ");
        strBld.append("minServers=").append(minServers).append("; ");
        strBld.append("maxServers=").append(maxServers);
        strBld.append("]");
        return strBld.toString();
    }



    public String getAppOid() {
        return appOid;
    }

    public void setAppOid(String appOid) {
        this.appOid = appOid;
    }

    public int getInitialServers() {
        return initialServers;
    }

    public void setInitialServers(int initialServers) {
        this.initialServers = initialServers;
    }

    public int getMaxServers() {
        return maxServers;
    }

    public void setMaxServers(int maxServers) {
        this.maxServers = maxServers;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getAppVersionOid() {
        return appVersionOid;
    }

    public void setAppVersionOid(String appVersionOid) {
        this.appVersionOid = appVersionOid;
    }

    public Calendar getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Calendar insertTime) {
        this.insertTime = insertTime;
    }

    public int getMinServers() {
        return minServers;
    }

    public void setMinServers(int minServers) {
        this.minServers = minServers;
    }

    public boolean getAutoScale() {
        return autoScale;
    }

    public boolean isAutoScale() {
        return autoScale;
    }

    public void setAutoScale(boolean autoScale) {
        this.autoScale = autoScale;
    }
}
