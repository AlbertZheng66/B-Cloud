/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xt.bcloud.td;

import com.xt.gt.sys.SystemConfiguration;

/**
 *
 * @author Albert
 */
public class Contants {
    
     /**
     * 连接超时的处理时间，单位为毫秒，如果指定小于等于0的数值，
     * 表示不对连接时间进行限制，默认为：30秒。
     */
    public static final int CLIENT_CONNECTION_TIMEOUT = 30 * 1000;
    
     /**
     * 核心线程池的大小，即使空闲，系统也会保留指定数量的线程。默认为：100。
     */
    public static final int CORE_POOL_SIZE = SystemConfiguration.getInstance().readInt("executor.corePoolSize", 100);

    /**
     * 可启动的最大线程数。默认为：1000。
     */
    public static final int MAX_POOL_SIZE = SystemConfiguration.getInstance().readInt("executor.maxPoolSize", 1000);

     /**
     * 线程的最大执行时间，单位：秒。默认为：10 * 60。
     */
    public static final int KEEP_ALIVE_TIME = SystemConfiguration.getInstance().readInt("executor.keepAliveTime", 10 * 60);

     /**
     * 连接上行（浏览器 --> 代理）的超时时间，单位为毫秒，如果指定小于等于0的数值，
     * 表示不对连接时间进行限制，默认为：300秒。
     */
    public static final int upTimeout = 300 * 1000;
    
    /**
     * 连接下行（原始服务器 --> 代理）的超时时间，单位为毫秒，如果指定小于等于0的数值，
     * 表示不对连接时间进行限制，默认为：300秒。
     */
    public static final int downTimeout = 300 * 1000;
}
