
package com.xt.bcloud.sys;

import com.xt.gt.ui.table.ColumnInfo;
import java.util.Date;

/**
 * ��������һ���������ˡ���ʵ����Ϣ����Ӧ�����ݿ��е�һ����¼��
 * @author albert
 */
public class Tenant {
    
    /**
     * �⻧���ڲ�����
     */ 
    private String oid;


    /**
     * �⻧���Զ������
     */
    @ColumnInfo(title = "����")
    private String code;

    /**
     * �⻧����ʵ���ƣ��Ϸ����ƣ�
     */
    @ColumnInfo(title = "����")
    private String name;

    /**
     * �ǳ�
     */
    @ColumnInfo(title = "�ǳ�")
    private String nickname;

    @ColumnInfo(title = "��Ч")
    private boolean enabled;

    @ColumnInfo(title = "����")
    private String description;

    @ColumnInfo(title = "����")
    private String password;

    @ColumnInfo(title = "����ʱ��")
    private Date createTime;

    @ColumnInfo(title = "�Ա�")
    private String gender;

    @ColumnInfo(title = "�����ʼ�")
    private String email;

    @ColumnInfo(title = "�绰")
    private String telephone;

    @ColumnInfo(title = "��ַ")
    private String address;

    public Tenant() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Tenant other = (Tenant) obj;
        if ((this.oid == null) ? (other.oid != null) : !this.oid.equals(other.oid)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 73 * hash + (this.oid != null ? this.oid.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder strBld = new StringBuilder();
        strBld.append(super.toString()).append("[");
        strBld.append("oid=").append(oid).append("; ");
        strBld.append("name=").append(name).append("; ");
        strBld.append("]");
        return strBld.toString();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }



}
