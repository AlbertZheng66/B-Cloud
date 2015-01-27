package com.xt.bcloud.td.impl;

import com.xt.bcloud.td.http.Request;
import com.xt.bcloud.td.http.Response;
import com.xt.bcloud.td.Rewritable;
import com.xt.bcloud.worker.Cattle;

/**
 * 通用的重写器.
 * @author albert
 */
public class GeneralRewriter implements Rewritable {

    private final Rewritable[] rewriters;
    /**
     * 用于快速返回的标识。
     */
    private final boolean norewritableFlag;

    public GeneralRewriter() {
        rewriters = null;
        norewritableFlag = (rewriters == null || rewriters.length == 0);
    }

    public Request rewrite(Cattle cattle, Request req) {
        if (norewritableFlag) {
            return req;
        }
        return req;
    }

    public Response rewrite(Cattle cattle, Request req, Response res) {
        if (norewritableFlag) {
            return res;
        }
        return res;
    }
}
