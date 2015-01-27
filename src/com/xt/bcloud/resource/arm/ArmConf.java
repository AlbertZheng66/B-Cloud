

package com.xt.bcloud.resource.arm;

import com.xt.bcloud.comm.CloudUtils;
import com.xt.gt.sys.SystemConfiguration;

/**
 * 和资源管理器相关的配置。
 * @author albert
 */
public class ArmConf {
    /**
     * 应用于资源管理器在系统配置文件中的参数名称--上下文
     */
    public static final String PARAM_CONTEXT = "arm.url.context";
    /**
     * 应用于资源管理器在系统配置文件中的参数名称--IP地址
     */
    public static final String PARAM_IP = "arm.url.ip";
    /**
     * 应用于资源管理器在系统配置文件中的参数名称--端口号
     */
    public static final String PARAM_PORT = "arm.url.port";
    /**
     * 应用于资源管理器在系统配置文件中的参数名称--协议
     */
    public static final String PARAM_PROTOCOL = "arm.url.protocol";

    /**
     * 资源和应用管理服务器的访问协议。
     */
    public static final String PROTOCOL = SystemConfiguration.getInstance().readString(PARAM_PROTOCOL,"http");

    /**
     * 资源和应用管理服务器的访问地址。
     */
    public static final String IP = SystemConfiguration.getInstance().readString(PARAM_IP, CloudUtils.getLocalHostAddress());

    /**
     * ARM 的默认端口号
     */
    public static final int WELL_KNOWN_PORT = 58080;

    /**
     * 资源和应用管理服务器的访问端口。
     */
     public static final int PORT = SystemConfiguration.getInstance().readInt(PARAM_PORT, WELL_KNOWN_PORT);

    /**
     * 资源和应用管理服务器的访问上下文（路径）。
     */
     public static final String CONTEXT = SystemConfiguration.getInstance().readString(PARAM_CONTEXT,"arMgr/");

    /**
     * 资源和应用管理服务器的访问地址(全路径)。
     */
    public static final String URL = String.format("%s://%s:%d/%s",
            PROTOCOL, IP, PORT, CONTEXT);
    
    /**
     * 资源和应用管理服务器的访问地址(全路径)。
     */
    public static final String[] URLS;
    
    static {
        URLS = SystemConfiguration.getInstance().readStrings("arm.urls");
    }

}
