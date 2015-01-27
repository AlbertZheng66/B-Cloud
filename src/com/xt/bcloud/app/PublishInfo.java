
package com.xt.bcloud.app;

import com.xt.core.db.pm.IPersistence;
import com.xt.gt.sys.SystemConfiguration;
import com.xt.gt.ui.table.ColumnInfo;
import java.util.Calendar;

/**
 * ����Ӧ��ʱ��Ҫ�������Ϣ.
 * @author albert
 */
public class PublishInfo implements IPersistence {

    private static final long serialVersionUID = -6586378478603869298L;

    /**
     * ȱʡ����С��������������ͨ��������publishInfo.minServers���������ã�Ĭ��Ϊ 1.
     */
    private final static int DEFAULT_MIN_SERVERS = SystemConfiguration.getInstance().readInt("publishInfo.minServers", 1);

     /**
     * ȱʡ�ĳ�ʼ����������������ͨ��������publishInfo.initialServers���������ã�Ĭ��Ϊ 1.
     */
    private final static int DEFAULT_INITIAL_SERVERS = SystemConfiguration.getInstance().readInt("publishInfo.initialServers", 1);


    /**
     * ȱʡ����С��������������ͨ��������publishInfo.minServers���������ã�Ĭ��Ϊ 10.
     */
    private final static int DEFAULT_MAX_SERVERS = SystemConfiguration.getInstance().readInt("publishInfo.maxServers", 10);
    
    /**
     * Ψһ��ʶ
     */
    private String oid;

    /**
     * ���������Ӧ��OID��
     */
    private String appOid;

    /**
     * ���������Ӧ�õİ汾�� OID��
     */
    private String appVersionOid;

    /**
     * ��ʼ�����ķ���������
     */
    @ColumnInfo(title="��ʼ������������")
    private int initialServers = DEFAULT_INITIAL_SERVERS;
    
    /**
     * ��С�����ķ���������, ���ϵͳС�ڴ�����, ����ʶΪʧ�ܡ�
     */
    @ColumnInfo(title="��С����������")
    private int minServers = DEFAULT_MIN_SERVERS;

    /**
     * ��������ķ���������
     */
    @ColumnInfo(title="������������")
    private int maxServers = DEFAULT_MAX_SERVERS;

    /**
     * �Ƿ��Զ����������������شﵽһ���޶ȣ���������ʵ���Ѿ���������ġ����������ʱ��
     * ϵͳ���Զ���չ��������������
     */
    private boolean autoScale = true;

    // TODO: �������������ʱʱ���Ҳ��Ҫ�ڴ����á�

    /**
     * ����ʱ��
     */
    @ColumnInfo(title=" ����ʱ��")
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
