package com.xt.bcloud.td7.impl;

import com.xt.bcloud.comm.CloudUtils;
import com.xt.bcloud.td.Contants;
import com.xt.bcloud.td.http.ErrorFactory;
import com.xt.bcloud.td.http.HttpException;
import com.xt.core.log.LogWriter;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;
import static com.xt.bcloud.td.http.HttpConstants.*;
import com.xt.bcloud.td.http.HttpError;
import com.xt.bcloud.td7.BufferFactory;
import com.xt.bcloud.td7.Parser;
import com.xt.bcloud.td7.Request;
import com.xt.bcloud.td7.Response;
import com.xt.bcloud.td7.Transmitter;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 *
 * @author Albert
 */
public class DefaultTransmitter2 implements Transmitter {

    private final Logger logger = Logger.getLogger(DefaultTransmitter2.class);
    private final String ip;
    private final int port;
    private Socket redirectSocket;
    private final Parser parser = new HttpResponseParser();

    public DefaultTransmitter2(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public boolean open() {
        if (redirectSocket == null || redirectSocket.isClosed()) {
            // open or reopen the socket
            try {
                redirectSocket = new Socket();
                SocketAddress remote = new InetSocketAddress(ip, port);
                redirectSocket.connect(remote, Contants.upTimeout);
            } catch (IOException ex) {
                // 抛出特定的异常（如果正好选中的牛已经不可用（宕机），需要重新选择一个特定的牛？）
                LogWriter.warn2(logger, ex, "连接服务器[%s],端口[%d]失败。", ip, port);
                return false;
            }
        }
        return true;
    }

    public Response send(Request request) throws IOException {
        if (request == null) {
            return null;
        }

        writeTo(request);

        InputStream is = redirectSocket.getInputStream();

        long _startTime = System.currentTimeMillis();

        int count = 0;
        Response response = null;
        byte[] bytes = new byte[1024];
        while ((count = is.read(bytes)) > -1) {
            // 如果连接超时，系统将返回
            if (isTimeout(_startTime)) {
                LogWriter.info2(logger, "接收服务端[%s:%d]的响应已经超时[%d(ms)]。",
                        ip, port, Contants.downTimeout);
                break;
            }
            ByteBuffer byteBuffer = BufferFactory.getInstance().wrap(bytes);

//                    // 输出到测试文件
//                    if (stdDumper != null) {
//                        stdDumper.writRes(_b);
//                    }


            // 解析响应消息
            response = (Response) parser.parse(byteBuffer);
            if (response != null) {  // 至少读取一个完整的响应
                // response = (Response) responses.get(0);
                break;
            }
            bytes = new byte[1024];
        }

        if (count < 0 && response == null) {
            // “原始服务器”的网络中断，但是请求尚未读取结束
            throw new HttpException(ErrorFactory.ERROR_500);
        }
        return response;
    }

    protected boolean isTimeout(long startTime) {
        if (System.currentTimeMillis() - startTime > Contants.downTimeout) {

            // dump 错误请求
//                        HttpMessage msg = requestParser.getLastMessage();
//                        if (msg != null) {
//                            errorDumper.writReq(msg.getOriginalMessage());
//                        }
            // 返回“请求超时”错误代码
            HttpError error = ErrorFactory.getInstance().create("408");
            throw new HttpException(error);
        }
        return false;
    }

    private void writeTo(Request request) throws IOException {
        // 发送消息;
        OutputStream os = redirectSocket.getOutputStream();
        System.out.println("socketChanel=" + os);
        try {

            //:IF if need to dump the message, which is sending to the original server
//        FilesocketChannelStream fos = new FilesocketChannelStream("e:\\dump\\test-" + System.currentTimeMillis());
//        TeesocketChannelStream tos = new TeesocketChannelStream(redirectOs, fos);


            // 输出到原始服务器
            // 写请求行
            os.write(CloudUtils.toHeaderBytes(request.getMethodName()));
            os.write(BLANK);
            os.write(CloudUtils.toHeaderBytes(request.getContextPath()));
            os.write(BLANK);
            os.write(CloudUtils.toHeaderBytes(request.getVersion()));

            // 写请求头
            for (Iterator<Map.Entry<String, String>> it = request.getHeader().getFields().entrySet().iterator();
                    it.hasNext();) {
                Map.Entry<String, String> entry = it.next();
                os.write(CloudUtils.toHeaderBytes(entry.getKey()));
                os.write(COLON);
                os.write(CloudUtils.toHeaderBytes(entry.getValue()));
            }
            os.write(CRLF);  // 写一个消息体与消息体之间的空行

            if (request.getBody() != null) {
                for (int i = 0; i < request.getBody().getBuffers().length; i++) {
                    ByteBuffer bb = request.getBody().getBuffers()[i];
                    os.write(bb.array());    
                }
            }
        } finally {
            // 是否需要主动关闭
//            if (socketChannel != null) {
//                socketChannel.close();
//            }
        }
    }

    public void end() {
        try {
            if (redirectSocket != null) {
                redirectSocket.close();
            }
        } catch (IOException ex) {
            LogWriter.warn2(logger, ex, "关闭连接到服务器[%s:%d]的客户端时出现异常。", ip, port);
        }
    }
}
