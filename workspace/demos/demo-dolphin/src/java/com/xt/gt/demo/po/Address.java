/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.xt.gt.demo.po;

import com.xt.core.db.pm.IPersistence;
import com.xt.gt.ui.table.ColumnInfo;

/**
 *
 * @author albert
 */
public class Address implements IPersistence {

    private String oid;

    private String userOid;
    
    @ColumnInfo(title="所在城市")
    private String cityCode;

    @ColumnInfo(title="地址类型")
    private AddressType type; // OFFICE, HOME, NONE

    @ColumnInfo(title="地址明细")
    private String detail;

    public Address() {
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public AddressType getType() {
        return type;
    }

    public void setType(AddressType type) {
        this.type = type;
    }

    public String getUserOid() {
        return userOid;
    }

    public void setUserOid(String userOid) {
        this.userOid = userOid;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Address other = (Address) obj;
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

    

}
