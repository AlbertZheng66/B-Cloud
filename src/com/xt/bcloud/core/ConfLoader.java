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
 * ���ü����������ڼ��شӡ���Դ��Ӧ�ù���������ȡ��������Ϣ��
 * @author albert
 */
public class ConfLoader implements SystemLifecycle {

    /**
     * �����ļ����õı����ʽ
     */
    public static final String CONF_ENCODING = "UTF-8";
    /**
     * ���õ�����
     */
    public static final String CONF_NAME = SystemConfiguration.getInstance().readString("confLoader.prefix", "system_parameters");
    private final Logger logger = Logger.getLogger(ConfLoader.class);
    /**
     * �����ĵ�ǰ׺����·��
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

            // ��ȡ�Ĳ���
            String params = (String) envCtx.lookup(CONF_NAME);
            if (params != null) {
                ByteArrayInputStream bais = new ByteArrayInputStream(params.getBytes(CONF_ENCODING));
                SystemConfiguration.getInstance().load(bais, false);

                //ִ�к���ص��������ں���
                SystemLifecycle[] Lifecycles = SystemConfiguration.getInstance().readObjects("appLifecycles", SystemLifecycle.class);
                for (int i = 0; i < Lifecycles.length; i++) {
                    SystemLifecycle systemLifecycle = Lifecycles[i];
                    if (isNew(systemLifecycle)) {
                        LogWriter.info2(logger, "�����������ں���[%s]��", systemLifecycle);
                        systemLifecycle.onInit();
                    }
                }
            }
            // FIXME: ���¼���һ�Ρ�
            GeneralProcessorFactory.getInstance().onInit();
        } catch (NamingException ex) {
            LogWriter.warn2(logger, ex, "���������ж�ȡ����[%s]����[%s]����", contextPrefix, CONF_NAME);
        } catch (UnsupportedEncodingException ex) {
            throw new SystemException(String.format("��֧�ֵ��ַ���[%s]��", CONF_ENCODING), ex);
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
