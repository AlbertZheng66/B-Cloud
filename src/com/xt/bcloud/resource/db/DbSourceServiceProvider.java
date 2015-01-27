package com.xt.bcloud.resource.db;

import com.xt.bcloud.app.App;
import com.xt.bcloud.app.AppVersion;
import com.xt.bcloud.resource.ConfException;
import com.xt.bcloud.resource.ProviderHelper;
import com.xt.bcloud.resource.ResourceException;
import com.xt.bcloud.resource.ServiceProvider;
import com.xt.bcloud.worker.Cattle;
import com.xt.core.db.conn.DatabaseContext;
import com.xt.core.db.pm.IPOPersistenceManager;
import com.xt.core.log.LogWriter;
import com.xt.core.proc.impl.MultiIPOPersistenceFactory;
import com.xt.core.utils.SqlUtils;
import com.xt.core.utils.VarTemplate;
import com.xt.gt.sys.impl.MasterSlaveDataBaseParameterParser;
import com.xt.gt.sys.impl.MasterSlavesContext;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.Element;
import static com.xt.gt.sys.SystemConfiguration.*;
import static com.xt.gt.sys.impl.DataBaseParameterParser.*;

/**
 *
 * @author albert
 */
public class DbSourceServiceProvider implements ServiceProvider {

    private final Logger logger = Logger.getLogger(DbSourceServiceProvider.class);

    public DbSourceServiceProvider() {
    }

    /**
     * ���������Ӧ�õ����ݿ���Դ��
     * @param root
     * @param app
     * @param version
     * @param persistenceManager
     */
    public void createConf(Element root, Cattle cattle, IPOPersistenceManager persistenceManager) {
        final App app            = cattle.getApp();
        final AppVersion version = cattle.getAppVersion();
        if (app == null || StringUtils.isEmpty(app.getOid())) {
            throw new ResourceException("Ӧ�ü��������Ϣ����Ϊ�ա�");
        }
        //
        DbGroup group = getDBGroup(persistenceManager, app.getOid(), version == null ? null : version.getOid());
        LogWriter.info2(logger, "ΪӦ��[%s]���������ݿ���[%s]��", app, group);

        createDatabases(root, group);
    }

    /**
     * �������ݿ⼯�ϵĽڵ㣨�����ԴΪ�գ���ʾδ������Ӧ����Դ��
     * @param root
     */
    private void createDatabases(Element root, DbGroup group) {
        if (root == null || group == null
                || group.getDbSources() == null || group.getDbSources().isEmpty()) {
            return;
        }
        // ���ȼ������ݿ����
        /*
        <system>
        <param name="parsers" type="map">
        <entry key="msDatabases" value="com.xt.gt.sys.impl.DataBaseParameterParser" />
        </param>
        </system>*/
        Element system = root.getChild(CONF_FILE_TAG_SYSTEM);
        if (system == null) {
            system = new Element(CONF_FILE_TAG_SYSTEM);
            root.addContent(system);
        }
        Element parsers = ProviderHelper.getChild(system, CONF_FILE_TAG_PARSERS);
        if (parsers == null) {
            parsers = ProviderHelper.createParamNode(system, CONF_FILE_TAG_PARSERS, PAPAM_TYPE_MAP);
        }
        Element entry = new Element(CONF_FILE_TAG_MAP_ENTRY);
        entry.setAttribute(CONF_FILE_TAG_MAP_KEY, MasterSlaveDataBaseParameterParser.MSDB_PARSER_TAG_DATABASES);
        entry.setAttribute(CONF_FILE_TAG_MAP_VALUE, MasterSlaveDataBaseParameterParser.class.getName());
        parsers.addContent(entry);

        // ���� �ֲ�ʽ Session ������
        /*
        <system>
        <param name="processorFactories" type="list">
        <data value="com.xt.core.proc.impl.MultiIPOPersistenceFactory" />
        </param>
        </system>
         */
        Element processorFactories = ProviderHelper.getChild(system, ProviderHelper.TAG_PROCESSOR_FACTORIES);
        if (processorFactories == null) {
            processorFactories = ProviderHelper.createListNode(system, ProviderHelper.TAG_PROCESSOR_FACTORIES);
        }
        Element data = new Element(CONF_FILE_TAG_LIST_DATA);
        data.setAttribute(CONF_FILE_TAG_LIST_VALUE, MultiIPOPersistenceFactory.class.getName());
        processorFactories.addContent(data);

        // �������ݿ�ڵ�
        /* <msDatabases default="demo" master="m1,m2" slaves="s1,s2">
        <database id="demo" type="jdbc" >
        <param name="user"     value="sa" />
        <param name="password" value="" />
        <param name="schema"   value="PUBLIC" />
        <param name="url"      value="jdbc:hsqldb:file:${appContext}WEB-INF/db/gt_demo" />
        <param name="driver"   value="org.hsqldb.jdbcDriver" />
        </database>
        <database id="test" type="jdbc">
        <param name="user"     value="gt_demo" />
        <param name="password" value="gt_demo" />
        <param name="schema"   value="public" />
        <param name="url"      value="jdbc:postgresql://localhost/gt_demo" />
        <param name="driver"   value="org.postgresql.Driver" />
        </database>
        </msDatabases>
         * */
        // ��ȡ����������Ϣ
        List<String> masterNames = new ArrayList();
        List<DbSource> masters = new ArrayList();
        readDbSource(group, true, masterNames, masters);
        if (masters.isEmpty()) {
            throw new ConfException(String.format("���ݿ���[%s]�������ݿⲻ��Ϊ�ա�", group));
        }
        String defaultDbName = masterNames.get(0);

        // ��ȡ����������Ϣ
        List<String> slaveNames = new ArrayList();
        List<DbSource> slaves = new ArrayList();
        readDbSource(group, false, slaveNames, slaves);

        Element databases = new Element(MasterSlaveDataBaseParameterParser.MSDB_PARSER_TAG_DATABASES);
        root.addContent(databases);
        databases.setAttribute(DB_PARSER_TAG_ID, MasterSlavesContext.MASTERS_SLAVES_PARAM_NAME);
        databases.setAttribute(DB_PARSER_TAG_DEFAULT, defaultDbName);
        databases.setAttribute(MasterSlaveDataBaseParameterParser.MSDB_PARSER_TAG_MASTERS, StringUtils.join(masterNames.iterator(), ","));
        databases.setAttribute(MasterSlaveDataBaseParameterParser.MSDB_PARSER_TAG_SLAVES, StringUtils.join(slaveNames.iterator(), ","));

        // ��������
        for (DbSource source : masters) {
            Element database = createDatabase(group, source);
            databases.addContent(database);
        }

        // �������ݿ�
        for (DbSource source : slaves) {
            Element database = createDatabase(group, source);
            databases.addContent(database);
        }
    }

    /**
     * ��ȡָ�������ݿ���Ϣ
     * @param group      ��ʵ��
     * @param masterFlag �Ƿ�������
     * @param names ���ڴ洢����Դ������
     * @param sources ���ڴ洢��ȡ������Դʵ��
     */
    private void readDbSource(DbGroup group, boolean masterFlag, List<String> names, List<DbSource> sources) {
        for (Iterator<DbSource> it = group.getDbSources().iterator(); it.hasNext();) {
            DbSource source = it.next();
            if (masterFlag == source.isMaster()) {
                names.add(generateId(group, source));
                sources.add(source);
            }
        }
    }

    /**
     * �Զ��������ݿ�ı���
     * @param source
     * @param group
     * @return
     */
    private String generateId(DbGroup group, DbSource source) {
        return String.format("%s-%s", source.getOid(), group.getName());
    }

    private Element createDatabase(DbGroup group, DbSource source) {
        Element database = new Element(DB_PARSER_TAG_DATABASE);
        database.setAttribute(DB_PARSER_TAG_ID, generateId(group, source));
        database.setAttribute(DB_PARSER_TAG_TYPE, DatabaseContext.JDBC);
        addParam(database, DB_PARSER_TAG_USER, group.getUserName());
        addParam(database, DB_PARSER_TAG_PASSWORD, group.getPasswd());
        if (group.getDbSchema() != null) {
            addParam(database, DB_PARSER_TAG_SCHEMA, group.getDbSchema());
        }
        String url = group.getUrl();
        // �滻 URL ���� IP �Ͷ˿�
        url = VarTemplate.format(url, source);
        addParam(database, DB_PARSER_TAG_URL, url);
        addParam(database, DB_PARSER_TAG_DRIVER, group.getDriverClass());
        return database;
    }

    private void addParam(Element database, String name, String value) {
        Element param = new Element(CONF_FILE_TAG_PARAM);
        database.addContent(param);
        param.setAttribute(CONF_FILE_TAG_PARAM_NAME, name);
        param.setAttribute(CONF_FILE_TAG_PARAM_VALUE, value);
    }

    public DbGroup getDBGroup(IPOPersistenceManager persistenceManager,
            String appOid, String appVersionOid) {
        LogWriter.info(logger, String.format("����ΪӦ��[%s]�İ汾[%s]�������ݿ��顣",
                appOid, appVersionOid));
        List<DbSourceInstance> instances = persistenceManager.findAll(DbSourceInstance.class,
                "APP_OID=? and APP_VERSION_OID=?",
                SqlUtils.getParams(appOid, appVersionOid), null);

        // ����Ѱ�Ұ汾ר�õ����ݿ��飬��������ڣ�����Ӧ�ù��õ����ݿ���
        if (instances == null || instances.isEmpty()) {
            LogWriter.info(logger, String.format("ʹ��Ӧ��[%s]��ͨ�����ݿ��顣",
                    appOid, appVersionOid));
            instances = persistenceManager.findAll(DbSourceInstance.class,
                    "APP_OID=? and APP_VERSION_OID IS NULL", SqlUtils.getParams(appOid), null);
        }
        if (instances == null || instances.isEmpty()) {
            LogWriter.warn(logger, String.format("Ӧ��[%s]δ����ͨ�����ݿ��顣",
                    appOid));
            return null;
        }

        // ֻȡ��һ�����ݿ���
        DbSourceInstance instance = instances.get(0);
        String groupOid = instance.getDbGroupOid();
        DbGroup dbGroup = (DbGroup) persistenceManager.findByPK(DbGroup.class, groupOid);
        if (dbGroup == null) {
            LogWriter.warn(logger, String.format("���ݿ���[%s]�������Ѿ�ȱʧ��",
                    groupOid));
            return null;
        }
        List<DbSource> dbSources = persistenceManager.findAll(DbSource.class,
                "GROUP_OID=?", SqlUtils.getParams(groupOid), null);
        for (DbSource dbSource : dbSources) {
            dbGroup.addDbSource(dbSource);
        }
        LogWriter.info(logger, String.format("�Ѿ�ΪӦ��[%s]�İ汾[%s]�������ݿ���[%s]��",
                appOid, appVersionOid, dbGroup));
        return dbGroup;
    }
}
