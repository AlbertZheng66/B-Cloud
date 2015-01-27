package com.xt.bcloud.td;

import com.xt.bcloud.td.http.Response;
import com.xt.bcloud.td.http.Request;
import com.xt.bcloud.td.http.HttpRequestParser;
import com.xt.bcloud.worker.Cattle;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.log4j.Logger;

/**
 * 转发应用。
 * @author albert
 */
public class Redirector {

    private Logger logger = Logger.getLogger(Redirector.class);

//    /**
//     * 接受消息队列(从客户端发送到服务器端的请求消息)
//     */
//    private final List<Request> recievingRequests = new ArrayList<Request>(1);
//
//    private List<Response> responses;

    /**
     * 从服务器端发挥给客户端的消息列表。(保证顺序)
     */
    // private final Map<Request, Response> sendingResponses = new LinkedHashMap<Request, Response>(1);
    /**
     * 请求的顺序值(全 JVM 排序的值)。
     */
    private static AtomicLong reqSeq = new AtomicLong(0);
    /**
     *
     */
    private final long fileSeq;
    /**
     * 停止标记
     */
    private volatile boolean stop = false;
    /**
     * 转发是否完成的标记
     */
//    private boolean finishedFlag = false;
    
    /**
     * 处理客户端端请求对应的 Key
     */
    private final SelectionKey clientKey;
    /**
     * 转发选择器。
     */
    private final Selector redirectorSelector;
    /**
     * 用于读取原始服务器响应信息的buffer。
     */
    ByteBuffer readBuf = ByteBuffer.allocate(1024 * 1024);

    /**
     * 请求解析器
     */
    private final HttpRequestParser requestParser;

    /**
     * 找到一个
     */
    private final CattleManager workerFinder;

    /**
     * 将相应消息写入客户端通道
     */
    private final DispatcherWriter dispatcherWriter = new DispatcherWriter();

    /**
     * 已经处理结束待发送的队列
     */
    private Queue<Task> processingTask = new ConcurrentLinkedQueue();

    public Redirector(SelectionKey clientKey, Selector redirectorSelector,
            final CattleManager taskDispatcherChannel) {
        this.clientKey = clientKey;
        this.redirectorSelector = redirectorSelector;
        this.workerFinder = taskDispatcherChannel;
        requestParser = new HttpRequestParser();
        fileSeq = reqSeq.incrementAndGet();
    }

    /**
     * 从客户端读取请求。
     * @param buf 待读取的Buffer数据（注意：此时已经为读取做好了准备，即已经进行了“flip”）
     * @return 读取结束，返回true，否则返回false。
     */
    public void read(ByteBuffer buf) throws IOException {
        byte[] bytes = new byte[buf.limit()];
        buf.get(bytes);

        // 根据参数将 HTTP 请求缓存成文件
        FileOutputStream output = new FileOutputStream("e:\\req\\req-" + System.currentTimeMillis() + "-" + fileSeq + ".txt", true);
        output.write(bytes);

        List messages = requestParser.parse(bytes);

        if (messages != null && !messages.isEmpty()) {
            logger.info(String.format("接受到的消息的个数[%d]。", messages.size()));
            // 处理发送的请求
            for (Iterator<Request> it = messages.iterator(); it.hasNext();) {
                Request request = it.next();
                send(request);
            }
        }
    }

    /**
     * 当客户端通道意外关闭时，调用此方法取消正在处理的所有操作。
     */
    public void cancel() {
    }

    /**
     * 停止软件的执行
     */
    public void stop() {
        this.stop = true;
    }

    private void send(Request request) throws IOException {
        if (request == null) {
            return;
        }
        Task task = new Task(request, this);
        Cattle cattle = workerFinder.findCattle(request, null);
        if (cattle == null) {
            // 输出一个固定的网页（此应用尚未注册，或者服务于此应用的服务器尚未启动）。
            // 将此请求转发给资源和应用管理器。
            return;
        }
        SocketChannel channel = ServerChannelFactory.getInstance().register(cattle.getIp(), cattle.getPort(),
                redirectorSelector, task);

        this.processingTask.add(task);

        redirectorSelector.wakeup();
    }

    public void writeToOriginServer(SelectionKey key) throws IOException {
        System.out.println("writeToServer................................");

        SocketChannel channel = (SocketChannel) key.channel();
        // FileOutputStream fos = new FileOutputStream("e:\\test.http"); // 输出到测试文件
        // FileChannel channel = fos.getChannel();
        try {
            ByteBuffer buf = ByteBuffer.allocate(1024);
            // 写 request method
            Task task = (Task) key.attachment();
            if (task == null) {
                logger.warn(String.format("通道[%s]的任务为空，取消此通道。", key));
                key.cancel(); // 错误的Key（记录Warning，不继续处理）
                return;
            }

            Request request = task.getRequest();

            // TODO: 应该采用逐步输出方式，避免服务器端拥塞
            // 目前的情况是没有直接修改“头”，直接转发原始消息
            buf.clear();
            buf.put(request.getOriginalMessage());
            buf.flip();
            channel.write(buf);

//                CloudUtils.write(buf, request.getRequestMethod(), channel);
//                // 写request headers
//                for (Iterator<Map.Entry<String, String>> it = request.getHeaders().entrySet().iterator(); it.hasNext();) {
//                    Map.Entry<String, String> requestHeader = it.next();
//                    CloudUtils.write(buf, requestHeader.getKey(), channel);
//                    CloudUtils.writeByte(buf, (byte) 58, channel);  // 58 即冒号“:”
//                    CloudUtils.write(buf, requestHeader.getValue(), channel);
//                }
//
//                //TODO 写消息体
//                CloudUtils.write(buf, request.geto, channel)
//
//                // 写两个空行作为结尾
//                CloudUtils.write(buf, HttpParser.CRLF, channel);
//                CloudUtils.write(buf, HttpParser.CRLF, channel);
            // 等待读取请求信息
            // key.interestOps(SelectionKey.OP_READ);
        } catch (IOException e) {
            key.cancel();
            channel.close();
            // finishedFlag = true;

        //TODO 异常情况的处理需要在好好考虑一下(开启其他的服务器通道？)
        }
    }

    /**
     * TODO: 如果很长时间没有应答, 系统将此通道转发给其他服务器.
     * 从服务器端读取处理结果（响应），并将读取的信息存储到发送队列中。
     * @param key
     * @throws java.io.IOException
     */
    public void readFromOriginServer(SelectionKey key) throws IOException {
        System.out.println("read http response from origin server ");
        //
        SocketChannel channel = (SocketChannel) key.channel();

        readBuf.clear();
        // Attempt to read off the channel
        int numRead = 0;
        try {
            numRead = channel.read(readBuf);
            readBuf.flip();
            if (numRead > 0) {
                System.out.println("readFromServer.......numRead=" + numRead);
                Task task = (Task) key.attachment();
                if (task == null) {
                    key.cancel(); // 错误的Key（记录Warning，不继续处理）
                    return;
                }
                int pos = readBuf.limit();
                byte[] contents = new byte[pos];
                readBuf.get(contents);

                if (contents.length > 0) {
                    // 根据参数将 HTTP 响应缓存成文件
                    FileOutputStream output = new FileOutputStream("e:\\res\\res-" + System.currentTimeMillis() + "-" + fileSeq + ".txt", true);
                    output.write(contents);
                }

                // 解析内容
                Response res = task.parse(contents);

                // 将数据写到客户端(所有请求都处理结束后)
//                if (res != null) {
                    // System.out.println("转发结束，关闭转发通道.............");
                    // finishedFlag = true;

                    // 结束通道相关的操作
//                    key.attach(null);
//                    key.cancel();
//                    channel.socket().close();
//                    channel.close();

                    //
                    // clientKey.interestOps(SelectionKey.OP_WRITE);
                    // clientKey.selector().wakeup();
//                }
            }
        } catch (IOException e) {
            // The remote forcibly closed the connection, cancel
            // the selection key and close the channel.
            // TODO: 考虑此处的通道是否可以重用
            key.cancel();
            channel.close();
            // finishedFlag = true;
            return;
        }

        if (numRead < 0) {
            // Remote entity shut the socket down cleanly. Do the
            // same from our end and cancel the channel.
            stop = true;

            key.channel().close();
            channel.close();
            return;
        }
    }

//    public List<Request> getRecievingRequests() {
//        return recievingRequests;
//    }

//    public Map<Request, Response> getSendingResponses() {
//        return sendingResponses;
//    }

    /**
     * 将返回消息转向输出通道
     * @param channel
     */
    public void writeToClient (SocketChannel channel) throws IOException {
        if (channel == null) {
            return;
        }
        if (!dispatcherWriter.isFinish()) {
            dispatcherWriter.write(channel);
        } else {
            Response res = getProcessingResponse();
            if (res != null) {
                dispatcherWriter.setResponse(res);
                dispatcherWriter.write(channel);
            }
        }
    }

    private Response getProcessingResponse() {
        if (processingTask.isEmpty()) {
            return null;
        }
        Task task = processingTask.peek();
        // TODO: 在此处进行超时处理
        if (task != null && task.getRequest() != null && task.getResponse() != null) {
            return processingTask.poll().getResponse();
        }
        return null;
    }

    public boolean isWakeup () {
        if (processingTask.isEmpty()) {
            return false;
        }
        Task task = processingTask.peek();
        if (task != null && task.getResponse() != null) {
            return (task.getResponse() != null);
        }
        return false;
    }
}
