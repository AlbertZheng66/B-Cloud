
import java.nio.channels.SocketChannel;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.ByteBuffer;
import java.net.InetSocketAddress;
import java.io.IOException;
import java.io.Console;
import java.util.Iterator;

/**
 * @author : Vincent Chan
 * @class : MultiPortEchoClient
 * @date : 2009-3-3
 * @time : 14:56:56
 */
public class MultiPortEchoClient {

    public static void main(String[] args) throws IOException {
        if (args.length <= 0) {
            System.err.println("Usage: java MultiPortEcho port [port port ...]");
            System.exit(1);
        }
        int port = 0;
        port = Integer.parseInt(args[0]);
        InetSocketAddress address = new InetSocketAddress(port);
        SocketChannel client = SocketChannel.open();
        client.configureBlocking(false);
        Selector selector = Selector.open();
        client.register(selector, SelectionKey.OP_CONNECT);
        client.connect(address);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while (true) {
            if (selector.select() > 0) {
                Iterator iter = selector.selectedKeys().iterator();
                while (iter.hasNext()) {
                    SelectionKey key = (SelectionKey) iter.next();
                    iter.remove();
                    if (key.isConnectable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        if (channel.isConnectionPending()) {
                            channel.finishConnect();
                        }
                        System.out.print("aaaaaaaaaaaaaaa");

                        String input = null;
                        while ((input = System.console().readLine()) != null) {
                            input = input.trim().toLowerCase();
                            break;
                        }
                        if (input.equals("q") || input.equals("quit")) {
                            channel.close();
                            System.exit(0);
                        }
                        buffer.clear();
                        buffer.put(input.getBytes());
                        buffer.flip();
                        channel.write(buffer);
                        channel.register(selector, SelectionKey.OP_READ);
                    } else if (key.isReadable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        int count = channel.read(buffer);
                        if (count > 0) {
                            System.out.println("????иш????" + buffer);
                            buffer.clear();
                        }
                    }
                }
            }
        }
    }
}
