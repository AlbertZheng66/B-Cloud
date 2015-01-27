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
 * ���ݿ���صĲ���.
 * @author albert
 */
public class DBService implements IService, IPOPersistenceAware {

    private final Logger logger = Logger.getLogger(DBService.class);
    
    /**
     * �־û��ļ�ʵ��
     */
    private transient IPOPersistenceManager persistenceManager;

    public DBService() {
        
    }

    /**
     * ���ص�ǰ���еġ����ݿ��顱��Ϣ
     * @return
     */
    public List<DbGroup> listGroups() {
        return persistenceManager.findAll(DbGroup.class, null, null, null);
    }

    /**
     * ��ʾ��ǰ���������������Դ��
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
            throw new ResourceException("���ݿ��鼰����벻��Ϊ�ա�");
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
