

package com.xt.bcloud.worker;

import com.xt.bcloud.comm.ServerThread;

/**
 * �������ڿ��Ʒ�����������,ֹͣ.
 * @author albert
 */
public class JettyServerController  implements ServerController {

    private Cattle cattle;

    private String resourceBase;  // Ӧ�õķ���·��

    public JettyServerController() {
    }
    
    public void init(Cattle cattle, String resourceBase) {
        this.cattle = cattle;
        this.resourceBase = resourceBase;
    }

    /**
     * ����һͷţ
     * @param cattle
     */
    public void start() {
//        // ��������ʱ���̷߳�ʽ����
//        ServerThread st = new ServerThread(cattle.getPort(), cattle.getContextPath(), resourceBase);
//
//        new Thread(st).start();
    }

    public void stop (boolean forcefully) {

    }

    synchronized public void restart() {

    }

}
