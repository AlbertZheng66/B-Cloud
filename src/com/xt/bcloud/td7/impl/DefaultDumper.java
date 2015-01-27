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
     * ��־ʵ��
     */
    private final Logger logger = Logger.getLogger(DefaultDumper.class);
    /**
     * �ļ����Ƶĸ�ʽ
     */
    private String fileNamePattern = "\\dumper_${_time}_${_random}.dump";
//    /**
//     * ˳��ָʾ����
//     */
//    private static int seq = 0;
    
    /**
     * �����ĸ�Ŀ¼
     */
    protected final File rootPath = new File(SystemConfiguration.getInstance().readString("dumper.rootPath", "e:\\dump"));
    
    /**
     * ��ǰ����д����ļ�ͨ����
     */
    protected FileChannel fileChannel;
    
    /**
     * ��ǰ����д����ļ���
     */
    protected File writingFile;
    /**
     * ��ǰд��ļ�¼��
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
            LogWriter.warn2(logger, ex, "�����ļ�[%s]ʧ�ܡ�", fileName);
        }
    }

    public void write(ByteBuffer[] b) {
        if (b == null || b.length == 0) {
            return;
        }
        if (fileChannel == null) {
            LogWriter.warn2(logger, "��ǰ�ļ���δ�򿪡�");
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
            LogWriter.warn2(logger, "��ǰ�ļ���δ�򿪡�");
            return;
        }
        ByteBuffer _b = b.duplicate();
        _b.flip();
        try {
            count += fileChannel.write(_b);
        } catch (IOException ex) {
            // ���Բ�д�����ǲ��ܳ��������ܸ����������������
            LogWriter.warn2(logger, ex, "д�ļ�ʧ�ܡ�");
        }
    }

    public void close() {
        // ɾ��0���ȵ��ļ�
        if (count <= 0 && writingFile != null) {
            writingFile.delete();
        }
        IOHelper.closeSilently(fileChannel);
        fileChannel = null;
        writingFile = null;
        count = 0;
    }
}
