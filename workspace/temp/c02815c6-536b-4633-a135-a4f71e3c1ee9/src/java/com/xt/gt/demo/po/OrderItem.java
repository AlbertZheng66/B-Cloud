
package com.xt.gt.demo.po;

import com.xt.core.db.pm.IPersistence;
import com.xt.gt.ui.table.ColumnInfo;

/**
 *
 * @author albert
 */
public class OrderItem implements IPersistence {

    private String oid;
    /**
     *
     */
    private String bookOid;

    private String orderOid;

    @ColumnInfo(title="折后费用")
    private float fee;


    @ColumnInfo(title="优惠费用")
    private int mount;

    @ColumnInfo(title="优惠费用")
    private float discFee;

    public OrderItem() {
    }

    public String getBookOid() {
        return bookOid;
    }

    public void setBookOid(String bookOid) {
        this.bookOid = bookOid;
    }

    public float getDiscFee() {
        return discFee;
    }

    public void setDiscFee(float discFee) {
        this.discFee = discFee;
    }

    public float getFee() {
        return fee;
    }

    public void setFee(float fee) {
        this.fee = fee;
    }

    public int getMount() {
        return mount;
    }

    public void setMount(int mount) {
        this.mount = mount;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getOrderOid() {
        return orderOid;
    }

    public void setOrderOid(String orderOid) {
        this.orderOid = orderOid;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OrderItem other = (OrderItem) obj;
        if ((this.oid == null) ? (other.oid != null) : !this.oid.equals(other.oid)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + (this.oid != null ? this.oid.hashCode() : 0);
        return hash;
    }

    

}
