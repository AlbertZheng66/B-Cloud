package com.xt.bcloud.td7.impl;

import com.xt.bcloud.td.http.ErrorFactory;
import com.xt.bcloud.td.http.HttpException;
import com.xt.bcloud.td7.*;
import com.xt.core.log.LogWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author Albert
 */
public class DefaultAsyncTransmitter2 extends AbstractTransmitter implements AsyncTransmitter, Runnable {

    private final Thread asyncTransmitterThread;
    /**
     * ��ǰ������Ĺ����б�
     */
    private final List<Worker> workList = Collections.synchronizedList(new ArrayList());
    /**
     * ��ʼ������������
     */
    private final int initConnCount = 5;
    private final SocketAddress serverAddress;
    /**
     * ���������������
     */
    private final int maxConnCount = 15;
    /**
     * ���ڷ������ݵ�ͨ��
     */
    private final List<WorkThread> workingWorkers = Collections.synchronizedList(new ArrayList(maxConnCount));
    /**
     * ��ǰ���õ�ͨ��
     */
    private final Queue<WorkThread> availableWorkers = new ConcurrentLinkedQueue();
    /**
     * ������
     */
    private final Object lock = new Object();

    public DefaultAsyncTransmitter2(String ip, int port) {
        super(ip, port);
        serverAddress = new InetSocketAddress(ip, port);
        asyncTransmitterThread = new Thread(this);
    }

    synchronized public boolean open() {
        // ����������õ�ͨ��
        for (int i = 0; i < initConnCount; i++) {
            SocketChannel channel = createChannel();
            if (channel != null) {
                availableWorkers.add(new WorkThread(channel));
            } else {
                // FIXME: �������㼸��������ʧ�ܶ�ʧ�ܣ�Ŀǰ�Ĵ�ʩ��
                end();
                return false;
            }
        }
        asyncTransmitterThread.start();
        return true;
    }

    public void asyncSend(Request request, Callable callable) throws IOException {
        Worker worker = new Worker(request, callable);
        workList.add(worker);
        synchronized (asyncTransmitterThread) {
            asyncTransmitterThread.notify();
        }
    }

    public void run() {
        while (!stopFlag) {
            // This may block for a long time. Upon returning, the
            // selected set contains keys of the ready channels.

            if (workList.isEmpty()) {
                waitFor();
            }
            for (Iterator<Worker> it = workList.iterator(); it.hasNext();) {
                Worker worker = it.next();
                WorkThread channel = getWorkThread();
                if (channel != null) {
                    channel.send(worker);
                    channel.start();
                    it.remove();
                } else {
                    waitFor();
                    break;
                }
            }
        }
    }

    private void waitFor() {
        try {
            synchronized (asyncTransmitterThread) {
                asyncTransmitterThread.wait(1000);
            }
        } catch (InterruptedException ex) {
            LogWriter.warn2(logger, ex, "�̱߳��ж�");
        }
    }

    private WorkThread getWorkThread() {
        synchronized (lock) {
            // If there are some workers which can be used
            while (!availableWorkers.isEmpty()) {
                WorkThread workThread = availableWorkers.poll();
                // ���󱻹ر��������л���
                if (!workThread.socketChannel.isConnected()) {
                    // socketChannel
                    continue;
                }
                workingWorkers.add(workThread);
                return workThread;
            }
        }

        // You can create more channel after reaching the maxium count
        if (workingWorkers.size() >= maxConnCount) {
            return null;
        }
        // ���С������������������½�һ������
        SocketChannel channel = createChannel();
        if (channel == null) {
            return null;
        }
        WorkThread wt = new WorkThread(channel);
        synchronized (lock) {
            workingWorkers.add(wt);
        }
        return wt;

    }

    private SocketChannel createChannel() {
        SocketChannel channel = null;
        try {
            channel = SocketChannel.open(serverAddress);
            channel.configureBlocking(true);
        } catch (IOException ex) {
            LogWriter.warn2(logger, ex, "���Խ�����������[%s:%d]���ӵ�����ʧ�ܡ�", ip, port);
        }
        return channel;
    }
    
    protected Response receive(SocketChannel socketChannel) throws IOException {
        // start to parse the response
        long _startTime = System.currentTimeMillis();

        int count = 0;
        Response response = null;
        ByteBuffer byteBuffer = BufferFactory.getInstance().allocate();
        final Parser parser = new HttpResponseParser();

        while ((count = socketChannel.read(byteBuffer)) > 0) {
            // ������ӳ�ʱ��ϵͳ������
            if (isTimeout(_startTime)) {
                LogWriter.info2(logger, "���շ����[%s:%d]����Ӧ�Ѿ���ʱ[%d(ms)]��",
                        ip, port, com.xt.bcloud.td.Contants.downTimeout);
                break;
            }

            // ����������ļ�
            DumperFactory.getInstance().write(dumperPrefix + "_receiving_from_server_", 
                    socketChannel.getRemoteAddress(), byteBuffer);

            // ������Ӧ��Ϣ
            response = (Response) parser.parse(byteBuffer);
            if (parser.finished()) {  // ���ٶ�ȡһ����������Ӧ
                break;
            }
        }

        if (count < 0 && response == null) {
            // ��ԭʼ���������������жϣ�����������δ��ȡ����
            throw new HttpException(ErrorFactory.ERROR_500);
        }
        return response;
    }

    class Worker {

        private final Request request;
        private final Callable callable;

        public Worker(Request request, Callable callable) {
            this.request = request;
            this.callable = callable;
        }
    }

    class WorkThread extends Thread {

        private final SocketChannel socketChannel;
        private Worker worker;

        public WorkThread(SocketChannel socketChannel) {
            this.socketChannel = socketChannel;
        }

        public void send(Worker worker) {
            this.worker = worker;
        }

        @Override
        public void run() {
            try {
                writeTo(socketChannel, worker.request);
                Response response = DefaultAsyncTransmitter2.this.receive(socketChannel);
                worker.callable.execute(response);
            } catch (Throwable t) {
                worker.callable.handle(t);
            } finally {
                synchronized (lock) {
                    workingWorkers.remove(this);
                    availableWorkers.add(this);
                }
                synchronized (asyncTransmitterThread) {
                    asyncTransmitterThread.notify();
                }
            }
        }
    }

    @Override
    public void end() {
        super.end();
        for (Iterator<WorkThread> it = availableWorkers.iterator(); it.hasNext();) {
            WorkThread workThread = it.next();
            try {
                workThread.socketChannel.close();
            } catch (IOException ex) {
                LogWriter.warn2(logger, ex, "���Թرյ�������[%s:%d]���ӵ��쳣��", ip, port);
            }
        }
        for (Iterator<WorkThread> it = workingWorkers.iterator(); it.hasNext();) {
            WorkThread workThread = it.next();
            try {
                workThread.socketChannel.close();
            } catch (IOException ex) {
                LogWriter.warn2(logger, ex, "���Թرյ�������[%s:%d]���ӵ��쳣��", ip, port);
            }
        }
    }
}
