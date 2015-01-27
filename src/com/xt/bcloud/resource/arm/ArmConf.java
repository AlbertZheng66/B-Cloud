

package com.xt.bcloud.resource.arm;

import com.xt.bcloud.comm.CloudUtils;
import com.xt.gt.sys.SystemConfiguration;

/**
 * ����Դ��������ص����á�
 * @author albert
 */
public class ArmConf {
    /**
     * Ӧ������Դ��������ϵͳ�����ļ��еĲ�������--������
     */
    public static final String PARAM_CONTEXT = "arm.url.context";
    /**
     * Ӧ������Դ��������ϵͳ�����ļ��еĲ�������--IP��ַ
     */
    public static final String PARAM_IP = "arm.url.ip";
    /**
     * Ӧ������Դ��������ϵͳ�����ļ��еĲ�������--�˿ں�
     */
    public static final String PARAM_PORT = "arm.url.port";
    /**
     * Ӧ������Դ��������ϵͳ�����ļ��еĲ�������--Э��
     */
    public static final String PARAM_PROTOCOL = "arm.url.protocol";

    /**
     * ��Դ��Ӧ�ù���������ķ���Э�顣
     */
    public static final String PROTOCOL = SystemConfiguration.getInstance().readString(PARAM_PROTOCOL,"http");

    /**
     * ��Դ��Ӧ�ù���������ķ��ʵ�ַ��
     */
    public static final String IP = SystemConfiguration.getInstance().readString(PARAM_IP, CloudUtils.getLocalHostAddress());

    /**
     * ARM ��Ĭ�϶˿ں�
     */
    public static final int WELL_KNOWN_PORT = 58080;

    /**
     * ��Դ��Ӧ�ù���������ķ��ʶ˿ڡ�
     */
     public static final int PORT = SystemConfiguration.getInstance().readInt(PARAM_PORT, WELL_KNOWN_PORT);

    /**
     * ��Դ��Ӧ�ù���������ķ��������ģ�·������
     */
     public static final String CONTEXT = SystemConfiguration.getInstance().readString(PARAM_CONTEXT,"arMgr/");

    /**
     * ��Դ��Ӧ�ù���������ķ��ʵ�ַ(ȫ·��)��
     */
    public static final String URL = String.format("%s://%s:%d/%s",
            PROTOCOL, IP, PORT, CONTEXT);
    
    /**
     * ��Դ��Ӧ�ù���������ķ��ʵ�ַ(ȫ·��)��
     */
    public static final String[] URLS;
    
    static {
        URLS = SystemConfiguration.getInstance().readStrings("arm.urls");
    }

}
