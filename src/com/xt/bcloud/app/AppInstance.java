package com.xt.bcloud.app;

import com.xt.core.db.pm.IPersistence;
import com.xt.gt.ui.table.ColumnInfo;
import java.util.Calendar;

/**
 * �����Ӧ�õľ����������� Cattle �ĸ������ƣ�ֻ����������Ҫ���ڳ־û�֮�á�
 * @author albert
 */
public class AppInstance implements IPersistence {

    private static final long serialVersionUID = -2753839720424337361L;


    /**
     *  �ڲ�����
     */
    private String oid;

    /**
     * ����������
     */
    @ColumnInfo(title="����������")
    private String serverOid;

    /**
     * ��ǰʵ���ı��루�����
     */
    private String cattleOid;

    /**
     * Ӧ�ñ���
     */
    private String appOid;
    /**
     * Ӧ�ð汾����
     */
    private String appVersionOid;
    /**
     * ��������ַ
     */
    @ColumnInfo(title="��������ַ")
    private String ip;
    /**
     * ռ�ö˿ں�
     */
    @ColumnInfo(title="ռ�ö˿ں�")
    private String port;
    /**
     * ����������
     */
    @ColumnInfo(title="����������")
    private String contextPath;
    /**
     * �Ƿ����, Y/N
     */
    @ColumnInfo(title="�Ƿ����")
    private boolean valid;
    /**
     * ����ʱ��
     */
    @ColumnInfo(title="����ʱ��")
    private Calendar startupTime;
    
    /**
     * ��ǰ״̬
     */
    @ColumnInfo(title="��ǰ״̬")
    private AppInstanceState state;

//    /**
//     * ��ͨ�ŵİ󶨵�ַ
//     */
//    private String bindAddr;
//
//
//    /**
//     * ��ͨ�ŵİ󶨵�ַ
//     */
//    private String bindPort;

    /**
     * �ر�ʱ��
     */
    private Calendar shutdownTime;

    /**
     * ʧЧʱ�䣨�Զ�Ѳ�ӳ��򽫶Է���������̽�⣬���������Ӧ����������ΪʧЧ��
     */
    private Calendar invalidTime;

     /**
     * �ر�ʱ��
     */
    @ColumnInfo(title="����·��")
    private String deployPath;

    public AppInstance() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AppInstance other = (AppInstance) obj;
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
        strBld.append("serverOid=").append(serverOid).append("; ");
        strBld.append("appOid=").append(appOid).append("; ");
        strBld.append("appVersionOid=").append(appVersionOid).append("; ");
        strBld.append("startupTime=").append(startupTime).append("; ");
        strBld.append("deployPath=").append(deployPath).append("; ");
        strBld.append("ip=").append(ip);
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

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
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

    public String getServerOid() {
        return serverOid;
    }

    public void setServerOid(String serverOid) {
        this.serverOid = serverOid;
    }

    public Calendar getStartupTime() {
        return startupTime;
    }

    public void setStartupTime(Calendar startupTime) {
        this.startupTime = startupTime;
    }

    public AppInstanceState getState() {
        return state;
    }

    public void setState(AppInstanceState state) {
        this.state = state;
    }

    public boolean getValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getCattleOid() {
        return cattleOid;
    }

    public void setCattleOid(String cattleOid) {
        this.cattleOid = cattleOid;
    }

//    public String getBindAddr() {
//        return bindAddr;
//    }
//
//    public void setBindAddr(String bindAddr) {
//        this.bindAddr = bindAddr;
//    }
//
//    public String getBindPort() {
//        return bindPort;
//    }
//
//    public void setBindPort(String bindPort) {
//        this.bindPort = bindPort;
//    }

    public Calendar getShutdownTime() {
        return shutdownTime;
    }

    public void setShutdownTime(Calendar shutdownTime) {
        this.shutdownTime = shutdownTime;
    }

    public String getDeployPath() {
        return deployPath;
    }

    public void setDeployPath(String deployPath) {
        this.deployPath = deployPath;
    }
}
