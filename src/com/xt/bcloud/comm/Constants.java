package com.xt.bcloud.comm;

import com.xt.core.utils.DateUtils;
import java.util.Calendar;

/**
 * ��ƽ̨���õ���ϵͳ����.
 * @author albert
 */
public class Constants {
    /**
     * ��Դ�������Ķ˿�(Ĭ��)
     */
    public final static int PORT_RESOURCE_MGR = 5088;

    /**
     * �˿ںŵķ�Χ��Ӧ�õķ�Χ
     */
    public final static PortRange PORT_RANGE_APP_MGR = new PortRange("appMgr", 60000, 62000);

    /**
     * ���ؾ������ķ�Χ
     */
    public final static PortRange PORT_RANGE_LOAD_BALANCE_MGR = new PortRange("loadBalanceMgr", 62000, 64000);

    /**
     * ũ���������ķ�Χ��
     */
    public final static PortRange PORT_RANGE_FARM_MGR = new PortRange("farmMgr", 58000, 60000);

    /**
     * �������ı�׼����
     */
    public final static Capacity STANDARD_CAPACITY = new Capacity();


    /**
     * MySQL �ڴ����ʱ��ʱ�����⣬������ʱ���ֶ�Ϊ�յ�����£�ʹ�ô�ֵ������䡣
     */
    public final static Calendar INVALID_TIME = DateUtils.parseCalendar("1970-01-01 23:23:59", "yyyy-MM-dd HH:mm:ss");
    
     /**
     * ����������ĳ�ʱʱ��
     */
    public final static long DEPLOYING_SERVICE_CONNECT_TIMEOUT = 30*1000;
    
    /**
     * ����Ӧ�÷�����ʵ����OID������ϵͳ���������ò�����ʹ�ã�
     */
    public final static String APP_SERVER_INSTANCE_OID = "appServerInstanceOid";
    
    /**
     * ����Ӧ�÷�����ģ���OID������ϵͳ���������ò�����ʹ�ã�
     */
    public final static String APP_SERVER_TEMPLATE_OID = "appServerTemplateOid";

    
}
