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
 * ת��Ӧ�á�
 * @author albert
 */
public class Redirector {

    private Logger logger = Logger.getLogger(Redirector.class);

//    /**
//     * ������Ϣ����(�ӿͻ��˷��͵��������˵�������Ϣ)
//     */
//    private final List<Request> recievingRequests = new ArrayList<Request>(1);
//
//    private List<Response> responses;

    /**
     * �ӷ������˷��Ӹ��ͻ��˵���Ϣ�б�(��֤˳��)
     */
    // private final Map<Request, Response> sendingResponses = new LinkedHashMap<Request, Response>(1);
    /**
     * �����˳��ֵ(ȫ JVM �����ֵ)��
     */
    private static AtomicLong reqSeq = new AtomicLong(0);
    /**
     *
     */
    private final long fileSeq;
    /**
     * ֹͣ���
     */
    private volatile boolean stop = false;
    /**
     * ת���Ƿ���ɵı��
     */
//    private boolean finishedFlag = false;
    
    /**
     * ����ͻ��˶������Ӧ�� Key
     */
    private final SelectionKey clientKey;
    /**
     * ת��ѡ������
     */
    private final Selector redirectorSelector;
    /**
     * ���ڶ�ȡԭʼ��������Ӧ��Ϣ��buffer��
     */
    ByteBuffer readBuf = ByteBuffer.allocate(1024 * 1024);

    /**
     * ���������
     */
    private final HttpRequestParser requestParser;

    /**
     * �ҵ�һ��
     */
    private final CattleManager workerFinder;

    /**
     * ����Ӧ��Ϣд��ͻ���ͨ��
     */
    private final DispatcherWriter dispatcherWriter = new DispatcherWriter();

    /**
     * �Ѿ�������������͵Ķ���
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
     * �ӿͻ��˶�ȡ����
     * @param buf ����ȡ��Buffer���ݣ�ע�⣺��ʱ�Ѿ�Ϊ��ȡ������׼�������Ѿ������ˡ�flip����
     * @return ��ȡ����������true�����򷵻�false��
     */
    public void read(ByteBuffer buf) throws IOException {
        byte[] bytes = new byte[buf.limit()];
        buf.get(bytes);

        // ���ݲ����� HTTP ���󻺴���ļ�
        FileOutputStream output = new FileOutputStream("e:\\req\\req-" + System.currentTimeMillis() + "-" + fileSeq + ".txt", true);
        output.write(bytes);

        List messages = requestParser.parse(bytes);

        if (messages != null && !messages.isEmpty()) {
            logger.info(String.format("���ܵ�����Ϣ�ĸ���[%d]��", messages.size()));
            // �����͵�����
            for (Iterator<Request> it = messages.iterator(); it.hasNext();) {
                Request request = it.next();
                send(request);
            }
        }
    }

    /**
     * ���ͻ���ͨ������ر�ʱ�����ô˷���ȡ�����ڴ�������в�����
     */
    public void cancel() {
    }

    /**
     * ֹͣ�����ִ��
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
            // ���һ���̶�����ҳ����Ӧ����δע�ᣬ���߷����ڴ�Ӧ�õķ�������δ��������
            // ��������ת������Դ��Ӧ�ù�������
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
        // FileOutputStream fos = new FileOutputStream("e:\\test.http"); // ����������ļ�
        // FileChannel channel = fos.getChannel();
        try {
            ByteBuffer buf = ByteBuffer.allocate(1024);
            // д request method
            Task task = (Task) key.attachment();
            if (task == null) {
                logger.warn(String.format("ͨ��[%s]������Ϊ�գ�ȡ����ͨ����", key));
                key.cancel(); // �����Key����¼Warning������������
                return;
            }

            Request request = task.getRequest();

            // TODO: Ӧ�ò����������ʽ�������������ӵ��
            // Ŀǰ�������û��ֱ���޸ġ�ͷ����ֱ��ת��ԭʼ��Ϣ
            buf.clear();
            buf.put(request.getOriginalMessage());
            buf.flip();
            channel.write(buf);

//                CloudUtils.write(buf, request.getRequestMethod(), channel);
//                // дrequest headers
//                for (Iterator<Map.Entry<String, String>> it = request.getHeaders().entrySet().iterator(); it.hasNext();) {
//                    Map.Entry<String, String> requestHeader = it.next();
//                    CloudUtils.write(buf, requestHeader.getKey(), channel);
//                    CloudUtils.writeByte(buf, (byte) 58, channel);  // 58 ��ð�š�:��
//                    CloudUtils.write(buf, requestHeader.getValue(), channel);
//                }
//
//                //TODO д��Ϣ��
//                CloudUtils.write(buf, request.geto, channel)
//
//                // д����������Ϊ��β
//                CloudUtils.write(buf, HttpParser.CRLF, channel);
//                CloudUtils.write(buf, HttpParser.CRLF, channel);
            // �ȴ���ȡ������Ϣ
            // key.interestOps(SelectionKey.OP_READ);
        } catch (IOException e) {
            key.cancel();
            channel.close();
            // finishedFlag = true;

        //TODO �쳣����Ĵ�����Ҫ�ںúÿ���һ��(���������ķ�����ͨ����)
        }
    }

    /**
     * TODO: ����ܳ�ʱ��û��Ӧ��, ϵͳ����ͨ��ת��������������.
     * �ӷ������˶�ȡ����������Ӧ����������ȡ����Ϣ�洢�����Ͷ����С�
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
                    key.cancel(); // �����Key����¼Warning������������
                    return;
                }
                int pos = readBuf.limit();
                byte[] contents = new byte[pos];
                readBuf.get(contents);

                if (contents.length > 0) {
                    // ���ݲ����� HTTP ��Ӧ������ļ�
                    FileOutputStream output = new FileOutputStream("e:\\res\\res-" + System.currentTimeMillis() + "-" + fileSeq + ".txt", true);
                    output.write(contents);
                }

                // ��������
                Response res = task.parse(contents);

                // ������д���ͻ���(�������󶼴��������)
//                if (res != null) {
                    // System.out.println("ת���������ر�ת��ͨ��.............");
                    // finishedFlag = true;

                    // ����ͨ����صĲ���
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
            // TODO: ���Ǵ˴���ͨ���Ƿ��������
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
     * ��������Ϣת�����ͨ��
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
        // TODO: �ڴ˴����г�ʱ����
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
