
package com.xt.bcloud.pf.server.mbeans;

import com.xt.bcloud.pf.server.ServerProfilingInfo;

/**
 * 服务器总体的汇总信息，避免多次读取服务器。
 * @author Albert
 */
public interface ServerProfilingMBean {
    public ServerProfilingInfo getServerProfilingInfo();
}
