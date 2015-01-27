
package com.xt.bcloud.td7.impl;

import com.xt.bcloud.td7.BufferFactory;
import com.xt.bcloud.td7.Connector;
import com.xt.bcloud.td7.Utils;
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
public class HttpNIOConnectorClient3 extends TestCase {
    
    public void testConnecting() {
        ByteBuffer buffer = Utils.createBuffer(this, "http.request.formdata.txt");
        SocketAddress sa = new InetSocketAddress("127.0.0.1", 4900);
        try {
            buffer.flip();
            SocketChannel channel = SocketChannel.open();
            channel.connect(sa);
            channel.write(buffer);
            //-------------------- start to read
            System.out.println("start to read..............");
            buffer = BufferFactory.getInstance().allocate(2048);
            channel.read(buffer);
            buffer.flip();
            Utils.print(buffer);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
}
