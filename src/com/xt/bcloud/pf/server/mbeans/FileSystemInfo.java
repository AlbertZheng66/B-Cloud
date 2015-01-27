
package com.xt.bcloud.pf.server.mbeans;

import java.io.Serializable;
import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.FileSystemUsage;

/**
 *
 * @author Albert
 */
public class FileSystemInfo implements Serializable {
    
    private static final long serialVersionUID = 3847280728598524899L;

    private FileSystem fileSystem;
    
    private FileSystemUsage fileSystemUsage;

    public FileSystemInfo() {
    }

    public FileSystem getFileSystem() {
        return fileSystem;
    }

    public void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    public FileSystemUsage getFileSystemUsage() {
        return fileSystemUsage;
    }

    public void setFileSystemUsage(FileSystemUsage fileSystemUsage) {
        this.fileSystemUsage = fileSystemUsage;
    }

    @Override
    public String toString() {
        return "FileSystemInfo{" + "fileSystem=" + fileSystem + ", fileSystemUsage=" + fileSystemUsage + '}';
    }
    
}
