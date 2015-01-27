package com.xt.bcloud.td.impl;

import com.xt.bcloud.td.Dumpable;
import com.xt.core.log.LogWriter;
import com.xt.core.utils.IOHelper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.log4j.Logger;

/**
 *
 * @author albert
 */
public class DefaultDumper implements Dumpable {

    /**
     * ��־ʵ��
     */
    private final Logger logger = Logger.getLogger(DefaultDumper.class);
    
    /**
     * ˳��ָʾ����
     */
    private static int seq = 0;

    /**
     * �����ĸ�Ŀ¼
     */
    protected File rootPath = new File("e:\\dump");

    /**
     * ���������
     */
    protected FileOutputStream requestOutputStream;
    
    /**
     * ��Ӧ�����
     */
    protected FileOutputStream responseOutputStream;

    public DefaultDumper() {
        init();
    }

    protected void init() {
        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }
        requestOutputStream = createFile("req");
        responseOutputStream = createFile("res");
    }

    protected FileOutputStream createFile(String prefix) {
        String fileName = String.format("%s-%d-%d", prefix, System.currentTimeMillis(), seq++);
        File file = new File(rootPath, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException ex) {
            LogWriter.warn2(logger, ex, "���������ļ�[%s]ʱ�����쳣��", file.getAbsolutePath());
        }
        return fos;
    }

    public void writReq(byte[] b) {
        if (requestOutputStream != null && b != null) {
            try {
                requestOutputStream.write(b);
            } catch (IOException ex) {
                LogWriter.warn2(logger, ex, "д�����ļ�ʱ�����쳣��");
                //requestOutputStream == null;  // �Ƿ���Ҫ����д��
            }
        }
    }

    public void closeReq() {
        IOHelper.closeSilently(requestOutputStream);
    }

    public void writRes(byte[] b) {
        if (responseOutputStream != null && b != null) {
            try {
                responseOutputStream.write(b);
            } catch (IOException ex) {
                LogWriter.warn2(logger, ex, "д��Ӧ�ļ�ʱ�����쳣��");
                //responseOutputStream == null;  // �Ƿ���Ҫ����д��
            }
        }
    }

    public void closeRes() {
        IOHelper.closeSilently(responseOutputStream);
    }
}
