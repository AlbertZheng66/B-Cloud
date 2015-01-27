package com.xt.bcloud.resource;

import com.xt.bcloud.resource.db.DbGroup;
import com.xt.bcloud.resource.db.DbSource;
import com.xt.core.db.pm.IPOPersistenceManager;
import com.xt.core.proc.impl.IPOPersistenceAware;
import com.xt.core.service.IService;
import com.xt.core.service.LocalMethod;
import com.xt.core.utils.SqlUtils;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * 数据库相关的操作.
 * @author albert
 */
public class DBService implements IService, IPOPersistenceAware {

    private final Logger logger = Logger.getLogger(DBService.class);
    
    /**
     * 持久化文件实例
     */
    private transient IPOPersistenceManager persistenceManager;

    public DBService() {
        
    }

    /**
     * 返回当前所有的“数据库组”信息
     * @return
     */
    public List<DbGroup> listGroups() {
        return persistenceManager.findAll(DbGroup.class, null, null, null);
    }

    /**
     * 显示当前主下面的所有数据源。
     * @param dbGroup
     * @return
     */
    public List<DbSource> listDbSources(DbGroup dbGroup) {
        assertDbGroup(dbGroup);
        return persistenceManager.findAll(DbSource.class, "GROUP_OID=?",
                SqlUtils.getParams(dbGroup.getOid()), null);
    }

    private void assertDbGroup(DbGroup dbGroup) {
        if (dbGroup == null || StringUtils.isEmpty(dbGroup.getOid())) {
            throw new ResourceException("数据库组及其编码不能为空。");
        }
    }

    @LocalMethod
    public void setPersistenceManager(IPOPersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    @LocalMethod
    public IPOPersistenceManager getPersistenceManager() {
        return persistenceManager;
    }
}
