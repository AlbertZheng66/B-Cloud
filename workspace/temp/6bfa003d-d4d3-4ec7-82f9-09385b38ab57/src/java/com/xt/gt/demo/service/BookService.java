
package com.xt.gt.demo.service;

import com.xt.core.service.AbstractService;
import com.xt.core.utils.SqlUtils;
import com.xt.gt.chr.event.FspParameterUtils;
import com.xt.gt.demo.po.Book;
import java.io.InputStream;
import java.util.List;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;

/**
 *
 * @author albert
 */
public class BookService extends AbstractService {


    public BookService() {
    }

    /**
     * 返回所有图书的列表
     * @return
     */
    public List list() {
        List params = SqlUtils.getParams();
        // String where = FspParameterUtils.attach("1=1", fspParameter, params);
        // return persistenceManager.findAll(Book.class, where, params, null);
        System.out.println("fspParameter=" + ToStringBuilder.reflectionToString(fspParameter));
        List books = FspParameterUtils.query(persistenceManager, fspParameter, "SELECT * FROM BOOK B WHERE 1=1 ", "B", params, Book.class);
        return books;
    }

    /**
     * 新增一本图书
     * @param book
     */
    public boolean add(Book book) {
        book.setOid(String.valueOf(System.currentTimeMillis()));
        return persistenceManager.insert(book);
    }

    /**
     * 更新一本图书
     * @param book
     */
    public boolean update(Book book) {
        if (persistenceManager.findByPK(book) == null) {
            return false;
        }
        return persistenceManager.update(book);
    }

    /**
     * 删除一本书
     * @param book
     * @return
     */
    public boolean delete(Book book) {
        if (persistenceManager.findByPK(book) == null) {
            return false;
        }
        return persistenceManager.delete(book);
    }

    public InputStream getCover (Book book) {
        book = (Book)persistenceManager.findByPK(book);
        if (book != null && book.getCover() != null) {
            return book.getCover();
        }
        return null;
    }

    transient private final  Logger logger = Logger.getLogger(BookService.class);

}
