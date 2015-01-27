
package com.xt.bcloud.td;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 *
 * @author albert
 */
public class RedirectorThread6 implements Runnable {

    /**
     * 停止标记
     */
    private volatile boolean stop = false;

    /**
     * 转发选择器。
     */
    private final Selector redirectorSelector;

    private final Selector clientSelector;

    public RedirectorThread6(final Selector clientSelector, final Selector redirectorSelector) {
        this.redirectorSelector = redirectorSelector;
        this.clientSelector     = clientSelector;
    }

    public void run() {
        try {
            while (!stop) {
                redirectorSelector.select(10);
                Iterator selectedKeys = redirectorSelector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    SelectionKey key = (SelectionKey) selectedKeys.next();
                    selectedKeys.remove();
                    if (!key.isValid()) {
                        continue;
                    }

                    // Check what event is available and deal with it
                    if (key.isConnectable()) {
                        finishConnection(key);
                    } else if (key.isReadable()) {
                        this.readFromServer(key);
                    } else if (key.isWritable()) {
                        this.writeToServer(key);
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

     private void finishConnection(SelectionKey key) throws IOException {
        System.out.println("connect................................");
        SocketChannel socketChannel = (SocketChannel) key.channel();

        // Finish the connection. If the connection operation failed
        // this will raise an IOException.
        try {
            socketChannel.finishConnect();
        } catch (IOException e) {
            // Cancel the channel's registration with our selector
            System.out.println(e);
            key.cancel();
            return;
        }
        
        // Register an interest in writing on this channel
        key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
    }

    private void writeToServer(SelectionKey key) throws IOException {
        Task task = (Task)key.attachment();
        task.getRedirector().writeToOriginServer(key);
    }

    /**
     * TODO: 如果很长时间没有应答, 系统将此通道转发给其他服务器.
     * 从服务器端读取处理结果（响应），并将读取的信息存储到发送队列中。
     * @param key
     * @throws java.io.IOException
     */
    private void readFromServer(SelectionKey key) throws IOException {
         Task task = (Task)key.attachment();
         task.getRedirector().readFromOriginServer(key);
         if (task.getRedirector().isWakeup()) {
             this.clientSelector.wakeup();
         }
    }
}
