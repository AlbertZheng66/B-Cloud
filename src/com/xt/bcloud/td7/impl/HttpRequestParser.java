
package com.xt.bcloud.td7.impl;

import com.xt.bcloud.td7.Buffers;
import com.xt.bcloud.td7.Message;
import com.xt.bcloud.td7.Request;

/**
 *
 * @author Albert
 */
public class HttpRequestParser extends AbstractHttpParser {
    
    private Request request;

    public HttpRequestParser() {
        super();
    }

    @Override
    public Message begin() {
        request = new Request();
        return request;
    }

    @Override
    public Message end(Buffers headBuffers, Buffers bodyBytes, Buffers originalBytes) {
        request.setOriginalBytes(originalBytes);
        request.getHeader().setOriginalHeader(headBuffers);
        request.appendBuffers(bodyBytes);
        request = new Request();
        return request;
    }

    @Override
    void setStartLine(String row) {
        request.setRequestMethod(row);;
    }

    @Override
    void appendHeader(String name, String value) {
        request.appendHeader(name, value);
    }
    
    @Override
    public void putHeader(String name, String value) {
        super.putHeader(name, value);
        request.putHeader(name, value);
    }
    
}
