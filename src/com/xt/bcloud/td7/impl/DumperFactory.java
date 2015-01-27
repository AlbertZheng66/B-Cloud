
package com.xt.bcloud.td7.impl;

import com.xt.bcloud.td7.Dumpable;
import com.xt.gt.sys.SystemConfiguration;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Albert
 */
public class DumperFactory {
    
    private volatile static DumperFactory instance = new DumperFactory();
    /**
     * �ر��������
     */
    private final boolean disabled = SystemConfiguration.getInstance().readBoolean("dumper.disabled", true);
    
    /**
     * �ر��������
     */
    private final Set<String> disabledPrefixes = Collections.synchronizedSet(new HashSet<String>());
    
    private DumperFactory() {
        String[] prefixes = SystemConfiguration.getInstance().readStrings("dumper.disabledPrefixes");
        if (prefixes != null) {
            disabledPrefixes.addAll(Arrays.asList(prefixes));
        }
    }
    
    public static DumperFactory getInstance() {
        return instance;
    }
    
     /**
     * ����ǰ���ֽ�����д��Dumper�ļ�.
     * @param b
     */
    public void write(String prefix, SocketAddress socketAddress, ByteBuffer[] b) {
        if (isDisabled(prefix)) {
            return;
        }
        final Dumpable dumpable = new DefaultDumper();
        String fileName = getFilename(prefix, socketAddress);
        dumpable.setFilenamePattern(fileName);
        dumpable.open();
        dumpable.write(b);
        dumpable.close();
    }
    
    /**
     * ����ǰ���ֽ�����д��Dumper�ļ�.
     * @param b
     */
    public void write(String prefix, SocketAddress socketAddress, ByteBuffer b) {
        if (isDisabled(prefix)) {
            return;
        }
        final Dumpable dumpable = new DefaultDumper();
        String fileName = getFilename(prefix, socketAddress);
        dumpable.setFilenamePattern(fileName);
        dumpable.open();
        dumpable.write(b);
        dumpable.close();
    }

    private String getFilename(String prefix, SocketAddress socketAddress) {
        String ip;
        if (socketAddress instanceof InetSocketAddress) {
            InetSocketAddress isa = (InetSocketAddress)socketAddress;
            ip =  String.format("%s_%d", isa.getHostString(), isa.getPort());
        } else {
            ip = "unknown";
        }
        String fileName = String.format("%s_%s_${_time}_${_random}.dump", prefix, ip);
        return fileName;
    }
    
    
    private boolean isDisabled(String prefix) {
        if (disabled) {
            return true;
        }
        if (prefix == null) {
            return false;
        }
        return disabledPrefixes.contains(prefix);
    }
}
