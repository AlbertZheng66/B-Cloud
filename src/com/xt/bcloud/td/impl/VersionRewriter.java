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
// 如果已经以应用的“上下文”开头（如：JS 和 CSS 文件的情况），不需要重新指定
        if (oldContextPath != null
                && oldContextPath.startsWith(contextPath)) {
            LogWriter.debug2(logger, "上下文[%s]不需要调整。", oldContextPath);
            return req;
        }


        // 重新指定 JS
        StringBuilder newContextPath = new StringBuilder(contextPath);
        if (StringUtils.isEmpty(oldContextPath)) {
            if (newContextPath.charAt(newContextPath.length() - 1) != '/') {
                newContextPath.append("/");
            }
        } else {
            newContextPath.append(oldContextPath);
        }

        req.setContextPath(newContextPath.toString());
        LogWriter.debug2(logger, "将上下文[%s]改写为[%s]。", oldContextPath, req.getContextPath());
        return req;
    }

    private String createContextPath(Cattle cattle) {
        App app = cattle.getApp();
        AppVersion version = cattle.getAppVersion();
        if (app == null || version == null) {
            LogWriter.warn2(logger, "错误的服务器实例[%s]，其关联的应用及其版本都不能为空。", cattle);
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
