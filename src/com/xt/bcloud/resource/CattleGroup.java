
package com.xt.bcloud.resource;

import com.xt.bcloud.worker.Cattle;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.View;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;

/**
 *
 * @author albert
 */
public class CattleGroup {
    
    
    /**
     * ÿ��Ӧ��һ��������
     */
    public static final String CATTLE_MGR_GROUP_PREFIX = "CattleMgrGroup_";

    private final Cattle cattle;

    /**
     * ͨ��ͨ��
     */
    private JChannel cattleMgrChannel;

     /**
     * �Ƿ�ֹͣ�ı�־
     */
    private volatile boolean stoped = false;

    public CattleGroup(Cattle cattle) {
        this.cattle = cattle;
    }


    public void start() {
        // ���߳���Ҫһֱ������
        while (!stoped) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Cattle.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * ����һ��Ⱥ��(����Ҫ��Ϊ�˹���֮��)
     */
    private void joinGroup() {
        if (cattleMgrChannel != null) {
            return;
        }
        try {
            File file = new File("E:\\work\\xthinker\\B-Cloud\\src\\files\\tcp.1.xml");
            cattleMgrChannel = new JChannel(file);
//            cattleMgrChannel = new JChannel("UDP(mcast_addr=224.10.10.10;mcast_port=5679)");
            cattleMgrChannel.setReceiver(new ReceiverAdapter() {

                @Override
                public void receive(Message msg) {
                    System.out.println("received msg from " + msg.getSrc() + ": " + msg.getObject());
                }

                public void viewAccepted(View new_view) {
                    System.out.println("received view " + new_view);
                }
            });
            cattleMgrChannel.connect(CATTLE_MGR_GROUP_PREFIX + cattle.getApp().getOid());
        } catch (Exception ex) {
            Logger.getLogger(Cattle.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

     /**
     * ֹͣ��ͷţ�Ĺ����������ͷţ��������δ��ɣ���
     */
    public void stop() {
//        if (server == null) {
//            return;
//        }
//
//        // �����������δ����������ȴ�����
//        try {
//            server.stop();
//            server = null;
//        } catch (Exception ex) {
//            throw new SystemException("ֹͣ�����������쳣��", ex);
//        }
    }


    /**
     * ��������������
     */
    private void reset() {
//        server.stop();
//        server.start();
    }

}
