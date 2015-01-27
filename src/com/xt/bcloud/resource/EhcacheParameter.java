package com.xt.bcloud.resource;

import com.xt.core.db.pm.IPersistence;
import java.util.Calendar;

/**
 * ��¼�ֲ�ʽ����İ󶨶˿ڵ���ز�����
 * @author albert
 */
public class EhcacheParameter implements IPersistence {

    private static final long serialVersionUID = 7143639028637966560L;
    /**
     * �ڲ�����
     */
    private String oid;
//    /**
//     * Ӧ�ñ���
//     */
//    private String appOid;
//    /**
//     * Ӧ�ð汾����
//     */
//    private String appVersionOid;
    /**
     * Ӧ��ʵ������
     */
    private String appInstanceOid;

    /**
     * ����ı��루ÿ�������İ汾ʹ��ͬһ�����棬�������İ汾Ҳʹ�õ����Ļ��棩
     */
    private String cacheId;
    /**
     * ��ͨ�ŵİ󶨵�ַ
     */
    private String bindAddr;
    /**
     * ��ͨ��ռ�õĶ˿�
     */
    private String bindPort;
    /**
     * ����ʱ��
     */
    private Calendar insertTime;

    public EhcacheParameter() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EhcacheParameter other = (EhcacheParameter) obj;
        if ((this.oid == null) ? (other.oid != null) : !this.oid.equals(other.oid)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + (this.oid != null ? this.oid.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder strBld = new StringBuilder();
        strBld.append(super.toString()).append("[");
        strBld.append("oid=").append(oid).append("; ");
//        strBld.append("appOid=").append(appOid).append("; ");
//        strBld.append("appVersionOid=").append(appVersionOid).append("; ");
        strBld.append("appInstanceOid=").append(appInstanceOid).append("; ");
        strBld.append("cacheId=").append(cacheId).append("; ");
        strBld.append("bindAddr=").append(bindAddr).append("; ");
        strBld.append("bindPort=").append(bindPort);
        strBld.append("]");
        return strBld.toString();
    }

//    public String getAppOid() {
//        return appOid;
//    }
//
//    public void setAppOid(String appOid) {
//        this.appOid = appOid;
//    }
//
//    public String getAppVersionOid() {
//        return appVersionOid;
//    }
//
//    public void setAppVersionOid(String appVersionOid) {
//        this.appVersionOid = appVersionOid;
//    }

    public String getBindAddr() {
        return bindAddr;
    }

    public void setBindAddr(String bindAddr) {
        this.bindAddr = bindAddr;
    }

    public String getBindPort() {
        return bindPort;
    }

    public void setBindPort(String bindPort) {
        this.bindPort = bindPort;
    }

    public String getCacheId() {
        return cacheId;
    }

    public void setCacheId(String cacheId) {
        this.cacheId = cacheId;
    }

    public Calendar getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Calendar insertTime) {
        this.insertTime = insertTime;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getAppInstanceOid() {
        return appInstanceOid;
    }

    public void setAppInstanceOid(String appInstanceOid) {
        this.appInstanceOid = appInstanceOid;
    }

    
}
