
package com.xt.bcloud.td7;

import java.io.IOException;
import java.nio.ByteBuffer;
import junit.framework.TestCase;

/**
 *
 * @author Albert
 */
public class BuffersTest extends TestCase {
    
    public BuffersTest(String testName) {
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

    /**
     * Test of put method, of class Buffers.
     */
    public void testPut() throws IOException {
        System.out.println("put");
        byte[] bytes = "1234567890".getBytes();
        Buffers buffers = new Buffers();
        buffers.setCapacity(3);
        buffers.put(bytes, 2, 7); // 3-9
        System.out.print("result=");
        buffers.print(System.out);
        buffers.dispose();
        System.out.println();
        buffers.setCapacity(2);
        buffers.put(bytes, 1, 8); // 2-9
        System.out.print("result2=");
        buffers.print(System.out);
        buffers.dispose();
        System.out.println();
        buffers.setCapacity(10);
        buffers.put(bytes, 1, 6); // 2-9
        System.out.print("result2=");
        buffers.print(System.out);
    }

    /**
     * Test of flip method, of class Buffers.
     */
    public void xtestFlip() {
        System.out.println("flip");
        Buffers instance = new Buffers();
        instance.flip();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    

    /**
     * Test of append method, of class Buffers.
     */
    public void xtestAppend() {
        System.out.println("append");
        ByteBuffer buffer = null;
        Buffers instance = new Buffers();
        instance.append(buffer);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
