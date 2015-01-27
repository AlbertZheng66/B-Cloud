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
 * 配置服务,用于给应用提供一个统一的参数读取接口.
 * @author albert
 */
public class ConfService implements IService, IPOPersistenceAware, FileServiceAware {

    /**
     * 日志实例。
     */
    private final Logger logger = Logger.getLogger(ServerService.class);

    /**
     * 读取任务分配器配置相关的方法名称。
     */
    public static final String READ_TASK_MGR_GROUP_CONF = "readTaskMgrGroupConf";

    /**
     * 任务分配器管理组（Group）的文件名称, 可以通过参数“taskDispatcherMgrGroup.conf”进行配置，
     * 默认的文件名称是：WEB-INF/conf/task_dispatcher_group.xml。
     */
    private final static String taskDispatcherConfFileName =
            SystemConfiguration.getInstance().readString("taskDispatcherMgrGroup.conf",
            "WEB-INF/conf/task_dispatcher_group.xml");
    /**
     * 农场管理组（Group）的文件名称, 可以通过参数“ranchMgrGroup.fileName”进行配置，
     * 默认的文件名称是：WEB-INF/conf/ranch_mgr_group.xml。
     */
    private final static String ranchMgrGroupConfFileName =
            SystemConfiguration.getInstance().readString("ranchMgrGroup.conf",
            "WEB-INF/conf/ranch_mgr_group.xml");
    /**
     * Ehcache（用于分布式缓存的通信组Group）的配置文件(模板文件), 可以通过参数“session.ehcache.conf”进行配置，
     * 默认的文件名称是：WEB-INF/conf/ecache.xml。
     */
    private final static String ehcacheConfFileName =
            SystemConfiguration.getInstance().readString("session.ehcache.conf",
            "WEB-INF/conf/ehcache.xml");
    /**
     * 农场管理组（Group）的文件的编码格式, 可以通过参数“ranchMgrGroup.encoding”，默认的文件名称是：UTF-8。
     */
    private final String encoding = SystemConfiguration.getInstance().readString("JGroup.conf.encoding", "UTF-8");
    /**
     * 持久化管理器实例
     */
    private transient IPOPersistenceManager persistenceManager;
    /**
     * 文件服务器实例
     */
    private transient FileService fileService;
    /**
     * 服务提供者
     */
    private final ServiceProvider[] serviceProviders = new ServiceProvider[]{
        new ArmProvider(), new DbSourceServiceProvider(), new EhcacheSessionProvider()
    };

    public ConfService() {
    }

    /**
     * 读取应用的所有参数。
     * 只有在应用启动的时候可能调用此方法，暂时不需要进行高速缓存!
     * @return
     */
    public InputStream readSystemParams(Cattle cattle) {
        final App        app     = cattle.getApp();
        final AppVersion version = cattle.getAppVersion();
        if (app == null || version == null) {
            throw new ResourceException("应用实例及其版本信息都不能为空。");
        }
        LogWriter.info2(logger, "读取应用[%s]（版本[%s]）的配置文件。", app, version);
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
     * 读取“农场管理组”的配置文件。
     * @param groupConf, 应用编码作为其分组编码“GroupID”（即应用的所有实例都使用同一个组）；
     * 实体ID（EntityId）为应用实例编码。
     * @return
     */
    public InputStream readRanchGroupConf(GroupConf groupConf) {
        if (groupConf == null) {
            throw new ConfException("组参数不能为空。");
        }

        if (!fileService.exists(ranchMgrGroupConfFileName)) {
            throw new SystemException(String.format("农场管理组的配置文件[%s]不存在。",
                    ranchMgrGroupConfFileName));
        }
        InputStream is = fileService.read(ranchMgrGroupConfFileName);
        if (is == null) {
            throw new SystemException(String.format("读取农场管理组的配置文件[%s]输入流异常。",
                    ranchMgrGroupConfFileName));
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        IOHelper.i2o(is, baos);
        saveGroupConf(groupConf);
        Map params = createParams(groupConf);
        return substitute(baos.toByteArray(), params, "ranch");
    }

    /**
     * 读取“任务管理组”的配置文件。
     * @param app
     * @param version
     * @return
     */
    public InputStream readTaskMgrGroupConf(GroupConf groupConf) {
        if (!fileService.exists(taskDispatcherConfFileName)) {
            throw new SystemException(String.format("任务管理组的配置文件[%s]不存在。", taskDispatcherConfFileName));
        }
        InputStream is = fileService.read(taskDispatcherConfFileName);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOHelper.i2o(is, baos);
        saveGroupConf(groupConf);
        
        // 替换变量
        Map params = createParams(groupConf);
        return substitute(baos.toByteArray(), params, "taskMgr");
    }

    private Map createParams (GroupConf groupConf) {
        Map<String, Object> params = new HashMap();
         List<GroupConf> groups = getGroupConf(groupConf.getGroupId());
        String initialHosts = getInitialHosts(groups);
        params.put("initialHosts", initialHosts);     // 初始启动的主机地址
        params.put("numInitialMembers", groups.size());  // 初始启动的主机数
        params.put("bindAddr", groupConf.getBindAddr());
        params.put("bindPort", groupConf.getBindPort());
        return params;
    }

    /**
     * 读取 Ehcache 的配置文件。
     *
     * @param app 当前分配的应用实例
     * @param groupConf  Ehcache 的组标识是以CacheID进行区分的，实体标识是以“应用实体OID”进行标记。
     * @return 配置文件的流。
     */
    public InputStream readEhcache(App app, AppVersion version, GroupConf groupConf) {
        if (!fileService.exists(ehcacheConfFileName)) {
            throw new SystemException(String.format("Ehcache的配置文件[%s]不存在。", ehcacheConfFileName));
        }
        LogWriter.info2(logger, "读取应用[%s]版本[%s]的 Ehcache 的配置信息（原始绑定信息[%s]）。", app, version, groupConf);
        InputStream is = fileService.read(ehcacheConfFileName);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOHelper.i2o(is, baos);

        // 保存信息（在此进行保存么？）
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
            throw new ConfException(String.format("读取农场管理组的配置文件的编码[%s]不存在。", encoding), ex);
        }
    }

    private void saveGroupConf(GroupConf groupConf) {
        if (groupConf == null) {
            throw new ConfException("组配置不能为空。");
        }
        // 清理残存的数据
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
     * 返回Ehcache绑定时需要的主机地址，形式如下：localhost[7800],localhost[7801]
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
