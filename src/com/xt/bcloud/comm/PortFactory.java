
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
     * ����ѡ��˿ںŵ������������
     */
    private final Random random = new Random(System.nanoTime());

    /**
     * ��ǰ��Ч�Ķ˿ں�
     */
    private Queue validPorts = new LinkedList();

    private PortFactory() {
        ports.put(DEFAULT_PORT_RANGE_NAME, new PortRange(DEFAULT_PORT_RANGE_NAME, 20000, 30000));
    }

    static public PortFactory getInstance() {
        return instance;
    }

    /**
     * ע��һ���˿ںš�
     * @param portRange
     */
    synchronized  public void register(PortRange portRange) {
        if (portRange == null || StringUtils.isEmpty(portRange.getName())) {
            throw new BadParameterException("ע��Ķ˿ڷ�Χ�������ƾ�����Ϊ�ա�");
        }
        ports.put(portRange.getName(), portRange);
    }

    /**
     * ����һ��Ĭ�ϵĶ˿ں�
     * @return
     */
    synchronized public int getPort () {
        return getPort(null);
    }

    /**
     * ����
     * @return
     */
    synchronized public int getPort (String name) {
        if (StringUtils.isEmpty(name)) {
            name = DEFAULT_PORT_RANGE_NAME;
        }
        PortRange pr = ports.get(name);
        if (pr == null) {
            throw new SystemException(String.format("����[%s]�Ķ˿ڷ�Χ�����ڡ�", name));
        }

        return getPort(pr.getStartIndex(), pr.getEndIndex());
    }

    synchronized public int getPort(int startIndex, int endIndex) throws ResourceException {
        int _port = -1;
        int range = Math.abs(endIndex - startIndex);
        while (!isValid(_port)) {
            _port = random.nextInt(range) + Math.min(startIndex, endIndex);
        }
        
        // У��˿ں��Ƿ�Ϸ�
        if (_port < MIN_PORT_NUMBER && _port > MAX_PORT_NUMBER) {
            throw new ResourceException(String.format("δ�ҵ��Ϸ��Ķ˿ں�[%d]��", _port));
        }

        return _port;
    }

    /**
     * ���һ���˿ں��Ƿ�ռ��
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
     * �黹һ���˿ں�
     * @deprecated 
     * @param port
     */
    public void giveBack(int port) {

    }
}
