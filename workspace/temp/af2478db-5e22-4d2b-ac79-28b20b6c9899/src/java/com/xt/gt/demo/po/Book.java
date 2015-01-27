
package com.xt.gt.demo.po;


import com.xt.core.db.pm.IPersistence;
import com.xt.gt.ui.table.ColumnInfo;
import java.io.InputStream;
import java.sql.Date;

/**
 *
 * @author albert
 */
public class Book implements IPersistence {

    private String oid;

    @ColumnInfo(title="书名")
    private String name;

    @ColumnInfo(title="ISDN")
    private String isdn;

    @ColumnInfo(title="价格")
    private float price;

    @ColumnInfo(title="作者")
    private String author;

    @ColumnInfo(title="译者")
    private String translator;

    @ColumnInfo(title="出版社")
    private String press;

    @ColumnInfo(title="出版时间")
    private Date publishDate;

    @ColumnInfo(title="封面")
    private InputStream cover;

    public Book() {
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsdn() {
        return isdn;
    }

    public void setIsdn(String isdn) {
        this.isdn = isdn;
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

    public String getPress() {
        return press;
    }

    public void setPress(String press) {
        this.press = press;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getTranslator() {
        return translator;
    }

    public void setTranslator(String translator) {
        this.translator = translator;
    }

    public InputStream getCover() {
        return cover;
    }

    public void setCover(InputStream cover) {
//        FileOutputStream fos;
//        try {
//            fos = new FileOutputStream("e:\\" + System.currentTimeMillis() + ".jpg");
//            IOHelper.i2o(cover, fos, true, true);
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(Book.class.getName()).log(Level.SEVERE, null, ex);
//        }
        this.cover = cover;
    }

//    public void setCover(byte[] cover) {
//        FileOutputStream fos;
//        try {
//            fos = new FileOutputStream("e:\\" + System.currentTimeMillis() + ".jpg");
//            IOHelper.i2o(new ByteArrayInputStream(cover), fos, true, true);
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(Book.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        this.cover = cover;
//    }
    public Date getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Book other = (Book) obj;
        if ((this.oid == null) ? (other.oid != null) : !this.oid.equals(other.oid)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + (this.oid != null ? this.oid.hashCode() : 0);
        return hash;
    }



}
