/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xt.bcloud.td7;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import junit.framework.TestCase;

/**
 *
 * @author Albert
 */
public class Utils {
    

    public static ByteBuffer createBuffer(TestCase tc, String fileName) {
        try {
            fileName = "E:/work/xthinker/B-Cloud/test/files/" + fileName;
            File file = new File(fileName);
            ByteBuffer byteBuffer = ByteBuffer.allocate((int) file.length());
            FileChannel channel = (new FileInputStream(file)).getChannel();
            channel.read(byteBuffer);
            System.out.println("byteBuffer=" + new String(byteBuffer.array(), "utf-8"));
            return byteBuffer;
        } catch (IOException ex) {
            tc.fail(String.format("¶Á²âÊÔÎÄ¼þ[%s]´íÎó", fileName));
            ex.printStackTrace();
        }
        return null;
    }
    
    static public void print(Buffers buffers) {
        for (ByteBuffer byteBuffer : buffers.getBuffers()) {
            print(byteBuffer, System.out);
        }
    }
    
static public void print(ByteBuffer byteBuffer) {
        print(byteBuffer, System.out);
    }
    static public void print(ByteBuffer byteBuffer, OutputStream os) {
        try {
            os.write(byteBuffer.array());
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }
}
