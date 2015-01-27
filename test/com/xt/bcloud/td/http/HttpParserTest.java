
package com.xt.bcloud.td.http;

import com.xt.bcloud.td.http.HttpMessage;
import com.xt.bcloud.td.http.HttpParser;
import com.xt.bcloud.td.http.HttpRequestParser;
import com.xt.core.utils.IOHelper;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author albert
 */
public class HttpParserTest extends TestCase {

    public HttpParserTest(String testName) {
        super(testName);
    }

     public void testPlainRequest() throws Exception {
        HttpParser parser = new HttpRequestParser();
        File file = new File("E:\\work\\xthinker\\B-Cloud\\src\\files\\request.plain.txt");
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOHelper.i2o(fis, baos);
        List requests = parser.parse(baos.toByteArray());
        // assertTrue(parser.isMessageEnd());
        System.out.println("requests=" + requests);
    }

    public void xtestParse1() throws Exception {
        HttpParser parser = new HttpRequestParser();
//        System.out.println("\n=" + "\n".getBytes()[0]);
//        System.out.println("\r=" + "\r".getBytes()[0]);
        File file = new File("E:\\work\\xthinker\\B-Cloud\\src\\files\\HttpParser.test.txt");
//        file.renameTo(new File("E:\\HttpParser.test.txt.2"));
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOHelper.i2o(fis, baos);
        List requests = parser.parse(baos.toByteArray());
        System.out.println("requests=" + requests);
    }

    public void xtestParseFormData() throws Exception {
        HttpRequestParser parser = new HttpRequestParser();
//        System.out.println("\n=" + "\n".getBytes()[0]);
//        System.out.println("\r=" + "\r".getBytes()[0]);
        File file = new File("E:\\work\\xthinker\\B-Cloud\\test\\httpRequest.formdata.txt");
//        file.renameTo(new File("E:\\HttpParser.test.txt.2"));
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        IOHelper.i2o(fis, bos, true, true);
        List<HttpMessage> list = (List<HttpMessage>)parser.parse(bos.toByteArray());
        System.out.println("parser.isMessageEnd()=" + parser.isMessageEnd());
        System.out.println("messageBody=" + new String(list.get(0).getMessageBody()));
        System.out.println("requests=" + list);
    }
}
