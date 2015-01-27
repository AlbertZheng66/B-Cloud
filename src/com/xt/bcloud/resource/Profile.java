
package com.xt.bcloud.resource;

import com.xt.gt.sys.SystemConfiguration;
import java.io.File;

/**
 * 描述一个资源的相关信息
 * @author albert
 */
public class Profile {
    /**
     * 工作路径(必须指定), 默认为应用的当前路径
     */
    private File workspace;

    /**
     * 分配的内存空间(单位:M)，默认为当前系统的所有内存的一半。
     */
    private long ram = Runtime.getRuntime().maxMemory() / 3 * 2 ;

    /**
     * CPU 的个数
     */
    private int cpuCount = Runtime.getRuntime().availableProcessors();

    public Profile() {
        // TODO: 默认空间（Windows平台下：c:\temp; Linux 平台下：/var/temp）
        String workspacePath = SystemConfiguration.getInstance().readString("profile.workspace", "E:\\work\\xthinker\\B-Cloud\\workspace\\temp");
        workspace = new File(workspacePath);
    }

    public int getCpuCount() {
        return cpuCount;
    }

    public void setCpuCount(int cpuCount) {
        this.cpuCount = cpuCount;
    }

    public long getRam() {
        return ram;
    }

    public void setRam(long ram) {
        this.ram = ram;
    }

    public File getWorkspace() {
        return workspace;
    }

    public void setWorkspace(File workspace) {
        this.workspace = workspace;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Profile other = (Profile) obj;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }
    
}
