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
     * 输出文件类型：XML文件，追加式加入
     */
    public static final String OUTPUT_TYPE_XML = "xml";
    
//    /**
//     * 输出文件类型：数据库语句
//     */
//    public static final String OUTPUT_TYPE_SQL = "sql";
    
    /**
     * 输出文件类型：普通文件（默认）,产生式生成
     */
    public static final String OUTPUT_TYPE_PLAIN_FILE = "PLAIN_FILE";
    
    private final Logger logger = Logger.getLogger(VarProcessor.class.getName());

    
    public void generate(String templateFileName, String targetFileName, Map<String, Object> params) {
        LogWriter.info2(logger, "使用模板[%s]生成文件[%s]", templateFileName, targetFileName);
        try {
            Velocity.init();

            /*
             * lets make a Context and put data into it
             */
            VelocityContext context = new VelocityContext();
            // 注入变量
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String name = entry.getKey();
                Object value = entry.getValue();
                context.put(name, value);
            }

            generate0(context, templateFileName, targetFileName);
        } catch (Exception ex) {
            throw new MduException("使用模板[" + templateFileName + "]时输出文件产生异常！", ex);
        }
    }
    
    private void generate0(VelocityContext context, 
            String templateFileName, String targetFileName) throws Exception {

        Reader vmTempFile = null;

        try {
            vmTempFile = new InputStreamReader(new FileInputStream(templateFileName), DEFAULT_TEMPLATE_FILE_ENCODING);

            // 已经生成内容
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
     * 采用追加式生成XML文件
     *
     * @param context
     * @param sw
     * @throws java.io.IOException
     */
    private void appendXmlFile(VelocityContext context, String content) throws Exception {
//        OutputException.assertNotNull(context.get("vmTargetFile"), "vmTargetFile");        // targetFile = context.get(OUTPUT_TYPE_XML)
//        OutputException.assertNotNull(context.get("vmXPath"), "vmXPath");                  // set( $xpath = "/root/Service" )

        String targetFile = context.get("vmTargetFile").toString();


        // 将生成的节点合并到指定的XPATH节点下
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputStream is = new ByteArrayInputStream(content.getBytes("UTF-8"));
        Document sourceDoc = builder.parse(is);

        StringBuffer fileNameBuf = new StringBuffer();
        fileNameBuf.append(targetFile);

        // 将Window路径的分隔符统一换成uinx风格，以保持统一
        String fileName = fileNameBuf.toString().replaceAll("\\\\", "/");
        File file = new File(fileName);
        if (!file.exists()) {
            String pathStr = parsePath(fileName);
            File path = new File(pathStr);
            path.mkdirs();
            // 如果目标文件不存在，使用源模板生成目标文件, 模板文件应该是标准的XML文件
            file.createNewFile();
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            trans.transform(new DOMSource(sourceDoc), new StreamResult(file));
        }

        // 使用 XML 文件读取目标文件并得到指定的XPATH节点
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
     * 目标节点是否含有和源节点（待加入的节点）相同的节点
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
     * 根据当前内容创建文件
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
