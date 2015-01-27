
package com.xt.gt.demo.po;

import com.xt.core.db.pm.IPersistence;
import com.xt.gt.ui.dic.DictionaryDecl;
import com.xt.gt.ui.table.ColumnInfo;
import java.util.Calendar;

/**
 *
 * @author albert
 */
public class UserInfo implements IPersistence {

    private String oid;

    @ColumnInfo(title="用户编码")
    private String userId;

    @ColumnInfo(title="性别")
    private String gender;

    @DictionaryDecl(name="degree_type")
    @ColumnInfo(title="学历")
    private String degree;

    @ColumnInfo(title="名称")
    private String name;

    @ColumnInfo(title="身份证号")
    private String ssn;

    @ColumnInfo(title="电子邮件")
    private String email;

    @ColumnInfo(title="生日")
    private Calendar birthday;
    @ColumnInfo(title="办公电话")
    private String officeNumber;
    @ColumnInfo(title="手机")
    private String mobile;
    private Calendar insertTime;
    @ColumnInfo(title="密码")
    private String passwd;

    public UserInfo() {
    }

    public Calendar getBirthday() {
        return birthday;
    }

    public void setBirthday(Calendar birthday) {
        this.birthday = birthday;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Calendar getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Calendar insertTime) {
        this.insertTime = insertTime;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOfficeNumber() {
        return officeNumber;
    }

    public void setOfficeNumber(String officeNumber) {
        this.officeNumber = officeNumber;
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

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UserInfo other = (UserInfo) obj;
        if ((this.oid == null) ? (other.oid != null) : !this.oid.equals(other.oid)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (this.oid != null ? this.oid.hashCode() : 0);
        return hash;
    }

    

}
