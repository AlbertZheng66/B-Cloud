package com.xt.bcloud.worker;

import com.xt.bcloud.app.App;
import com.xt.bcloud.app.AppVersion;
import com.xt.bcloud.comm.CloudUtils;
import com.xt.bcloud.core.ConfLoader;
import com.xt.bcloud.resource.ConfService;
import com.xt.bcloud.resource.ResourceException;
import com.xt.core.exception.BadParameterException;
import com.xt.core.log.LogWriter;
import com.xt.core.service.IService;
import com.xt.core.utils.IOHelper;
import com.xt.proxy.Proxy;
import com.xt.proxy.ServiceFactory;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Server;
import org.apache.catalina.ServerFactory;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.deploy.ContextEnvironment;
import org.apache.catalina.startup.Embedded;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author albert
 */
public class TomcatServerController implements IService, ServerController {

    private String engineName= "Catalina-";
    private String hostName = "host-";
    private String contextPath = "/";
    private Cattle cattle;
    private String resourceBase;  // 应用的发布路径
    private Logger logger = Logger.getLogger(TomcatServerController.class);
    private Service service;

    public TomcatServerController() {
    }

    /**
     * 初始化服务信息
     * @param cattle
     * @param resourceBase
     */
    public void init(Cattle cattle, String resourceBase) {
        if (cattle == null || StringUtils.isEmpty(resourceBase)) {
            throw new BadParameterException("工作实例和资源路径都不能为空。");
        }
        App app = cattle.getApp();
        AppVersion version = cattle.getAppVersion();
        if (app == null || version == null) {
            throw new BadParameterException("工作实例的应用和版本都不能为空。");
        }
        engineName = String.format("Catalina-%s-%d", app.getOid(), System.currentTimeMillis());
        hostName   = String.format("Host-%s-%d", app.getOid(), System.currentTimeMillis());
        // 如果版本的上下文不为空，则使用版本的上下文，否则使用应用的上下文，
        // (ContextPath的形式是：以“/”开头，不能以“/”结尾，否则发布后不能访问。例如："/gt_demo")
        this.contextPath = cattle.getContextPath();

        this.cattle = cattle;
        this.resourceBase = resourceBase;
    }

    private Service initService() throws Exception {
        logger.info(String.format("启动服务器，信息如下：engineName=%s; hostName=%s;"
                + " resourceBase=%s; contextPath=%s; port=%d;",
                engineName, hostName, resourceBase, contextPath, cattle.getPort()));

        // Embedded embedded = (Embedded)(Class.forName(Embedded.class.getName(), true, getClass().getClassLoader().getParent())).newInstance(); // new Embedded();
        Embedded embedded = new Embedded();
        //注意：需要使用Servlet上一级的类加载器（当前也是一个WebappClassLoader类加载器，需要使用上层类加载器(org.apache.catalina.loader.StandardClassLoader)，避免出现类空间的问题，）
        // Loader loader = embedded.createLoader(getClass().getClassLoader().getParent());

        //embedded.setCatalinaHome(catalinaHome);

        // Create an Engine
        Engine engine = embedded.createEngine();
        // engine.setLoader(loader);
        engine.setName(engineName);
        engine.setDefaultHost(hostName);
        embedded.addEngine(engine);

        // Create a Host
        //File webAppsLocation = new File("E:/work/xthinker/gt_html_demo/web");
        Host host = embedded.createHost(
                hostName, resourceBase);
        // host.setLoader(loader);
        engine.addChild(host);



        // Add the context
        // File docBase = new File(webAppsLocation, DOC_BASE);
        Context context = createContext(embedded, contextPath, resourceBase);
        // context.setLoader(loader);
        // 增加一个装载系统参数的监听器
//        ParametersLoader pLoader = new ParametersLoader(cattle.getApp(), cattle.getAppVersion());
//        context.addContainerListener(pLoader);
//        ContextResource resource = new ContextResource();
//        resource.setName("system_parameters");
//        resource.setType(App.class.getName());
//        resource.setProperty("oid", "hello jndi");
//        context.getNamingResources().addResource(resource);
        // 从中心服务器读取参数
        byte[] params = readConf(cattle);
        System.out.println("读取配置params=" + params);
        if (params != null) {
            ContextEnvironment ctxEnv = new ContextEnvironment();
            ctxEnv.setName(ConfLoader.CONF_NAME);
            ctxEnv.setType(String.class.getName());
            ctxEnv.setValue(new String(params, ConfLoader.CONF_ENCODING));
            context.getNamingResources().addEnvironment(ctxEnv);
        }

        host.addChild(context);

        // Create a connector that listens on all addresses
        // on port 8888
        Connector connector = embedded.createConnector(
                (String) null, cattle.getPort(), false);

        // Wire up the connector
        embedded.addConnector(connector);
        return embedded;
    }

    private byte[] readConf(Cattle cattle) {
        // LogWriter.info2(logger, "生命周期type=%s", le.getType());
        LogWriter.info2(logger, "开始从应用与资源管理器为应用实例[%s]加载系统参数。", cattle);
        Proxy proxy = CloudUtils.createArmProxy();
        ConfService confSerivce = ServiceFactory.getInstance().getService(ConfService.class, proxy);
        // 发布到指定路径(将上传的文件解压缩)
        InputStream is = confSerivce.readSystemParams(cattle);
        if (is != null) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOHelper.i2o(is, bos, true, true);
            return bos.toByteArray();
        } else {
            LogWriter.warn2(logger, "不能加载应用实例[%s]的系统参数。", cattle);
        }
        return null;
    }

    private Context createContext(Embedded embedded, String path, String docBase) {
        // Create a Context
        Context context = embedded.createContext(path, docBase);
        context.setParentClassLoader(this.getClass().getClassLoader().getParent());
        return context;
    }

    /**
     * 启动一个应用服务器实例。
     * @param cattle
     */
    synchronized public void start() {
        if (service != null) {
            LogWriter.warn2(logger, "服务[%s]已经启动。", service);
            return;
        }
        try {
            Server server = ServerFactory.getServer();
            service = initService();
            server.addService(service);
        } catch (Exception ex) {
            throw new ResourceException("启动服务器时出现异常。", ex);
        }
    }

    /**
     * 启动一头牛
     * @param cattle
     */
    public void startJMX() throws MalformedObjectNameException, InstanceNotFoundException, MBeanException, ReflectionException {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        // ObjectName oname = ObjectName.getInstance("Catalina:type=SERVICE, name=Catalina");
        ObjectName oname = new ObjectName("Catalina:type=Server");


        printMBeanInfo(mbs, oname);

//        Server server = ServerFactory.getServer();
//        Service service = new StandardService();
//        service.setName("Catalina2");
//
//
//        Connector connector = createConnector(service);
//        server.addService(service);

//        ObjectInstance obj = mbs.getObjectInstance(oname);
//        System.out.println("obj.getClassName()=" + obj.getClassName());
//        if (obj instanceof DynamicMBean) {
//            DynamicMBean dmb = (DynamicMBean)obj;
//            System.out.println("dmb.getMBeanInfo()=" + dmb.getMBeanInfo());
//            System.out.println("dmb.getMBeanInfo().getOperations()=" + dmb.getMBeanInfo().getOperations());
//        }
        // mbs.invoke(oname, "addServiced", params, signature)

//        oname = new ObjectName("Catalina:type=Service,serviceName=Catalina");
//        printMBeanInfo(mbs, oname);
//        oname = new ObjectName("Catalina2:type=Service,serviceName=Catalina2");
//        printMBeanInfo(mbs, oname);

        // 创建服务
//        ObjectName beanFactoryName  = new ObjectName("Catalina:type=MBeanFactory");
//        mbs.invoke(beanFactoryName, "createStandardService",
//                new String[]{oname.getCanonicalName(), "uni-name", "Catalina"},
//                new String[]{String.class.getName(), String.class.getName(), String.class.getName()});
    }

    private void printMBeanInfo(MBeanServer mbs,
            ObjectName mbeanObjectName) {
        MBeanInfo info = null;
        try {
            info = mbs.getMBeanInfo(mbeanObjectName);
        } catch (Exception e) {
            System.out.println("!!! Could not get MBeanInfo object for "
                    + mbeanObjectName + " !!!");
            e.printStackTrace();
            return;
        }
        MBeanAttributeInfo[] attrInfo = info.getAttributes();
        if (attrInfo.length > 0) {
            for (int i = 0; i < attrInfo.length; i++) {
                System.out.println(" ** NAME: " + attrInfo[i].getName());
                System.out.println(" DESCR: " + attrInfo[i].getDescription());
                System.out.println(" TYPE: " + attrInfo[i].getType()
                        + "READ: " + attrInfo[i].isReadable()
                        + "WRITE: " + attrInfo[i].isWritable());
            }
        } else {
            System.out.println(" ** No attributes **");
        }
    }

    synchronized public void stop(boolean forcefully) {
        if (service == null) {
            LogWriter.warn(logger, "服务尚未启动。");
        }
        Server server = ServerFactory.getServer();
        server.removeService(service);
    }

    synchronized public void restart() {
        stop(true);
        start();
    }
}
