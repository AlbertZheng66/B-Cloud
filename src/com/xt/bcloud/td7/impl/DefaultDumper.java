package com.xt.bcloud.td7.impl;

import com.xt.bcloud.comm.VarParser;
import com.xt.bcloud.mdu.command.CommandValueReader;
import com.xt.bcloud.td7.Dumpable;
import com.xt.bcloud.td7.TaskDispatcherException;
import com.xt.core.log.LogWriter;
import com.xt.core.utils.IOHelper;
import com.xt.gt.sys.SystemConfiguration;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.logging.Level;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Albert
 */
public class DefaultDumper implements Dumpable {

    /**
     * 日志实例
     */
    private final Logger logger = Logger.getLogger(DefaultDumper.class);
    /**
     * 文件名称的格式
     */
    private String fileNamePattern = "\\dumper_${_time}_${_random}.dump";
//    /**
//     * 顺序指示器。
//     */
//    private static int seq = 0;
    
    /**
     * 导出的根目录
     */
    protected final File rootPath = new File(SystemConfiguration.getInstance().readString("dumper.rootPath", "e:\\dump"));
    
    /**
     * 当前正在写入的文件通道。
     */
    protected FileChannel fileChannel;
    
    /**
     * 当前正在写入的文件。
     */
    protected File writingFile;
    /**
     * 当前写入的记录数
     */
    private long count = 0;

    public DefaultDumper() {
    }

    public void setFilenamePattern(String fileNamePattern) {
        if (StringUtils.isNotEmpty(fileNamePattern)) {
            this.fileNamePattern = fileNamePattern;
        }
    }

    public void open() {
        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }
        String fileName = VarParser.parse(fileNamePattern, new CommandValueReader(Collections.EMPTY_MAP));
        writingFile = new File(rootPath, fileName);
        try {
            if (!writingFile.exists()) {
                writingFile.createNewFile();

            }
            FileOutputStream fos = new FileOutputStream(writingFile);
            fileChannel = fos.getChannel();
        } catch (IOException ex) {
            fileChannel = null;
            LogWriter.warn2(logger, ex, "创建文件[%s]失败。", fileName);
        }
    }

    public void write(ByteBuffer[] b) {
        if (b == null || b.length == 0) {
            return;
        }
        if (fileChannel == null) {
            LogWriter.warn2(logger, "当前文件尚未打开。");
            return;
        }
        for (int i = 0; i < b.length; i++) {
            write(b[i]);
        }
    }

    public void write(ByteBuffer b) {
        if (b == null) {
            return;
        }
        if (fileChannel == null) {
            LogWriter.warn2(logger, "当前文件尚未打开。");
            return;
        }
        ByteBuffer _b = b.duplicate();
        _b.flip();
        try {
            count += fileChannel.write(_b);
        } catch (IOException ex) {
            // 可以不写，但是不能出错，即不能干扰正常程序的运行
            LogWriter.warn2(logger, ex, "写文件失败。");
        }
    }

    public void close() {
        // 删除0长度的文件
        if (count <= 0 && writingFile != null) {
            writingFile.delete();
        }
        IOHelper.closeSilently(fileChannel);
        fileChannel = null;
        writingFile = null;
        count = 0;
    }
}
