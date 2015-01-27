
package com.xt.bcloud.resource;

import com.xt.gt.sys.SystemConfiguration;
import java.io.File;

/**
 * ����һ����Դ�������Ϣ
 * @author albert
 */
public class Profile {
    /**
     * ����·��(����ָ��), Ĭ��ΪӦ�õĵ�ǰ·��
     */
    private File workspace;

    /**
     * ������ڴ�ռ�(��λ:M)��Ĭ��Ϊ��ǰϵͳ�������ڴ��һ�롣
     */
    private long ram = Runtime.getRuntime().maxMemory() / 3 * 2 ;

    /**
     * CPU �ĸ���
     */
    private int cpuCount = Runtime.getRuntime().availableProcessors();

    public Profile() {
        // TODO: Ĭ�Ͽռ䣨Windowsƽ̨�£�c:\temp; Linux ƽ̨�£�/var/temp��
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
