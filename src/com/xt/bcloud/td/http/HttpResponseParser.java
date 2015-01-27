
package com.xt.bcloud.td.http;

import java.io.ByteArrayOutputStream;

/**
 *
 * @author albert
 */
public class HttpResponseParser extends HttpParser{

//    // �Ƿ������һ��
//    private boolean lastTrunk = false;

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

    public HttpMessage begin() {
        if (response == null) {
            response = new Response();
        }
        return response;
    }

//    @Override
//    protected boolean isEmptyRow(ByteArrayOutputStream _rowContent) {
//        return super.isEmptyRow(_rowContent)
//                // chunked ʵ����"0CRLF" ��β
//                || ("chunked".equals(transferEncoding) && _rowContent.toByteArray().length == 3
//                && _rowContent.toByteArray()[0] == '0' && _rowContent.toByteArray()[1] == CARRAIGE_RETURN);
//    }



    @Override
    public boolean isMessageEnd() {
        return (statusCode != null && (statusCode.startsWith("1")
                || "204".equals(statusCode) || "304".equals(statusCode))
                && isEmptyRow(rowContent)) || super.isMessageEnd(); // TODO:any response to a HEAD request
    }

//    public void processBody(String content) {
//        lastTrunk = (content != null && content.startsWith("0") && content.endsWith(CRLF));
//    }

    public Response end(byte[] bodyBytes, byte[] originalBytes) {
        response.setOriginalMessage(originalBytes);
        response.setMessageBody(bodyBytes);
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

    public static void main(String[] argv) {
        HttpResponseParser hrp = new HttpResponseParser();
        hrp.begin();
        hrp.setStartLine("HTTP/1.1 200 OK");
        System.out.println("statusCode=" + hrp.getStatusCode());
    }
}
