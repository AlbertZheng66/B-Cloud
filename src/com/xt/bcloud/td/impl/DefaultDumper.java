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
     * 日志实例
     */
    private final Logger logger = Logger.getLogger(DefaultDumper.class);
    
    /**
     * 顺序指示器。
     */
    private static int seq = 0;

    /**
     * 导出的根目录
     */
    protected File rootPath = new File("e:\\dump");

    /**
     * 请求输出流
     */
    protected FileOutputStream requestOutputStream;
    
    /**
     * 响应输出流
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
            LogWriter.warn2(logger, ex, "创建导出文件[%s]时出现异常。", file.getAbsolutePath());
        }
        return fos;
    }

    public void writReq(byte[] b) {
        if (requestOutputStream != null && b != null) {
            try {
                requestOutputStream.write(b);
            } catch (IOException ex) {
                LogWriter.warn2(logger, ex, "写请求文件时出现异常。");
                //requestOutputStream == null;  // 是否需要继续写？
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
                LogWriter.warn2(logger, ex, "写响应文件时出现异常。");
                //responseOutputStream == null;  // 是否需要继续写？
            }
        }
    }

    public void closeRes() {
        IOHelper.closeSilently(responseOutputStream);
    }
}
