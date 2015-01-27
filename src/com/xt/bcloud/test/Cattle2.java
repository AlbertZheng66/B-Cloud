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
 * TODO:管理接口是通过“组”还是“Socket”？！
 * 一个线程占用大约 1M RAM
 * @author albert
 */
public class Cattle2 {

    private boolean created = false;  // 标识是否已经创建

    /**
     * 用于“牛”管理的通信通道
     */
    private JChannel cattleMgrChannel;

    /**
     * 是否被停止的标志
     */
    private boolean stoped = false;

    // ScoreThread 评分维护线程（注意: 线程使用合适的优先级）
    // Rancher     线程（只有当前服务器是管理者的时候才维护此线程）
    // 启动管理线程
    public Cattle2() {
    }

    /**
     * 创建一个服务器.
     */
    public void create() {
        if (created) {
            return;
        }

        // 创建或者加入一个指定的组（农场）
        joinGroup();
   

        // 组线程需要一直在运行
        while (!stoped) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Cattle.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * 加入一个群组(组主要是为了管理之用)
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
                        // 加入(任务分配器)组
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
     * 返回当前的负载
     * @return
     */
    public Load getLoad() {
        LoadCalculator loadGen = new LoadCalculator();
        return loadGen.calculate();
    }

}

