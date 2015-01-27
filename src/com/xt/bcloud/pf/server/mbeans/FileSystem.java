package com.xt.bcloud.pf.server.mbeans;

import com.xt.bcloud.pf.ProfilingException;
import com.xt.core.log.LogWriter;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

/**
 *
 * @author Albert
 */
public class FileSystem implements FileSystemMBean {

    private final Sigar sigar = new Sigar();
    
    private final Logger logger = Logger.getLogger(FileSystem.class);

    public FileSystem() {
    }

    public List<FileSystemInfo> getFileSystem() {
        try {
            org.hyperic.sigar.FileSystem[] fsArr = sigar.getFileSystemList();
            List<FileSystemInfo> fsList = new ArrayList();
            for (org.hyperic.sigar.FileSystem fileSystem : fsArr) {
                FileSystemInfo fsData = gather(sigar, fileSystem);
                if (fsData != null) {
                    fsList.add(fsData);
                }
            }
            return fsList;
        } catch (SigarException ex) {
             throw new ProfilingException("读取服务器文件系统信息时出现异常。", ex);
        }
    }

    private FileSystemInfo gather(Sigar sigar, org.hyperic.sigar.FileSystem fileSystem)
             {
        FileSystemUsage usage;
        try {
            usage = sigar.getFileSystemUsage(fileSystem.getDirName());
        } catch (SigarException ex) {
            LogWriter.warn2(logger, ex, "读取文件系统[%s]时出现异常。", fileSystem.getDirName());
            return null;
           
        }
        FileSystemInfo fis = new FileSystemInfo();
        fis.setFileSystem(fileSystem);
        fis.setFileSystemUsage(usage);
        return fis;
    }
}
