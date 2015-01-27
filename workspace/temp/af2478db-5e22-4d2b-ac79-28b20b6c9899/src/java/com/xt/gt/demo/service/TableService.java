
package com.xt.gt.demo.service;

import com.xt.core.service.AbstractService;
import com.xt.gt.demo.po.ValidationBean;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * 表测试服务。
 * @author albert
 */
public class TableService extends AbstractService {

    public TableService() {
    }

    public List list() {
        fspParameter.getPagination().setMaxRowCount(1000);
        int startIndex = fspParameter != null ? fspParameter.getPagination().getStartIndex() : 0;
        ValidationBean.seq = startIndex;
        List data = new ArrayList();
        data.add(new ValidationBean());
        data.add(new ValidationBean());
        data.add(new ValidationBean());
        data.add(new ValidationBean());
        data.add(new ValidationBean());
        data.add(new ValidationBean());
        data.add(new ValidationBean());
        data.add(new ValidationBean());
        data.add(new ValidationBean());
        data.add(new ValidationBean());
        fspParameter.getPagination().setTotalCount(data.size() * 5);
        return data;
    }

    public boolean remove(ValidationBean bean) {
        if (bean == null /*|| bean 在数据库中不存在*/) {
            return false;
        }
        System.out.println("deleting bean=" + bean.toString());
        return true;
    }

    public boolean update(ValidationBean bean) {
        if (bean == null /*|| bean 在数据库中不存在*/) {
            return false;
        }
        System.out.println("updating bean=" + bean.toString());
        return true;
    }

    public boolean add(ValidationBean bean) {
        if (bean == null || (ValidationBean.seq++ % 2 == 0) /*|| bean 在数据库中已经存在*/) {
            return false;
        }
        System.out.println("adding bean=" + bean.toString());
        return true;
    }

   

    transient private final  Logger logger = Logger.getLogger(TableService.class);

}

