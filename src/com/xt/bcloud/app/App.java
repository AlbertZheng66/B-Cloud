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
 * 描述一项基本的应用。
 * @author albert
 */
public class App implements IPersistence {
    
    private static final long serialVersionUID = 4720780632181342065L;

    /**
     * 描述一个应用的内部标识。
     */
    private String oid;
    /**
     * 一个的标识一个应用
     */
    @ColumnInfo(title = "应用编码")
    private String id;
    /**
     * 应用名称
     */
    @ColumnInfo(title = "名称")
    private String name;
    /**
     * 应用的描述信息
     */
    @ColumnInfo(title = "描述信息")
    private String description;
    /**
     * 应用标签（标识一个应用的特征）
     */
    @ColumnInfo(title = "应用标签")
    private String tags;

    /**
     * 应用的版本号（默认版本）
     */
    @ColumnInfo(title = "默认版本")
    private String versionOid;

    /**
     * 应用的状态
     */
    @ColumnInfo(title = "状态")
    @EnumConverterTypeDecl(EnumConverterType.String)
    private AppState state;
    
    /**
     * 此应用是否可用, Y/N
     */
    @ColumnInfo(title = "是否可用")
    private boolean valid;

    /**
     * 应用的所属人（拥有者）
     */
    @ColumnInfo(title = "所属人")
    private String owner;

    /**
     * 应用的上下文（如果版本的上下文为空，则使用此作为应用的上下文）
     * 使用了此上下文,任务分配器将自动调整"URL"的路径
     */
    @ColumnInfo(title = "上下文路径")
    private String contextPath;
    
    /**
     * 创建时间
     */
    @ColumnInfo(title = "注册时间")
    private Calendar insertTime;

    /**
     * 应用对应的域名（包括二级域名，二级域名应该以“/”开头）
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
