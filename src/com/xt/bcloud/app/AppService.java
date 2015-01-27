package com.xt.bcloud.app;

import com.xt.bcloud.comm.CloudUtils;
import com.xt.bcloud.comm.Constants;
import com.xt.bcloud.resource.*;
import com.xt.bcloud.resource.server.ServerInfo;
import com.xt.bcloud.session.AppCacheService;
import com.xt.bcloud.sys.Tenant;
import com.xt.bcloud.worker.Cattle;
import com.xt.core.db.pm.IPOPersistenceManager;
import com.xt.core.db.pm.PersistenceException;
import com.xt.core.exception.ServiceException;
import com.xt.core.exception.SystemException;
import com.xt.core.log.LogWriter;
import com.xt.core.proc.impl.IPOPersistenceAware;
import com.xt.core.proc.impl.fs.FileService;
import com.xt.core.proc.impl.fs.FileServiceAware;
import com.xt.core.service.IService;
import com.xt.core.service.LocalMethod;
import com.xt.core.utils.EnumUtils;
import com.xt.core.utils.IOHelper;
import com.xt.core.utils.SqlUtils;
import com.xt.proxy.Proxy;
import com.xt.proxy.ServiceFactory;
import com.xt.proxy.impl.http.stream.HttpStreamProxy;
import com.xt.gt.sys.SystemConfiguration;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * 集中应用相关的服务接口。
 *
 * @author albert
 */
public class AppService implements IService, IPOPersistenceAware, FileServiceAware {

    /**
     * 持久化文件实例
     */
    private transient IPOPersistenceManager persistenceManager;
    /**
     * 文件服务器实例
     */
    private transient FileService fileService;
    private final Logger logger = Logger.getLogger(AppService.class);
    /**
     * 应用管理的端口号
     */
    public static final int APP_MGR_PORT = 58080;
    /**
     * 应用管理的访问地址的定位名称
     */
    public static final String APP_MGR_URL = "appMgr.url";
    /**
     * 应用管理的上下文路径
     */
    public static final String APP_MGR_CONTEXT_PATH = "/arMgr";
    /**
     * 用于存放应用版本的发布文件的基础路径，可以通过参数“app.version.deployed.path”进行调整，默认为：{文件服务基础路径}/WEB-INF/deployPackages
     */
    private static final String deployedBasePath = SystemConfiguration.getInstance().readString("app.version.deployed.path", "WEB-INF/deployPackages");

    public AppService() {
    }

    /**
     * 注册一个承租人。
     *
     * @param tenant
     */
    public void registryTenant(Tenant tenant) {
    }

    /**
     * 注册一个应用资源。
     */
    public void registerApp(App app) {
        if (app == null) {
            throw new AppException(String.format("应用不能为空。"));
        }
        if (StringUtils.isEmpty(app.getId())
                || StringUtils.isEmpty(app.getName())) {
            throw new AppException(String.format("应用[%s]的编码和名称均不能为空。", app));
        }
        if (duplicateAppId(app.getId())) {
            throw new AppException(String.format("编码[%s]已经存在。", app.getId()));
        }
        app.setOid(CloudUtils.generateOid());
        this.persistenceManager.insert(app);
    }

    /**
     * 更新一个应用资源的基本信息。
     */
    public void updateApp(App app) {
        if (app == null) {
            throw new AppException(String.format("应用不能为空。"));
        }
        if (StringUtils.isEmpty(app.getOid())) {
            throw new AppException(String.format("应用的OID不能为空。", app));
        }
        this.persistenceManager.update(app);
    }

    /**
     * 查找一个指定的App应用。
     */
    public App findApp(String oid) {
        if (StringUtils.isEmpty(oid)) {
            throw new AppException(String.format("应用的OID不能为空。"));
        }
        return (App) persistenceManager.findByPK(App.class, oid);
    }

    public boolean duplicateAppId(String id) {
        int count = this.persistenceManager.queryInt("select count(*) from app where id=?",
                SqlUtils.getParams(id == null ? "" : id));
        return (count > 0);
    }

    /**
     * 注册指定应用的新版本。如果当前应用无默认版本，则此版本为默认版本。
     */
    public void registerAppVersion(App app, AppVersion version, InputStream uploadingDeployedFile) {
        App loadedApp = assertAndLoadApp(app);
        if (version == null) {
            throw new AppException("注册的版本不能为空。");
        }
        if (uploadingDeployedFile == null) {
            throw new AppException(String.format("应用[%s](版本[%s])的上传文件不能为空。",
                    app.getId(), version.getVersion()));
        }

        version.setOid(CloudUtils.generateOid());
        String fileName = generateFileName(app, version);
        LogWriter.info2(logger, "自动生成的上传文件名为[%s]。", fileName);
        OutputStream os = fileService.writeTo(fileName, false);
        long fileSize = IOHelper.i2o(uploadingDeployedFile, os, true, false);
        version.setDeployFileName(fileName);
        version.setFileSize((int) fileSize);
        version.setAppOid(app.getOid());
        version.setInsertTime(Calendar.getInstance());
        version.setState(AppVersionState.REGISTERED);
        version.setValid(true);
        persistenceManager.insert(version);

        // 如果当前应用的为空，或者定义的版本已经不存在，则使用当前版本作为默认版本。
        if (isVersionExisted(loadedApp.getVersionOid())) {
            loadedApp.setVersionOid(version.getOid());
            persistenceManager.update(loadedApp);
        }
    }

    /**
     * 判断指定的版本是否存在。
     *
     * @param versionOid 版本好
     * @return 存在：true；否则：false。
     */
    private boolean isVersionExisted(String versionOid) {
        if (StringUtils.isEmpty(versionOid)) {
            return false;
        }
        Object version = persistenceManager.findByPK(AppVersion.class, versionOid);
        return (version != null);
    }
    
     /**
     * 判断指定的版本是否存在。
     *
     * @param versionOid 版本好
     * @return 存在：true；否则：false。
     */
    public AppVersion getAppVersion(String versionOid) {
        if (StringUtils.isEmpty(versionOid)) {
            throw new AppException("版本号不能为空。");
        }
        AppVersion version = (AppVersion)persistenceManager.findByPK(AppVersion.class, versionOid);
        return version;
    }

    /**
     * 根据应用及其名称自动生成发布包的名称
     *
     * @param app 应用实例
     * @param appVersion 版本名称
     * @return
     */
    protected String generateFileName(App app, AppVersion appVersion) {
        String basePath = deployedBasePath == null ? "" : deployedBasePath;
        StringBuilder strBld = new StringBuilder(basePath);
        final String fileSep = "/"; // File.separator;
        if (StringUtils.isNotEmpty(basePath) && !basePath.endsWith(fileSep)) {
            strBld.append(fileSep);
        }
        strBld.append(app.getOid());  // 一级目录
        strBld.append(fileSep);
        strBld.append(appVersion.getOid());
        strBld.append(fileSep);

        // 目录结构
        if (!fileService.exists(strBld.toString())) {
            fileService.mkdirs(strBld.toString());
        }
        strBld.append(appVersion.getVersion());  // 以版本号作为前缀
        strBld.append("_").append(System.currentTimeMillis());  // 以当前时间作为标识。
        strBld.append(".war");
        String fileName = strBld.toString();
        if (fileService.exists(fileName)) {
            LogWriter.info2(logger, "文件[%s]已经存在，系统将其自动删除。", fileName);
            fileService.delete(fileName);
        }
        return fileName;
    }

    /**
     * 删除一个应用资源
     *
     * @param instance
     */
    public void removeApp(App app) {
        assertAndLoadApp(app);

        List<AppVersion> versions = persistenceManager.findAll(AppVersion.class, "APP_OID=?",
                SqlUtils.getParams(app.getOid()), null);

        for (Iterator<AppVersion> it = versions.iterator(); it.hasNext();) {
            AppVersion appVersion = it.next();
            removeAppVersion(app, appVersion);
        }

        // 删除应用信息
        this.persistenceManager.delete(app);
    }

    /**
     * 移除应用的指定版本。 TODO: 是否只是设置暂停状态，然后有后台线程删除此应用。
     *
     * @param app
     * @return 应用的默认版本号。
     */
    public String removeAppVersion(App app, AppVersion version) {
        App loadedApp = assertAndLoadApp(app);

        version = assertAndLoadVersion(version);

        List params = SqlUtils.getParams(version.getOid());

        List<AppInstance> appInstances = persistenceManager.findAll(AppInstance.class,
                "APP_VERSION_OID=?", params, null);

        // 删除应用实例表中对应的信息
        persistenceManager.execute("DELETE FROM APP_INSTANCE WHERE APP_VERSION_OID=?", params);

        // 停用所有实例
        for (AppInstance instance : appInstances) {
            try {
                ServerService serserService = getServerService(instance.getServerOid(), true);
                if (serserService != null) {
                    serserService.undeploy(instance.getCattleOid(), true);   // 暂时都采用强制退出方式
                }
            } catch (Throwable t) {
                LogWriter.warn2(logger, t, "停用实例[%s]出现异常。", instance);
            }
        }

        // 删除上传的发布文件
        String deployedFileName = version.getDeployFileName();
        if (StringUtils.isNotEmpty(deployedFileName)
                && fileService.exists(deployedFileName)) {
            try {
                fileService.delete(deployedFileName);
            } catch (Exception ignored) {
                // 出现错误的时候有后台程序处理
                LogWriter.warn2(logger, ignored, "删除发布文件[%s]出错。", deployedFileName);
            }
        }

        // 删除版本信息
        persistenceManager.delete(version);

        // 删除发布信息
        persistenceManager.execute("DELETE FROM PUBLISH_INFO WHERE APP_VERSION_OID=?", params);

        // 删除缓存的配置信息
        persistenceManager.execute("DELETE FROM APP_CACHE WHERE APP_VERSION_OID=?", params);

        // 删除的版本为默认版本时，自动选择最新的版本作为默认版本。
        String defaultVersion = loadedApp.getVersionOid();
        if (version.getOid().equals(defaultVersion)) {
            defaultVersion = getDefaultVersion(loadedApp.getOid());
            loadedApp.setVersionOid(defaultVersion);
            persistenceManager.update(loadedApp);
        }
        return defaultVersion;
    }

    /**
     * 自动选择应用的默认版本，即可用的最新版本。
     *
     * @param appOid 应用编码
     * @return
     */
    private String getDefaultVersion(String appOid) {
        String sql = "SELECT OID FROM APP_VERSION WHERE APP_OID=?";
        return persistenceManager.queryString(sql, SqlUtils.getParams(appOid));
    }

    /**
     * 停止指定的应用实例
     *
     * @param instance
     */
    public void stopInstance(AppInstance instance) {
        instance = assertAndLoadInstance(instance);
        ServerService serserService = getServerService(instance.getServerOid(), true);
        if (serserService != null) {
            serserService.undeploy(instance.getCattleOid(), true);   // 暂时都采用强制退出方式
        }
        instance.setState(AppInstanceState.STOPED);
        persistenceManager.update(instance);
        deleteGroupConf(instance);
    }

    /**
     * 删除和组配置相关的信息。
     *
     * @param instance
     */
    private void deleteGroupConf(AppInstance instance) {
        // 删除相关的组配置信息
        persistenceManager.execute("DELETE FROM GROUP_CONF WHERE ENTITY_ID=?", SqlUtils.getParams(instance.getOid()));
    }

    /**
     * 重新启动指定的应用实例。
     *
     * @param appInstance
     */
    public void restartInstance(AppInstance instance) {
        instance = assertAndLoadInstance(instance);
        ServerService serserService = getServerService(instance.getServerOid(), false);
        serserService.restart(instance.getCattleOid());
    }

    /**
     * 重新分配是指，停掉当前的应用，然后重新分配一个实例。 这个处理的好处是：针对某些内存泄漏比较严重的应用，可以通过这种
     * 方式达到稍微稳定的状态（相当于重新启动服务器）。
     *
     * @param instance 应用的实例
     */
    public void redeployInstance(AppInstance instance) {
        throw new UnsupportedOperationException("暂时未实现");
//        instance = assertAndLoadInstance(instance);
//        ServerService serserService = getServerService(instance.getServerOid());
//        serserService.restart(instance.getCattleOid());
    }

    private App assertAndLoadApp(App app) throws ServiceException {
        if (app == null || StringUtils.isEmpty(app.getOid())) {
            throw new AppException("应用及其编码信息都不能为空。");
        }
        App loaded = (App) persistenceManager.findByPK(app);
        if (loaded == null) {
            throw new AppException(String.format("应用[%s](OID:%s)不存在。", app.getId(), app.getOid()));
        }
        return loaded;
    }

    /**
     * 检查应用的版本实例是否存在。
     *
     * @param oldVersion
     * @return 从数据库中加载的新实例。
     * @throws ServiceException
     */
    private AppVersion assertAndLoadVersion(AppVersion version) throws ServiceException {
        if (version == null || StringUtils.isEmpty(version.getOid())) {
            throw new AppException("应用版本及其编码信息都不能为空。");
        }
        AppVersion loaded = (AppVersion) persistenceManager.findByPK(version);
        if (loaded == null) {
            throw new AppException(String.format("应用版本[%s](OID=%s)在数据库中不存在。",
                    version.getVersion(), version.getOid()));
        }
        return loaded;
    }

    private AppInstance assertAndLoadInstance(AppInstance instance) throws ServiceException {
        if (instance == null || StringUtils.isEmpty(instance.getOid())) {
            throw new ServiceException("应用实例及其编码信息都不能为空。");
        }
        AppInstance loaded = (AppInstance) persistenceManager.findByPK(instance);
        if (loaded == null) {
            throw new AppException(String.format("应用实例[%s]不存在。", instance.getOid()));
        }
        return loaded;
    }

    private AppVersion assertVersion(String versionOid, String message) throws PersistenceException, AppException {
        if (StringUtils.isEmpty(versionOid)) {
            throw new AppException(message + "不能为空。");
        }
        AppVersion version = (AppVersion) persistenceManager.findByPK(AppVersion.class, versionOid);
        if (version == null) {
            throw new AppException(String.format("[%s]不存在。", message, versionOid));
        }
        AppVersion _version = new AppVersion();
        _version.setOid(versionOid);
        return _version;
    }

    private ResourceService createResourceService() {
        // 向资源工厂申请资源
        ResourceService resourceSerivce = new ResourceService(); // CloudUtils.createResourceService();
        resourceSerivce.setPersistenceManager(persistenceManager);
        return resourceSerivce;
    }

    private ServerService getServerService(String serverOid, boolean nullable) {
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setOid(serverOid);

        serverInfo = (ServerInfo) persistenceManager.findByPK(serverInfo);
        if (serverInfo == null) {
            // 不对空进行处理
            if (nullable) {
                LogWriter.warn2(logger, "服务器[%s]信息已经丢失(可能是服务器已经注销)。", serverOid);
                return null;
            } else {
                throw new SystemException(String.format("服务器[%s]信息已经丢失(可能是服务器已经注销)。", serverOid));
            }
        }

        String proxyUrl = String.format("http://%s:%d/%s/", serverInfo.getIp(),
                serverInfo.getManagerPort(), serverInfo.getContextPath());
        Proxy proxy = new HttpStreamProxy(proxyUrl);
        // 如果连接失败，继续向下处理，有清理程序来处理不响应的服务或者实例
        ServerService serverSerivce = ServiceFactory.getInstance().getService(ServerService.class, proxy);
        return serverSerivce;
    }

    /**
     * 显示所有应用的列表
     *
     * @return
     */
    public List<App> list() {
        return this.persistenceManager.findAll(App.class, null, null, null);
    }

    /**
     * 列出指定应用的所有版本
     *
     * @param instance 应用实例，如果为空则抛出异常。
     * @return 应用版本列表
     */
    public List<AppVersion> listVersions(App app) {
        assertAndLoadApp(app);
        return this.persistenceManager.findAll(AppVersion.class, "APP_OID=?", SqlUtils.getParams(app.getOid()), "INSERT_TIME DESC");
    }

    /**
     * 列出指定应用的所有版本
     *
     * @param instance 应用实例，如果为空则抛出异常。
     * @return 应用版本列表
     */
    public List<AppInstance> listInstances(App app, AppVersion version) {
        assertAndLoadApp(app);
        assertAndLoadVersion(version);
        return persistenceManager.findAll(AppInstance.class, "APP_OID=? AND APP_VERSION_OID=?",
                SqlUtils.getParams(app.getOid(), version.getOid()), "STARTUP_TIME DESC");
    }

    /**
     * 切换当前用用程序的版本
     *
     * @param instance
     */
    public void setVersion(App app, String version) {
        // TODO: 向任务管理器发出失效通知
        app.setVersionOid(version);
        this.persistenceManager.update(app);
    }

    /**
     * 将一个应用资源设置为测试状态, 只有管理员帐号才能访问此应用。 判断一个帐号是否为管理员帐号需要在Cookie里设置特定的信息。
     *
     * @param instance
     */
    public void setStateTest(App app, AppVersion appVersion) {
    }

    /**
     * 发布当前应用的最新版本；如果当前应用无版本，或者无有效的未发布版本， 将抛出发布异常。 TODO: 考虑是否有必要，或者前期有必要实现。
     *
     * @param app 找到有效版本
     * @param info 发布信息
     * @return
     */
    private void publishApp(App app, PublishInfo info) {
    }

    protected AppVersion getLatestUnployedVersion() {
        return null;
    }

    /**
     * 发布一个应用.
     *
     * @param instance
     * @return int 发布成功的实例个数（小于等于初始值）
     */
    public int publishAppVersion(App app, AppVersion appVersion, PublishInfo info) {
        App loadedApp = assertAndLoadApp(app);
        AppVersion loadedVersion = assertAndLoadVersion(appVersion);
        if (info == null) {
            throw new ServiceException("发布信息不能为空。");
        }
        // 保存版本信息
        AppCacheService cacheService = new AppCacheService();
        cacheService.setPersistenceManager(persistenceManager);
        cacheService.generateCacheId(app, appVersion);


        LogWriter.info(logger, String.format("正在发布应用[%s]，使用版本[%s]，发布信息为[%s]。", app,
                appVersion, info));

        // 保存发布信息
        savePublishInfo(info, app, appVersion);


        int maxTries = info.getInitialServers() + 3;  // 最大尝试次数
        int index = 0;
        // 初始化 N 个服务器。
        for (int i = 0; i < info.getInitialServers(); i++) {
            if (index > maxTries) {
                // 当失败的操作最大容忍度时，宣布发布失败
                return i;
            }
            index++;
            Cattle cattle = publishInstance(app, appVersion);
            if (cattle == null) {
                // 不成功，继续初始化
                i--;
                continue;
            }
        }
        loadedApp.setState(AppState.RUNNING);
        persistenceManager.update(loadedApp);

        // 测试发布的时候已经赋值
        if (loadedVersion.getState() != AppVersionState.TESTING) {
            loadedVersion.setState(AppVersionState.RUNNING);
        }
        persistenceManager.update(loadedVersion);
        return info.getInitialServers();
    }

    /**
     * 发布一个实例。
     *
     * @param app
     * @param appVersion
     */
    private Cattle publishInstance(App app, AppVersion appVersion) {
        // 加载应用的“主机”地址
        if (app.getHosts().isEmpty()) {
            List<AppHost> hosts = persistenceManager.findAll(AppHost.class, "APP_OID=?",
                    SqlUtils.getParams(app.getOid()), null);
            for (AppHost appHost : hosts) {
                if (appHost.isValid()) {
                    app.addHost(appHost.getHost());
                }
            }
        }

        final String appInstanceOid = UUID.randomUUID().toString();

        // 创建资源工厂
        ResourceService resourceSerivce = createResourceService();
        Cattle cattle = resourceSerivce.applyFor(app, appVersion, appInstanceOid, null);
        if (cattle != null) {
            // 持久化发布信息(记入应用启动实例表)
            saveAppInstance(app, appVersion, appInstanceOid, cattle);
        }
        return cattle;
    }

    /**
     * 持久化发布信息
     *
     * @param info
     * @param app
     * @param appVersion
     * @throws PersistenceException
     */
    private void savePublishInfo(PublishInfo info, App app, AppVersion appVersion) throws PersistenceException {
        // 先删除已经存在的发布信息（扩容的情况可能会出现这种情况）
        persistenceManager.execute("DELETE FROM PUBLISH_INFO WHERE APP_VERSION_OID=?",
                SqlUtils.getParams(appVersion.getOid()));

        info.setOid(CloudUtils.generateOid());
        info.setAppOid(app.getOid());
        info.setAppVersionOid(appVersion.getOid());
        info.setInsertTime(Calendar.getInstance());
        persistenceManager.insert(info);
    }

    /**
     * 读取发布信息
     *
     * @return
     */
    public PublishInfo readPublishInfo(AppVersion appVersion) {
        assertAndLoadVersion(appVersion);
        List<PublishInfo> list = persistenceManager.findAll(PublishInfo.class, "APP_VERSION_OID=?", SqlUtils.getParams(appVersion.getOid()), null);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    /**
     * 读取当前版本的实例个数。
     *
     * @return
     */
    public int readInstanceCount(AppVersion appVersion) {
        assertAndLoadVersion(appVersion);
        return persistenceManager.queryInt("SELECT COUNT(*) FROM APP_INSTANCE"
                + " WHERE APP_VERSION_OID=? AND STATE=?",
                SqlUtils.getParams(appVersion.getOid(),
                EnumUtils.toString(AppInstanceState.RUNNING)));
    }

    /**
     * 将应用发布为一个测试版本。
     */
    public void publishTestVersion(App app, AppVersion appVersion) {
        PublishInfo info = new PublishInfo();
        info.setMaxServers(1);
        info.setInitialServers(1);
        publishAppVersion(app, appVersion, info);
        appVersion.setState(AppVersionState.TESTING);
        persistenceManager.update(app);
    }

    /**
     * 保存应用的实例，每保存一个实例，数据库都将自动提交。
     *
     * @param instance
     * @param appVersion
     * @param cattle
     */
    private void saveAppInstance(App app, AppVersion appVersion, String appInstanceOid, Cattle cattle) {
        AppInstance instance = new AppInstance();
        instance.setOid(appInstanceOid);
        instance.setAppOid(app.getOid());
        instance.setServerOid(cattle.getServerOid());
        instance.setAppVersionOid(appVersion.getOid());
        instance.setCattleOid(cattle.getId());
        instance.setState(AppInstanceState.RUNNING);
        instance.setIp(cattle.getIp());
        instance.setPort(String.valueOf(cattle.getPort()));
        instance.setContextPath(cattle.getContextPath());
        instance.setDeployPath(cattle.getDeployPath());
        instance.setStartupTime(Calendar.getInstance());
        instance.setInvalidTime(Constants.INVALID_TIME);  //TODO: MySQL 在处理空日期上有点问题
        instance.setShutdownTime(Constants.INVALID_TIME);
        persistenceManager.insert(instance);

        // 每个应用实例要提交一次（因为，发布的实例不容易回退）
        persistenceManager.commit();
    }

    /**
     * 读取应用程序的部署包。
     *
     * @param instance 应用信息。
     * @return 包的流文件。
     */
    public InputStream getDeployedPackage(App app, AppVersion appVersion) {
        if (app == null || StringUtils.isEmpty(app.getOid())) {
            throw new AppException("应用及其编码不能为空。");
        }
        if (appVersion == null && StringUtils.isEmpty(app.getVersionOid())) {
            throw new AppException(String.format("未指定应用[%s]的版本。", app));
        }

        // 检查发布的版本是否存在
        appVersion = (AppVersion) persistenceManager.findByPK(appVersion);
        if (appVersion == null) {
            throw new AppException(String.format("未找到应用[%s]的版本[%s]。", app, appVersion));
        }
        if (StringUtils.isEmpty(appVersion.getDeployFileName())) {
            throw new AppException(String.format("未定义应用[%s]版本[%s]的发布路径。", app, appVersion));
        }

        String fileName = appVersion.getDeployFileName();

        if (!fileService.exists(fileName)) {
            throw new AppException(String.format("应用[%s]的部署文件[%s]不存在。", app, fileName));
        }


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOHelper.i2o(fileService.read(fileName), baos, true, true);
        byte[] bytes = baos.toByteArray();
        LogWriter.info(logger, String.format("读取应用的部署文件[%s]，字节数[%d]。", fileName, bytes.length));
        InputStream bais = new ByteArrayInputStream(bytes);
        return new BufferedInputStream(bais);
    }

    /**
     * 暂停一个应用（不再对外提供服务）。
     *
     * @param instance
     */
    public void pauseApp(App app) {
        assertAndLoadApp(app);
        // TODO: 检查状态

        // 由“Ranch”实例通知“任务分配器”暂停应用。为了避免通知失败，
        // 可以使用多个通知的方式（即向多个服务器实例发送通知）
        List<AppInstance> instances = persistenceManager.findAll(AppInstance.class, "APP_OID=? AND STATE=?",
                SqlUtils.getParams(app.getOid(), EnumUtils.toString(AppInstanceState.RUNNING)), "STARTUP_TIME ASC");
        int count = 0;
        int maxCount = 2;  // 最大的通知数，默认为“2”。
        for (Iterator<AppInstance> it = instances.iterator(); it.hasNext();) {
            AppInstance instance = it.next();
            ServerService serserService = getServerService(instance.getServerOid(), true);
            if (serserService == null
                    || serserService.pauseApp(app, instance.getCattleOid())) {
                count++;
            }
            // 超过最大通知数即停止（如果尚未暂停，或者暂停失败，则有“巡查员”负责处理）
            if (count >= maxCount) {
                break;
            }
        }
        app.setState(AppState.PAUSED);
        persistenceManager.update(app);
    }

    /**
     * 重新启动暂停的应用。
     *
     * @param app
     */
    public void resumeApp(App app) {
        assertAndLoadApp(app);
        // TODO: 检查状态

        // TODO: 通知“任务分配器”重启应用。

        app.setState(AppState.RUNNING);
        persistenceManager.update(app);
    }

    /**
     * 暂停一个应用的指定版本。如果此应用已无正在运行的版本，则将此应用的状态设置为“暂停”。
     *
     * @param app 应用实例
     * @param oldVersion 应用版本实例
     */
    private void pauseAppVersion(App app, AppVersion version) {
        app = assertAndLoadApp(app);
        version = assertAndLoadVersion(version);
        version.setState(AppVersionState.PAUSED);
        persistenceManager.update(version);

        long count = persistenceManager.count(AppVersion.class, "APP_OID=? AND STATE=?",
                SqlUtils.getParams(app.getOid(), EnumUtils.toString(AppVersionState.RUNNING)));
        if (count < 1) {
            app.setState(AppState.PAUSED);
            persistenceManager.update(app);
        }
    }

    /**
     * 对一个应用进行升级，从老版本升级到新版本。
     *
     * @param app 应用实例，不能为空。
     * @param oldVersion 旧版本，为空表示升级所有旧版本。
     * @param newVersion 新版本，不能为空。
     * @param mode 升级的模式，不能为空。
     */
    public void upgrade(App app, String oldVersionOid,
            String newVersionOid, UpdateMode mode) {
        LogWriter.info2(logger, "应用[%s]正在从版本[%s]向版本[%s]升级，采用的方式是：[%s]。",
                app, oldVersionOid, newVersionOid, mode);
        App loadedApp = assertAndLoadApp(app);
        AppVersion newVersion = assertVersion(newVersionOid, "新版本");
        AppVersion oldVersion = assertVersion(oldVersionOid, "旧版本");

        // 如果存在旧版本的发布信息，则新版本也使用相同的发布信息
        PublishInfo oldPublishInfo = persistenceManager.findFirst(PublishInfo.class,
                "APP_VERSION_OID=?", SqlUtils.getParams(oldVersionOid), null);
        PublishInfo newPublishInfo = new PublishInfo();
        if (oldPublishInfo != null) {
            newPublishInfo.setMinServers(oldPublishInfo.getMinServers());
            newPublishInfo.setMaxServers(oldPublishInfo.getMaxServers());
            newPublishInfo.setInitialServers(oldPublishInfo.getInitialServers());
            newPublishInfo.setAutoScale(oldPublishInfo.getAutoScale());
        }

        // 保存版本信息
        if (UpdateMode.GRACEFUL == mode) {
            // 和老版本使用相同的缓存信息，以保证此Session可以复用
            AppCacheService cacheService = new AppCacheService();
            cacheService.setPersistenceManager(persistenceManager);
            cacheService.clear(newVersionOid);  // 清除以前留存的错误记录
            String cacheId = cacheService.getCacheId(oldVersionOid);
            if (StringUtils.isEmpty(cacheId)) {
                throw new AppException(String.format("版本[%s]的缓存信息丢失，升级失败。", oldVersion));
            }
            cacheService.saveCacheId(app.getOid(), newVersionOid, oldVersionOid, cacheId);
        }


        // 发布新的应用
        publishAppVersion(app, newVersion, newPublishInfo);

        List<AppInstance> instances = persistenceManager.findAll(AppInstance.class,
                "APP_OID=? AND APP_VERSION_OID=? AND STATE=?",
                SqlUtils.getParams(app.getOid(), oldVersionOid,
                EnumUtils.toString(AppInstanceState.RUNNING)), null);

        // 如果当前应用是默认版本，则通知升级控制器发布新的版本
        if (oldVersionOid.equals(app.getVersionOid())) {
            // 为了保证通信安全，可通过两个实例进行通知（冗余处理）。
            int i = 0;
            for (Iterator<AppInstance> it = instances.iterator(); it.hasNext() && i < 2;) {
                AppInstance appInstance = it.next();
                ServerService serverService = getServerService(appInstance.getServerOid(), true);
                if (serverService != null) {
                    LogWriter.info2(logger, "将应用[%s]的默认版本改为[%s]。",
                            app, newVersion);
                    serverService.setAppDefaultVersion(app, newVersion, appInstance.getCattleOid());
                    i++;
                }
            }
            loadedApp.setVersionOid(newVersionOid);
            persistenceManager.update(loadedApp);
        }

        LogWriter.info2(logger, "采用的升级方式是：[%s]，==%s。", mode, String.valueOf(UpdateMode.GRACEFUL == mode));
        if (UpdateMode.GRACEFUL == mode) {
            // 平稳过渡式的升级，只将版本做废弃标记，向任务控制器发送注销信息，等待任务监控程序回收其实例
            unregister(app, oldVersion);
        } else if (UpdateMode.PROGRESSIVE == mode) {
            // TODO:采用逐步升级方式暂时不收回老版本，只将版本做废弃标记，等所有的登录用户都退出后再升级
            //
        }

        for (Iterator<AppInstance> it = instances.iterator(); it.hasNext();) {
            AppInstance appInstance = it.next();
            //TODO: 判断实例是否已经失效
            ServerService serverService = getServerService(appInstance.getServerOid(), true);
            if (serverService == null) {
                // 实例的状态由“巡视程序”自动处理
                LogWriter.warn2(logger, "服务器实例[%s]已经失效。", appInstance.getServerOid());
                continue;
            }

            // 只设置状态，等待巡检程序将其收回
            appInstance.setState(AppInstanceState.STOPED);
            persistenceManager.update(appInstance);

            if (UpdateMode.SHOCKED == mode) {
                // 回收老版本
                serverService.undeploy(appInstance.getCattleOid(), true);
            }
        }
        //
    }

    /**
     * 注销一个应用的版本，即向任务分派器发送一个注销通知。
     *
     * @param app
     * @param newVersionOid 旧版本的状态可能已经被停掉，所以也使用正在升级的新版本进行通知。
     * @param oldVersion
     */
    public void unregister(App app, AppVersion oldVersion) {
        List<AppInstance> instances = persistenceManager.findAll(AppInstance.class,
                "APP_VERSION_OID=? AND STATE=?",
                SqlUtils.getParams(oldVersion.getOid(),
                EnumUtils.toString(AppInstanceState.RUNNING)), "STARTUP_TIME DESC");

        LogWriter.info2(logger, "正在撤销应用[%s]版本[%s]的注册信息，"
                + "当前实例数量[%s]。", app, oldVersion, instances.size());
        for (Iterator<AppInstance> it = instances.iterator(); it.hasNext();) {
            AppInstance instance = it.next();
            ServerService serserService = getServerService(instance.getServerOid(), true);
            if (serserService != null) {
                serserService.unregister(app, oldVersion, instance.getCattleOid());
            }
        }
    }

    /**
     * 读取指定应用正在运行的版本
     *
     * @param app
     * @return
     */
    public List<AppVersion> readRunningVersions(App app) {
        // 返回所有运行状态的版本
        List<AppVersion> versions = persistenceManager.findAll(AppVersion.class,
                "APP_OID=? AND STATE=?",
                SqlUtils.getParams(app.getOid(), EnumUtils.toString(AppVersionState.RUNNING)), null);
        return versions;
    }

    /**
     * 读取指定应用正在运行的版本
     *
     * @param app
     * @return
     */
    public List<AppVersion> readAvailableVersions(App app) {
        // 返回所有运行状态的版本
        List<AppVersion> versions = persistenceManager.findAll(AppVersion.class,
                "APP_OID=? AND (STATE=? OR STATE=? OR STATE=? )",
                SqlUtils.getParams(new Object[]{app.getOid(),
                    EnumUtils.toString(AppVersionState.REGISTERED),
                    EnumUtils.toString(AppVersionState.TESTING),
                    EnumUtils.toString(AppVersionState.STOPED)}), null);
        return versions;
    }

    /**
     * 停止某个服务器，系统将停止所有的服务器的运行。
     *
     * @param instance
     */
    public void stopApp(App app) {
        assertAndLoadApp(app);

        // TODO: 通知“任务分配器”停止应用。

        // 停止所有正在运行的实例
        List<AppVersion> versions = persistenceManager.findAll(AppVersion.class, "APP_OID=?",
                SqlUtils.getParams(app.getOid()), null);
        for (Iterator<AppVersion> it = versions.iterator(); it.hasNext();) {
            AppVersion appVersion = it.next();
            _stopAppVersion(app, appVersion, false);
        }

        setAppState(app, AppState.STOPED);
    }

    /**
     * 停止指定版本的应用，系统将停止所有的此版本的实例的运行。 如果当前应用的所有版本都停止了，当前应用的状态也修改为停止。
     *
     * @param app 应用实例，不能为空。
     * @param oldVersion 版本实例，不能为空。
     *
     */
    public void stopAppVersion(App app, AppVersion version) {
        _stopAppVersion(app, version, true);
    }

    /**
     * 停止指定版本的应用，系统将停止所有的此版本的实例的运行。
     *
     * @param app 应用实例，不能为空。
     * @param oldVersion 版本实例，不能为空。
     * @param checkApp 是否检查当前应用的所有版本，即如果当前应用的所有版本都停止了，当前应用的状态也修改为停止。
     *
     */
    protected void _stopAppVersion(App app, AppVersion version, boolean checkApp) {
        app = assertAndLoadApp(app);
        version = assertAndLoadVersion(version);

        // 首先暂停所有实例
        List<AppInstance> instances = persistenceManager.findAll(AppInstance.class,
                "APP_VERSION_OID=? AND STATE=?",
                SqlUtils.getParams(version.getOid(), EnumUtils.toString(AppInstanceState.RUNNING)),
                "STARTUP_TIME DESC");
        for (Iterator<AppInstance> it = instances.iterator(); it.hasNext();) {
            AppInstance appInstance = it.next();
            stopInstance(appInstance);
        }

        // 将应用的所有版本都设置为“停止”
        version.setState(AppVersionState.STOPED);
        persistenceManager.update(version);

        if (checkApp) {
            long count = persistenceManager.count(AppVersion.class, "APP_OID=? AND STATE != ?",
                    SqlUtils.getParams(app.getOid(), EnumUtils.toString(AppVersionState.STOPED)));
            LogWriter.info2(logger, "当前应用[%s]还有[%d]个非运行实例", app, count);
            if (count < 1) {
                // 应用的所有版本都为停止状态时，此应用也设置为“停止”状态。
                setAppState(app, AppState.STOPED);
            }
        }
    }

    private void setAppState(App app, AppState state) {
        app.setState(state);
        persistenceManager.update(app);
    }

    /**
     * 重新启动应用。系统将停止所有的服务实例，然后再进行重新分配。 启动有两种模式(目前使用第一种方式)： 1.
     * 在原来的基础之上进行重新启动，这样的好处是不需要重新获取发布文件， 已经编译的JSP文件不需要重新编译。 2.
     * 重新选择服务器进行发布，这种模式不但可以重新选择负载较轻的服务器资源， 而且保证收回所有的资源（内存，数据库等等）。
     *
     * @param instance
     * @return 重新启动的实例个数
     */
    public int restart(AppVersion version) {
        LogWriter.info2(logger, "重新启动应用[%s]的版本[%s]。",
                version.getAppOid(), version.getVersion());
        assertAndLoadVersion(version);
        List<AppInstance> instances = persistenceManager.findAll(AppInstance.class,
                "APP_VERSION_OID=? AND STATE=?",
                SqlUtils.getParams(version.getOid(), EnumUtils.toString(AppInstanceState.RUNNING)), null);
        if (instances.isEmpty()) {
            LogWriter.info2(logger, "应用[%s]的版本[%s]的当前实例为空。",
                    version.getAppOid(), version.getVersion());
            return 0;
        }
        int count = 0;
        for (Iterator<AppInstance> it = instances.iterator(); it.hasNext();) {
            AppInstance appInstance = it.next();
            ServerService serserService = getServerService(appInstance.getServerOid(), true);
            if (serserService != null) {
                serserService.restart(appInstance.getCattleOid());
                count++;
            }
        }
        return count;
    }

    /**
     * 调整指定版本的容量。
     *
     * @param oldVersion
     * @param info
     */
    public void alterCapacity(App app, AppVersion version, PublishInfo info) {
        LogWriter.info2(logger, "调整应用[%s]（版本[%s]）的容量到[%s]。", app, version, info);
        assertAndLoadVersion(version);
        // 当前实例的个数
        long instanceCount = readVersionInstanceCount(version);

        // 保存发布信息
        savePublishInfo(info, app, version);

        // 小于最小数量
        if (instanceCount < info.getInitialServers()) {
            final long deployingCount = info.getInitialServers() - instanceCount;  // 需发布的个数
            LogWriter.info2(logger, "当前运行实例个数小于请求的最小实例数，系统尝试再启动[%d]个实例。",
                    deployingCount);
            // 启动缺少的服务器
            for (int i = 0; i < deployingCount; i++) {
                publishInstance(app, version);
            }
        } else if (instanceCount > info.getMaxServers()) {
            LogWriter.info2(logger, "当前运行实例个数大于请求的最大实例数，系统尝试停止[%d]个实例。", (instanceCount - info.getMaxServers()));
            // 停止多余的服务器
            List<AppInstance> instances = persistenceManager.findAll(AppInstance.class, "APP_VERSION_OID=? AND STATE=?",
                    SqlUtils.getParams(version.getOid(), EnumUtils.toString(AppInstanceState.RUNNING)), null);
            for (int i = 0; i < instanceCount - info.getMaxServers(); i++) {
                // TODO: 出现错误也不处理
                stopInstance(instances.get(i));
            }
        }
    }

    /**
     * 读取指定版本的实例数量
     *
     * @param oldVersion
     * @return
     */
    public int readVersionInstanceCount(AppVersion version) {
        this.assertAndLoadVersion(version);
        return (int) persistenceManager.count(AppInstance.class, "APP_VERSION_OID=? AND STATE=?",
                SqlUtils.getParams(version.getOid(), EnumUtils.toString(AppInstanceState.RUNNING)));
    }

    /**
     * 读取指定应用的实例数量。
     *
     * @param app 应用实例，不能为空。
     * @return
     */
    public int readAppInstanceCount(App app) {
        this.assertAndLoadApp(app);
        return (int) persistenceManager.count(AppInstance.class, "APP_OID=? AND STATE=?",
                SqlUtils.getParams(app.getOid(), EnumUtils.toString(AppInstanceState.RUNNING)));
    }

    /**
     * 读取指定主机的已经“停止”的服务器实例。
     *
     * @param host 主机地址
     * @return 如果主机地址为空，返回空，否则返回和此主机地址相同的所有实例。
     */
    public List<AppInstance> readStopedInstances(String host, int reserverdDays) {
        if (StringUtils.isEmpty(host)) {
            return Collections.emptyList();
        }
        List<AppInstance> instances = persistenceManager.findAll(AppInstance.class,
                "STATE=? AND (TO_DAYS(NOW())-TO_DAYS(SHUTDOWN_TIME)>? AND IP=?)",
                SqlUtils.getParams(AppInstanceState.STOPED, reserverdDays, host), null);
        return instances;
    }

    /**
     * 清除发布路径。
     *
     * @param appInstance
     */
    public void clearDeployedPath(AppInstance appInstance) {
        appInstance = assertAndLoadInstance(appInstance);
        appInstance.setDeployPath("");
        persistenceManager.update(appInstance);
    }

    @LocalMethod
    public void setPersistenceManager(IPOPersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    @LocalMethod
    public IPOPersistenceManager getPersistenceManager() {
        return persistenceManager;
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
