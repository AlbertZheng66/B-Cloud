
package com.xt.bcloud.td7;

/**
 * 代表了一个转发的过程
 * @author Albert
 */
public class Process {
    /**
     * 处理编号
     */
    private String oid;
    
    /**
     * 创建的客户端连接对象，可能是ClientSocket，或者其他对象。
     */
    private Object client;
    
    /**
     * 读取的请求消息
     */
    private Message requestMessage = null;
    
    /**
     * 来自服务器的响应消息
     */
    private Message responseMessage = null;
    
    /**
     * 
     */
    private Object server;
    
    
}
