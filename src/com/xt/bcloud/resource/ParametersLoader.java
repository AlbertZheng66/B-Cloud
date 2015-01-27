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
 * 系统参数装载器, 用于自动装载从"应用及资源管理器装载参数".
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
        // 在Servlet初始化之前调用此方法。
        load(Lifecycle.INIT_EVENT.equals(le.getType()));
    }

    private boolean load(boolean trigger) {
        // 保证只初始化一次
        if (initialized) {
            return true;
        }
        // LogWriter.info2(logger, "生命周期type=%s", le.getType());
        if (trigger) {
            initialized = true;
            LogWriter.info2(logger, "开始从应用与资源管理器为应用[%s](版本[%s])加载系统参数。", app, version);
            Proxy proxy = CloudUtils.createArmProxy();
            ConfService confSerivce = ServiceFactory.getInstance().getService(ConfService.class, proxy);
            // 发布到指定路径(将上传的文件解压缩)
//            InputStream is = confSerivce.readSystemParams(app, version);
//            if (is != null) {
//                BufferedInputStream bis = new BufferedInputStream(is);
//                // 测试加载的系统参数
//                //FileOutputStream fos = new FileOutputStream("e:\\sys.xml", false);
//                //IOHelper.i2o(is, fos);
////                    TeeInputStream tis = new TeeInputStream(is, fos, false);
////                    fos.close();
//                SystemConfiguration.getInstance().load(bis, false);
//            } else {
//                LogWriter.warn2(logger, "不能加载应用[%s](版本[%s])的系统参数。", app, version);
//            }
        }
        return false;
    }

    public void containerEvent(ContainerEvent ce) {
        load(true);
    }
}
