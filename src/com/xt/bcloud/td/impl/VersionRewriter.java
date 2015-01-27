package com.xt.bcloud.td.impl;

import com.xt.bcloud.app.App;
import com.xt.bcloud.app.AppVersion;
import com.xt.bcloud.td.http.Request;
import com.xt.bcloud.td.http.Response;
import com.xt.bcloud.td.Rewritable;
import com.xt.bcloud.worker.Cattle;
import com.xt.core.log.LogWriter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author albert
 */
public class VersionRewriter implements Rewritable {

    private final Logger logger = Logger.getLogger(VersionRewriter.class);

    public VersionRewriter() {
    }

    public Request rewrite(Cattle cattle, Request req) {
        if (cattle == null || req == null) {
            return req;
        }

        String oldContextPath = req.getContextPath();
        String contextPath = createContextPath(cattle);
        contextPath = contextPath == null ? "" : contextPath;
// ����Ѿ���Ӧ�õġ������ġ���ͷ���磺JS �� CSS �ļ��������������Ҫ����ָ��
        if (oldContextPath != null
                && oldContextPath.startsWith(contextPath)) {
            LogWriter.debug2(logger, "������[%s]����Ҫ������", oldContextPath);
            return req;
        }


        // ����ָ�� JS
        StringBuilder newContextPath = new StringBuilder(contextPath);
        if (StringUtils.isEmpty(oldContextPath)) {
            if (newContextPath.charAt(newContextPath.length() - 1) != '/') {
                newContextPath.append("/");
            }
        } else {
            newContextPath.append(oldContextPath);
        }

        req.setContextPath(newContextPath.toString());
        LogWriter.debug2(logger, "��������[%s]��дΪ[%s]��", oldContextPath, req.getContextPath());
        return req;
    }

    private String createContextPath(Cattle cattle) {
        App app = cattle.getApp();
        AppVersion version = cattle.getAppVersion();
        if (app == null || version == null) {
            LogWriter.warn2(logger, "����ķ�����ʵ��[%s]���������Ӧ�ü���汾������Ϊ�ա�", cattle);
            return "";
        }
        if (StringUtils.isNotEmpty(version.getContextPath())) {
            return version.getContextPath();
        }
        return app.getContextPath();
    }

    public Response rewrite(Cattle cattle, Request req, Response res) {
        return res;
    }
}
