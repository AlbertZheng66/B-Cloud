
package com.xt.bcloud.td7.impl;

import com.xt.bcloud.comm.CloudUtils;
import com.xt.bcloud.td.Contants;
import com.xt.bcloud.td.http.ErrorFactory;
import com.xt.bcloud.td.http.HttpConstants;
import com.xt.bcloud.td.http.HttpError;
import com.xt.bcloud.td.http.HttpException;
import com.xt.bcloud.td7.*;
import com.xt.core.log.LogWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import org.apache.log4j.Logger;

/**
 *
 * @author Albert
 */
abstract public class AbstractConnector implements Connector {

    protected final Logger logger = Logger.getLogger(this.getClass());
    /**
     * 当前连接器使用的端口
     */
    protected int port = 0;
    /**
     * 停止的标记
     */
    protected volatile boolean stopFlag = false;
    protected Chooser chooser = new DefaultSelector();
    protected String dumperPrefix = "connector";
    
    
    protected String errorDumperPrefix = "connector_error";
    
    /**
     * bindAddr 参数可以在 ServerSocket 的多穴主机 (multi-homed host) 上使用， ServerSocket
     * 仅接受对其地址之一的连接请求。如果 bindAddr 为 null， 则默认接受任何/所有本地地址上的连接。
     */
    protected InetAddress bindAddr;

    public AbstractConnector() {
    }

    public void init() {
        // do nothing
    }

    protected boolean isTimeout(long startTime, SocketAddress socketAddress, Parser requestParser) {
        if (System.currentTimeMillis() - startTime > Contants.upTimeout) {
                        LogWriter.warn2(logger, "客户端[%s]发送的请求[%s]已经超时 %d(ms)。",
                                socketAddress,
                                requestParser.getLastMessage() != null
                                ? ((Request) requestParser.getLastMessage()).getRequestMethod()
                                : "<null>", Contants.upTimeout);
                        // dump 错误请求
                        Message msg = requestParser.getLastMessage();
                        if (msg != null) {
                            DumperFactory.getInstance().write(errorDumperPrefix,
                                    socketAddress,
                                    msg.getOriginalBytes().getBuffers());
                        }
            // 返回“请求超时”错误代码
            throw new HttpException(ErrorFactory.ERROR_408);
        }
        return false;
    }

    protected void writeToClient(SocketChannel clientChannel, Response response) throws IOException {
        LogWriter.info2(logger, "writing the response to the client[%s]", clientChannel.getRemoteAddress());
        // 输出客户端
        DumperFactory.getInstance().write(dumperPrefix + "_writing_to_client_",
                clientChannel.getRemoteAddress(), response.getOriginalBytes().getBuffers());
        // 输出到客户端
        Buffers originalBytes = response.getOriginalBytes();
        if (originalBytes != null) {
            for (int i = 0; i < originalBytes.getBuffers().length; i++) {
                ByteBuffer byteBuffer = originalBytes.getBuffers()[i].duplicate();
                byteBuffer.flip();
                clientChannel.write(byteBuffer);
            }
        }
        clientChannel.shutdownOutput();
    }

    protected void writeException(SocketChannel socketChannel, Throwable ex) {
        StringBuilder strBld = new StringBuilder();
        boolean dealed = false;
        // 输出到客户端
        if (ex instanceof HttpException) {
            HttpException he = (HttpException) ex;
            if (he.getError() != null) {
                HttpError error = he.getError();
                strBld.append(error.getErrorCode()).append(new String(HttpConstants.CRLF)); // 服务器端未知错误
                strBld.append(error.getErrorMessage());
                dealed = true;
            }
        }

        if (!dealed) {
            strBld.append("500").append(HttpConstants.CRLF); // 服务器端未知错误
            strBld.append(ex.getMessage());
        }
        ByteBuffer byteBuffer = CloudUtils.toHeaderBuffer(strBld.toString());
        try {
            socketChannel.write(byteBuffer);
            socketChannel.close();
        } catch (IOException ioe) {
            LogWriter.warn2(logger, ioe, "向客户端[%s]写异常信息时出现错误。", socketChannel);
        } finally {
            BufferFactory.getInstance().dispose(byteBuffer);
        }
    }

    public void stop() {
        this.stopFlag = true;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Chooser getChooser() {
        return chooser;
    }

    public void setChooser(Chooser chooser) {
        this.chooser = chooser;
    }
}
