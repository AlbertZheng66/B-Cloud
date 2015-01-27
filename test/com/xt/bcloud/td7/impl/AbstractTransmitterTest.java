package com.xt.bcloud.td7.impl;

import com.xt.bcloud.td7.BufferFactory;
import com.xt.bcloud.td7.Request;
import com.xt.bcloud.td7.Utils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import junit.framework.TestCase;

/**
 *
 * @author Albert
 */
public class AbstractTransmitterTest extends TestCase {

    public AbstractTransmitterTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void xtestWriteTo() throws IOException {
        ByteBuffer buffer = Utils.createBuffer(this, "http.request.formdata.txt");
        HttpRequestParser parser = new HttpRequestParser();
        Request request = (Request) parser.parse(buffer);

        AbstractTransmitter at = new TestAbstractTransmitter();
        File file = new File("e:\\test.request");
        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(file, false);
        at.writeTo(fos.getChannel(), request);
        fos.close();
    }

    public void testWriteTo2() throws IOException {
        ByteBuffer buffer = Utils.createBuffer(this, "http.request.formdata.txt");
        HttpRequestParser parser = new HttpRequestParser();
        Request request = (Request) parser.parse(buffer);

        AbstractTransmitter at = new TestAbstractTransmitter();
        SocketAddress remote = new InetSocketAddress("127.0.0.1", 58080);
        SocketChannel socketChannel = SocketChannel.open(remote);
        socketChannel.configureBlocking(true);
//        socketChannel.configureBlocking(true);
//        buffer.flip();
//        socketChannel.write(buffer);
        at.writeTo(socketChannel, request);
        buffer = BufferFactory.getInstance().allocate();
        socketChannel.read(buffer);
        buffer.flip();
        Utils.print(buffer);
        socketChannel.close();
    }

    class TestAbstractTransmitter extends AbstractTransmitter {

        public TestAbstractTransmitter() {
            super("", 0);
        }

        public boolean open() {
            return true;
        }

        public void end() {
        }
    }
}
