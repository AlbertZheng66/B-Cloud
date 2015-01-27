package com.xt.bcloud.pf.server.mbeans;

import com.xt.bcloud.pf.ProfilingException;
import java.util.ArrayList;
import java.util.List;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

/**
 *
 * @author Albert
 */
public class Network implements NetworkMBean {

    private final Sigar sigar = new Sigar();

    public Network() {
    }

    public List<NetworkInfo> getNetworkInfo() {
        List<NetworkInfo> netIfList = new ArrayList();
        try {
            String[] netIfs = sigar.getNetInterfaceList();
            for (String name : netIfs) {
                NetworkInfo netIfData1 = gather(sigar, name);
                netIfList.add(netIfData1);
            }
        } catch (SigarException ex) {
            throw new ProfilingException("读取服务器网络信息时出现异常。", ex);
        }
        return netIfList;
    }

    private NetworkInfo gather(Sigar sigar, String name)
            throws SigarException {
        NetworkInfo info = new NetworkInfo();
        NetInterfaceStat stat = sigar.getNetInterfaceStat(name);
        info.setStat(stat);
        NetInterfaceConfig config = sigar.getNetInterfaceConfig(name);
        info.setConfig(config);
        return info;
    }
}
