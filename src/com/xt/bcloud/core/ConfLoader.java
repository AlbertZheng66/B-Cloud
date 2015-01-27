package com.xt.bcloud.core;

import com.xt.core.app.init.SystemLifecycle;
import com.xt.core.exception.SystemException;
import com.xt.core.log.LogWriter;
import com.xt.core.proc.impl.GeneralProcessorFactory;
import com.xt.gt.sys.SystemConfiguration;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.log4j.Logger;

/**
 * 配置加载器，用于加载从“资源和应用管理器”读取的配置信息。
 * @author albert
 */
public class ConfLoader implements SystemLifecycle {

    /**
     * 配置文件采用的编码格式
     */
    public static final String CONF_ENCODING = "UTF-8";
    /**
     * 配置的名称
     */
    public static final String CONF_NAME = SystemConfiguration.getInstance().readString("confLoader.prefix", "system_parameters");
    private final Logger logger = Logger.getLogger(ConfLoader.class);
    /**
     * 上下文的前缀或者路径
     */
    private final String contextPrefix = SystemConfiguration.getInstance().readString("confLoader.prefix", "java:comp/env");
    private final String[] oldLifecycles = SystemConfiguration.getInstance().readStrings("appLifecycles");

    public ConfLoader() {
    }

    public void onInit() {
        LogWriter.info2(logger, "ConfLoader onInit...............");
        try {
            Context ctx = new InitialContext();
            Context envCtx = (Context) ctx.lookup(contextPrefix);

            // 读取的参数
            String params = (String) envCtx.lookup(CONF_NAME);
            if (params != null) {
                ByteArrayInputStream bais = new ByteArrayInputStream(params.getBytes(CONF_ENCODING));
                SystemConfiguration.getInstance().load(bais, false);

                //执行后加载的生命周期函数
                SystemLifecycle[] Lifecycles = SystemConfiguration.getInstance().readObjects("appLifecycles", SystemLifecycle.class);
                for (int i = 0; i < Lifecycles.length; i++) {
                    SystemLifecycle systemLifecycle = Lifecycles[i];
                    if (isNew(systemLifecycle)) {
                        LogWriter.info2(logger, "加载生命周期函数[%s]。", systemLifecycle);
                        systemLifecycle.onInit();
                    }
                }
            }
            // FIXME: 重新加载一次。
            GeneralProcessorFactory.getInstance().onInit();
        } catch (NamingException ex) {
            LogWriter.warn2(logger, ex, "在上下文中读取参数[%s]或者[%s]错误。", contextPrefix, CONF_NAME);
        } catch (UnsupportedEncodingException ex) {
            throw new SystemException(String.format("不支持的字符集[%s]。", CONF_ENCODING), ex);
        }
    }

    private boolean isNew(SystemLifecycle sl) {
        if (sl == null) {
            return false;
        }
        if (sl.getClass() == ConfLoader.class) {
            return false;
        }
        for (int i = 0; i < oldLifecycles.length; i++) {
            String className = oldLifecycles[i];
            if (sl.getClass().getName().equals(className)) {
                return false;
            }
        }
        return true;
    }

    public void onDestroy() {
        SystemLifecycle[] Lifecycles = SystemConfiguration.getInstance().readObjects("appLifecycles", SystemLifecycle.class);
        for (int i = 0; i < Lifecycles.length; i++) {
            SystemLifecycle systemLifecycle = Lifecycles[i];
            if (isNew(systemLifecycle)) {
                systemLifecycle.onDestroy();
            }
        }
    }
}
