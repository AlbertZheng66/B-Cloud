
package com.xt.bcloud.sys;

import com.xt.gt.ui.table.ColumnInfo;
import java.util.Date;

/**
 * 用于描述一个“承租人”的实例信息，对应于数据库中的一条记录。
 * @author albert
 */
public class Tenant {
    
    /**
     * 租户的内部编码
     */ 
    private String oid;


    /**
     * 租户的自定义编码
     */
    @ColumnInfo(title = "编码")
    private String code;

    /**
     * 租户的真实名称（合法名称）
     */
    @ColumnInfo(title = "名称")
    private String name;

    /**
     * 昵称
     */
    @ColumnInfo(title = "昵称")
    private String nickname;

    @ColumnInfo(title = "有效")
    private boolean enabled;

    @ColumnInfo(title = "描述")
    private String description;

    @ColumnInfo(title = "密码")
    private String password;

    @ColumnInfo(title = "创建时间")
    private Date createTime;

    @ColumnInfo(title = "性别")
    private String gender;

    @ColumnInfo(title = "电子邮件")
    private String email;

    @ColumnInfo(title = "电话")
    private String telephone;

    @ColumnInfo(title = "地址")
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
