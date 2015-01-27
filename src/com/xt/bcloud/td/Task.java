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
     * ����ʱ�䣨��ʱʱ��Ҫ����ɾ����
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
     * ������Ӧ��Ϣ
     * @param bytes
     * @return
     */
    public Response parse(byte[] bytes) {
        try {
            List<HttpMessage> msgs = responseParser.parse(bytes);

            // ���÷ǡ�Pipelined Connections��ʽ�ķ�ʽ������������ÿ��һ������
            // TODO: �Ժ�ɿ��ǲ���Pipelined ������������
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
//     * ���������Ƿ������״̬
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

