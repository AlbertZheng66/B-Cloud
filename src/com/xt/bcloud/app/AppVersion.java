

package com.xt.bcloud.app;

import com.xt.core.db.pm.IPersistence;
import com.xt.gt.ui.table.ColumnInfo;
import java.util.Calendar;

/**
 * 应用程序的版本.
 * 版本实现了比较接口, 通过"."分割的字符串进行比较大小。
 * 比如：2.30 小于 3.2, 自然序的倒序进行排列，即：4.0 > 3.2 > 3.1 > 2.5。
 * 注意：如果两个版本的长度不相等，只比较相等部分：即，2.3.3 == 2.3
 * @author albert
 */
public class AppVersion implements IPersistence, Comparable<AppVersion> {

    
    private static final long serialVersionUID = 4554230348502590060L;
        
    /**
     * 内部编码
     */
    private String oid;

    /**
     * 应用编码
     */
     private String appOid;

     /**
      * 版本号
      */
    @ColumnInfo(title="版本号")
    private String version;

    /**
     * 开发代码
     */
    @ColumnInfo(title="开发代码")
    private String code;

    /**
     * 描述信息
     */
    @ColumnInfo(title="描述信息")
    private String description;

    /**
     * 发布包的位置
     */
    private String deployFileName;

    /**
     * 应用的上下文
     * 使用了此上下文,任务分配器将自动调整"URL"的路径
     */
    @ColumnInfo(title = "上下文路径")
    private String contextPath;

    /**
     * 上传的文件流（即用户发布的文件流），注意：不持久化此字段，
     * 将上传的包保存到文件系统中。
     */
//    @Transient
//    private InputStream uploadingDeployedFile;

    // private String seq; //发布序列

    /**
     * 此版本是否可用, Y/N
     */
    @ColumnInfo(title="是否生效")
    private boolean valid;

    /**
     * 创建时间
     */
    @ColumnInfo(title="创建时间")
    private Calendar insertTime;

    /**
     * 发布文件大小（字节数）
     */
    @ColumnInfo(title="文件大小")
    private int fileSize;

    /**
     * 当前版本的状态：运行，测试，未发布
     */
    @ColumnInfo(title="状态")
    private AppVersionState state;

    public AppVersion() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AppVersion other = (AppVersion) obj;
        if ((this.oid == null) ? (other.oid != null) : !this.oid.equals(other.oid)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.oid != null ? this.oid.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder strBld = new StringBuilder();
        strBld.append(super.toString()).append("[");
        strBld.append("oid=").append(oid).append("; ");
        strBld.append("appOid=").append(appOid).append("; ");
        strBld.append("version=").append(version).append("; ");
        strBld.append("contextPath=").append(contextPath).append("; ");
        strBld.append("state=").append(state).append("; ");
        strBld.append("fileSize=").append(fileSize).append("; ");
        strBld.append("code=").append(code);
        strBld.append("]");
        return strBld.toString();
    }

    public String getAppOid() {
        return appOid;
    }

    public void setAppOid(String appOid) {
        this.appOid = appOid;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDeployFileName() {
        return deployFileName;
    }

    public void setDeployFileName(String deployFileName) {
        this.deployFileName = deployFileName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Calendar getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Calendar insertTime) {
        this.insertTime = insertTime;
    }

    public AppVersionState getState() {
        return state;
    }

    public void setState(AppVersionState state) {
        this.state = state;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }



    public int compareTo(AppVersion o) {
        if (o == null || o.version == null) {
            return 1;
        }
        if (this.version == null) {
            return -1;
        }
        String[] otherSegs = o.version.split("[.]");
        String[] thisSegs  = version.split("[.]");
        for (int i = 0; i < Math.min(thisSegs.length, otherSegs.length); i++) {
            int value = thisSegs[i].compareTo(otherSegs[i]);
            if (value != 0) {
                // 最老的版本排在前面
                return (value);
            }
        }
        return 0;
    }

}
