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
    private String resourceBase;  // Ӧ�õķ���·��
    private Logger logger = Logger.getLogger(TomcatServerController.class);
    private Service service;

    public TomcatServerController() {
    }

    /**
     * ��ʼ��������Ϣ
     * @param cattle
     * @param resourceBase
     */
    public void init(Cattle cattle, String resourceBase) {
        if (cattle == null || StringUtils.isEmpty(resourceBase)) {
            throw new BadParameterException("����ʵ������Դ·��������Ϊ�ա�");
        }
        App app = cattle.getApp();
        AppVersion version = cattle.getAppVersion();
        if (app == null || version == null) {
            throw new BadParameterException("����ʵ����Ӧ�úͰ汾������Ϊ�ա�");
        }
        engineName = String.format("Catalina-%s-%d", app.getOid(), System.currentTimeMillis());
        hostName   = String.format("Host-%s-%d", app.getOid(), System.currentTimeMillis());
        // ����汾�������Ĳ�Ϊ�գ���ʹ�ð汾�������ģ�����ʹ��Ӧ�õ������ģ�
        // (ContextPath����ʽ�ǣ��ԡ�/����ͷ�������ԡ�/����β�����򷢲����ܷ��ʡ����磺"/gt_demo")
        this.contextPath = cattle.getContextPath();

        this.cattle = cattle;
        this.resourceBase = resourceBase;
    }

    private Service initService() throws Exception {
        logger.info(String.format("��������������Ϣ���£�engineName=%s; hostName=%s;"
                + " resourceBase=%s; contextPath=%s; port=%d;",
                engineName, hostName, resourceBase, contextPath, cattle.getPort()));

        // Embedded embedded = (Embedded)(Class.forName(Embedded.class.getName(), true, getClass().getClassLoader().getParent())).newInstance(); // new Embedded();
        Embedded embedded = new Embedded();
        //ע�⣺��Ҫʹ��Servlet��һ���������������ǰҲ��һ��WebappClassLoader�����������Ҫʹ���ϲ��������(org.apache.catalina.loader.StandardClassLoader)�����������ռ�����⣬��
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
        // ����һ��װ��ϵͳ�����ļ�����
//        ParametersLoader pLoader = new ParametersLoader(cattle.getApp(), cattle.getAppVersion());
//        context.addContainerListener(pLoader);
//        ContextResource resource = new ContextResource();
//        resource.setName("system_parameters");
//        resource.setType(App.class.getName());
//        resource.setProperty("oid", "hello jndi");
//        context.getNamingResources().addResource(resource);
        // �����ķ�������ȡ����
        byte[] params = readConf(cattle);
        System.out.println("��ȡ����params=" + params);
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
        // LogWriter.info2(logger, "��������type=%s", le.getType());
        LogWriter.info2(logger, "��ʼ��Ӧ������Դ������ΪӦ��ʵ��[%s]����ϵͳ������", cattle);
        Proxy proxy = CloudUtils.createArmProxy();
        ConfService confSerivce = ServiceFactory.getInstance().getService(ConfService.class, proxy);
        // ������ָ��·��(���ϴ����ļ���ѹ��)
        InputStream is = confSerivce.readSystemParams(cattle);
        if (is != null) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOHelper.i2o(is, bos, true, true);
            return bos.toByteArray();
        } else {
            LogWriter.warn2(logger, "���ܼ���Ӧ��ʵ��[%s]��ϵͳ������", cattle);
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
     * ����һ��Ӧ�÷�����ʵ����
     * @param cattle
     */
    synchronized public void start() {
        if (service != null) {
            LogWriter.warn2(logger, "����[%s]�Ѿ�������", service);
            return;
        }
        try {
            Server server = ServerFactory.getServer();
            service = initService();
            server.addService(service);
        } catch (Exception ex) {
            throw new ResourceException("����������ʱ�����쳣��", ex);
        }
    }

    /**
     * ����һͷţ
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

        // ��������
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
            LogWriter.warn(logger, "������δ������");
        }
        Server server = ServerFactory.getServer();
        server.removeService(service);
    }

    synchronized public void restart() {
        stop(true);
        start();
    }
}
