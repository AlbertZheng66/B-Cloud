/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xt.bcloud.comm;

import java.io.*;
import junit.framework.TestCase;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

/**
 *
 * @author Albert
 */
public class ZipTest extends TestCase {
    
    public ZipTest(String testName) {
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

    public void xtestSomeMethod() throws FileNotFoundException, IOException {
         OutputStream outputStream = new FileOutputStream("e:\\test.zip");
         final File rootFile = new File("e:\\dump");
        Zip zip = new Zip(rootFile, outputStream);
        zip.zip();
    }
    
    public void testProperties () {
        System.getProperties().list(System.out);
    }
}
