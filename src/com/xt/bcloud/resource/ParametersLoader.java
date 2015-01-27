package com.xt.bcloud.resource;

import com.xt.bcloud.app.App;
import com.xt.bcloud.app.AppVersion;
import com.xt.bcloud.comm.CloudUtils;
import com.xt.core.log.LogWriter;
import com.xt.proxy.Proxy;
import com.xt.proxy.ServiceFactory;
import com.xt.gt.sys.SystemConfiguration;
import java.io.BufferedInputStream;
import java.io.InputStream;
import org.apache.catalina.ContainerEvent;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.log4j.Logger;

/**
 * ϵͳ����װ����, �����Զ�װ�ش�"Ӧ�ü���Դ������װ�ز���".
 * @author albert
 */
public class ParametersLoader implements LifecycleListener, ContainerListener {

    private final Logger logger = Logger.getLogger(ParametersLoader.class);
    private boolean initialized = false;
    private final App app;
    private final AppVersion version;

    public ParametersLoader(App app, AppVersion version) {
        this.app = app;
        this.version = version;
    }

    public void lifecycleEvent(LifecycleEvent le) {
        // ��Servlet��ʼ��֮ǰ���ô˷�����
        load(Lifecycle.INIT_EVENT.equals(le.getType()));
    }

    private boolean load(boolean trigger) {
        // ��ֻ֤��ʼ��һ��
        if (initialized) {
            return true;
        }
        // LogWriter.info2(logger, "��������type=%s", le.getType());
        if (trigger) {
            initialized = true;
            LogWriter.info2(logger, "��ʼ��Ӧ������Դ������ΪӦ��[%s](�汾[%s])����ϵͳ������", app, version);
            Proxy proxy = CloudUtils.createArmProxy();
            ConfService confSerivce = ServiceFactory.getInstance().getService(ConfService.class, proxy);
            // ������ָ��·��(���ϴ����ļ���ѹ��)
//            InputStream is = confSerivce.readSystemParams(app, version);
//            if (is != null) {
//                BufferedInputStream bis = new BufferedInputStream(is);
//                // ���Լ��ص�ϵͳ����
//                //FileOutputStream fos = new FileOutputStream("e:\\sys.xml", false);
//                //IOHelper.i2o(is, fos);
////                    TeeInputStream tis = new TeeInputStream(is, fos, false);
////                    fos.close();
//                SystemConfiguration.getInstance().load(bis, false);
//            } else {
//                LogWriter.warn2(logger, "���ܼ���Ӧ��[%s](�汾[%s])��ϵͳ������", app, version);
//            }
        }
        return false;
    }

    public void containerEvent(ContainerEvent ce) {
        load(true);
    }
}
