
package com.xt.bcloud.td;

/**
 * 用于输出原始请求或者响应的原始接口。
 * @author albert
 */
public interface Dumpable {

//    /**
//     * 设置是否启用 ？
//     * @param enable
//     */
//    public void setEnable(boolean enable);

    /**
     * 将当前的字节数组(来自请求)写入Dumper文件.
     * @param b
     */
    public void writReq(byte[] b);

    /**
     * 当前的请求结束或者超时.
     */
    public void closeReq();

    /**
     * 将当前的字节数组（来自响应）写入Dumper文件.
     * @param b
     */
    public void writRes(byte[] b);

    /**
     * 当前的响应结束或者超时.
     */
    public void closeRes();

}
