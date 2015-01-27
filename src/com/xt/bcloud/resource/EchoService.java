

package com.xt.bcloud.resource;

import com.xt.core.service.IService;

/**
 * 用于判断服务器是否正在运行的服务类.
 * @author albert
 */
public class EchoService implements IService {

    public EchoService() {
    }

    /**
     * 测试服务器是否可用的接口。
     * @param msg
     * @return 如果消息为空，返回本地时间，如果不为空，返回原始消息。
     */
    public String echo(String msg) {
        return ((msg == null) ? String.valueOf(System.currentTimeMillis()) : msg);
    }

}
