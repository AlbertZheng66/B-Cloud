package com.xt.bcloud.td7.impl;

import com.xt.bcloud.comm.CloudUtils;
import com.xt.bcloud.td.Contants;
import com.xt.bcloud.td.http.ErrorFactory;
import com.xt.bcloud.td7.Request;
import com.xt.bcloud.td7.Transmitter;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;
import static com.xt.bcloud.td.http.HttpConstants.*;
import com.xt.bcloud.td.http.HttpError;
import com.xt.bcloud.td.http.HttpException;
import com.xt.bcloud.td7.*;
import com.xt.core.log.LogWriter;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 *
 * @author Albert
 */
abstract public class AbstractTransmitter implements Transmitter {

    protected volatile boolean stopFlag = false;
    protected final Logger logger = Logger.getLogger(DefaultSyncTransmitter.class);
    
    protected final String ip;
    protected final int port;
    protected SocketChannel serverSocketChannel;
    
    protected String dumperPrefix = "trasmitter";

    public AbstractTransmitter(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    /**
     * �����ԭʼ������
     *
     * @param request
     * @throws IOException
     */
    protected void writeTo(WritableByteChannel channel, Request request) throws IOException {
        //:IF if need to dump the message, which is sending to the original server
//        FilesocketChannelStream fos = new FilesocketChannelStream("e:\\dump\\test-" + System.currentTimeMillis());
//        TeeWritableByteChannel tos = new TeeWritableByteChannel(channel, fos);

        // 
        // д������
        channel.write(CloudUtils.toHeaderBuffer(request.getMethodName()));
        channel.write(blank());
        channel.write(CloudUtils.toHeaderBuffer(request.getContextPath()));
        channel.write(blank());
        channel.write(CloudUtils.toHeaderBuffer(request.getVersion()));

        // д����ͷ
        for (Iterator<Map.Entry<String, String>> it = request.getHeader().getFields().entrySet().iterator();
                it.hasNext();) {
            Map.Entry<String, String> entry = it.next();
            String key = entry.getKey();
            String value = entry.getValue();

            // ��Ҫ�����ر�����ͨ��
            if ("Connection".equals(key) && "close".equalsIgnoreCase(value)) {
                continue;
            }
            channel.write(CloudUtils.toHeaderBuffer(key));
            channel.write(colon());
            channel.write(CloudUtils.toHeaderBuffer(value));
        }
        channel.write(crlf());  // дһ����Ϣ������Ϣ��֮��Ŀ���

        if (request.getBody() != null) {
            for (ByteBuffer bb : request.getBody().getBuffers()) {
                channel.write(bb);
            }
        }        
    }

    protected boolean isTimeout(long startTime) {
        if (System.currentTimeMillis() - startTime > Contants.downTimeout) {
            // ���ء�����ʱ���������
            HttpError error = ErrorFactory.getInstance().create("408");
            throw new HttpException(error);
        }
        return false;
    }

    public void end() {
        this.stopFlag = true;
    }
}
