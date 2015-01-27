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
     * ���ӳ�ʱ�Ĵ���ʱ�䣬��λΪ���룬���ָ��С�ڵ���0����ֵ��
     * ��ʾ��������ʱ��������ƣ�Ĭ��Ϊ��30�롣
     */
    public static final int CLIENT_CONNECTION_TIMEOUT = 30 * 1000;
    
     /**
     * �����̳߳صĴ�С����ʹ���У�ϵͳҲ�ᱣ��ָ���������̡߳�Ĭ��Ϊ��100��
     */
    public static final int CORE_POOL_SIZE = SystemConfiguration.getInstance().readInt("executor.corePoolSize", 100);

    /**
     * ������������߳�����Ĭ��Ϊ��1000��
     */
    public static final int MAX_POOL_SIZE = SystemConfiguration.getInstance().readInt("executor.maxPoolSize", 1000);

     /**
     * �̵߳����ִ��ʱ�䣬��λ���롣Ĭ��Ϊ��10 * 60��
     */
    public static final int KEEP_ALIVE_TIME = SystemConfiguration.getInstance().readInt("executor.keepAliveTime", 10 * 60);

     /**
     * �������У������ --> �����ĳ�ʱʱ�䣬��λΪ���룬���ָ��С�ڵ���0����ֵ��
     * ��ʾ��������ʱ��������ƣ�Ĭ��Ϊ��300�롣
     */
    public static final int upTimeout = 300 * 1000;
    
    /**
     * �������У�ԭʼ������ --> �����ĳ�ʱʱ�䣬��λΪ���룬���ָ��С�ڵ���0����ֵ��
     * ��ʾ��������ʱ��������ƣ�Ĭ��Ϊ��300�롣
     */
    public static final int downTimeout = 300 * 1000;
}
