
package com.xt.bcloud.mdu;

import com.xt.core.db.pm.IPersistence;
import com.xt.gt.ui.table.ColumnInfo;
import java.util.Calendar;

/**
 * ��װ��һ������������������Ϣ��
 * @author Albert
 */
public class PhyServer  implements IPersistence {
    
    private static final long serialVersionUID = 5721666662957147036L;
    
     /**
     * Ψһ��ʶ
     */
    private String oid;

//    /**
//     * �Զ��� ID ��ʶ��
//     */
//    @ColumnInfo(title="�Զ����ʶ")
//    private String id;

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
    
//    /**
//     * �������� MAC ��ַ
//     */
//    @ColumnInfo(title="IP ��ַ")
//    private String macAddress;
    
    /**
     * ����ϵͳ����
     */
    @ColumnInfo(title="����ϵͳ����")
    private String osName;
    
    /**
     * ����ϵͳ�汾
     */
    @ColumnInfo(title="����ϵͳ�汾")
    private String osVersion;

    /**
     * �����õĶ˿ںš�
     */
    @ColumnInfo(title="����˿ں�")
    private int managerPort;
    
    /**
     * ����·�������ڷ���Ӧ�õ�·��
     */
    @ColumnInfo(title="����·��")
    private String workPath;
    
    
    /**
     * ��ǰ����·��
     */
    @ColumnInfo(title="��ǰ����·��")
    private String userPath;
    
    /**
     * ��ʱĿ¼����������Ӧ�õȲ�������ʱĿ¼
     */
    @ColumnInfo(title="��ʱĿ¼")
    private String tempPath;
    

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
     * ���һ�θ���ʱ��
     */
    @ColumnInfo(title="���һ�θ���ʱ��")
    private Calendar lastUpdatedTime;

    /**
     * ʧЧʱ��
     */
    @ColumnInfo(title="lastUpdatedTime")
    private Calendar invalidTime;

    /**
     * ��ǰ״̬
     */
    @ColumnInfo(title="��ǰ״̬")
    private PhyServerState state;
    

    public PhyServer() {
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

    public Calendar getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(Calendar lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
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

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public PhyServerState getState() {
        return state;
    }

    public void setState(PhyServerState state) {
        this.state = state;
    }

    public String getTempPath() {
        return tempPath;
    }

    public void setTempPath(String tempPath) {
        this.tempPath = tempPath;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    
    
    public String getUserPath() {
        return userPath;
    }

    public void setUserPath(String userDir) {
        this.userPath = userDir;
    }

    public boolean isValid() {
        return valid;
    }
    
    
    public boolean getValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getWorkPath() {
        return workPath;
    }

    public void setWorkPath(String workPath) {
        this.workPath = workPath;
    }
  
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PhyServer other = (PhyServer) obj;
        if ((this.oid == null) ? (other.oid != null) : !this.oid.equals(other.oid)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + (this.oid != null ? this.oid.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "PhyServer{" + "oid=" + oid + ", name=" + name + ", ip=" + ip + ", workPath=" + workPath + ", userDir=" + userPath + ", tempPath=" + tempPath + ", osName=" + osName + ", managerPort=" + managerPort + ", insertTime=" + insertTime + ", valid=" + valid + ", lastUpdatedTime=" + lastUpdatedTime + ", invalidTime=" + invalidTime + ", state=" + state + '}';
    }
    
    
    
    
}
