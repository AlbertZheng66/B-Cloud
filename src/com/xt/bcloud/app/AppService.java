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
 * ����Ӧ����صķ���ӿڡ�
 *
 * @author albert
 */
public class AppService implements IService, IPOPersistenceAware, FileServiceAware {

    /**
     * �־û��ļ�ʵ��
     */
    private transient IPOPersistenceManager persistenceManager;
    /**
     * �ļ�������ʵ��
     */
    private transient FileService fileService;
    private final Logger logger = Logger.getLogger(AppService.class);
    /**
     * Ӧ�ù���Ķ˿ں�
     */
    public static final int APP_MGR_PORT = 58080;
    /**
     * Ӧ�ù���ķ��ʵ�ַ�Ķ�λ����
     */
    public static final String APP_MGR_URL = "appMgr.url";
    /**
     * Ӧ�ù����������·��
     */
    public static final String APP_MGR_CONTEXT_PATH = "/arMgr";
    /**
     * ���ڴ��Ӧ�ð汾�ķ����ļ��Ļ���·��������ͨ��������app.version.deployed.path�����е�����Ĭ��Ϊ��{�ļ��������·��}/WEB-INF/deployPackages
     */
    private static final String deployedBasePath = SystemConfiguration.getInstance().readString("app.version.deployed.path", "WEB-INF/deployPackages");

    public AppService() {
    }

    /**
     * ע��һ�������ˡ�
     *
     * @param tenant
     */
    public void registryTenant(Tenant tenant) {
    }

    /**
     * ע��һ��Ӧ����Դ��
     */
    public void registerApp(App app) {
        if (app == null) {
            throw new AppException(String.format("Ӧ�ò���Ϊ�ա�"));
        }
        if (StringUtils.isEmpty(app.getId())
                || StringUtils.isEmpty(app.getName())) {
            throw new AppException(String.format("Ӧ��[%s]�ı�������ƾ�����Ϊ�ա�", app));
        }
        if (duplicateAppId(app.getId())) {
            throw new AppException(String.format("����[%s]�Ѿ����ڡ�", app.getId()));
        }
        app.setOid(CloudUtils.generateOid());
        this.persistenceManager.insert(app);
    }

    /**
     * ����һ��Ӧ����Դ�Ļ�����Ϣ��
     */
    public void updateApp(App app) {
        if (app == null) {
            throw new AppException(String.format("Ӧ�ò���Ϊ�ա�"));
        }
        if (StringUtils.isEmpty(app.getOid())) {
            throw new AppException(String.format("Ӧ�õ�OID����Ϊ�ա�", app));
        }
        this.persistenceManager.update(app);
    }

    /**
     * ����һ��ָ����AppӦ�á�
     */
    public App findApp(String oid) {
        if (StringUtils.isEmpty(oid)) {
            throw new AppException(String.format("Ӧ�õ�OID����Ϊ�ա�"));
        }
        return (App) persistenceManager.findByPK(App.class, oid);
    }

    public boolean duplicateAppId(String id) {
        int count = this.persistenceManager.queryInt("select count(*) from app where id=?",
                SqlUtils.getParams(id == null ? "" : id));
        return (count > 0);
    }

    /**
     * ע��ָ��Ӧ�õ��°汾�������ǰӦ����Ĭ�ϰ汾����˰汾ΪĬ�ϰ汾��
     */
    public void registerAppVersion(App app, AppVersion version, InputStream uploadingDeployedFile) {
        App loadedApp = assertAndLoadApp(app);
        if (version == null) {
            throw new AppException("ע��İ汾����Ϊ�ա�");
        }
        if (uploadingDeployedFile == null) {
            throw new AppException(String.format("Ӧ��[%s](�汾[%s])���ϴ��ļ�����Ϊ�ա�",
                    app.getId(), version.getVersion()));
        }

        version.setOid(CloudUtils.generateOid());
        String fileName = generateFileName(app, version);
        LogWriter.info2(logger, "�Զ����ɵ��ϴ��ļ���Ϊ[%s]��", fileName);
        OutputStream os = fileService.writeTo(fileName, false);
        long fileSize = IOHelper.i2o(uploadingDeployedFile, os, true, false);
        version.setDeployFileName(fileName);
        version.setFileSize((int) fileSize);
        version.setAppOid(app.getOid());
        version.setInsertTime(Calendar.getInstance());
        version.setState(AppVersionState.REGISTERED);
        version.setValid(true);
        persistenceManager.insert(version);

        // �����ǰӦ�õ�Ϊ�գ����߶���İ汾�Ѿ������ڣ���ʹ�õ�ǰ�汾��ΪĬ�ϰ汾��
        if (isVersionExisted(loadedApp.getVersionOid())) {
            loadedApp.setVersionOid(version.getOid());
            persistenceManager.update(loadedApp);
        }
    }

    /**
     * �ж�ָ���İ汾�Ƿ���ڡ�
     *
     * @param versionOid �汾��
     * @return ���ڣ�true������false��
     */
    private boolean isVersionExisted(String versionOid) {
        if (StringUtils.isEmpty(versionOid)) {
            return false;
        }
        Object version = persistenceManager.findByPK(AppVersion.class, versionOid);
        return (version != null);
    }
    
     /**
     * �ж�ָ���İ汾�Ƿ���ڡ�
     *
     * @param versionOid �汾��
     * @return ���ڣ�true������false��
     */
    public AppVersion getAppVersion(String versionOid) {
        if (StringUtils.isEmpty(versionOid)) {
            throw new AppException("�汾�Ų���Ϊ�ա�");
        }
        AppVersion version = (AppVersion)persistenceManager.findByPK(AppVersion.class, versionOid);
        return version;
    }

    /**
     * ����Ӧ�ü��������Զ����ɷ�����������
     *
     * @param app Ӧ��ʵ��
     * @param appVersion �汾����
     * @return
     */
    protected String generateFileName(App app, AppVersion appVersion) {
        String basePath = deployedBasePath == null ? "" : deployedBasePath;
        StringBuilder strBld = new StringBuilder(basePath);
        final String fileSep = "/"; // File.separator;
        if (StringUtils.isNotEmpty(basePath) && !basePath.endsWith(fileSep)) {
            strBld.append(fileSep);
        }
        strBld.append(app.getOid());  // һ��Ŀ¼
        strBld.append(fileSep);
        strBld.append(appVersion.getOid());
        strBld.append(fileSep);

        // Ŀ¼�ṹ
        if (!fileService.exists(strBld.toString())) {
            fileService.mkdirs(strBld.toString());
        }
        strBld.append(appVersion.getVersion());  // �԰汾����Ϊǰ׺
        strBld.append("_").append(System.currentTimeMillis());  // �Ե�ǰʱ����Ϊ��ʶ��
        strBld.append(".war");
        String fileName = strBld.toString();
        if (fileService.exists(fileName)) {
            LogWriter.info2(logger, "�ļ�[%s]�Ѿ����ڣ�ϵͳ�����Զ�ɾ����", fileName);
            fileService.delete(fileName);
        }
        return fileName;
    }

    /**
     * ɾ��һ��Ӧ����Դ
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

        // ɾ��Ӧ����Ϣ
        this.persistenceManager.delete(app);
    }

    /**
     * �Ƴ�Ӧ�õ�ָ���汾�� TODO: �Ƿ�ֻ��������ͣ״̬��Ȼ���к�̨�߳�ɾ����Ӧ�á�
     *
     * @param app
     * @return Ӧ�õ�Ĭ�ϰ汾�š�
     */
    public String removeAppVersion(App app, AppVersion version) {
        App loadedApp = assertAndLoadApp(app);

        version = assertAndLoadVersion(version);

        List params = SqlUtils.getParams(version.getOid());

        List<AppInstance> appInstances = persistenceManager.findAll(AppInstance.class,
                "APP_VERSION_OID=?", params, null);

        // ɾ��Ӧ��ʵ�����ж�Ӧ����Ϣ
        persistenceManager.execute("DELETE FROM APP_INSTANCE WHERE APP_VERSION_OID=?", params);

        // ͣ������ʵ��
        for (AppInstance instance : appInstances) {
            try {
                ServerService serserService = getServerService(instance.getServerOid(), true);
                if (serserService != null) {
                    serserService.undeploy(instance.getCattleOid(), true);   // ��ʱ������ǿ���˳���ʽ
                }
            } catch (Throwable t) {
                LogWriter.warn2(logger, t, "ͣ��ʵ��[%s]�����쳣��", instance);
            }
        }

        // ɾ���ϴ��ķ����ļ�
        String deployedFileName = version.getDeployFileName();
        if (StringUtils.isNotEmpty(deployedFileName)
                && fileService.exists(deployedFileName)) {
            try {
                fileService.delete(deployedFileName);
            } catch (Exception ignored) {
                // ���ִ����ʱ���к�̨������
                LogWriter.warn2(logger, ignored, "ɾ�������ļ�[%s]����", deployedFileName);
            }
        }

        // ɾ���汾��Ϣ
        persistenceManager.delete(version);

        // ɾ��������Ϣ
        persistenceManager.execute("DELETE FROM PUBLISH_INFO WHERE APP_VERSION_OID=?", params);

        // ɾ�������������Ϣ
        persistenceManager.execute("DELETE FROM APP_CACHE WHERE APP_VERSION_OID=?", params);

        // ɾ���İ汾ΪĬ�ϰ汾ʱ���Զ�ѡ�����µİ汾��ΪĬ�ϰ汾��
        String defaultVersion = loadedApp.getVersionOid();
        if (version.getOid().equals(defaultVersion)) {
            defaultVersion = getDefaultVersion(loadedApp.getOid());
            loadedApp.setVersionOid(defaultVersion);
            persistenceManager.update(loadedApp);
        }
        return defaultVersion;
    }

    /**
     * �Զ�ѡ��Ӧ�õ�Ĭ�ϰ汾�������õ����°汾��
     *
     * @param appOid Ӧ�ñ���
     * @return
     */
    private String getDefaultVersion(String appOid) {
        String sql = "SELECT OID FROM APP_VERSION WHERE APP_OID=?";
        return persistenceManager.queryString(sql, SqlUtils.getParams(appOid));
    }

    /**
     * ָֹͣ����Ӧ��ʵ��
     *
     * @param instance
     */
    public void stopInstance(AppInstance instance) {
        instance = assertAndLoadInstance(instance);
        ServerService serserService = getServerService(instance.getServerOid(), true);
        if (serserService != null) {
            serserService.undeploy(instance.getCattleOid(), true);   // ��ʱ������ǿ���˳���ʽ
        }
        instance.setState(AppInstanceState.STOPED);
        persistenceManager.update(instance);
        deleteGroupConf(instance);
    }

    /**
     * ɾ������������ص���Ϣ��
     *
     * @param instance
     */
    private void deleteGroupConf(AppInstance instance) {
        // ɾ����ص���������Ϣ
        persistenceManager.execute("DELETE FROM GROUP_CONF WHERE ENTITY_ID=?", SqlUtils.getParams(instance.getOid()));
    }

    /**
     * ��������ָ����Ӧ��ʵ����
     *
     * @param appInstance
     */
    public void restartInstance(AppInstance instance) {
        instance = assertAndLoadInstance(instance);
        ServerService serserService = getServerService(instance.getServerOid(), false);
        serserService.restart(instance.getCattleOid());
    }

    /**
     * ���·�����ָ��ͣ����ǰ��Ӧ�ã�Ȼ�����·���һ��ʵ���� �������ĺô��ǣ����ĳЩ�ڴ�й©�Ƚ����ص�Ӧ�ã�����ͨ������
     * ��ʽ�ﵽ��΢�ȶ���״̬���൱��������������������
     *
     * @param instance Ӧ�õ�ʵ��
     */
    public void redeployInstance(AppInstance instance) {
        throw new UnsupportedOperationException("��ʱδʵ��");
//        instance = assertAndLoadInstance(instance);
//        ServerService serserService = getServerService(instance.getServerOid());
//        serserService.restart(instance.getCattleOid());
    }

    private App assertAndLoadApp(App app) throws ServiceException {
        if (app == null || StringUtils.isEmpty(app.getOid())) {
            throw new AppException("Ӧ�ü��������Ϣ������Ϊ�ա�");
        }
        App loaded = (App) persistenceManager.findByPK(app);
        if (loaded == null) {
            throw new AppException(String.format("Ӧ��[%s](OID:%s)�����ڡ�", app.getId(), app.getOid()));
        }
        return loaded;
    }

    /**
     * ���Ӧ�õİ汾ʵ���Ƿ���ڡ�
     *
     * @param oldVersion
     * @return �����ݿ��м��ص���ʵ����
     * @throws ServiceException
     */
    private AppVersion assertAndLoadVersion(AppVersion version) throws ServiceException {
        if (version == null || StringUtils.isEmpty(version.getOid())) {
            throw new AppException("Ӧ�ð汾���������Ϣ������Ϊ�ա�");
        }
        AppVersion loaded = (AppVersion) persistenceManager.findByPK(version);
        if (loaded == null) {
            throw new AppException(String.format("Ӧ�ð汾[%s](OID=%s)�����ݿ��в����ڡ�",
                    version.getVersion(), version.getOid()));
        }
        return loaded;
    }

    private AppInstance assertAndLoadInstance(AppInstance instance) throws ServiceException {
        if (instance == null || StringUtils.isEmpty(instance.getOid())) {
            throw new ServiceException("Ӧ��ʵ�����������Ϣ������Ϊ�ա�");
        }
        AppInstance loaded = (AppInstance) persistenceManager.findByPK(instance);
        if (loaded == null) {
            throw new AppException(String.format("Ӧ��ʵ��[%s]�����ڡ�", instance.getOid()));
        }
        return loaded;
    }

    private AppVersion assertVersion(String versionOid, String message) throws PersistenceException, AppException {
        if (StringUtils.isEmpty(versionOid)) {
            throw new AppException(message + "����Ϊ�ա�");
        }
        AppVersion version = (AppVersion) persistenceManager.findByPK(AppVersion.class, versionOid);
        if (version == null) {
            throw new AppException(String.format("[%s]�����ڡ�", message, versionOid));
        }
        AppVersion _version = new AppVersion();
        _version.setOid(versionOid);
        return _version;
    }

    private ResourceService createResourceService() {
        // ����Դ����������Դ
        ResourceService resourceSerivce = new ResourceService(); // CloudUtils.createResourceService();
        resourceSerivce.setPersistenceManager(persistenceManager);
        return resourceSerivce;
    }

    private ServerService getServerService(String serverOid, boolean nullable) {
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setOid(serverOid);

        serverInfo = (ServerInfo) persistenceManager.findByPK(serverInfo);
        if (serverInfo == null) {
            // ���Կս��д���
            if (nullable) {
                LogWriter.warn2(logger, "������[%s]��Ϣ�Ѿ���ʧ(�����Ƿ������Ѿ�ע��)��", serverOid);
                return null;
            } else {
                throw new SystemException(String.format("������[%s]��Ϣ�Ѿ���ʧ(�����Ƿ������Ѿ�ע��)��", serverOid));
            }
        }

        String proxyUrl = String.format("http://%s:%d/%s/", serverInfo.getIp(),
                serverInfo.getManagerPort(), serverInfo.getContextPath());
        Proxy proxy = new HttpStreamProxy(proxyUrl);
        // �������ʧ�ܣ��������´��������������������Ӧ�ķ������ʵ��
        ServerService serverSerivce = ServiceFactory.getInstance().getService(ServerService.class, proxy);
        return serverSerivce;
    }

    /**
     * ��ʾ����Ӧ�õ��б�
     *
     * @return
     */
    public List<App> list() {
        return this.persistenceManager.findAll(App.class, null, null, null);
    }

    /**
     * �г�ָ��Ӧ�õ����а汾
     *
     * @param instance Ӧ��ʵ�������Ϊ�����׳��쳣��
     * @return Ӧ�ð汾�б�
     */
    public List<AppVersion> listVersions(App app) {
        assertAndLoadApp(app);
        return this.persistenceManager.findAll(AppVersion.class, "APP_OID=?", SqlUtils.getParams(app.getOid()), "INSERT_TIME DESC");
    }

    /**
     * �г�ָ��Ӧ�õ����а汾
     *
     * @param instance Ӧ��ʵ�������Ϊ�����׳��쳣��
     * @return Ӧ�ð汾�б�
     */
    public List<AppInstance> listInstances(App app, AppVersion version) {
        assertAndLoadApp(app);
        assertAndLoadVersion(version);
        return persistenceManager.findAll(AppInstance.class, "APP_OID=? AND APP_VERSION_OID=?",
                SqlUtils.getParams(app.getOid(), version.getOid()), "STARTUP_TIME DESC");
    }

    /**
     * �л���ǰ���ó���İ汾
     *
     * @param instance
     */
    public void setVersion(App app, String version) {
        // TODO: ���������������ʧЧ֪ͨ
        app.setVersionOid(version);
        this.persistenceManager.update(app);
    }

    /**
     * ��һ��Ӧ����Դ����Ϊ����״̬, ֻ�й���Ա�ʺŲ��ܷ��ʴ�Ӧ�á� �ж�һ���ʺ��Ƿ�Ϊ����Ա�ʺ���Ҫ��Cookie�������ض�����Ϣ��
     *
     * @param instance
     */
    public void setStateTest(App app, AppVersion appVersion) {
    }

    /**
     * ������ǰӦ�õ����°汾�������ǰӦ���ް汾����������Ч��δ�����汾�� ���׳������쳣�� TODO: �����Ƿ��б�Ҫ������ǰ���б�Ҫʵ�֡�
     *
     * @param app �ҵ���Ч�汾
     * @param info ������Ϣ
     * @return
     */
    private void publishApp(App app, PublishInfo info) {
    }

    protected AppVersion getLatestUnployedVersion() {
        return null;
    }

    /**
     * ����һ��Ӧ��.
     *
     * @param instance
     * @return int �����ɹ���ʵ��������С�ڵ��ڳ�ʼֵ��
     */
    public int publishAppVersion(App app, AppVersion appVersion, PublishInfo info) {
        App loadedApp = assertAndLoadApp(app);
        AppVersion loadedVersion = assertAndLoadVersion(appVersion);
        if (info == null) {
            throw new ServiceException("������Ϣ����Ϊ�ա�");
        }
        // ����汾��Ϣ
        AppCacheService cacheService = new AppCacheService();
        cacheService.setPersistenceManager(persistenceManager);
        cacheService.generateCacheId(app, appVersion);


        LogWriter.info(logger, String.format("���ڷ���Ӧ��[%s]��ʹ�ð汾[%s]��������ϢΪ[%s]��", app,
                appVersion, info));

        // ���淢����Ϣ
        savePublishInfo(info, app, appVersion);


        int maxTries = info.getInitialServers() + 3;  // ����Դ���
        int index = 0;
        // ��ʼ�� N ����������
        for (int i = 0; i < info.getInitialServers(); i++) {
            if (index > maxTries) {
                // ��ʧ�ܵĲ���������̶�ʱ����������ʧ��
                return i;
            }
            index++;
            Cattle cattle = publishInstance(app, appVersion);
            if (cattle == null) {
                // ���ɹ���������ʼ��
                i--;
                continue;
            }
        }
        loadedApp.setState(AppState.RUNNING);
        persistenceManager.update(loadedApp);

        // ���Է�����ʱ���Ѿ���ֵ
        if (loadedVersion.getState() != AppVersionState.TESTING) {
            loadedVersion.setState(AppVersionState.RUNNING);
        }
        persistenceManager.update(loadedVersion);
        return info.getInitialServers();
    }

    /**
     * ����һ��ʵ����
     *
     * @param app
     * @param appVersion
     */
    private Cattle publishInstance(App app, AppVersion appVersion) {
        // ����Ӧ�õġ���������ַ
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

        // ������Դ����
        ResourceService resourceSerivce = createResourceService();
        Cattle cattle = resourceSerivce.applyFor(app, appVersion, appInstanceOid, null);
        if (cattle != null) {
            // �־û�������Ϣ(����Ӧ������ʵ����)
            saveAppInstance(app, appVersion, appInstanceOid, cattle);
        }
        return cattle;
    }

    /**
     * �־û�������Ϣ
     *
     * @param info
     * @param app
     * @param appVersion
     * @throws PersistenceException
     */
    private void savePublishInfo(PublishInfo info, App app, AppVersion appVersion) throws PersistenceException {
        // ��ɾ���Ѿ����ڵķ�����Ϣ�����ݵ�������ܻ�������������
        persistenceManager.execute("DELETE FROM PUBLISH_INFO WHERE APP_VERSION_OID=?",
                SqlUtils.getParams(appVersion.getOid()));

        info.setOid(CloudUtils.generateOid());
        info.setAppOid(app.getOid());
        info.setAppVersionOid(appVersion.getOid());
        info.setInsertTime(Calendar.getInstance());
        persistenceManager.insert(info);
    }

    /**
     * ��ȡ������Ϣ
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
     * ��ȡ��ǰ�汾��ʵ��������
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
     * ��Ӧ�÷���Ϊһ�����԰汾��
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
     * ����Ӧ�õ�ʵ����ÿ����һ��ʵ�������ݿⶼ���Զ��ύ��
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
        instance.setInvalidTime(Constants.INVALID_TIME);  //TODO: MySQL �ڴ�����������е�����
        instance.setShutdownTime(Constants.INVALID_TIME);
        persistenceManager.insert(instance);

        // ÿ��Ӧ��ʵ��Ҫ�ύһ�Σ���Ϊ��������ʵ�������׻��ˣ�
        persistenceManager.commit();
    }

    /**
     * ��ȡӦ�ó���Ĳ������
     *
     * @param instance Ӧ����Ϣ��
     * @return �������ļ���
     */
    public InputStream getDeployedPackage(App app, AppVersion appVersion) {
        if (app == null || StringUtils.isEmpty(app.getOid())) {
            throw new AppException("Ӧ�ü�����벻��Ϊ�ա�");
        }
        if (appVersion == null && StringUtils.isEmpty(app.getVersionOid())) {
            throw new AppException(String.format("δָ��Ӧ��[%s]�İ汾��", app));
        }

        // ��鷢���İ汾�Ƿ����
        appVersion = (AppVersion) persistenceManager.findByPK(appVersion);
        if (appVersion == null) {
            throw new AppException(String.format("δ�ҵ�Ӧ��[%s]�İ汾[%s]��", app, appVersion));
        }
        if (StringUtils.isEmpty(appVersion.getDeployFileName())) {
            throw new AppException(String.format("δ����Ӧ��[%s]�汾[%s]�ķ���·����", app, appVersion));
        }

        String fileName = appVersion.getDeployFileName();

        if (!fileService.exists(fileName)) {
            throw new AppException(String.format("Ӧ��[%s]�Ĳ����ļ�[%s]�����ڡ�", app, fileName));
        }


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOHelper.i2o(fileService.read(fileName), baos, true, true);
        byte[] bytes = baos.toByteArray();
        LogWriter.info(logger, String.format("��ȡӦ�õĲ����ļ�[%s]���ֽ���[%d]��", fileName, bytes.length));
        InputStream bais = new ByteArrayInputStream(bytes);
        return new BufferedInputStream(bais);
    }

    /**
     * ��ͣһ��Ӧ�ã����ٶ����ṩ���񣩡�
     *
     * @param instance
     */
    public void pauseApp(App app) {
        assertAndLoadApp(app);
        // TODO: ���״̬

        // �ɡ�Ranch��ʵ��֪ͨ���������������ͣӦ�á�Ϊ�˱���֪ͨʧ�ܣ�
        // ����ʹ�ö��֪ͨ�ķ�ʽ��������������ʵ������֪ͨ��
        List<AppInstance> instances = persistenceManager.findAll(AppInstance.class, "APP_OID=? AND STATE=?",
                SqlUtils.getParams(app.getOid(), EnumUtils.toString(AppInstanceState.RUNNING)), "STARTUP_TIME ASC");
        int count = 0;
        int maxCount = 2;  // ����֪ͨ����Ĭ��Ϊ��2����
        for (Iterator<AppInstance> it = instances.iterator(); it.hasNext();) {
            AppInstance instance = it.next();
            ServerService serserService = getServerService(instance.getServerOid(), true);
            if (serserService == null
                    || serserService.pauseApp(app, instance.getCattleOid())) {
                count++;
            }
            // �������֪ͨ����ֹͣ�������δ��ͣ��������ͣʧ�ܣ����С�Ѳ��Ա��������
            if (count >= maxCount) {
                break;
            }
        }
        app.setState(AppState.PAUSED);
        persistenceManager.update(app);
    }

    /**
     * ����������ͣ��Ӧ�á�
     *
     * @param app
     */
    public void resumeApp(App app) {
        assertAndLoadApp(app);
        // TODO: ���״̬

        // TODO: ֪ͨ�����������������Ӧ�á�

        app.setState(AppState.RUNNING);
        persistenceManager.update(app);
    }

    /**
     * ��ͣһ��Ӧ�õ�ָ���汾�������Ӧ�������������еİ汾���򽫴�Ӧ�õ�״̬����Ϊ����ͣ����
     *
     * @param app Ӧ��ʵ��
     * @param oldVersion Ӧ�ð汾ʵ��
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
     * ��һ��Ӧ�ý������������ϰ汾�������°汾��
     *
     * @param app Ӧ��ʵ��������Ϊ�ա�
     * @param oldVersion �ɰ汾��Ϊ�ձ�ʾ�������оɰ汾��
     * @param newVersion �°汾������Ϊ�ա�
     * @param mode ������ģʽ������Ϊ�ա�
     */
    public void upgrade(App app, String oldVersionOid,
            String newVersionOid, UpdateMode mode) {
        LogWriter.info2(logger, "Ӧ��[%s]���ڴӰ汾[%s]��汾[%s]���������õķ�ʽ�ǣ�[%s]��",
                app, oldVersionOid, newVersionOid, mode);
        App loadedApp = assertAndLoadApp(app);
        AppVersion newVersion = assertVersion(newVersionOid, "�°汾");
        AppVersion oldVersion = assertVersion(oldVersionOid, "�ɰ汾");

        // ������ھɰ汾�ķ�����Ϣ�����°汾Ҳʹ����ͬ�ķ�����Ϣ
        PublishInfo oldPublishInfo = persistenceManager.findFirst(PublishInfo.class,
                "APP_VERSION_OID=?", SqlUtils.getParams(oldVersionOid), null);
        PublishInfo newPublishInfo = new PublishInfo();
        if (oldPublishInfo != null) {
            newPublishInfo.setMinServers(oldPublishInfo.getMinServers());
            newPublishInfo.setMaxServers(oldPublishInfo.getMaxServers());
            newPublishInfo.setInitialServers(oldPublishInfo.getInitialServers());
            newPublishInfo.setAutoScale(oldPublishInfo.getAutoScale());
        }

        // ����汾��Ϣ
        if (UpdateMode.GRACEFUL == mode) {
            // ���ϰ汾ʹ����ͬ�Ļ�����Ϣ���Ա�֤��Session���Ը���
            AppCacheService cacheService = new AppCacheService();
            cacheService.setPersistenceManager(persistenceManager);
            cacheService.clear(newVersionOid);  // �����ǰ����Ĵ����¼
            String cacheId = cacheService.getCacheId(oldVersionOid);
            if (StringUtils.isEmpty(cacheId)) {
                throw new AppException(String.format("�汾[%s]�Ļ�����Ϣ��ʧ������ʧ�ܡ�", oldVersion));
            }
            cacheService.saveCacheId(app.getOid(), newVersionOid, oldVersionOid, cacheId);
        }


        // �����µ�Ӧ��
        publishAppVersion(app, newVersion, newPublishInfo);

        List<AppInstance> instances = persistenceManager.findAll(AppInstance.class,
                "APP_OID=? AND APP_VERSION_OID=? AND STATE=?",
                SqlUtils.getParams(app.getOid(), oldVersionOid,
                EnumUtils.toString(AppInstanceState.RUNNING)), null);

        // �����ǰӦ����Ĭ�ϰ汾����֪ͨ���������������µİ汾
        if (oldVersionOid.equals(app.getVersionOid())) {
            // Ϊ�˱�֤ͨ�Ű�ȫ����ͨ������ʵ������֪ͨ�����ദ����
            int i = 0;
            for (Iterator<AppInstance> it = instances.iterator(); it.hasNext() && i < 2;) {
                AppInstance appInstance = it.next();
                ServerService serverService = getServerService(appInstance.getServerOid(), true);
                if (serverService != null) {
                    LogWriter.info2(logger, "��Ӧ��[%s]��Ĭ�ϰ汾��Ϊ[%s]��",
                            app, newVersion);
                    serverService.setAppDefaultVersion(app, newVersion, appInstance.getCattleOid());
                    i++;
                }
            }
            loadedApp.setVersionOid(newVersionOid);
            persistenceManager.update(loadedApp);
        }

        LogWriter.info2(logger, "���õ�������ʽ�ǣ�[%s]��==%s��", mode, String.valueOf(UpdateMode.GRACEFUL == mode));
        if (UpdateMode.GRACEFUL == mode) {
            // ƽ�ȹ���ʽ��������ֻ���汾��������ǣ����������������ע����Ϣ���ȴ������س��������ʵ��
            unregister(app, oldVersion);
        } else if (UpdateMode.PROGRESSIVE == mode) {
            // TODO:������������ʽ��ʱ���ջ��ϰ汾��ֻ���汾��������ǣ������еĵ�¼�û����˳���������
            //
        }

        for (Iterator<AppInstance> it = instances.iterator(); it.hasNext();) {
            AppInstance appInstance = it.next();
            //TODO: �ж�ʵ���Ƿ��Ѿ�ʧЧ
            ServerService serverService = getServerService(appInstance.getServerOid(), true);
            if (serverService == null) {
                // ʵ����״̬�ɡ�Ѳ�ӳ����Զ�����
                LogWriter.warn2(logger, "������ʵ��[%s]�Ѿ�ʧЧ��", appInstance.getServerOid());
                continue;
            }

            // ֻ����״̬���ȴ�Ѳ��������ջ�
            appInstance.setState(AppInstanceState.STOPED);
            persistenceManager.update(appInstance);

            if (UpdateMode.SHOCKED == mode) {
                // �����ϰ汾
                serverService.undeploy(appInstance.getCattleOid(), true);
            }
        }
        //
    }

    /**
     * ע��һ��Ӧ�õİ汾�������������������һ��ע��֪ͨ��
     *
     * @param app
     * @param newVersionOid �ɰ汾��״̬�����Ѿ���ͣ��������Ҳʹ�������������°汾����֪ͨ��
     * @param oldVersion
     */
    public void unregister(App app, AppVersion oldVersion) {
        List<AppInstance> instances = persistenceManager.findAll(AppInstance.class,
                "APP_VERSION_OID=? AND STATE=?",
                SqlUtils.getParams(oldVersion.getOid(),
                EnumUtils.toString(AppInstanceState.RUNNING)), "STARTUP_TIME DESC");

        LogWriter.info2(logger, "���ڳ���Ӧ��[%s]�汾[%s]��ע����Ϣ��"
                + "��ǰʵ������[%s]��", app, oldVersion, instances.size());
        for (Iterator<AppInstance> it = instances.iterator(); it.hasNext();) {
            AppInstance instance = it.next();
            ServerService serserService = getServerService(instance.getServerOid(), true);
            if (serserService != null) {
                serserService.unregister(app, oldVersion, instance.getCattleOid());
            }
        }
    }

    /**
     * ��ȡָ��Ӧ���������еİ汾
     *
     * @param app
     * @return
     */
    public List<AppVersion> readRunningVersions(App app) {
        // ������������״̬�İ汾
        List<AppVersion> versions = persistenceManager.findAll(AppVersion.class,
                "APP_OID=? AND STATE=?",
                SqlUtils.getParams(app.getOid(), EnumUtils.toString(AppVersionState.RUNNING)), null);
        return versions;
    }

    /**
     * ��ȡָ��Ӧ���������еİ汾
     *
     * @param app
     * @return
     */
    public List<AppVersion> readAvailableVersions(App app) {
        // ������������״̬�İ汾
        List<AppVersion> versions = persistenceManager.findAll(AppVersion.class,
                "APP_OID=? AND (STATE=? OR STATE=? OR STATE=? )",
                SqlUtils.getParams(new Object[]{app.getOid(),
                    EnumUtils.toString(AppVersionState.REGISTERED),
                    EnumUtils.toString(AppVersionState.TESTING),
                    EnumUtils.toString(AppVersionState.STOPED)}), null);
        return versions;
    }

    /**
     * ֹͣĳ����������ϵͳ��ֹͣ���еķ����������С�
     *
     * @param instance
     */
    public void stopApp(App app) {
        assertAndLoadApp(app);

        // TODO: ֪ͨ�������������ֹͣӦ�á�

        // ֹͣ�����������е�ʵ��
        List<AppVersion> versions = persistenceManager.findAll(AppVersion.class, "APP_OID=?",
                SqlUtils.getParams(app.getOid()), null);
        for (Iterator<AppVersion> it = versions.iterator(); it.hasNext();) {
            AppVersion appVersion = it.next();
            _stopAppVersion(app, appVersion, false);
        }

        setAppState(app, AppState.STOPED);
    }

    /**
     * ָֹͣ���汾��Ӧ�ã�ϵͳ��ֹͣ���еĴ˰汾��ʵ�������С� �����ǰӦ�õ����а汾��ֹͣ�ˣ���ǰӦ�õ�״̬Ҳ�޸�Ϊֹͣ��
     *
     * @param app Ӧ��ʵ��������Ϊ�ա�
     * @param oldVersion �汾ʵ��������Ϊ�ա�
     *
     */
    public void stopAppVersion(App app, AppVersion version) {
        _stopAppVersion(app, version, true);
    }

    /**
     * ָֹͣ���汾��Ӧ�ã�ϵͳ��ֹͣ���еĴ˰汾��ʵ�������С�
     *
     * @param app Ӧ��ʵ��������Ϊ�ա�
     * @param oldVersion �汾ʵ��������Ϊ�ա�
     * @param checkApp �Ƿ��鵱ǰӦ�õ����а汾���������ǰӦ�õ����а汾��ֹͣ�ˣ���ǰӦ�õ�״̬Ҳ�޸�Ϊֹͣ��
     *
     */
    protected void _stopAppVersion(App app, AppVersion version, boolean checkApp) {
        app = assertAndLoadApp(app);
        version = assertAndLoadVersion(version);

        // ������ͣ����ʵ��
        List<AppInstance> instances = persistenceManager.findAll(AppInstance.class,
                "APP_VERSION_OID=? AND STATE=?",
                SqlUtils.getParams(version.getOid(), EnumUtils.toString(AppInstanceState.RUNNING)),
                "STARTUP_TIME DESC");
        for (Iterator<AppInstance> it = instances.iterator(); it.hasNext();) {
            AppInstance appInstance = it.next();
            stopInstance(appInstance);
        }

        // ��Ӧ�õ����а汾������Ϊ��ֹͣ��
        version.setState(AppVersionState.STOPED);
        persistenceManager.update(version);

        if (checkApp) {
            long count = persistenceManager.count(AppVersion.class, "APP_OID=? AND STATE != ?",
                    SqlUtils.getParams(app.getOid(), EnumUtils.toString(AppVersionState.STOPED)));
            LogWriter.info2(logger, "��ǰӦ��[%s]����[%d]��������ʵ��", app, count);
            if (count < 1) {
                // Ӧ�õ����а汾��Ϊֹͣ״̬ʱ����Ӧ��Ҳ����Ϊ��ֹͣ��״̬��
                setAppState(app, AppState.STOPED);
            }
        }
    }

    private void setAppState(App app, AppState state) {
        app.setState(state);
        persistenceManager.update(app);
    }

    /**
     * ��������Ӧ�á�ϵͳ��ֹͣ���еķ���ʵ����Ȼ���ٽ������·��䡣 ����������ģʽ(Ŀǰʹ�õ�һ�ַ�ʽ)�� 1.
     * ��ԭ���Ļ���֮�Ͻ������������������ĺô��ǲ���Ҫ���»�ȡ�����ļ��� �Ѿ������JSP�ļ�����Ҫ���±��롣 2.
     * ����ѡ����������з���������ģʽ������������ѡ���ؽ���ķ�������Դ�� ���ұ�֤�ջ����е���Դ���ڴ棬���ݿ�ȵȣ���
     *
     * @param instance
     * @return ����������ʵ������
     */
    public int restart(AppVersion version) {
        LogWriter.info2(logger, "��������Ӧ��[%s]�İ汾[%s]��",
                version.getAppOid(), version.getVersion());
        assertAndLoadVersion(version);
        List<AppInstance> instances = persistenceManager.findAll(AppInstance.class,
                "APP_VERSION_OID=? AND STATE=?",
                SqlUtils.getParams(version.getOid(), EnumUtils.toString(AppInstanceState.RUNNING)), null);
        if (instances.isEmpty()) {
            LogWriter.info2(logger, "Ӧ��[%s]�İ汾[%s]�ĵ�ǰʵ��Ϊ�ա�",
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
     * ����ָ���汾��������
     *
     * @param oldVersion
     * @param info
     */
    public void alterCapacity(App app, AppVersion version, PublishInfo info) {
        LogWriter.info2(logger, "����Ӧ��[%s]���汾[%s]����������[%s]��", app, version, info);
        assertAndLoadVersion(version);
        // ��ǰʵ���ĸ���
        long instanceCount = readVersionInstanceCount(version);

        // ���淢����Ϣ
        savePublishInfo(info, app, version);

        // С����С����
        if (instanceCount < info.getInitialServers()) {
            final long deployingCount = info.getInitialServers() - instanceCount;  // �跢���ĸ���
            LogWriter.info2(logger, "��ǰ����ʵ������С���������Сʵ������ϵͳ����������[%d]��ʵ����",
                    deployingCount);
            // ����ȱ�ٵķ�����
            for (int i = 0; i < deployingCount; i++) {
                publishInstance(app, version);
            }
        } else if (instanceCount > info.getMaxServers()) {
            LogWriter.info2(logger, "��ǰ����ʵ������������������ʵ������ϵͳ����ֹͣ[%d]��ʵ����", (instanceCount - info.getMaxServers()));
            // ֹͣ����ķ�����
            List<AppInstance> instances = persistenceManager.findAll(AppInstance.class, "APP_VERSION_OID=? AND STATE=?",
                    SqlUtils.getParams(version.getOid(), EnumUtils.toString(AppInstanceState.RUNNING)), null);
            for (int i = 0; i < instanceCount - info.getMaxServers(); i++) {
                // TODO: ���ִ���Ҳ������
                stopInstance(instances.get(i));
            }
        }
    }

    /**
     * ��ȡָ���汾��ʵ������
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
     * ��ȡָ��Ӧ�õ�ʵ��������
     *
     * @param app Ӧ��ʵ��������Ϊ�ա�
     * @return
     */
    public int readAppInstanceCount(App app) {
        this.assertAndLoadApp(app);
        return (int) persistenceManager.count(AppInstance.class, "APP_OID=? AND STATE=?",
                SqlUtils.getParams(app.getOid(), EnumUtils.toString(AppInstanceState.RUNNING)));
    }

    /**
     * ��ȡָ���������Ѿ���ֹͣ���ķ�����ʵ����
     *
     * @param host ������ַ
     * @return ���������ַΪ�գ����ؿգ����򷵻غʹ�������ַ��ͬ������ʵ����
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
     * �������·����
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
