
package com.xt.bcloud.td7.impl;

import com.xt.bcloud.td7.Buffers;
import com.xt.bcloud.td7.Message;
import com.xt.bcloud.td7.Response;
import com.xt.bcloud.td7.impl.AbstractHttpParser;
import java.io.ByteArrayOutputStream;

/**
 *
 * @author Albert
 */
public class HttpResponseParser  extends AbstractHttpParser{
    
     /**
     * ��Ӧ��
     */
    private String statusCode;

    /**
     * ��ǰ�������Ӧ
     */
    private Response response;

    public HttpResponseParser() {
    }

    public Message begin() {
        if (response == null) {
            response = new Response();
        }
        return response;
    }

    @Override
    public boolean isMessageEnd() {
        return (statusCode != null && (statusCode.startsWith("1")
                || "204".equals(statusCode) || "304".equals(statusCode))
                && isEmptyRow(rowContent)) || super.isMessageEnd(); // TODO:any response to a HEAD request
    }

   public Message end(Buffers headBuffers, Buffers bodyBytes, Buffers originalBytes)  {
        response.setOriginalBytes(originalBytes);
        response.appendBuffers(bodyBytes);
        Response res = this.response;
        response = new Response();
        return res;
    }

    public void setStartLine(String row) {
        response.setStatusLine(row);
        if (row == null) {
            return;
        }
        String[] segs = row.split(" ");
        if (segs.length > 2) {
            statusCode = segs[1].trim();
        }
    }

    /**
     * ���ý������ͷ��Ϣ
     */
    @Override
    public void putHeader(String name, String value) {
        super.putHeader(name, value);
        response.putHeader(name, value);
    }

    /**
     * ׷�ӽ������ͷ��Ϣ
     */
    public void appendHeader(String name, String value) {
        response.appendHeader(name, value);
    }

    public String getStatusCode() {
        return statusCode;
    }
    
}
