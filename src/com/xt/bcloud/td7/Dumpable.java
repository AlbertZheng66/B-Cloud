
package com.xt.bcloud.td7;

import java.nio.ByteBuffer;

/**
 * 标准数据导出接口，用于导出Http请求及响应，尤其是系统出现异常时。
 * @author Albert
 */
public interface Dumpable {
    
    public void setFilenamePattern(String fileNamePattern);
    
    public void open();
    
    /**
     * 将当前的字节数组写入Dumper文件.
     * @param b
     */
    public void write(ByteBuffer[] b);
    
    /**
     * 将当前的字节数组写入Dumper文件.
     * @param b
     */
    public void write(ByteBuffer b);

    /**
     * 当前的请求结束或者超时.
     */
    public void close();
}
