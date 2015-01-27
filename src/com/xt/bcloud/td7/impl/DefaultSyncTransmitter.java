package com.xt.bcloud.td7.impl;

import com.xt.bcloud.td.Contants;
import com.xt.bcloud.td.http.ErrorFactory;
import com.xt.bcloud.td.http.HttpException;
import com.xt.core.log.LogWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import com.xt.bcloud.td7.*;
import com.xt.bcloud.test.Utils;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Albert
 */
public class DefaultSyncTransmitter extends AbstractTransmitter implements SyncTransmitter {

    public DefaultSyncTransmitter(String ip, int port) {
        super(ip, port);
    }

    public boolean open() {
        if (serverSocketChannel == null || !serverSocketChannel.isConnected()) {
            // open or reopen the socket
            try {
                SocketAddress remote = new InetSocketAddress(ip, port);
                serverSocketChannel = SocketChannel.open(remote);
                serverSocketChannel.configureBlocking(true);
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

        writeTo(serverSocketChannel, request);
        serverSocketChannel.shutdownOutput();  // 发送EOF字符

        long _startTime = System.currentTimeMillis();

        int count = 0;
        Response response = null;
        final List<ByteBuffer> recycledBuffers = new ArrayList();  //FIXME: 什么时候回收比较合适呢？
        ByteBuffer byteBuffer = createByteBuffer(recycledBuffers);
        
        final Parser parser = new HttpResponseParser();
        while ((count = serverSocketChannel.read(byteBuffer)) > -1) {
            // 如果连接超时，系统将返回
            if (isTimeout(_startTime)) {
                LogWriter.info2(logger, "接收服务端[%s:%d]的响应已经超时[%d(ms)]。",
                        ip, port, Contants.downTimeout);
                break;
            }
            
            // 输出到测试文件
            DumperFactory.getInstance().write(dumperPrefix + "_reading_from_server_",
                    serverSocketChannel.getRemoteAddress(), byteBuffer);

            // 解析响应消息
            response = (Response) parser.parse(byteBuffer);
            if (parser.finished()) {  // 至少读取一个完整的响应
                break;
            }
            // 重新创建Buffer
            byteBuffer = createByteBuffer(recycledBuffers);
        }


        if (count < 0 && response == null) {
            // “原始服务器”的网络中断，但是请求尚未读取结束
            throw new HttpException(ErrorFactory.ERROR_500);
        }
        return response;
    }

    private ByteBuffer createByteBuffer(final List<ByteBuffer> recycledBuffers) {
        ByteBuffer byteBuffer = BufferFactory.getInstance().allocate();
        recycledBuffers.add(byteBuffer);
        return byteBuffer;
    }

    @Override
    public void end() {
        try {
            if (serverSocketChannel != null) {
                serverSocketChannel.close();
            }
        } catch (IOException ex) {
            LogWriter.warn2(logger, ex, "关闭连接到服务器[%s:%d]的客户端时出现异常。", ip, port);
        }
    }
}
