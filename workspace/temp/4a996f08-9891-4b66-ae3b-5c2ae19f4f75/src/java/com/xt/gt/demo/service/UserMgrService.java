
package com.xt.gt.demo.service;

import com.xt.core.service.AbstractService;
import com.xt.core.utils.SqlUtils;
import com.xt.gt.chr.event.FspParameterUtils;
import com.xt.gt.demo.po.Address;
import com.xt.gt.demo.po.Book;
import com.xt.gt.demo.po.DatOrder;
import com.xt.gt.demo.po.UserInfo;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author albert
 */
public class UserMgrService extends AbstractService {

    public UserMgrService() {
    }



    /**
     * 返回所有用户的列表
     * @return
     */
    public List list() {
        List params = new ArrayList(2);
        String where = FspParameterUtils.createFilter(fspParameter, null, params);
        return persistenceManager.findAll(UserInfo.class, where, params, null);
    }

    /**
     * 新增一个用户
     * @param book
     */
    public void add(UserInfo user) {
        persistenceManager.insert(user);
    }

    /**
     * 更新一个用户
     * @param book
     */
    public void update(UserInfo user) {
        persistenceManager.update(user);
    }

    /**
     * 列出指定用户所有的喜欢的书
     * @param userOid
     * @return
     */
    public List<Book> listFavorites (String userOid) {
        String sql = "select b.* from book b, favorite f where b.oid=f.book_oid and f.user_oid=? ";
        List params = SqlUtils.getParams(userOid);
        return FspParameterUtils.query(persistenceManager, fspParameter, sql, "b", params, Book.class);
    }

    /**
     * 列出指定用户所有的订单
     * @param userOid
     * @return
     */
    public List<DatOrder> listOrders (String userOid) {
        String sql = "SELECT * FROM DAT_ORDER O WHERE O.USER_OID=?";
        return FspParameterUtils.query(persistenceManager, fspParameter, sql, "o",
                SqlUtils.getParams(userOid), DatOrder.class);
    }


    /**
     * 列出指定用户所有的订单
     * @param userOid
     * @return
     */
    public List<Address> listAddresses (String userOid) {
        String sql = "user_oid=?";
        return persistenceManager.findAll(Address.class, sql, SqlUtils.getParams(userOid), null);
    }

    transient private final  Logger logger = Logger.getLogger(UserMgrService.class);

}
