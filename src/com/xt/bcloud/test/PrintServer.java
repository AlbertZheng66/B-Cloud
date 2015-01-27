package com.xt.bcloud.test;

import com.xt.core.utils.IOHelper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This server is very simple and is in charge of receiving requests and
 * printing the original contents of them.
 *
 * @author Albert
 */
public class PrintServer implements Runnable {

    public final static int port = 5800;
    // The host:port combination to listen on
    private InetAddress hostAddress;
    // The channel on which we'll accept connections
    private ServerSocketChannel serverChannel;
    // The selector we'll be monitoring
    private Selector selector;
    // The buffer into which we'll read data when it's available
    private ByteBuffer readBuffer = ByteBuffer.allocate(8192);

    public PrintServer() throws IOException {
        this.hostAddress = InetAddress.getLocalHost();
        this.selector = this.initSelector();
    }

    public void run() {
        while (true) {
            try {

                // Wait for an event one of the registered channels
                this.selector.select();

                // Iterate over the set of keys for which events are available
                Iterator selectedKeys = this.selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    SelectionKey key = (SelectionKey) selectedKeys.next();
                    selectedKeys.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    // Check what event is available and deal with it
                    if (key.isAcceptable()) {
                        this.accept(key);
                    } else if (key.isReadable()) {
                        this.read(key);
                    } else if (key.isWritable()) {
                        this.write(key);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void accept(SelectionKey key) throws IOException {
        // For an accept to be pending the channel must be a server socket channel.
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        // Accept the connection and make it non-blocking
        SocketChannel socketChannel = serverSocketChannel.accept();
        Socket socket = socketChannel.socket();
        socketChannel.configureBlocking(false);

        // Register the new SocketChannel with our Selector, indicating
        // we'd like to be notified when there's data waiting to be read
        socketChannel.register(this.selector, SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        // Clear out our read buffer so it's ready for new data
        this.readBuffer.clear();

        // Attempt to read off the channel
        int numRead;
        try {
            numRead = socketChannel.read(this.readBuffer);
            readBuffer.flip();
            Utils.print(readBuffer);
        } catch (IOException e) {
            // The remote forcibly closed the connection, cancel
            // the selection key and close the channel.
            key.cancel();
            socketChannel.close();
            return;
        }

        if (numRead == -1) {
            // Remote entity shut the socket down cleanly. Do the
            // same from our end and cancel the channel.
            key.channel().close();
            key.cancel();
            return;
        }
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        ByteBuffer buf = ByteBuffer.wrap("hello".getBytes());
        socketChannel.write(buf);

        key.interestOps(SelectionKey.OP_READ);
    }

    private Selector initSelector() throws IOException {
        // Create a new selector
        Selector socketSelector = SelectorProvider.provider().openSelector();

        // Create a new non-blocking server socket channel
        this.serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        // Bind the server socket to the specified address and port
        InetSocketAddress isa = new InetSocketAddress(this.hostAddress, this.port);
        serverChannel.socket().bind(isa);

        // Register the server socket channel, indicating an interest in 
        // accepting new connections
        serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

        return socketSelector;
    }

    public static void main(String[] args) {
        try {
            EchoWorker worker = new EchoWorker();
            new Thread(worker).start();
            new Thread(new PrintServer()).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//    public static void main(String[] argv) {
//
//        ServerSocket server = null;
//        try {
//            server = new ServerSocket(port);
//        } catch (IOException ex) {
//            Logger.getLogger(PrintServer.class.getName()).log(Level.SEVERE, null, ex);
//            return;
//        }
//        System.out.println(String.format("I'm listening on the port[%d]", port));
//        while (true) {
//            try {
//                Socket connection = server.accept();
//                InputStream is = connection.getInputStream();
//                byte[] bytes = new byte[1024];
//                int count = is.read(bytes);
//                System.out.write(bytes, 0, count);
//                
//                OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
//                out.write(String.format("I've received [%s] bytes\r\n", count));
//                connection.close();
//            } catch (Throwable ex) {
//                ex.printStackTrace();
//            }
//        }
//
//
//    }
}
