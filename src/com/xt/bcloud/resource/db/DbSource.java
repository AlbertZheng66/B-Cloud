package com.xt.bcloud.resource.db;

import com.xt.core.db.pm.IPersistence;
import com.xt.gt.ui.table.ColumnInfo;
import java.util.Calendar;

/**
 * ��¼��һ���ݿ�������ص���Ϣ.
 * @author albert
 */
public class DbSource implements IPersistence {
    
    private static final long serialVersionUID = 3319341908264581439L;

    /**
     * �ڲ�����
     */
    private String oid;
    /**
     * ���ݿ���ı���
     */
    private String groupOid;
    /**
     * ���ݿ����ƣ���һ���ݿ⣩
     */
    @ColumnInfo(title="���ݿ�����")
    private String name;
    /**
     * ���ݿ����ڵķ�������ַ
     */
    @ColumnInfo(title="��������ַ")
    private String ip;
    /**
     * ���ݿ�ʹ�õĶ˿ں�
     */
    @ColumnInfo(title="�˿ں�")
    private String port;
    /**
     * ������Ϣ
     */
    @ColumnInfo(title="������Ϣ")
    private String description;
    /**
     * �Ƿ����, Y/N
     */
    @ColumnInfo(title="�Ƿ����")
    private boolean valid;

    /**
     * ���⣬���Ǳ��⣬Y/N
     */
    @ColumnInfo(title="�Ƿ�������")
    private boolean master;
    
    /**
     * ��ǰ״̬�����ã����ã�
     */
    @ColumnInfo(title="��ǰ״̬")
    private String state;

    /**
     * ����ʱ��
     */
    @ColumnInfo(title="����ʱ��")
    private Calendar createTime;

    public DbSource() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DbSource other = (DbSource) obj;
        if ((this.oid == null) ? (other.oid != null) : !this.oid.equals(other.oid)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.oid != null ? this.oid.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder strBld = new StringBuilder();
        strBld.append(super.toString()).append("[");
        strBld.append("oid=").append(oid).append("; ");
        strBld.append("groupOid=").append(groupOid).append("; ");
        strBld.append("name=").append(name).append("; ");
        strBld.append("ip=").append(ip).append("; ");
        strBld.append("port=").append(port).append("; ");
        strBld.append("valid=").append(valid).append("; ");
        strBld.append("state=").append(state).append("; ");
        strBld.append("master=").append(master);
        strBld.append("]");
        return strBld.toString();
    }

    public Calendar getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Calendar createTime) {
        this.createTime = createTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGroupOid() {
        return groupOid;
    }

    public void setGroupOid(String groupOid) {
        this.groupOid = groupOid;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public boolean isMaster() {
        return master;
    }

    public boolean getMaster() {
        return master;
    }

    public void setMaster(boolean master) {
        this.master = master;
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

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean getValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
