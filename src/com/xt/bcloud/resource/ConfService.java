package com.xt.bcloud.resource;

import com.xt.bcloud.app.App;
import com.xt.bcloud.app.AppVersion;
import com.xt.bcloud.comm.CloudUtils;
import com.xt.bcloud.resource.db.DbSourceServiceProvider;
import com.xt.bcloud.session.EhcacheSessionProvider;
import com.xt.bcloud.worker.Cattle;
import com.xt.core.db.pm.IPOPersistenceManager;
import com.xt.core.db.pm.PersistenceException;
import com.xt.core.exception.SystemException;
import com.xt.core.log.LogWriter;
import com.xt.core.proc.impl.IPOPersistenceAware;
import com.xt.core.proc.impl.fs.FileService;
import com.xt.core.proc.impl.fs.FileServiceAware;
import com.xt.core.service.IService;
import com.xt.core.service.LocalMethod;
import com.xt.core.utils.IOHelper;
import com.xt.core.utils.SqlUtils;
import com.xt.core.utils.VarTemplate;
import com.xt.core.utils.XmlHelper;
import com.xt.gt.sys.SystemConfiguration;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 * ���÷���,���ڸ�Ӧ���ṩһ��ͳһ�Ĳ�����ȡ�ӿ�.
 * @author albert
 */
public class ConfService implements IService, IPOPersistenceAware, FileServiceAware {

    /**
     * ��־ʵ����
     */
    private final Logger logger = Logger.getLogger(ServerService.class);

    /**
     * ��ȡ���������������صķ������ơ�
     */
    public static final String READ_TASK_MGR_GROUP_CONF = "readTaskMgrGroupConf";

    /**
     * ��������������飨Group�����ļ�����, ����ͨ��������taskDispatcherMgrGroup.conf���������ã�
     * Ĭ�ϵ��ļ������ǣ�WEB-INF/conf/task_dispatcher_group.xml��
     */
    private final static String taskDispatcherConfFileName =
            SystemConfiguration.getInstance().readString("taskDispatcherMgrGroup.conf",
            "WEB-INF/conf/task_dispatcher_group.xml");
    /**
     * ũ�������飨Group�����ļ�����, ����ͨ��������ranchMgrGroup.fileName���������ã�
     * Ĭ�ϵ��ļ������ǣ�WEB-INF/conf/ranch_mgr_group.xml��
     */
    private final static String ranchMgrGroupConfFileName =
            SystemConfiguration.getInstance().readString("ranchMgrGroup.conf",
            "WEB-INF/conf/ranch_mgr_group.xml");
    /**
     * Ehcache�����ڷֲ�ʽ�����ͨ����Group���������ļ�(ģ���ļ�), ����ͨ��������session.ehcache.conf���������ã�
     * Ĭ�ϵ��ļ������ǣ�WEB-INF/conf/ecache.xml��
     */
    private final static String ehcacheConfFileName =
            SystemConfiguration.getInstance().readString("session.ehcache.conf",
            "WEB-INF/conf/ehcache.xml");
    /**
     * ũ�������飨Group�����ļ��ı����ʽ, ����ͨ��������ranchMgrGroup.encoding����Ĭ�ϵ��ļ������ǣ�UTF-8��
     */
    private final String encoding = SystemConfiguration.getInstance().readString("JGroup.conf.encoding", "UTF-8");
    /**
     * �־û�������ʵ��
     */
    private transient IPOPersistenceManager persistenceManager;
    /**
     * �ļ�������ʵ��
     */
    private transient FileService fileService;
    /**
     * �����ṩ��
     */
    private final ServiceProvider[] serviceProviders = new ServiceProvider[]{
        new ArmProvider(), new DbSourceServiceProvider(), new EhcacheSessionProvider()
    };

    public ConfService() {
    }

    /**
     * ��ȡӦ�õ����в�����
     * ֻ����Ӧ��������ʱ����ܵ��ô˷�������ʱ����Ҫ���и��ٻ���!
     * @return
     */
    public InputStream readSystemParams(Cattle cattle) {
        final App        app     = cattle.getApp();
        final AppVersion version = cattle.getAppVersion();
        if (app == null || version == null) {
            throw new ResourceException("Ӧ��ʵ������汾��Ϣ������Ϊ�ա�");
        }
        LogWriter.info2(logger, "��ȡӦ��[%s]���汾[%s]���������ļ���", app, version);
        Element root = new Element("xt-config");
        if (serviceProviders != null && serviceProviders.length > 0) {
            for (int i = 0; i < serviceProviders.length; i++) {
                ServiceProvider provider = serviceProviders[i];
                provider.createConf(root, cattle, persistenceManager);
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmlHelper.output(baos, root, XmlHelper.UTF8);
        byte[] bytes = baos.toByteArray();
        // test
        if (logger.isDebugEnabled()) {
            try {
                FileOutputStream fos = new FileOutputStream("e:\\param-" + System.currentTimeMillis() + ".conf");
                IOHelper.i2o(new ByteArrayInputStream(bytes), fos, true, true);
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        }

        return new ByteArrayInputStream(bytes);
    }

    /**
     * ��ȡ��ũ�������顱�������ļ���
     * @param groupConf, Ӧ�ñ�����Ϊ�������롰GroupID������Ӧ�õ�����ʵ����ʹ��ͬһ���飩��
     * ʵ��ID��EntityId��ΪӦ��ʵ�����롣
     * @return
     */
    public InputStream readRanchGroupConf(GroupConf groupConf) {
        if (groupConf == null) {
            throw new ConfException("���������Ϊ�ա�");
        }

        if (!fileService.exists(ranchMgrGroupConfFileName)) {
            throw new SystemException(String.format("ũ��������������ļ�[%s]�����ڡ�",
                    ranchMgrGroupConfFileName));
        }
        InputStream is = fileService.read(ranchMgrGroupConfFileName);
        if (is == null) {
            throw new SystemException(String.format("��ȡũ��������������ļ�[%s]�������쳣��",
                    ranchMgrGroupConfFileName));
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        IOHelper.i2o(is, baos);
        saveGroupConf(groupConf);
        Map params = createParams(groupConf);
        return substitute(baos.toByteArray(), params, "ranch");
    }

    /**
     * ��ȡ����������顱�������ļ���
     * @param app
     * @param version
     * @return
     */
    public InputStream readTaskMgrGroupConf(GroupConf groupConf) {
        if (!fileService.exists(taskDispatcherConfFileName)) {
            throw new SystemException(String.format("���������������ļ�[%s]�����ڡ�", taskDispatcherConfFileName));
        }
        InputStream is = fileService.read(taskDispatcherConfFileName);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOHelper.i2o(is, baos);
        saveGroupConf(groupConf);
        
        // �滻����
        Map params = createParams(groupConf);
        return substitute(baos.toByteArray(), params, "taskMgr");
    }

    private Map createParams (GroupConf groupConf) {
        Map<String, Object> params = new HashMap();
         List<GroupConf> groups = getGroupConf(groupConf.getGroupId());
        String initialHosts = getInitialHosts(groups);
        params.put("initialHosts", initialHosts);     // ��ʼ������������ַ
        params.put("numInitialMembers", groups.size());  // ��ʼ������������
        params.put("bindAddr", groupConf.getBindAddr());
        params.put("bindPort", groupConf.getBindPort());
        return params;
    }

    /**
     * ��ȡ Ehcache �������ļ���
     *
     * @param app ��ǰ�����Ӧ��ʵ��
     * @param groupConf  Ehcache �����ʶ����CacheID�������ֵģ�ʵ���ʶ���ԡ�Ӧ��ʵ��OID�����б�ǡ�
     * @return �����ļ�������
     */
    public InputStream readEhcache(App app, AppVersion version, GroupConf groupConf) {
        if (!fileService.exists(ehcacheConfFileName)) {
            throw new SystemException(String.format("Ehcache�������ļ�[%s]�����ڡ�", ehcacheConfFileName));
        }
        LogWriter.info2(logger, "��ȡӦ��[%s]�汾[%s]�� Ehcache ��������Ϣ��ԭʼ����Ϣ[%s]����", app, version, groupConf);
        InputStream is = fileService.read(ehcacheConfFileName);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOHelper.i2o(is, baos);

        // ������Ϣ���ڴ˽��б���ô����
        saveGroupConf(groupConf);

        Map<String, Object> params = createParams(groupConf);
        params.put("cacheId", groupConf.getGroupId());
        return substitute(baos.toByteArray(), params, "echche");
    }

    private List<GroupConf> getGroupConf(String groupId) throws PersistenceException {
        final String sql = "SELECT * FROM GROUP_CONF WHERE GROUP_ID=? ORDER BY INSERT_TIME ASC";
        List<GroupConf> params = persistenceManager.query(GroupConf.class, sql, SqlUtils.getParams(groupId));
        return params;
    }

    private InputStream substitute(byte[] bytes, Map<String, Object> params, String prefix) throws SystemException {
        try {
            String content = new String(bytes, encoding);
            content = VarTemplate.format(content, params);
            ByteArrayInputStream output = new ByteArrayInputStream(content.getBytes(encoding));
            // if (logger.isDebugEnabled()) {
            try {
                FileOutputStream fos = new FileOutputStream(String.format("e:\\confService-%s-%d.conf.xml", prefix, System.currentTimeMillis()));
                IOHelper.i2o(new ByteArrayInputStream(content.getBytes(encoding)), fos, true, true);
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
            return output;
        } catch (UnsupportedEncodingException ex) {
            throw new ConfException(String.format("��ȡũ��������������ļ��ı���[%s]�����ڡ�", encoding), ex);
        }
    }

    private void saveGroupConf(GroupConf groupConf) {
        if (groupConf == null) {
            throw new ConfException("�����ò���Ϊ�ա�");
        }
        // ����д������
        List<GroupConf> oldConfs = persistenceManager.findAll(GroupConf.class, 
                "ENTITY_ID=? AND GROUP_ID=?", SqlUtils.getParams(groupConf.getEntityId(), groupConf.getGroupId()), null);
        for (Iterator<GroupConf> it = oldConfs.iterator(); it.hasNext();) {
            GroupConf oldGroupConf = it.next();
            persistenceManager.delete(oldGroupConf);
        }
        groupConf.setOid(CloudUtils.generateOid());
        groupConf.setInsertTime(Calendar.getInstance());
        persistenceManager.insert(groupConf);
    }

    /**
     * ����Ehcache��ʱ��Ҫ��������ַ����ʽ���£�localhost[7800],localhost[7801]
     * @return
     */
    private String getInitialHosts(List<GroupConf> params) {
        final StringBuilder initialHosts = new StringBuilder();
        for (Iterator<GroupConf> it = params.iterator(); it.hasNext();) {
            GroupConf groupConf = it.next();
            initialHosts.append(groupConf.getBindAddr()).append("[").append(groupConf.getBindPort()).append("]");
            if (it.hasNext()) {
                initialHosts.append(",");
            }
        }
        return initialHosts.toString();
    }

    @LocalMethod
    public void setPersistenceManager(IPOPersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    @LocalMethod
    public IPOPersistenceManager getPersistenceManager() {
        return this.persistenceManager;
    }

    @LocalMethod
    public void setFileService(FileService _fileService) {
        this.fileService = _fileService;
    }

    @LocalMethod
    public FileService getFileService() {
        return fileService;
    }
}
