package com.xt.bcloud.resource.db;

import com.xt.core.db.pm.IPersistence;
import com.xt.gt.ui.table.ColumnInfo;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 记录一组数据库相关的信息（在外边看来是一个数据库整体）.
 * 一个数据库组包含多个“主”数据库，和多个“备”数据库，但是至少有一个主数据库。
 * @author albert
 */
public class DbGroup implements IPersistence {

    private static final long serialVersionUID = 1212659115831359125L;
    
    /**
     *  内部编码
     */
    private String oid;

    /**
     * 外部编码
     */
    @ColumnInfo(title="外部编码")
    private String id;

    /**
     * 数据库组的名称
     */
    @ColumnInfo(title="名称")
    private String name;

    /**
     * 描述信息
     */
    @ColumnInfo(title="描述信息")
    private String description;

    /**
     * 是否可用, Y/N
     */
    @ColumnInfo(title="是否可用")
    private String valid;

    /**
     * 数据库的名称
     */
    @ColumnInfo(title="数据库名称")
    private String dbName;

    /**
     * 数据库用户（一个数据库组采用统一的用户）
     */
    @ColumnInfo(title="数据库用户")
    private String userName;

    /**
     * 数据库密码（一个数据库组采用统一的密码）
     */
    @ColumnInfo(title="数据库密码")
    private String passwd;

    /**
     * 数据库连接地址（带有参数的可替换的字符串）
     */
    @ColumnInfo(title="数据库连接地址")
    private String url;

    /**
     * 数据库使用的驱动程序
     */
    @ColumnInfo(title="数据库驱动类")
    private String driverClass;

    /**
     * 数据库采用的模式
     */
    @ColumnInfo(title="模式")
    private String dbSchema;

    /**
     * 当前状态（在用，闲置）
     */
    @ColumnInfo(title="状态")
    private String state;

    /**
     *  创建时间
     */
    @ColumnInfo(title="创建时间")
    private Calendar createTime;

    /**
     * 本数据库组包含的数据库资源
     */
    private final List<DbSource> dbSources = new ArrayList(2);

    public DbGroup() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DbGroup other = (DbGroup) obj;
        if ((this.oid == null) ? (other.oid != null) : !this.oid.equals(other.oid)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.oid != null ? this.oid.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder strBld = new StringBuilder();
        strBld.append(super.toString()).append("[");
        strBld.append("oid=").append(oid).append("; ");
        strBld.append("id=").append(id).append("; ");
        strBld.append("name=").append(name).append("; ");
        strBld.append("dbName=").append(dbName).append("; ");
        strBld.append("dbSchema=").append(dbSchema).append("; ");
        strBld.append("driverClass=").append(driverClass).append("; ");
        strBld.append("url=").append(url).append("; ");
        strBld.append("userName=").append(dbSchema).append("; ");
        strBld.append("passwd=******;");
        strBld.append("state=").append(state).append("; ");
        strBld.append("dbSources=").append(dbSources);
        strBld.append("]");
        return strBld.toString();
    }

    public Calendar getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Calendar createTime) {
        this.createTime = createTime;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDbSchema() {
        return dbSchema;
    }

    public void setDbSchema(String dbSchema) {
        this.dbSchema = dbSchema;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
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

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getValid() {
        return valid;
    }

    public void setValid(String valid) {
        this.valid = valid;
    }

    public List<DbSource> getDbSources() {
        return dbSources;
    }

    public void addDbSource(DbSource dbSource) {
        if (dbSource != null) {
            this.dbSources.add(dbSource);
        }
    }
    
}
