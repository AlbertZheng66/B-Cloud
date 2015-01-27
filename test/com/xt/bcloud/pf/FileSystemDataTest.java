/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xt.bcloud.pf;

import java.util.ArrayList;
import java.util.List;
import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

/**
 *
 * @author Albert
 */
public class FileSystemDataTest {
    private FileSystem config;  
    private FileSystemUsage stat;  
  
    public FileSystemDataTest() {}  
  
    public void populate(Sigar sigar, FileSystem fs)  
        throws SigarException {  
  
        config = fs;  
  
        try {  
            stat = sigar.getFileSystemUsage(fs.getDirName());  
        } catch (SigarException e) {  
              
        }  
    }  
  
    public static FileSystemDataTest gather(Sigar sigar, FileSystem fs)  
        throws SigarException {  
      
        FileSystemDataTest data = new FileSystemDataTest();  
        data.populate(sigar, fs);  
        return data;  
    }  
  
    public FileSystem getConfig() {  
        return config;  
    }  
  
    public FileSystemUsage getStat() {  
        return stat;  
    }

    @Override
    public String toString() {
        return "FileSystemDataTest{" + "config=" + config + ", stat=" + stat + '}';
    }
    
    public static void main(String[] args) throws Exception {  
        Sigar sigar = new Sigar();  
        FileSystem[] fsArr = sigar.getFileSystemList();  
        List fsList = new ArrayList();  
        for ( FileSystem fs:fsArr ) {  
            FileSystemDataTest fsData = FileSystemDataTest.gather(sigar, fs);  
            fsList.add(fsData);  
        }  
        System.out.println(fsList);  
    }     
}
