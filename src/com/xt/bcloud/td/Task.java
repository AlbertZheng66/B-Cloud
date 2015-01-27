package com.xt.bcloud.td;

import com.xt.bcloud.td.http.Response;
import com.xt.bcloud.td.http.Request;
import com.xt.bcloud.td.http.HttpResponseParser;
import com.xt.bcloud.td.http.HttpMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author albert
 */
public class Task {

    /**
     * 创建时间（超时时需要将其删除）
     */
    private long createdTime = System.nanoTime();

    private final Request request;

    private Response response;

    private final HttpResponseParser responseParser;

    private final Redirector redirector;

    public Task(Request request, Redirector redirector) {
        this.request = request;
        this.redirector = redirector;
        responseParser = new HttpResponseParser();
    }

    /**
     * 解析相应消息
     * @param bytes
     * @return
     */
    public Response parse(byte[] bytes) {
        try {
            List<HttpMessage> msgs = responseParser.parse(bytes);

            // 采用非“Pipelined Connections”式的方式处理任务，所以每个一个连接
            // TODO: 以后可考虑采用Pipelined 方法发送请求
            for (HttpMessage msg : msgs) {
                if (msg instanceof Response) {
                    this.response = (Response) msg;
                    return this.response;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Task.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

//    /**
//     * 返回任务是否结束的状态
//     * @return
//     */
//    public boolean isFinished() {
//        return responseParser == null ? true : responseParser.isFinished();
//    }

    public Redirector getRedirector() {
        return redirector;
    }

    public Request getRequest() {
        return request;
    }

    public Response getResponse() {
        return response;
    }
}

