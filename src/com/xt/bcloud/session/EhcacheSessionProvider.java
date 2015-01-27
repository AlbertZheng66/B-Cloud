package com.xt.bcloud.session;

import com.xt.bcloud.app.App;
import com.xt.bcloud.app.AppVersion;
import com.xt.bcloud.resource.ProviderHelper;
import com.xt.bcloud.resource.ResourceException;
import com.xt.bcloud.resource.ServiceProvider;
import com.xt.bcloud.worker.Cattle;
import com.xt.core.db.pm.IPOPersistenceManager;
import com.xt.core.exception.SystemException;
import com.xt.gt.jt.http.GreenTeaGeneralServlet;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.Element;
import static com.xt.gt.sys.SystemConfiguration.*;

/**
 * ��Ӧ���ṩ�ֲ�ʽ Session��
 * @author albert
 */
public class EhcacheSessionProvider implements ServiceProvider {

    /**
     * �Զ��������ļ������� Cache ��ID��
     */
    public static final String EHCACHE_ID = "ehcachSession.cacheId";


    /**
     * �Զ��������ļ������õ�ǰ��Ӧ�ð汾���롣
     */
    public static final String APP_VERSION_OID = "ehcachSession.appVersionOid";


    /**
     * �Զ��������ļ������õ�ǰ��Ӧ�÷�����ʵ���ı��롣
     */
    public static final String APP_INSTANCE_OID = "ehcachSession.appInstanceOid";

    /**
     * �Զ��������ļ������õ�ǰ��Ӧ�ð汾���롣
     */
    public static final String APP_OID = "ehcachSession.appOid";

    private final Logger logger = Logger.getLogger(EhcacheSessionProvider.class);

    public EhcacheSessionProvider() {
    }

    /**
     * ���������Ӧ�õ����ݿ���Դ��
     * @param root
     * @param app
     * @param version
     * @param persistenceManager
     */
    public void createConf(Element root, Cattle cattle, 
            IPOPersistenceManager persistenceManager) {
        if (cattle == null || cattle.getApp() == null || StringUtils.isEmpty(cattle.getApp().getOid())) {
            throw new ResourceException("Ӧ�ü��������Ϣ����Ϊ�ա�");
        }

        // ���� �ֲ�ʽ Session ������
        /*
        <system>
        <param name="processorFactories" type="list">
        <data value="com.xt.bcloud.session.SessionProcessorFactory" />
        </param>
        </system>
         */
        Element system = root.getChild(CONF_FILE_TAG_SYSTEM);
        if (system == null) {
            system = new Element(CONF_FILE_TAG_SYSTEM);
            root.addContent(system);
        }



        Element processorFactories = ProviderHelper.getChild(system, ProviderHelper.TAG_PROCESSOR_FACTORIES);
        if (processorFactories == null) {
            processorFactories = ProviderHelper.createListNode(system, ProviderHelper.TAG_PROCESSOR_FACTORIES);
        }
        Element data = new Element(CONF_FILE_TAG_LIST_DATA);
        data.setAttribute(CONF_FILE_TAG_LIST_VALUE, ClusterSessionProcessorFactory.class.getName());
        processorFactories.addContent(data);

        // ���뵱ǰ��Ӧ�� OID�����䡰�汾 OID��
        /**
         * <param name="CURRENT_APP_OID"  value="99990000" />
         */
        final App app            = cattle.getApp();
        final AppVersion version = cattle.getAppVersion();
        String cacheId = getCacheId(persistenceManager, app, version);
        ProviderHelper.createSimpleNode(system, EHCACHE_ID,      cacheId);
        ProviderHelper.createSimpleNode(system, APP_OID,         app.getOid());
        ProviderHelper.createSimpleNode(system, APP_VERSION_OID, version.getOid());
        ProviderHelper.createSimpleNode(system, APP_INSTANCE_OID, cattle.getAppInstanceOid());

         // ���뵱ǰ���ֲ�ʽSession�ı�ǡ�
        /**
         * <param name="system.session.clustered"  value="true" />
         */
        ProviderHelper.createSimpleNode(system, GreenTeaGeneralServlet.SYSTEM_CLUSTERED_FLAG, "true");

         // ���뵱ǰ���ֲ�ʽSession��ʵ���ࡱ
        /**
         * <param name="system.session.cluster.class"  value="EhcacheSession" />
         */
        ProviderHelper.createSimpleNode(system, GreenTeaGeneralServlet.SYSTEM_CLUSTER_SESSION_CLASS, EhcacheSession.class.getName());

        // �������� Loader
        /*
        <param name="appLifecycles" type='list'>
        <data value="com.xt.bcloud.session.EhcacheLoader" />
        </param>
         */
        Element appLifecycles = ProviderHelper.getChild(system, "appLifecycles");
        if (appLifecycles == null) {
            appLifecycles = ProviderHelper.createListNode(system, "appLifecycles");
        }
        Element alData = new Element(CONF_FILE_TAG_LIST_DATA);
        alData.setAttribute(CONF_FILE_TAG_LIST_VALUE, EhcacheLoader.class.getName());
        appLifecycles.addContent(alData);
    }

    private String getCacheId(IPOPersistenceManager persistenceManager,
            App app, AppVersion version) {
        AppCacheService appCacheService = new AppCacheService();
        appCacheService.setPersistenceManager(persistenceManager);
        // return appUpgradeService.generateCacheId(app, version, null, false);
        String cacheId = appCacheService.getCacheId(version.getOid());
        if (StringUtils.isEmpty(cacheId)) {
            throw new ClusterException(String.format("Ӧ��[%s]�汾[%s]�Ļ�����δ���䡣", app.getOid(), version.getOid()));
        }
        return cacheId;
    }

   
}

