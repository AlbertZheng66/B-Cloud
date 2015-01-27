package com.xt.bcloud.td;

import com.xt.bcloud.td.http.Response;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import org.apache.log4j.Logger;

/**
 * ����������д���ͻ���.
 * @author albert
 */
public class DispatcherWriter {

    private final Logger logger = Logger.getLogger(DispatcherWriter.class);

    // ���������
    private final ByteBuffer byteBuf = ByteBuffer.allocate(100 * 1024);
    /**
     * ��ǰ���ڴ������Ӧ��Ϣ
     */
    private Response writingResponse;
    /**
     * ��ǰ����ֽ���������λ��
     */
    private int currentIndex = 0;
    /**
     * �Ƿ�����ı��
     */
    // private boolean finishFlag = false;

    public DispatcherWriter() {
    }

    public void write(SocketChannel channel) throws IOException {
        if (writingResponse == null) {
            logger.warn("��ǰ��Ӧ��Ϣ����Ϊ�ա�");
            // finishFlag = true;  // ��ʹ�������⣬Ҳ����Ӱ������ִ�У���¼һ�´�����Ϣ��ִ����һ������
            currentIndex = 0;
            return;
        }
        byteBuf.clear();
        byte[] msg = writingResponse.getOriginalMessage();
        int capacity = byteBuf.capacity();  // ��ǰ������
        int count = currentIndex + capacity >= msg.length ? msg.length - currentIndex : capacity;  // д���˶��ٸ��ֽ�
        byteBuf.put(msg, currentIndex, count);
        byteBuf.flip();
        channel.write(byteBuf);
        if (currentIndex + count >= msg.length) {
            // finishFlag      = true;
            currentIndex = 0;
            writingResponse = null;
        } else {
            currentIndex += count;  // �ƶ���ǰ�ֽ���
        }
    }

    /**
     * д�������Ƿ����
     */
    public boolean isFinish() {
        return (writingResponse == null /*|| finishFlag*/);
    }

    public void setResponse(Response response) {
        this.writingResponse = response;
    }


}
