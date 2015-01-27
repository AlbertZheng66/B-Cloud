package com.xt.bcloud.app;

import com.xt.core.conv.impl.EnumConverterType;
import com.xt.core.conv.impl.EnumConverterTypeDecl;
import com.xt.core.db.po.Transient;
import com.xt.core.db.pm.IPersistence;
import com.xt.gt.ui.table.ColumnInfo;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 * ����һ�������Ӧ�á�
 * @author albert
 */
public class App implements IPersistence {
    
    private static final long serialVersionUID = 4720780632181342065L;

    /**
     * ����һ��Ӧ�õ��ڲ���ʶ��
     */
    private String oid;
    /**
     * һ���ı�ʶһ��Ӧ��
     */
    @ColumnInfo(title = "Ӧ�ñ���")
    private String id;
    /**
     * Ӧ������
     */
    @ColumnInfo(title = "����")
    private String name;
    /**
     * Ӧ�õ�������Ϣ
     */
    @ColumnInfo(title = "������Ϣ")
    private String description;
    /**
     * Ӧ�ñ�ǩ����ʶһ��Ӧ�õ�������
     */
    @ColumnInfo(title = "Ӧ�ñ�ǩ")
    private String tags;

    /**
     * Ӧ�õİ汾�ţ�Ĭ�ϰ汾��
     */
    @ColumnInfo(title = "Ĭ�ϰ汾")
    private String versionOid;

    /**
     * Ӧ�õ�״̬
     */
    @ColumnInfo(title = "״̬")
    @EnumConverterTypeDecl(EnumConverterType.String)
    private AppState state;
    
    /**
     * ��Ӧ���Ƿ����, Y/N
     */
    @ColumnInfo(title = "�Ƿ����")
    private boolean valid;

    /**
     * Ӧ�õ������ˣ�ӵ���ߣ�
     */
    @ColumnInfo(title = "������")
    private String owner;

    /**
     * Ӧ�õ������ģ�����汾��������Ϊ�գ���ʹ�ô���ΪӦ�õ������ģ�
     * ʹ���˴�������,������������Զ�����"URL"��·��
     */
    @ColumnInfo(title = "������·��")
    private String contextPath;
    
    /**
     * ����ʱ��
     */
    @ColumnInfo(title = "ע��ʱ��")
    private Calendar insertTime;

    /**
     * Ӧ�ö�Ӧ������������������������������Ӧ���ԡ�/����ͷ��
     */
    @Transient
    private final List<String> hosts = new ArrayList();

    public Calendar getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Calendar insertTime) {
        this.insertTime = insertTime;
    }

    public App() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final App other = (App) obj;
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
        strBld.append("id=").append(id).append("; ");
        strBld.append("versionOid=").append(versionOid).append("; ");
        strBld.append("contextPath=").append(contextPath).append("; ");
        strBld.append("hosts=").append(hosts).append("; ");
        strBld.append("name=").append(name);
        strBld.append("]");
        return strBld.toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AppState getState() {
        return state;
    }

    public void setState(AppState state) {
        this.state = state;
    }

    public String getVersionOid() {
        return versionOid;
    }

    public void setVersionOid(String versionOid) {
        this.versionOid = versionOid;
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

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public void addHost(String host) {
        if (StringUtils.isNotEmpty(host) && !hosts.contains(host)) {
            hosts.add(host.trim());
        }
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

}
