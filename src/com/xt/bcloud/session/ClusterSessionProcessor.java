package com.xt.bcloud.session;

import com.xt.core.log.LogWriter;
import java.lang.reflect.Method;

import com.xt.core.proc.Processor;
import com.xt.core.proc.impl.SessionAware;
import com.xt.core.session.Session;
import com.xt.gt.jt.http.ServletContext;
import com.xt.gt.sys.SystemConfiguration;
import com.xt.proxy.Context;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

/**
 * ʹ�ü�Ⱥģʽ���� Session ��װ�������书����Ҫ����: 1. ��Session���м�Ⱥ��װ; 2. ������� Cookie
 * �м���"clusterSessionId" �����ڼ�Ⱥ��ͬ������ "processingServer"������Sessionճ�ͣ���
 *
 * @author albert
 */
public class ClusterSessionProcessor implements Processor {

    private final Logger logger = Logger.getLogger(ClusterSessionProcessor.class);
    /**
     * �� Cookie �б���ģ����ڼ�Ⱥ������ ClusterID
     */
    public static final String CLUSTER_SESSION_ID_NAME = "clusterSessionId";
    public static final String PROCESSING_SERVER_IN_COOKIE = "processingServer";
    private Session session;

    public ClusterSessionProcessor() {
    }

    public void onCreate(Class serviceClass, Session session, Context context) {
        if (session != null && session.isClustered()) {
            // �Ѿ����˼�Ⱥ���������ظ�����
            this.session = session;
        } else {
            this.session = new EhcacheSession(session, context);
        }
        if (context instanceof ServletContext) {
            ServletContext _context = ((ServletContext) context);
            HttpServletResponse response = _context.getResponse();

            String clusterSessionId = this.session.getId();
            LogWriter.debug2(logger, "cluster Session id=%s", clusterSessionId);

            // �����Ⱥ�ڲ�ʹ�õ�SessionID
            response.addCookie(new Cookie(CLUSTER_SESSION_ID_NAME, this.session.getId()));

            // �ж� Cookie �Ƿ���ڣ���������ڣ�����дCookie��ֵ
            HttpServletRequest request = _context.getRequest();
            if (!isExisted(request)) {
                String localAddr = request.getServerName();
                int localPort = request.getServerPort();
                if (localAddr != null && localPort > 0) {
                    response.addCookie(new Cookie(PROCESSING_SERVER_IN_COOKIE,
                            String.format("%s:%d", localAddr, localPort)));
                }
            }
        }
    }

    private boolean isExisted(HttpServletRequest request) {
        boolean existed = false;
        if (request == null || request.getCookies() == null || request.getCookies().length == 0) {
            return existed;
        }
        for (Cookie cookie : request.getCookies()) {
            if (PROCESSING_SERVER_IN_COOKIE.equals(cookie.getName())) {
                String value = cookie.getValue();
                if (value != null && value.startsWith(request.getServerName())
                        && value.endsWith(String.valueOf(request.getServerPort()))) {
                    existed = true;
                    break;
                }
            }
        }
        return existed;
    }

    public Object[] onBefore(Object service, Method method, Object[] params) {
        if (service instanceof SessionAware) {
            ((SessionAware) service).setSession(session);
        }
        return params;
    }

    public void onAfter(Object service, Object ret) {
    }

    public void onFinally() {
        session = null;
    }

    public void onThrowable(Throwable t) {
    }

    public void onFinish() {
    }
}
