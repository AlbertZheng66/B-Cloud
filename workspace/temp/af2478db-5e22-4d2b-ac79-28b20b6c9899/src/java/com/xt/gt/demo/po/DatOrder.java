
package com.xt.gt.demo.po;

import com.xt.core.db.pm.IPersistence;
import com.xt.gt.ui.table.ColumnInfo;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author albert
 */
public class DatOrder implements IPersistence{

    private String oid;

    @ColumnInfo(title="订单号")
    private String orderNumber;

    @ColumnInfo(title="用户")
    private String userOid;

    @ColumnInfo(title="地址")
    private String addressOid;


    @ColumnInfo(title="下单时间")
    private Calendar orderTime;
    
    @ColumnInfo(title="需要发票？")
    private boolean needInvoice;

    @ColumnInfo(title="配送时间")
    private Date deliverTime;

    @ColumnInfo(title="订单状态")
    private String state;

    public DatOrder() {
    }

    public Date getDeliverTime() {
        return deliverTime;
    }

    public void setDeliverTime(Date deliverTime) {
        this.deliverTime = deliverTime;
    }

    public boolean isNeedInvoice() {
        return needInvoice;
    }

    public void setNeedInvoice(boolean needInvoice) {
        this.needInvoice = needInvoice;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Calendar getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(Calendar orderTime) {
        this.orderTime = orderTime;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getAddressOid() {
        return addressOid;
    }

    public void setAddressOid(String addressOid) {
        this.addressOid = addressOid;
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
        final DatOrder other = (DatOrder) obj;
        if ((this.oid == null) ? (other.oid != null) : !this.oid.equals(other.oid)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + (this.oid != null ? this.oid.hashCode() : 0);
        return hash;
    }

}
