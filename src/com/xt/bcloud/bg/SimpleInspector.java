package com.xt.bcloud.bg;

import com.xt.comm.quartz.AbstractQuartzService;
import com.xt.core.log.LogWriter;
import com.xt.core.session.LocalSession;
import com.xt.core.session.Session;
import com.xt.gt.sys.SystemConfiguration;
import com.xt.proxy.Context;
import com.xt.proxy.event.Request;
import com.xt.proxy.local.LocalProxy2;
import org.apache.log4j.Logger;

/**
 * ���ü򵥷�ʽ����Ѳ������.��ͨ����ʱ���񣬶�ʱ������Ӧ��Ѳ������
 * @author albert
 */
public class SimpleInspector extends AbstractQuartzService {

    private final Logger logger = Logger.getLogger(SimpleInspector.class);

    private final Inspectable[] inspectables =
            SystemConfiguration.getInstance().readObjects("system.inspectors", Inspectable.class);

    public SimpleInspector() {
    }

    @Override
    public void run() {
        if (inspectables == null || inspectables.length == 0) {
            return;
        }
        for (int i = 0; i < inspectables.length; i++) {
            Inspectable inspectable = inspectables[i];
            LogWriter.info2(logger, "����Ѳ������[%s]......", inspectable);
            if (inspectable == null) {
                continue;
            }
            try {
                // inspectable.excecute(persistenceManager);
                _execute(inspectable);
            } catch (Exception ignored) {
                logger.warn("Ѳ������׳��쳣��", ignored);
            }
        }
    }

    private void _execute(Inspectable inspectable) {
        Request request = new Request();
        request.setMethodName("excecute");
        request.setParamTypes(new Class[0]);
        request.setParams(new Object[0]);
        request.setService(inspectable.getClass());
        Context context = new InspectorContext();
        LocalProxy2 localProxy2 = new LocalProxy2();
        Session session = LocalSession.getInstance();
        localProxy2.setSession(session);
        try {
            localProxy2.invoke(request, context);
        } catch (Throwable ignored) {
            logger.warn("Ѳ������׳��쳣��", ignored);
        }
    }
}
