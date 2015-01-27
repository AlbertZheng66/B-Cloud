package com.xt.bcloud.mdu.command;

import com.xt.bcloud.mdu.MduException;
import com.xt.core.log.LogWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Albert
 */
public class VarProcessor {

    private static final String DEFAULT_TEMPLATE_FILE_ENCODING = "UTF-8";
    
    /**
     * ����ļ����ͣ�XML�ļ���׷��ʽ����
     */
    public static final String OUTPUT_TYPE_XML = "xml";
    
//    /**
//     * ����ļ����ͣ����ݿ����
//     */
//    public static final String OUTPUT_TYPE_SQL = "sql";
    
    /**
     * ����ļ����ͣ���ͨ�ļ���Ĭ�ϣ�,����ʽ����
     */
    public static final String OUTPUT_TYPE_PLAIN_FILE = "PLAIN_FILE";
    
    private final Logger logger = Logger.getLogger(VarProcessor.class.getName());

    
    public void generate(String templateFileName, String targetFileName, Map<String, Object> params) {
        LogWriter.info2(logger, "ʹ��ģ��[%s]�����ļ�[%s]", templateFileName, targetFileName);
        try {
            Velocity.init();

            /*
             * lets make a Context and put data into it
             */
            VelocityContext context = new VelocityContext();
            // ע�����
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String name = entry.getKey();
                Object value = entry.getValue();
                context.put(name, value);
            }

            generate0(context, templateFileName, targetFileName);
        } catch (Exception ex) {
            throw new MduException("ʹ��ģ��[" + templateFileName + "]ʱ����ļ������쳣��", ex);
        }
    }
    
    private void generate0(VelocityContext context, 
            String templateFileName, String targetFileName) throws Exception {

        Reader vmTempFile = null;

        try {
            vmTempFile = new InputStreamReader(new FileInputStream(templateFileName), DEFAULT_TEMPLATE_FILE_ENCODING);

            // �Ѿ���������
            StringWriter sw = new StringWriter();
            Velocity.evaluate(context, sw, templateFileName, vmTempFile);

            Object outputType = context.get("vmOutputType");
            if (OUTPUT_TYPE_XML.equalsIgnoreCase(String.valueOf(outputType))) {
                appendXmlFile(context, sw.toString());
            } else {
                createFile(targetFileName, sw.toString());
            }
        } finally {
            if (vmTempFile != null) {
                vmTempFile.close();
            }
        }

    }

    /**
     * ����׷��ʽ����XML�ļ�
     *
     * @param context
     * @param sw
     * @throws java.io.IOException
     */
    private void appendXmlFile(VelocityContext context, String content) throws Exception {
//        OutputException.assertNotNull(context.get("vmTargetFile"), "vmTargetFile");        // targetFile = context.get(OUTPUT_TYPE_XML)
//        OutputException.assertNotNull(context.get("vmXPath"), "vmXPath");                  // set( $xpath = "/root/Service" )

        String targetFile = context.get("vmTargetFile").toString();


        // �����ɵĽڵ�ϲ���ָ����XPATH�ڵ���
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputStream is = new ByteArrayInputStream(content.getBytes("UTF-8"));
        Document sourceDoc = builder.parse(is);

        StringBuffer fileNameBuf = new StringBuffer();
        fileNameBuf.append(targetFile);

        // ��Window·���ķָ���ͳһ����uinx����Ա���ͳһ
        String fileName = fileNameBuf.toString().replaceAll("\\\\", "/");
        File file = new File(fileName);
        if (!file.exists()) {
            String pathStr = parsePath(fileName);
            File path = new File(pathStr);
            path.mkdirs();
            // ���Ŀ���ļ������ڣ�ʹ��Դģ������Ŀ���ļ�, ģ���ļ�Ӧ���Ǳ�׼��XML�ļ�
            file.createNewFile();
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            trans.transform(new DOMSource(sourceDoc), new StreamResult(file));
        }

        // ʹ�� XML �ļ���ȡĿ���ļ����õ�ָ����XPATH�ڵ�
        String xPathStr = context.get("vmXPath").toString();
        Document targetDoc = builder.parse(file);
        XPath path = XPathFactory.newInstance().newXPath();
        Node targetNode = (Node) path.evaluate(xPathStr, targetDoc, XPathConstants.NODE);



        Node sourceParentNode = (Node) path.evaluate(xPathStr, sourceDoc, XPathConstants.NODE);
        NodeList sourceNodes = sourceParentNode.getChildNodes();
        for (int i = 0; i < sourceNodes.getLength(); i++) {
            Node node = targetDoc.importNode(sourceNodes.item(i), true);
            if (!containSameNode(targetNode, node)) {
                targetNode.appendChild(node);
            }
        }
        Transformer trans = TransformerFactory.newInstance().newTransformer();
        trans.transform(new DOMSource(targetDoc), new StreamResult(file));
    }

    /**
     * Ŀ��ڵ��Ƿ��к�Դ�ڵ㣨������Ľڵ㣩��ͬ�Ľڵ�
     *
     * @param targetNode
     * @param node
     * @return
     */
    private boolean containSameNode(Node targetNode, Node node) {
        if (node == null || targetNode == null) {
            return false;
        }
        NodeList children = targetNode.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (node.isEqualNode(child)) {
                return true;
            }
        }
        return false;
    }

    private String parsePath(String fileName) {
        int index = fileName.lastIndexOf("/");
        if (index > -1) {
            return fileName.substring(0, index);
        }
        return "";
    }

    /**
     * ���ݵ�ǰ���ݴ����ļ�
     *
     * @param path
     * @param fileName
     * @param content
     * @throws java.io.IOException
     */
    private void createFile(String fileName, String content) throws Exception {

        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
        }

        FileOutputStream fos = new FileOutputStream(file, false);
        fos.write(content.getBytes(DEFAULT_TEMPLATE_FILE_ENCODING));
        fos.close();
    }
}
