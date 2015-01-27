package com.xt.bcloud.td;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author albert
 */
public class Dispatcher6 {

    /**
     * ���ڴ���ͻ��������ѡ����
     */
    public Selector clientSelector = null;

    /**
     * ת��ѡ������
     */
    private Selector redirectorSelector;

    public ServerSocketChannel server = null;

    private final CattleManager taskDispatcherChannel = CattleManager.getInstance();

    public int port = 4900;

    public Dispatcher6() {
    }

    public Dispatcher6(int port) {
        this.port = port;
    }

    public void initializeOperations() throws IOException, UnknownHostException {
        clientSelector = Selector.open();
        redirectorSelector = Selector.open();

        // ����ת���߳�
        RedirectorThread6 rt = new RedirectorThread6(this.clientSelector, redirectorSelector);
        new Thread(rt).start();

        // ��������
        server = ServerSocketChannel.open();
        server.configureBlocking(false);
        InetAddress ia = InetAddress.getLocalHost();
        System.out.println("ia=" + ia);
        InetSocketAddress isa = new InetSocketAddress(ia, port);
        server.socket().bind(new InetSocketAddress(port));
    }

    public void startServer() throws IOException {
        System.out.println("Inside startserver");
        initializeOperations();

        System.out.println("Abt to block on select()");
        while (true) {
            SelectionKey acceptKey = server.register(clientSelector, SelectionKey.OP_ACCEPT);
            while (clientSelector.select() > 0) {
                // System.out.println("Select one...............");
                Set readyKeys = clientSelector.selectedKeys();
                Iterator it = readyKeys.iterator();
                while (it.hasNext()) {
                    SelectionKey key = (SelectionKey) it.next();
                    it.remove();

                    if (key.isAcceptable()) {
                        System.out.println("Key is Acceptable");
                        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                        SocketChannel channel = (SocketChannel) ssc.accept();
                        channel.configureBlocking(false);
                        SelectionKey another = channel.register(clientSelector,
                                SelectionKey.OP_READ | SelectionKey.OP_WRITE, null);
                    } else if (key.isReadable()) {
                        System.out.println("readable key=" + key);
                        readReqeust(key);
                    } else if (key.isWritable()) {
                        writeResponses(key);
                    }
                }
            }
        }
    }

    private void writeResponses(SelectionKey key) throws IOException {
        // System.out.println("To write responses to the client.......................");
        Object attachment = key.attachment();
        if (attachment == null) {
            return;
        }
        if (attachment instanceof Redirector) {
            Redirector redirector = (Redirector) attachment;
            SocketChannel channel = (SocketChannel) key.channel();
            redirector.writeToClient(channel);
        } else {
            //logger.
        }
    }

    /**
     * �رտͻ������ͨ��
     */
    private void close() {
////                channel.socket().close();
////                channel.close();
////                key.attach(null);
////                key.cancel();
    }

    public void readReqeust(SelectionKey key) {
        System.out.println("readMessage.......................");
        // ÿ�� Key ������Ψһ��ת��������
        Redirector redirector = (Redirector) key.attachment();
        if (redirector == null) {
            redirector = new Redirector(key, redirectorSelector, taskDispatcherChannel);
            key.attach(redirector);
        }
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buf = ByteBuffer.allocate(1024);
        try {
            int nBytes = channel.read(buf);
            if (nBytes > 0) {
                System.out.println("readMessage.nBytes=" + nBytes);
            }
            if (nBytes < 0) {
                System.out.println("�رշ������Ľ���ͨ��");
                // �ر�ͨ��
                redirector.cancel();
                channel.close();
                return;
            }

            // ׼���ö�ȡ����
            buf.flip();
            redirector.read(buf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void wakeup() {
        if (clientSelector != null) {
            clientSelector.wakeup();
        }
    }

    public static void main(String args[]) {
        Dispatcher6 nb = new Dispatcher6(4900);
        try {
            nb.startServer();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

    }
}
