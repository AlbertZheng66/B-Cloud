/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xt.bcloud.td7;

import com.xt.bcloud.worker.Cattle;

/**
 * 对请求和响应进行改写（比如：重新修改请求行，重新调整 Cookie）。
 * 注意：这个接口为多线程共用，因此需要保证其线程安全。
 * @author albert
 */
public interface Rewritable {

    /**
     * 改写请求。
     * @param req  请求实例,不为空.
     * @return  修改后的响应实例,如果返回空,则认为请求未作修改.
     */
    public Request rewrite(Cattle cattle, Request req);

    /**
     * 重新改写响应.
     * @param req 请求实例,不为空.
     * @param res 响应实例,不为空.
     * @return 修改后的响应实例,如果返回空,则认为响应未作修改.
     */
    public Response rewrite(Cattle cattle, Request req, Response res);
}

