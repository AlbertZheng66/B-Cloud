/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xt.bcloud.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 *
 * @author Albert
 */
public class Utils {

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
