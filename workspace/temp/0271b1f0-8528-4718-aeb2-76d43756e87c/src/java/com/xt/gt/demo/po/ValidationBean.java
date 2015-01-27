
package com.xt.gt.demo.po;

import com.xt.core.db.pm.IPersistence;
import com.xt.core.validation.decl.Chinese;
import com.xt.core.validation.decl.Email;
import com.xt.core.validation.decl.HTTP;
import com.xt.core.validation.decl.Mobile;
import com.xt.core.validation.decl.Phone;
import com.xt.core.validation.decl.PositiveInteger;
import com.xt.core.validation.decl.SSN;
import com.xt.gt.chr.event.filter.FilterType;
import com.xt.gt.ui.dic.DictionaryDecl;
import com.xt.gt.ui.fsp.FilterDecl;
import com.xt.gt.ui.fsp.Sort;
import com.xt.gt.ui.table.ColumnInfo;
import java.io.Serializable;
import java.util.Date;
import javax.validation.Length;
import javax.validation.NotEmpty;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 *
 * @author albert
 */
public class ValidationBean implements Serializable, IPersistence {

    public static int seq = 0;

    @ColumnInfo(title="字符串")
    public String string1 = "string1-" + (seq++);

    @ColumnInfo(title="日期")
    private Date date1 = new Date();


    @Length(max=2, message="密码长度不能大于 2。")
    @NotEmpty(message="密码不能为空。")
    @ColumnInfo(title="字符串 2")
    public String string2 = "字符串2-" + (seq++);

    @Sort
    @FilterDecl(type=FilterType.EQUALS, immediate=false)
    @ColumnInfo(title="标题")
    private String title;

    @DictionaryDecl(name="city")
    @ColumnInfo(title="字典")
    private String dictionary1 = "010";

    @Length(max=2,message="lookup1不能大于 2。")
    @NotEmpty(message="lookup1不能为空。")
    @FilterDecl(type=FilterType.ENDS_WITH, immediate=false)
    @ColumnInfo(title="查询")
    private String lookup1 = "lookup1-" + seq;

    @Length(max=2,message="lookup2不能大于 2。")
    @NotEmpty(message="lookup2不能为空。")
    private String lookup2 = "lookup2-" + seq;

    @ColumnInfo(title="整数")
    @com.xt.core.validation.decl.Integer
    private int int1;

    @com.xt.core.validation.decl.Long
    private long long1;

    @com.xt.core.validation.decl.Float
    private float float1;

    @com.xt.core.validation.decl.Double
    private double double1;

    @ColumnInfo(title="布尔")
    private boolean boolean1 = ((seq + System.currentTimeMillis()) % 3 == 0);

    @Email
    private String email;

    @HTTP
    private String url;

    @Phone
    private String phone;

    @Mobile
    private String mobile;

    @SSN
    private String ssn;

    @Chinese
    private String chinese;

    @PositiveInteger
    private int positiveInt1;

    public ValidationBean() {
    }

    public boolean isBoolean1() {
        return boolean1;
    }

    public boolean getBoolean1() {
        return boolean1;
    }

    public void setBoolean1(boolean boolean1) {
        this.boolean1 = boolean1;
    }

    public Date getDate1() {
        return date1;
    }

    public void setDate1(Date date1) {
        this.date1 = date1;
    }

    public String getDictionary1() {
        return dictionary1;
    }

    public void setDictionary1(String dictionary1) {
        this.dictionary1 = dictionary1;
    }

    public double getDouble1() {
        return double1;
    }

    public void setDouble1(double double1) {
        this.double1 = double1;
    }

    public float getFloat1() {
        return float1;
    }

    public void setFloat1(float float1) {
        this.float1 = float1;
    }

    public int getInt1() {
        return int1;
    }

    public void setInt1(int int1) {
        this.int1 = int1;
    }

    public long getLong1() {
        return long1;
    }

    public void setLong1(long long1) {
        this.long1 = long1;
    }

    public String getLookup1() {
        return lookup1;
    }

    public void setLookup1(String lookup1) {
        this.lookup1 = lookup1;
    }

    public String getLookup2() {
        return lookup2;
    }

    public void setLookup2(String lookup2) {
        this.lookup2 = lookup2;
    }

    public String getString1() {
        return string1;
    }

    public void setString1(String string1) {
        this.string1 = string1;
    }

    public String getString2() {
        return string2;
    }

    public void setString2(String string2) {
        this.string2 = string2;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getChinese() {
        return chinese;
    }

    public void setChinese(String chinese) {
        this.chinese = chinese;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getPositiveInt1() {
        return positiveInt1;
    }

    public void setPositiveInt1(int positiveInt1) {
        this.positiveInt1 = positiveInt1;
    }

    public static int getSeq() {
        return seq;
    }

    public static void setSeq(int seq) {
        ValidationBean.seq = seq;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }



    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ValidationBean other = (ValidationBean) obj;
        if ((this.string1 == null) ? (other.string1 != null) : !this.string1.equals(other.string1)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + (this.string1 != null ? this.string1.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }



}
