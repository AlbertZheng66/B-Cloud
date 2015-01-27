/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xt.bcloud.pf.server.mbeans;

import java.util.List;

/**
 *
 * @author Albert
 */
public interface FileSystemMBean {
    
    public List<FileSystemInfo> getFileSystem();
    
}
