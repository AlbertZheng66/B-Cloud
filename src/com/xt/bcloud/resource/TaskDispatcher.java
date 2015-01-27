
package com.xt.bcloud.resource;

import com.xt.bcloud.resource.server.ServerState;
import com.xt.core.db.pm.IPersistence;
import com.xt.gt.ui.table.ColumnInfo;
import java.util.Calendar;

/**
 *
 * @author Albert
 */
public class TaskDispatcher  implements IPersistence {
    
    private static final long serialVersionUID = 8628734067289042387L;
    
     /**
     * Ψһ��ʶ
     */
    private String oid;
    
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
     * ʧЧʱ��
     */
    private Calendar invalidTime;

    /**
     * ��ǰ״̬
     */
    @ColumnInfo(title="��ǰ״̬")
    private ServerState state;
    
    /**
     * ģ�����
     */
    @ColumnInfo(title="ģ�����")
    private String templateId;
    
    /**
     * ���һ�θ���ʱ��
     */
    @ColumnInfo(title="���һ�θ���ʱ��")
    private Calendar lastUpdatedTime;

    public TaskDispatcher() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TaskDispatcher other = (TaskDispatcher) obj;
        if ((this.oid == null) ? (other.oid != null) : !this.oid.equals(other.oid)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.oid != null ? this.oid.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "TaskDispatcher{" + "oid=" + oid + ", id=" + id + ", name=" + name + ", ip=" + ip + ", managerPort=" + managerPort + ", jmxRmiPort=" + jmxRmiPort + ", insertTime=" + insertTime + ", valid=" + valid + ", invalidTime=" + invalidTime + ", state=" + state + '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public int getJmxRmiPort() {
        return jmxRmiPort;
    }

    public void setJmxRmiPort(int jmxRmiPort) {
        this.jmxRmiPort = jmxRmiPort;
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

    public ServerState getState() {
        return state;
    }

    public void setState(ServerState state) {
        this.state = state;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public Calendar getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(Calendar lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }
    
    
}
