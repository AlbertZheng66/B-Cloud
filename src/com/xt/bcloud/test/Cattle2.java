package com.xt.bcloud.test;

import com.xt.bcloud.worker.*;
import com.xt.bcloud.comm.Load;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

/**
 * TODO:����ӿ���ͨ�����顱���ǡ�Socket������
 * һ���߳�ռ�ô�Լ 1M RAM
 * @author albert
 */
public class Cattle2 {

    private boolean created = false;  // ��ʶ�Ƿ��Ѿ�����

    /**
     * ���ڡ�ţ�������ͨ��ͨ��
     */
    private JChannel cattleMgrChannel;

    /**
     * �Ƿ�ֹͣ�ı�־
     */
    private boolean stoped = false;

    // ScoreThread ����ά���̣߳�ע��: �߳�ʹ�ú��ʵ����ȼ���
    // Rancher     �̣߳�ֻ�е�ǰ�������ǹ����ߵ�ʱ���ά�����̣߳�
    // ���������߳�
    public Cattle2() {
    }

    /**
     * ����һ��������.
     */
    public void create() {
        if (created) {
            return;
        }

        // �������߼���һ��ָ�����飨ũ����
        joinGroup();
   

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
//            File file = new File("E:\\work\\xthinker\\B-Cloud\\src\\files\\tcp.2.xml");
//            channel = new JChannel(file);

            cattleMgrChannel = new JChannel("UDP(mcast_addr=224.10.10.200;mcast_port=5888)");
            cattleMgrChannel.setReceiver(new ReceiverAdapter() {

                @Override
                public void receive(Message msg) {
                    System.out.println("received msg from " + msg.getSrc() + ": " + msg.getObject());
                }

                @Override
                public void viewAccepted(View new_view) {
                    System.out.println("received view " + new_view);
                    if (new_view.getMembers().size() == 1) {
                        // ����(���������)��
                    }
                }

                @Override
                public void suspect(Address suspected_mbr) {
                    System.out.println("suspect address " + suspected_mbr);
                }


            });
            cattleMgrChannel.connect("MyCluster-2");
            cattleMgrChannel.send(new Message(null, null, "hello 2222222222222"));
            // channel.getView().g
            List members = cattleMgrChannel.getView().getMembers();

            System.out.println("members=" + members);

            System.out.println("ClusterName=" + cattleMgrChannel.getClusterName());
        } catch (Exception ex) {
            Logger.getLogger(Cattle.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * ���ص�ǰ�ĸ���
     * @return
     */
    public Load getLoad() {
        LoadCalculator loadGen = new LoadCalculator();
        return loadGen.calculate();
    }

}

