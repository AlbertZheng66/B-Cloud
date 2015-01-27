/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xt.bcloud.resource;

import com.xt.core.db.pm.IPersistence;
import java.util.Calendar;

/**
 * @deprecated 
 * @author Albert
 */
public class TaskDispatcherTemplate  implements IPersistence {
    private static final long serialVersionUID = -1285340338580741242L;
    
    
     private String oid;
    
    private String name;
    
    /**
     * 版本号
     */
    private String version;
    
    /**
     * 存储位置
     */
    private String storePath;
    
    /**
     * 参数配置
     */
    private String params;
    
    /**
     * 启动命令模板
     */
    private String startupCmd;
    
    /**
     * 停止命令模板
     */
    private String stopCmd;
    
    /**
     * 文件大小
     */
    private long fileSize;
    
    private boolean valid;
    
    private Calendar insertTime;

    public TaskDispatcherTemplate() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TaskDispatcherTemplate other = (TaskDispatcherTemplate) obj;
        if ((this.oid == null) ? (other.oid != null) : !this.oid.equals(other.oid)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.oid != null ? this.oid.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "TaskDispatcherTemplate{" + "oid=" + oid + ", name=" + name + ", version=" + version + ", storePath=" + storePath + ", params=" + params + ", startupCmd=" + startupCmd + ", stopCmd=" + stopCmd + ", fileSize=" + fileSize + ", valid=" + valid + ", insertTime=" + insertTime + '}';
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public Calendar getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Calendar insertTime) {
        this.insertTime = insertTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getStartupCmd() {
        return startupCmd;
    }

    public void setStartupCmd(String startupCmd) {
        this.startupCmd = startupCmd;
    }

    public String getStopCmd() {
        return stopCmd;
    }

    public void setStopCmd(String stopCmd) {
        this.stopCmd = stopCmd;
    }

    public String getStorePath() {
        return storePath;
    }

    public void setStorePath(String storePath) {
        this.storePath = storePath;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
    
    
    
}
