

package com.xt.bcloud.app;

import com.xt.core.db.pm.IPersistence;
import com.xt.gt.ui.table.ColumnInfo;
import java.util.Calendar;

/**
 * Ӧ�ó���İ汾.
 * �汾ʵ���˱ȽϽӿ�, ͨ��"."�ָ���ַ������бȽϴ�С��
 * ���磺2.30 С�� 3.2, ��Ȼ��ĵ���������У�����4.0 > 3.2 > 3.1 > 2.5��
 * ע�⣺��������汾�ĳ��Ȳ���ȣ�ֻ�Ƚ���Ȳ��֣�����2.3.3 == 2.3
 * @author albert
 */
public class AppVersion implements IPersistence, Comparable<AppVersion> {

    
    private static final long serialVersionUID = 4554230348502590060L;
        
    /**
     * �ڲ�����
     */
    private String oid;

    /**
     * Ӧ�ñ���
     */
     private String appOid;

     /**
      * �汾��
      */
    @ColumnInfo(title="�汾��")
    private String version;

    /**
     * ��������
     */
    @ColumnInfo(title="��������")
    private String code;

    /**
     * ������Ϣ
     */
    @ColumnInfo(title="������Ϣ")
    private String description;

    /**
     * ��������λ��
     */
    private String deployFileName;

    /**
     * Ӧ�õ�������
     * ʹ���˴�������,������������Զ�����"URL"��·��
     */
    @ColumnInfo(title = "������·��")
    private String contextPath;

    /**
     * �ϴ����ļ��������û��������ļ�������ע�⣺���־û����ֶΣ�
     * ���ϴ��İ����浽�ļ�ϵͳ�С�
     */
//    @Transient
//    private InputStream uploadingDeployedFile;

    // private String seq; //��������

    /**
     * �˰汾�Ƿ����, Y/N
     */
    @ColumnInfo(title="�Ƿ���Ч")
    private boolean valid;

    /**
     * ����ʱ��
     */
    @ColumnInfo(title="����ʱ��")
    private Calendar insertTime;

    /**
     * �����ļ���С���ֽ�����
     */
    @ColumnInfo(title="�ļ���С")
    private int fileSize;

    /**
     * ��ǰ�汾��״̬�����У����ԣ�δ����
     */
    @ColumnInfo(title="״̬")
    private AppVersionState state;

    public AppVersion() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AppVersion other = (AppVersion) obj;
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
        strBld.append("version=").append(version).append("; ");
        strBld.append("contextPath=").append(contextPath).append("; ");
        strBld.append("state=").append(state).append("; ");
        strBld.append("fileSize=").append(fileSize).append("; ");
        strBld.append("code=").append(code);
        strBld.append("]");
        return strBld.toString();
    }

    public String getAppOid() {
        return appOid;
    }

    public void setAppOid(String appOid) {
        this.appOid = appOid;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDeployFileName() {
        return deployFileName;
    }

    public void setDeployFileName(String deployFileName) {
        this.deployFileName = deployFileName;
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

    public boolean isValid() {
        return valid;
    }

    public boolean getValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Calendar getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Calendar insertTime) {
        this.insertTime = insertTime;
    }

    public AppVersionState getState() {
        return state;
    }

    public void setState(AppVersionState state) {
        this.state = state;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }



    public int compareTo(AppVersion o) {
        if (o == null || o.version == null) {
            return 1;
        }
        if (this.version == null) {
            return -1;
        }
        String[] otherSegs = o.version.split("[.]");
        String[] thisSegs  = version.split("[.]");
        for (int i = 0; i < Math.min(thisSegs.length, otherSegs.length); i++) {
            int value = thisSegs[i].compareTo(otherSegs[i]);
            if (value != 0) {
                // ���ϵİ汾����ǰ��
                return (value);
            }
        }
        return 0;
    }

}
