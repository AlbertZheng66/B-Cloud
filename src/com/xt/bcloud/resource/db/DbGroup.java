package com.xt.bcloud.resource.db;

import com.xt.core.db.pm.IPersistence;
import com.xt.gt.ui.table.ColumnInfo;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * ��¼һ�����ݿ���ص���Ϣ������߿�����һ�����ݿ����壩.
 * һ�����ݿ������������������ݿ⣬�Ͷ�����������ݿ⣬����������һ�������ݿ⡣
 * @author albert
 */
public class DbGroup implements IPersistence {

    private static final long serialVersionUID = 1212659115831359125L;
    
    /**
     *  �ڲ�����
     */
    private String oid;

    /**
     * �ⲿ����
     */
    @ColumnInfo(title="�ⲿ����")
    private String id;

    /**
     * ���ݿ��������
     */
    @ColumnInfo(title="����")
    private String name;

    /**
     * ������Ϣ
     */
    @ColumnInfo(title="������Ϣ")
    private String description;

    /**
     * �Ƿ����, Y/N
     */
    @ColumnInfo(title="�Ƿ����")
    private String valid;

    /**
     * ���ݿ������
     */
    @ColumnInfo(title="���ݿ�����")
    private String dbName;

    /**
     * ���ݿ��û���һ�����ݿ������ͳһ���û���
     */
    @ColumnInfo(title="���ݿ��û�")
    private String userName;

    /**
     * ���ݿ����루һ�����ݿ������ͳһ�����룩
     */
    @ColumnInfo(title="���ݿ�����")
    private String passwd;

    /**
     * ���ݿ����ӵ�ַ�����в����Ŀ��滻���ַ�����
     */
    @ColumnInfo(title="���ݿ����ӵ�ַ")
    private String url;

    /**
     * ���ݿ�ʹ�õ���������
     */
    @ColumnInfo(title="���ݿ�������")
    private String driverClass;

    /**
     * ���ݿ���õ�ģʽ
     */
    @ColumnInfo(title="ģʽ")
    private String dbSchema;

    /**
     * ��ǰ״̬�����ã����ã�
     */
    @ColumnInfo(title="״̬")
    private String state;

    /**
     *  ����ʱ��
     */
    @ColumnInfo(title="����ʱ��")
    private Calendar createTime;

    /**
     * �����ݿ�����������ݿ���Դ
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
