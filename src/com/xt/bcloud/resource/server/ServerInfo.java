
package com.xt.bcloud.resource.server;

import com.xt.bcloud.resource.Profile;
import com.xt.core.db.pm.IPersistence;
import com.xt.gt.ui.table.ColumnInfo;
import java.util.Calendar;

/**
 *
 * @author albert
 */
public class ServerInfo implements IPersistence {
    
    private static final long serialVersionUID = 3469993927685961658L;

    /**
     * Ψһ��ʶ
     */
    private String oid;
    
    /**
     * Ψһ��ʶ
     */
    private String appServerInstanceOid;

    /**
     * �Զ��� ID ��ʶ��
     */
    @ColumnInfo(title="�Զ����ʶ")
    private String id;

    /**
     * ���������ƣ��������ƣ����ڹ���
     */
    @ColumnInfo(title="����")
    private String name;

    /**
     * ��������IP ��ַ
     */
    @ColumnInfo(title="IP ��ַ")
    private String ip = "127.0.0.1";

    /**
     * �����õĶ˿ںš�
     */
    @ColumnInfo(title="����˿ں�")
    private int managerPort;
    
    /**
     * JMX ͨ���õĶ˿ںš�
     */
    @ColumnInfo(title="����˿ں�")
    private int jmxRmiPort;

    /**
     * Ӧ�õ�������·��
     */
    @ColumnInfo(title="������·��")
    private String contextPath;

    /**
     * ����ʱ��
     */
    @ColumnInfo(title="����ʱ��")
    private Calendar insertTime;

    /**
     * �Ƿ����(Ĭ��Ϊ����)
     */
    @ColumnInfo(title="�Ƿ����")
    private boolean valid = true;

    /**
     * �����������������Ϣ
     */
    private Profile profile;

    /**
     * ʧЧʱ��
     */
    private Calendar invalidTime;

    /**
     * ��ǰ״̬
     */
    @ColumnInfo(title="��ǰ״̬")
    private ServerState state;

    public ServerInfo() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ServerInfo other = (ServerInfo) obj;
        if ((this.oid == null) ? (other.oid != null) : !this.oid.equals(other.oid)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this.oid != null ? this.oid.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder strBld = new StringBuilder();
        strBld.append(super.toString()).append("[");
        strBld.append("oid=").append(oid).append("; ");
        strBld.append("id=").append(id).append("; ");
        strBld.append("name=").append(name).append("; ");
        strBld.append("ip=").append(ip).append("; ");
        strBld.append("state=").append(state).append("; ");
        strBld.append("managerPort=").append(managerPort).append("; ");
        strBld.append("jmxRmiPort=").append(jmxRmiPort).append("; ");
        strBld.append("profile=").append(profile);
        strBld.append("]");
        return strBld.toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
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

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public Calendar getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Calendar insertTime) {
        this.insertTime = insertTime;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public Calendar getInvalidTime() {
        return invalidTime;
    }

    public void setInvalidTime(Calendar invalidTime) {
        this.invalidTime = invalidTime;
    }

    public ServerState getState() {
        return state;
    }

    public void setState(ServerState state) {
        this.state = state;
    }

    public int getJmxRmiPort() {
        return jmxRmiPort;
    }

    public void setJmxRmiPort(int jmxRmiPort) {
        this.jmxRmiPort = jmxRmiPort;
    }

    public String getAppServerInstanceOid() {
        return appServerInstanceOid;
    }

    public void setAppServerInstanceOid(String appServerInstanceOid) {
        this.appServerInstanceOid = appServerInstanceOid;
    }
    
    

}
