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
     * 构建分配给应用的数据库资源。
     * @param root
     * @param app
     * @param version
     * @param persistenceManager
     */
    public void createConf(Element root, Cattle cattle, IPOPersistenceManager persistenceManager) {
        final App app            = cattle.getApp();
        final AppVersion version = cattle.getAppVersion();
        if (app == null || StringUtils.isEmpty(app.getOid())) {
            throw new ResourceException("应用及其编码信息不能为空。");
        }
        //
        DbGroup group = getDBGroup(persistenceManager, app.getOid(), version == null ? null : version.getOid());
        LogWriter.info2(logger, "为应用[%s]分配了数据库组[%s]。", app, group);

        createDatabases(root, group);
    }

    /**
     * 创建数据库集合的节点（如果资源为空，表示未申请相应的资源）
     * @param root
     */
    private void createDatabases(Element root, DbGroup group) {
        if (root == null || group == null
                || group.getDbSources() == null || group.getDbSources().isEmpty()) {
            return;
        }
        // 首先加入数据库解析
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

        // 加入 分布式 Session 处理工厂
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

        // 创建数据库节点
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
        // 读取主库的相关信息
        List<String> masterNames = new ArrayList();
        List<DbSource> masters = new ArrayList();
        readDbSource(group, true, masterNames, masters);
        if (masters.isEmpty()) {
            throw new ConfException(String.format("数据库组[%s]的主数据库不能为空。", group));
        }
        String defaultDbName = masterNames.get(0);

        // 读取主库的相关信息
        List<String> slaveNames = new ArrayList();
        List<DbSource> slaves = new ArrayList();
        readDbSource(group, false, slaveNames, slaves);

        Element databases = new Element(MasterSlaveDataBaseParameterParser.MSDB_PARSER_TAG_DATABASES);
        root.addContent(databases);
        databases.setAttribute(DB_PARSER_TAG_ID, MasterSlavesContext.MASTERS_SLAVES_PARAM_NAME);
        databases.setAttribute(DB_PARSER_TAG_DEFAULT, defaultDbName);
        databases.setAttribute(MasterSlaveDataBaseParameterParser.MSDB_PARSER_TAG_MASTERS, StringUtils.join(masterNames.iterator(), ","));
        databases.setAttribute(MasterSlaveDataBaseParameterParser.MSDB_PARSER_TAG_SLAVES, StringUtils.join(slaveNames.iterator(), ","));

        // 创建主库
        for (DbSource source : masters) {
            Element database = createDatabase(group, source);
            databases.addContent(database);
        }

        // 创建备份库
        for (DbSource source : slaves) {
            Element database = createDatabase(group, source);
            databases.addContent(database);
        }
    }

    /**
     * 读取指定的数据库信息
     * @param group      组实例
     * @param masterFlag 是否是主库
     * @param names 用于存储数据源的名称
     * @param sources 用于存储读取搜数据源实例
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
     * 自动产生数据库的编码
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
        // 替换 URL 处的 IP 和端口
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
        LogWriter.info(logger, String.format("正在为应用[%s]的版本[%s]分配数据库组。",
                appOid, appVersionOid));
        List<DbSourceInstance> instances = persistenceManager.findAll(DbSourceInstance.class,
                "APP_OID=? and APP_VERSION_OID=?",
                SqlUtils.getParams(appOid, appVersionOid), null);

        // 首先寻找版本专用的数据库组，如果不存在，查找应用公用的数据库组
        if (instances == null || instances.isEmpty()) {
            LogWriter.info(logger, String.format("使用应用[%s]的通用数据库组。",
                    appOid, appVersionOid));
            instances = persistenceManager.findAll(DbSourceInstance.class,
                    "APP_OID=? and APP_VERSION_OID IS NULL", SqlUtils.getParams(appOid), null);
        }
        if (instances == null || instances.isEmpty()) {
            LogWriter.warn(logger, String.format("应用[%s]未定义通用数据库组。",
                    appOid));
            return null;
        }

        // 只取第一个数据库组
        DbSourceInstance instance = instances.get(0);
        String groupOid = instance.getDbGroupOid();
        DbGroup dbGroup = (DbGroup) persistenceManager.findByPK(DbGroup.class, groupOid);
        if (dbGroup == null) {
            LogWriter.warn(logger, String.format("数据库组[%s]的数据已经缺失。",
                    groupOid));
            return null;
        }
        List<DbSource> dbSources = persistenceManager.findAll(DbSource.class,
                "GROUP_OID=?", SqlUtils.getParams(groupOid), null);
        for (DbSource dbSource : dbSources) {
            dbGroup.addDbSource(dbSource);
        }
        LogWriter.info(logger, String.format("已经为应用[%s]的版本[%s]分配数据库组[%s]。",
                appOid, appVersionOid, dbGroup));
        return dbGroup;
    }
}
