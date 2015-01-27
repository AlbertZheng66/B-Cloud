/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xt.bcloud.pf.connector;

import com.xt.core.utils.VarTemplate;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 *
 * @author Albert
 * 
 * Á´½Ó×Ö·û´®£ºservice:jmx:rmi:///jndi/rmi://192.168.29.252:8877/jmxrmi
 * 
 * OPERATING_SYSTEM_MXBEAN_NAME
 * ----------------------------------
FreePhysicalMemorySize = 451383296
TotalPhysicalMemorySize = 2136199168
TotalSwapSpaceSize = 4294967295
CommittedVirtualMemorySize = 47607808
FreeSwapSpaceSize = 1806827520
ProcessCpuLoad = -1.0
ProcessCpuTime = 1716011000
SystemCpuLoad = 0.5224469387755102
AvailableProcessors = 2
Arch = x86
SystemLoadAverage = -1.0
Name = Windows Vista
Version = 6.0
ObjectName = java.lang:type=OperatingSystem
 * 
 * * OPERATING_SYSTEM_MXBEAN_NAME
 * ----------------------------------
 */
public class RmiConnector {

    private final static int DEFAULT_PORT = 8877;
    private int port = DEFAULT_PORT;
    private String host = "127.0.0.1";
    private final static String URL_TEMPLATE = "service:jmx:rmi:///jndi/rmi://${host}:${port}/jmxrmi";
    private MBeanServer mbs = null;

    public RmiConnector getClient() {

        mbs = ManagementFactory.getPlatformMBeanServer();


        try {
            // Create an RMI connector client 
            // 
            Map params = createParams();
            String strUrl = VarTemplate.format(URL_TEMPLATE, params);
            JMXServiceURL url = new JMXServiceURL(strUrl);
            JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
            ClientListener listener = new ClientListener();
            MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
            
            //waitForEnterPressed();

            // Get domains from MBeanServer 
            // 
            String domains[] = mbsc.getDomains();
            for (int i = 0; i < domains.length; i++) {
                System.out.println("Domain[" + i + "] = " + domains[i]);
            }


            String domain = mbsc.getDefaultDomain();


            // Query MBean names 
            Set names = mbsc.queryNames(null, null);
            for (Iterator i = names.iterator(); i.hasNext();) {
                System.out.println("ObjectName = " + (ObjectName) i.next());
            }
//            print(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME);
//            print(ManagementFactory.CLASS_LOADING_MXBEAN_NAME);
//            print(ManagementFactory.COMPILATION_MXBEAN_NAME);
            // print(ManagementFactory.getGarbageCollectorMXBeans());
            print(ManagementFactory.MEMORY_MXBEAN_NAME);
            print("java.lang:type=MemoryPool,name=Perm Gen");
//            print(ManagementFactory.RUNTIME_MXBEAN_NAME);
//            print(ManagementFactory.THREAD_MXBEAN_NAME);

//            mbsc.setAttribute(stdMBeanName,
//                    new Attribute("State", "changed state"));
//
//            SimpleStandardMBean proxy = JMX.newMBeanProxy(
//                    mbsc, stdMBeanName, SimpleStandardMBean.class, true);
//            echo("\nState = " + proxy.getState());

//            mbsc.addNotificationListener(stdMBeanName, listener, null, null);
//
//            mbsc.invoke(stdMBeanName, "reset", null, null);
//
//            mbsc.removeNotificationListener(stdMBeanName, listener);
//            mbsc.unregisterMBean(stdMBeanName);

            jmxc.close();
//            VirtualMachine m = VirtualMachine.
                 
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            
        }

        return null;
    }

    private void print(String objName) {
//        List<MemoryPoolMXBean> xb = java.lang.management.ManagementFactory.getMemoryPoolMXBeans();
//        for (Iterator<MemoryPoolMXBean> it = xb.iterator(); it.hasNext();) {
//            MemoryPoolMXBean memoryPoolMXBean = it.next();
//            System.out.println(memoryPoolMXBean.getName() + " = " + memoryPoolMXBean.getType() + ";" + memoryPoolMXBean);
//        }
        
        System.out.println("----------- " + objName + " ------------");
        try {
            // Assuming the OperatingSystem MXBean has been registered in mbs
            ObjectName oname = new ObjectName(objName);

            MBeanInfo info = mbs.getMBeanInfo(oname);

            for (int i = 0; i < info.getAttributes().length; i++) {
                MBeanAttributeInfo attr = info.getAttributes()[i];
                Object value = mbs.getAttribute(oname, attr.getName());
                System.out.println(attr.getName() + " = " + value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map createParams() {
        Map params = new HashMap();
        params.put("host", host);
        params.put("port", String.valueOf(port));
        return params;
    }

    class ClientListener implements NotificationListener {

        public void handleNotification(Notification notification, Object handback) {
            System.out.println("\nReceived notification: " + notification);
        }
    }
}
