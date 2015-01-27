package com.xt.bcloud.td.http;


/**
 *
 * @author albert
 */
public class HttpRequestParser extends HttpParser {

    /**
     * 当前处理的请求
     */
    private Request request;

    public HttpRequestParser() {
        super();
    }

    public HttpMessage begin() {
        if (request == null) {
            request = new Request();
        }
        return request;
    }

    @Override
    public boolean isMessageEnd() {
        return (isEmptyRow(rowContent) && (rowIndex > 0) && (prevRowEmpty || request.getContentLength() <= 0))
                || super.isMessageEnd();
    }


    public Request end(byte[] bodyBytes, byte[] originalBytes) {
        request.setOriginalMessage(originalBytes);
        request.setMessageBody(bodyBytes);
        Request req = this.request;
        request = new Request();
        return req;
    }

    public void setStartLine(String row) {
        request.setRequestMethod(row);
    }

    /**
     * 放置解析后的头信息
     */
    @Override
    public void putHeader(String name, String value) {
        super.putHeader(name, value);
        request.putHeader(name, value);
    }

    /**
     * 追加解析后的头信息
     */
    public void appendHeader(String name, String value) {
        request.appendHeader(name, value);
    }
    
}
