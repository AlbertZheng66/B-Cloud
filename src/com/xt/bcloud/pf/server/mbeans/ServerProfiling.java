package com.xt.bcloud.pf.server.mbeans;

import com.xt.bcloud.pf.server.ServerProfilingInfo;
import com.xt.core.utils.BeanHelper;
import com.xt.core.utils.VarTemplate;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.NetInterfaceStat;

/**
 *
 * @author Albert
 */
public class ServerProfiling implements ServerProfilingMBean {

    public ServerProfiling() {
    }

    public ServerProfilingInfo getServerProfilingInfo() {
        ServerProfilingInfo serverProfilingInfo = new ServerProfilingInfo();
        Memory mem = new Memory();
        Map params = BeanHelper.getFieldValues(mem.getMem());
        format(params);
        String memInfo = VarTemplate.format("total:${total};used:${used};freePercent:${freePercent}", params, true);
        serverProfilingInfo.setMemory(memInfo);

        // read and combine the information of cpus
        ServerCpu serverCpu = new ServerCpu();
        CPUInfo cpuInfo = serverCpu.getCpuInfo();
        StringBuilder cpu = new StringBuilder();
        cpu.append("{");
        for (int i = 0; i < cpuInfo.getCpuPercs().length; i++) {
            CpuPerc perc = cpuInfo.getCpuPercs()[i];
            params = BeanHelper.getFieldValues(perc);
            format(params);
            cpu.append(VarTemplate.format("[sys:${sys};user:${user};wait:${wait}],", params, true));
        }
        cpu.append("}");
        serverProfilingInfo.setCpu(cpu.toString());

        FileSystemMBean fsBean = new FileSystem();
        List<FileSystemInfo> fileSystems = fsBean.getFileSystem();
        StringBuilder disks = new StringBuilder();
        disks.append("{");
        for (Iterator<FileSystemInfo> it = fileSystems.iterator(); it.hasNext();) {
            FileSystemInfo fileSystemInfo = it.next();
            disks.append("[dirName:").append(fileSystemInfo.getFileSystem().getDirName()).append(";");
            params = BeanHelper.getFieldValues(fileSystemInfo.getFileSystemUsage());
            format(params);
            disks.append(VarTemplate.format("total:${total};used:${used};usePercent:${usePercent}],", params, true));
        }
        disks.append("}");
        serverProfilingInfo.setDisks(disks.toString());

        NetworkMBean networkBean = new Network();
        List<NetworkInfo> networkInfos = networkBean.getNetworkInfo();
        StringBuilder network = new StringBuilder();
        network.append("{");
        for (Iterator<NetworkInfo> it = networkInfos.iterator(); it.hasNext();) {
            NetworkInfo networkInfo = it.next();
            NetInterfaceStat stat = networkInfo.getStat();
            // 忽略传输数据为0的网卡信息
            if (stat.getRxBytes() == 0) {
                continue;
            }
            network.append("[name:").append(networkInfo.getConfig().getName()).append(";");
            params = BeanHelper.getFieldValues(networkInfo.getStat());
            format(params);
            network.append(VarTemplate.format("rxBytes:${rxBytes};txBytes:${txBytes};rxErrors:${rxErrors},", params, true));
        }
        network.append("}");
        serverProfilingInfo.setNetwork(network.toString());
        return serverProfilingInfo;
    }

    private void format(Map map) {
        for (Iterator<Map.Entry> it = map.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = it.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Number) {
                String formatPattern = "#,###,##0.00";
                if (value instanceof Short || value instanceof Integer || value instanceof Long) {
                    formatPattern = "#,###,##0";
                }
                NumberFormat numberFormat = new DecimalFormat(formatPattern);
                value = numberFormat.format(((Number) value).doubleValue());
                map.put(key, value);
            }
        }

    }
}

