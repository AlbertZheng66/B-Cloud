package com.xt.bcloud.comm;

import com.xt.core.utils.DateUtils;
import java.util.Calendar;

/**
 * 此平台中用到的系统常量.
 * @author albert
 */
public class Constants {
    /**
     * 资源管理器的端口(默认)
     */
    public final static int PORT_RESOURCE_MGR = 5088;

    /**
     * 端口号的范围：应用的范围
     */
    public final static PortRange PORT_RANGE_APP_MGR = new PortRange("appMgr", 60000, 62000);

    /**
     * 负载均衡器的范围
     */
    public final static PortRange PORT_RANGE_LOAD_BALANCE_MGR = new PortRange("loadBalanceMgr", 62000, 64000);

    /**
     * 农场管理器的范围。
     */
    public final static PortRange PORT_RANGE_FARM_MGR = new PortRange("farmMgr", 58000, 60000);

    /**
     * 服务器的标准容量
     */
    public final static Capacity STANDARD_CAPACITY = new Capacity();


    /**
     * MySQL 在处理空时间时有问题，所以在时间字段为空的情况下，使用此值进行填充。
     */
    public final static Calendar INVALID_TIME = DateUtils.parseCalendar("1970-01-01 23:23:59", "yyyy-MM-dd HH:mm:ss");
    
     /**
     * 部署服务器的超时时间
     */
    public final static long DEPLOYING_SERVICE_CONNECT_TIMEOUT = 30*1000;
    
    /**
     * 代表应用服务器实例的OID（可在系统参数和配置参数中使用）
     */
    public final static String APP_SERVER_INSTANCE_OID = "appServerInstanceOid";
    
    /**
     * 代表应用服务器模板的OID（可在系统参数和配置参数中使用）
     */
    public final static String APP_SERVER_TEMPLATE_OID = "appServerTemplateOid";

    
}
