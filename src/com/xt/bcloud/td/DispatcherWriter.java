package com.xt.bcloud.td;

import com.xt.bcloud.td.http.Response;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import org.apache.log4j.Logger;

/**
 * 将服务内容写到客户端.
 * @author albert
 */
public class DispatcherWriter {

    private final Logger logger = Logger.getLogger(DispatcherWriter.class);

    // 输出缓冲区
    private final ByteBuffer byteBuf = ByteBuffer.allocate(100 * 1024);
    /**
     * 当前正在处理的响应消息
     */
    private Response writingResponse;
    /**
     * 当前输出字节数的索引位置
     */
    private int currentIndex = 0;
    /**
     * 是否结束的标记
     */
    // private boolean finishFlag = false;

    public DispatcherWriter() {
    }

    public void write(SocketChannel channel) throws IOException {
        if (writingResponse == null) {
            logger.warn("当前响应消息不能为空。");
            // finishFlag = true;  // 即使出现问题，也不能影响程序的执行，记录一下错误信息，执行下一个任务。
            currentIndex = 0;
            return;
        }
        byteBuf.clear();
        byte[] msg = writingResponse.getOriginalMessage();
        int capacity = byteBuf.capacity();  // 当前的容量
        int count = currentIndex + capacity >= msg.length ? msg.length - currentIndex : capacity;  // 写入了多少个字节
        byteBuf.put(msg, currentIndex, count);
        byteBuf.flip();
        channel.write(byteBuf);
        if (currentIndex + count >= msg.length) {
            // finishFlag      = true;
            currentIndex = 0;
            writingResponse = null;
        } else {
            currentIndex += count;  // 移动当前字节数
        }
    }

    /**
     * 写的内容是否结束
     */
    public boolean isFinish() {
        return (writingResponse == null /*|| finishFlag*/);
    }

    public void setResponse(Response response) {
        this.writingResponse = response;
    }


}
