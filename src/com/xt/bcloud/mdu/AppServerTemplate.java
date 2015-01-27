
package com.xt.bcloud.mdu;

import com.xt.core.db.pm.IPersistence;
import java.util.Calendar;

/**
 *
 * @author Albert
 */
public class AppServerTemplate implements IPersistence {
    
    private static final long serialVersionUID = -8821942022211965433L;
    
    private String oid;
        
    private String name;
    
    /**
     * 应用服务器的类型：(TD)TASK_DISPATCHER, APP_SERER(AS),JMS,MDU
     */
    private ServerType serverType;
    
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

    public AppServerTemplate() {
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

    public String getStorePath() {
        return storePath;
    }

    public void setStorePath(String storePath) {
        this.storePath = storePath;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getStartupCmd() {
        return startupCmd;
    }

    public void setStartupCmd(String startupCmd) {
        this.startupCmd = startupCmd;
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

    public boolean isValid() {
        return valid;
    }
    
    public boolean getValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getStopCmd() {
        return stopCmd;
    }

    public void setStopCmd(String stopCmd) {
        this.stopCmd = stopCmd;
    }

    public ServerType getServerType() {
        return serverType;
    }

    public void setServerType(ServerType serverType) {
        this.serverType = serverType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AppServerTemplate other = (AppServerTemplate) obj;
        if ((this.oid == null) ? (other.oid != null) : !this.oid.equals(other.oid)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.oid != null ? this.oid.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "AppServerTemplate{" + "oid=" + oid + ", name=" + name + ", version=" + version + ", storePath=" + storePath + ", params=" + params + '}';
    }
}
