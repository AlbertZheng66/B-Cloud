
package com.xt.bcloud.comm;

import com.xt.bcloud.resource.ResourceException;
import com.xt.core.exception.BadParameterException;
import com.xt.core.exception.SystemException;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author albert
 */
public class PortFactory {
    public static final String DEFAULT_PORT_RANGE_NAME = "default";
    public static final int MAX_PORT_NUMBER = 65535;
    public static final int MIN_PORT_NUMBER = 1025;

    private static PortFactory instance = new  PortFactory();

    private Map<String, PortRange> ports = new HashMap();

    /**
     * 用于选择端口号的随机数发生器
     */
    private final Random random = new Random(System.nanoTime());

    /**
     * 当前生效的端口号
     */
    private Queue validPorts = new LinkedList();

    private PortFactory() {
        ports.put(DEFAULT_PORT_RANGE_NAME, new PortRange(DEFAULT_PORT_RANGE_NAME, 20000, 30000));
    }

    static public PortFactory getInstance() {
        return instance;
    }

    /**
     * 注册一个端口号。
     * @param portRange
     */
    synchronized  public void register(PortRange portRange) {
        if (portRange == null || StringUtils.isEmpty(portRange.getName())) {
            throw new BadParameterException("注册的端口范围及其名称均不能为空。");
        }
        ports.put(portRange.getName(), portRange);
    }

    /**
     * 返回一个默认的端口号
     * @return
     */
    synchronized public int getPort () {
        return getPort(null);
    }

    /**
     * 申请
     * @return
     */
    synchronized public int getPort (String name) {
        if (StringUtils.isEmpty(name)) {
            name = DEFAULT_PORT_RANGE_NAME;
        }
        PortRange pr = ports.get(name);
        if (pr == null) {
            throw new SystemException(String.format("名称[%s]的端口范围不存在。", name));
        }

        return getPort(pr.getStartIndex(), pr.getEndIndex());
    }

    synchronized public int getPort(int startIndex, int endIndex) throws ResourceException {
        int _port = -1;
        int range = Math.abs(endIndex - startIndex);
        while (!isValid(_port)) {
            _port = random.nextInt(range) + Math.min(startIndex, endIndex);
        }
        
        // 校验端口号是否合法
        if (_port < MIN_PORT_NUMBER && _port > MAX_PORT_NUMBER) {
            throw new ResourceException(String.format("未找到合法的端口号[%d]。", _port));
        }

        return _port;
    }

    /**
     * 检查一个端口号是否被占用
     * @param port
     * @return
     */
    public boolean isValid(int port) {
        if (port < MIN_PORT_NUMBER || port > MAX_PORT_NUMBER) {
            return false;
        }
        boolean valid = false;
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(port);
            ss.close();
            valid = true;
        } catch (IOException e) {
            valid = false;
        }
        return valid;
    }

    /**
     * 归还一个端口号
     * @deprecated 
     * @param port
     */
    public void giveBack(int port) {

    }
}
