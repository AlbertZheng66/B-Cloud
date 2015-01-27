
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
     * 每个应用一个管理组
     */
    public static final String CATTLE_MGR_GROUP_PREFIX = "CattleMgrGroup_";

    private final Cattle cattle;

    /**
     * 通信通道
     */
    private JChannel cattleMgrChannel;

     /**
     * 是否被停止的标志
     */
    private volatile boolean stoped = false;

    public CattleGroup(Cattle cattle) {
        this.cattle = cattle;
    }


    public void start() {
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
     * 停止这头牛的工作。如果这头牛有任务尚未完成，则
     */
    public void stop() {
//        if (server == null) {
//            return;
//        }
//
//        // 如果有任务尚未处理结束，等待处理
//        try {
//            server.stop();
//            server = null;
//        } catch (Exception ex) {
//            throw new SystemException("停止服务器出现异常。", ex);
//        }
    }


    /**
     * 重新启动服务器
     */
    private void reset() {
//        server.stop();
//        server.start();
    }

}
