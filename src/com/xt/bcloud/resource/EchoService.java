

package com.xt.bcloud.resource;

import com.xt.core.service.IService;

/**
 * �����жϷ������Ƿ��������еķ�����.
 * @author albert
 */
public class EchoService implements IService {

    public EchoService() {
    }

    /**
     * ���Է������Ƿ���õĽӿڡ�
     * @param msg
     * @return �����ϢΪ�գ����ر���ʱ�䣬�����Ϊ�գ�����ԭʼ��Ϣ��
     */
    public String echo(String msg) {
        return ((msg == null) ? String.valueOf(System.currentTimeMillis()) : msg);
    }

}
