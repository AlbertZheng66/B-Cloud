package com.xt.bcloud.worker;

import com.xt.bcloud.app.App;
import com.xt.bcloud.app.AppVersion;
import com.xt.bcloud.comm.ChannelCleaner;
import com.xt.bcloud.comm.CloudUtils;
import com.xt.bcloud.comm.Load;
import com.xt.bcloud.comm.PortFactory;
import com.xt.bcloud.resource.ConfService;
import com.xt.bcloud.resource.GroupConf;
import com.xt.bcloud.td.CattleManager;
import com.xt.comm.CleanerManager;
import com.xt.core.exception.SystemException;
import com.xt.core.log.LogWriter;
import com.xt.gt.sys.SystemConfiguration;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.jgroups.Address;
import org.jgroups.ExtendedReceiverAdapter;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.View;

/**
 * 
 * 负载发送器,用于将承载当前应用的所有服务器的负载情况发送给任务分配器。
 * @author albert
 */
public class Ranch implements Runnable {

    /**
     * 任务分配器管理组的配置文件
     */
    private final URL taskDispatchersGroupConfig;
    /**
     * 农场管理组的配置文件
     */
    private URL ranchMgrConfig;
    private final Logger logger = Logger.getLogger(Ranch.class);
    /**
     * 停止标记
     */
    private volatile boolean stoped = false;
    /**
     * 正在服务（处理）的应用信息
     */
    private final App app;
    /**
     * 用于“负载”发送的通信通道，即向“任务分配器发布当前的通道”
     */
    private JChannel taskDispatcherChannel;
    /**
     * 用于农场管理的通道
     */
    private JChannel ranchMgrChannel;
    /**
     * 可执行任务的所有服务实例负载情况（只有协调人才记录此数据）
     */
    private final Map<Cattle, Load> cattleLoad = Collections.synchronizedMap(new LinkedHashMap());
    /**
     * 可执行任务的所有服务器实例的地址信息（只有协调人才记录此数据）
     */
    private final Map<Address, Cattle> cattleAddress = Collections.synchronizedMap(new LinkedHashMap());
    /**
     * 对此服务器的性能评分，这个评分将影响资源的申请和分配。
     * 比如：当资源低于一定的域值时，系统将自动向“资源工厂”申请资源。
     */
    private volatile int score;
    /**
     * 本地地址
     */
    private Address localAddress;
    /**
     * 协调人（通常是第一个创建者）
     */
    private Address coordinatorAddress = null;
    /**
     * 本地的“牛”
     */
    private Cattle managedCattle;
    /**
     * 是否发送整体管理消息
     */
    private final boolean sendRanchLoad = SystemConfiguration.getInstance().readBoolean("Ranch.autoSendRanchLoad", true);

    public Ranch(Cattle cattle) {
        this.app = cattle.getApp();

        final GroupConf groupConf = new GroupConf();
        groupConf.setGroupId(CattleManager.TASK_MANAGER_GROUP_ID);
        groupConf.setEntityId(cattle.getAppInstanceOid());
        groupConf.setBindAddr(CloudUtils.getLocalHostAddress());
        String port = String.valueOf(PortFactory.getInstance().getPort());
        groupConf.setBindPort(port);
        taskDispatchersGroupConfig = CloudUtils.createArmUrl(ConfService.class,
                ConfService.READ_TASK_MGR_GROUP_CONF, new Object[]{groupConf});
        managedCattle = cattle;
        init();
    }

    /**
     * 组初始化操作
     */
    private void init() {
        RanchManager.getInstance().register(this);

        // 读取组参数
        String host = CloudUtils.getLocalHostAddress();
        int port = PortFactory.getInstance().getPort();
        GroupConf groupConf = new GroupConf();
        groupConf.setBindAddr(host);
        groupConf.setBindPort(String.valueOf(port));
        groupConf.setEntityId(managedCattle.getAppInstanceOid());
        groupConf.setGroupId("RANCH_" + app.getOid());
        this.ranchMgrConfig = CloudUtils.createArmUrl(ConfService.class,
                "readRanchGroupConf", new Object[]{groupConf});

        localAddress = joinRanchGroup();
        LogWriter.debug(logger, String.format("本机管理实例[%s]，本地地址[%s]",
                managedCattle, localAddress));
    }

    /**
     * 停止“一头牛”的工作。
     * @param cattle
     */
    synchronized public void unregister(Cattle cattle) {
        LogWriter.info2(logger, "撤销对应用实例[%s]。", cattle);
        if (cattle == null) {
            return;
        }
        
        RanchMessage unregisterMessage = new RanchMessage();
        unregisterMessage.setCattle(cattle);
        unregisterMessage.setOperator(Operator.UNREGISTER);

        try {
            // 发送一个撤销注册消息
            if (isCoordinator()) {
                if (taskDispatcherChannel != null) {
                    taskDispatcherChannel.send(new Message(null, null, unregisterMessage));
                }
            } else {
                if (ranchMgrChannel != null) {
                    ranchMgrChannel.send(new Message(coordinatorAddress, null, unregisterMessage));
                }
            }
        } catch (Exception ex) {
            logger.warn(String.format("注销应用实例[%s]失败。", managedCattle), ex);
        }
        
        // 注销时候应该关闭所有的“组通道”
        closeChannels();
    }

    /**
     * 加入一个群组，即任务分配群组。(只有农场的创建者需要创建此组)。
     */
    private void joinTaskGroup() {
        if (taskDispatcherChannel != null) {
            return;
        }
        try {
            taskDispatcherChannel = new JChannel(taskDispatchersGroupConfig);

            // 系统退出时关闭通道
            CleanerManager.getInstance().register(new ChannelCleaner(ranchMgrChannel));

            taskDispatcherChannel.setReceiver(new ExtendedReceiverAdapter() {

                @Override
                public void suspect(Address suspected_mbr) {
                    LogWriter.warn2(logger, "通道[%s]可能出现了问题。", suspected_mbr);
                }

                @Override
                public void receive(Message msg) {
                    Object value = msg.getObject();
                    if (value == null || !(value instanceof RanchMessage)) {
                        LogWriter.warn(logger, String.format("不能处理接收到的对象[%s]。", value));
                        return;
                    }
                    RanchMessage message = (RanchMessage) value;
                    if (message.getOperator() == Operator.TASK_SYN) {
                        try {
                            LogWriter.info2(logger, "从源[%s]接收到一个同步消息[%s]。", msg.getSrc(), message);
                            // 稍稍停一会，能起作用么？！
                            Thread.sleep(5 * 1000);
                            sendRanchLoad(msg.getSrc());  // 只给发起人返回负载消息(TODO: 有点问题啊！！)
                            // sendRanchLoad(null); // sendRegisterMessage(taskDispatcherChannel, msg.getSrc(), managedCattle);
                            // sendRegisterMessage(taskDispatcherChannel, msg.getSrc(), managedCattle);
                        } catch (InterruptedException ex) {
                            java.util.logging.Logger.getLogger(Ranch.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        LogWriter.warn2(logger, "从源[%s]接收到一个不能处理的消息[%s]。", msg.getSrc(), message);
                    }
                }
            });
            taskDispatcherChannel.connect(CattleManager.TASK_DISPATCHERS_GROUP_NAME);

            // 发送协调人消息
            // sendRegisterMessage(taskDispatherChannel, null, managedCattle);
        } catch (Exception ex) {
            // TODO: 如果创建农场失败了，已经发布的应用实例应该取消！？
            throw new SystemException(String.format("加入[%s]任务分派组失败。", this), ex);
        }
    }

    /**
     * 创建一个拥有农场内部管理的组。
     */
    private synchronized Address joinRanchGroup() {
        if (ranchMgrChannel != null) {
            return this.localAddress;
        }
        try {
            ranchMgrChannel = new JChannel(ranchMgrConfig);
            // 系统退出时关闭组通道
            CleanerManager.getInstance().register(new ChannelCleaner(ranchMgrChannel));

            ranchMgrChannel.setReceiver(new ExtendedReceiverAdapter() {

                @Override
                public void receive(Message msg) {
                    // 所有的注册消息都有协调人发送到任务分配组
                    processReceivedMessage(msg);
                }

                @Override
                public void suspect(Address suspected_mbr) {
                    // 向任务分配器发送怀疑信息
                    Cattle failedCattle = cattleAddress.get(suspected_mbr);
                    if (isCoordinator()) {
                        if (failedCattle != null) {
                            unregister(failedCattle);
                        }
                    } else {
                        Vector<Address> members = ranchMgrChannel.getView().getMembers();
                        if (!members.isEmpty()) {
                            LogWriter.info2(logger, "当前的第一个地址是：[%s]", members.get(0));
                            if (members.size() == 1 || (members.get(0) == localAddress)) {
                                // 将自己切换为“协调人”
                                coordinatorAddress = localAddress;
                                joinTaskGroup();
                                // 发送失败者的注销消息(和协调人的发送时机不一样)
                                if (failedCattle != null) {
                                    unregister(failedCattle);
                                }
                                // 发送自身的注册消息
                                sendRegisterMessage(taskDispatcherChannel, null, managedCattle);
                                sendCoordinatorMessage(null);
                            }
                        }
                    }
                }

                @Override
                public void viewAccepted(View new_view) {
                    prcessViewAccepted(new_view);
                }
            });
            ranchMgrChannel.connect(getRanchName());
            Address _localAddress = ranchMgrChannel.getLocalAddress();

            // if (locaAddress)
            // 发送一个成员加入 join 通知(发送通知一定在“viewAccepted”之后么？)
            sendRegisterMessage(ranchMgrChannel, coordinatorAddress, managedCattle);
            return _localAddress;
        } catch (Exception ex) {
            // TODO: 如果创建农场失败了，已经发布的应用实例应该取消！？
            throw new SystemException(String.format("加入或者创建应用实例[%s]管理组失败。", this), ex);
        }
    }

    /**
     * 发送一个协调人消息（有协调人自动发出）
     * @param target
     */
    private void sendCoordinatorMessage(Address target) {
        RanchMessage msg = new RanchMessage();
        msg.setOperator(Operator.COORDINATOR);
        msg.setCattle(managedCattle);
        sendMessage(ranchMgrChannel, target, msg);
    }

    /**
     * 处理接收到的农场管理组消息
     * @param msg
     */
    private void processReceivedMessage(Message msg) {
        LogWriter.info(logger, String.format("管理组接收到消息[%s]", msg));
        if (msg == null) {
            return;
        }
        Object value = msg.getObject();
        if (value == null || !(value instanceof RanchMessage)) {
            LogWriter.warn(logger, String.format("不能处理接收到的对象[%s]。", value));
            return;
        }
        RanchMessage message = (RanchMessage) value;
        if (message.getOperator() == Operator.RANCH_LOAD) {
            return;
        }
        // 一个农场只处理一个特定的应用(每个应用处于不同的组，因此不会产生此问题)
//                    if (managedCattle == null
//                            || managedCattle.getApp() == null
//                            || load.getCattle() == null
//                            || managedCattle.getApp().equals(load.getCattle().getApp())) {
//                        return;
//                    }

        // 记录地址信息
        if (msg.getSrc() != null && message.getCattle() != null) {
            cattleAddress.put(msg.getSrc(), message.getCattle());
        }

        // 所有节点都会记录负载信息
        if (message.getOperator() == Operator.LOAD) {
            LogWriter.info2(logger, "收到节点[%s]的负载消息[%s]。", message.getCattle(), message.getLoad());
            if (message.getCattle() != null && message.getLoad() != null) {
                cattleLoad.put(message.getCattle(), message.getLoad());
            }
            return;
        }

        // 只要不是自己发送的消息，都需要切换协调人。
        if (message.getOperator() == Operator.COORDINATOR) {
            if (!localAddress.equals(msg.getSrc())) {
                this.coordinatorAddress = msg.getSrc();
            }
            return;
        }

        // 所有的注册消息都有协调人发送到任务分配组
        if (isCoordinator()) {
            Operator op = message.getOperator();
            if (op == Operator.REGISTER) {
                sendRegisterMessage(taskDispatcherChannel, null, message.getCattle());
            } else if (op == Operator.UNREGISTER
                    || op == Operator.APP_PAUSE
                    || op == Operator.APP_RESTART
                    || op == Operator.APP_SET_DEFAULT_VERSION
                    || op == Operator.APP_STOP) {
                sendMessage(taskDispatcherChannel, null, message);
            } else {
            }
        }
    }

    /**
     * 判断本地地址是否是协调人
     * @return
     */
    private boolean isCoordinator() {
        return (localAddress != null && localAddress.equals(coordinatorAddress));
    }

    /**
     * 处理视图变化的情况
     * TODO: 如果新加入的“视图”是任务管理器（可能后加入了一个管理器，或者其重新启动了），
     * 需要将当前“服务器实例”信息向其注册，供其使用。
     * @param new_view
     */
    private void prcessViewAccepted(View new_view) {
        Vector<Address> members = new_view.getMembers();
        if (members.isEmpty()) {
            return;
        }
        coordinatorAddress = members.get(0);
        // 第一次创建管理组时，本地地址可能为空。
        if (members.size() == 1 || coordinatorAddress.equals(localAddress)) {
            joinTaskGroup();
            // 组成员发送了变化，给任务管理器发送一个通知(比如：某个成员已经不存在了)
            List<Address> missingAddrs = new ArrayList();
            for (Iterator<Address> it = cattleAddress.keySet().iterator(); it.hasNext();) {
                Address missingAddr = it.next();  // 可能有点地址丢失了
                if (!members.contains(missingAddr)) {
                    // 发送一个注销消息
                    RanchMessage msg = new RanchMessage();
                    Cattle missingCattle = cattleAddress.get(missingAddr);
                    msg.setCattle(missingCattle);
                    cattleLoad.remove(missingCattle);  // 去掉相应的负载信息
                    msg.setOperator(Operator.UNREGISTER);
                    sendMessage(taskDispatcherChannel, null, msg);
                    missingAddrs.add(missingAddr);
                }
            }
            for (Iterator<Address> it = missingAddrs.iterator(); it.hasNext();) {
                Address address = it.next();
                cattleAddress.remove(address);
            }
        } else {
            // 释放原有通道(交出协调人的管理权)
            if (taskDispatcherChannel != null && taskDispatcherChannel.isConnected()) {
                taskDispatcherChannel.close();
            }
            taskDispatcherChannel = null;
        }
    }

    /**
     * 组名称，每个应用一个组。
     * @return
     */
    private String getRanchName() {
        StringBuilder strBld = new StringBuilder();
        strBld.append(app.getOid()).append("(").append(app.getId()).append(")");
        return strBld.toString();
    }

    public void run() {
        while (!stoped) {
            try {
                synchronized (this) {
                    wait(10000);
                }
            } catch (InterruptedException ex) {
                logger.warn("线程被中断。", ex);
            }

            // 由协调人向任务分配组发送整体负载消息
            if (isCoordinator()) {
                if (sendRanchLoad) {
                    Load load = calculteLoad();
                    cattleLoad.put(managedCattle, load);
                    sendRanchLoad(null);
                }
            } else {
                sendLocalLoad();
            }
        }

        // 注销自己
        RanchManager.getInstance().unregister(this);
    }

    synchronized public void stop() {
        logger.info("关闭消息组.........");
        if (stoped) {
            return;
        }
        unregister(managedCattle);
        closeChannels();
        stoped = true;
    }

    /**
     * 关闭所有的组通道。
     */
    private void closeChannels() {
        if (taskDispatcherChannel != null) {
            taskDispatcherChannel.close();
            taskDispatcherChannel = null;
        }

        if (ranchMgrChannel != null) {
            ranchMgrChannel.close();
            ranchMgrChannel = null;
        }
    }

    /**
     * 只向协调人发生负载信息
     */
    private void sendLocalLoad() {
        try {
            if (ranchMgrChannel == null || coordinatorAddress == null) {
                return;
            }
            // 
            RanchMessage loadMsg = new RanchMessage();
            loadMsg.setCattle(managedCattle);
            loadMsg.setOperator(Operator.LOAD);
            loadMsg.setLoad(calculteLoad());
            if(!ranchMgrChannel.isConnected() || !ranchMgrChannel.isOpen()) {
                 LogWriter.warn2(logger, "任务分配器的管理通道[%s]的状态[connected=%s;open=%s]错误。",
                        ranchMgrChannel,
                        ranchMgrChannel.isConnected(), ranchMgrChannel.isOpen());
                return;
            }
            ranchMgrChannel.send(coordinatorAddress, localAddress, loadMsg);
        } catch (Exception ex) {
            logger.warn(String.format("发送负载消息失败。", this), ex);
        }
    }

    /**
     * 协调人向任务管理器发生整体负载信息
     * TODO: 为了减少网络流量，应该有目的的发送, 如果性能没有必要通知，则不要进行发送，
     * 以减少服务器端的压力。
     */
    private void sendRanchLoad(Address target) {
        RanchMessage loadMsg = null;
        try {
            if (taskDispatcherChannel == null || !isCoordinator()) {
                return;
            }
            //
            loadMsg = new RanchMessage();
            loadMsg.setCattle(managedCattle);
            loadMsg.setOperator(Operator.RANCH_LOAD);
            // TODO: 按负载大小及性能排序
            ArrayList<Cattle> cattles = new ArrayList(cattleLoad.keySet());
            if (!cattles.contains(managedCattle)) {  // 把自己也加上
                cattles.add(managedCattle);
            }
            loadMsg.setAttibute(RanchMessage.CATTLES_LOAD, cattles);
            LogWriter.info2(logger, "向目标[%s]发送一个负载消息[%s]。",
                    target == null ? "all" : target, loadMsg);

            if (!taskDispatcherChannel.isConnected()
                    || !taskDispatcherChannel.isOpen()) {
                LogWriter.warn2(logger, "任务分配器的管理通道[%s]的状态[connected=%s;open=%s]错误。",
                        taskDispatcherChannel,
                        taskDispatcherChannel.isConnected(), taskDispatcherChannel.isOpen());
                return;
            }
            taskDispatcherChannel.send(target, localAddress, loadMsg);
        } catch (Exception ex) {
            LogWriter.warn2(logger, ex, "发送负载消息失败。", loadMsg, ex);
        }
    }

    /**
     * 计算本地服务器的负载情况
     * @return
     */
    private Load calculteLoad() {
        Load load = new LoadCalculator().calculate();
        return load;
    }

    /**
     * 向指定通道发送一个注册消息。
     */
    private void sendRegisterMessage(JChannel channel, Address target, Cattle cattle) {
        RanchMessage msg = new RanchMessage();
        msg.setOperator(Operator.REGISTER);
        msg.setCattle(cattle);
        sendMessage(channel, target, msg);
    }

    private void sendMessage(JChannel channel, Address target, Serializable msg) {
        if (channel == null) {
            LogWriter.info(logger, "发送通道尚未创建。");
            return;
        }
        try {
            LogWriter.info(logger, String.format("向位于[%s]的通道[%s]发送注册消息[%s]。", target, channel, msg));
            channel.send(new Message(target, localAddress, msg));
        } catch (Exception ex) {
            LogWriter.warn2(logger, ex, "向通道[%s]发送消息[%s]时出现异常。", channel, msg);
        }
    }

    public Cattle getManagedCattle() {
        return managedCattle;
    }

    /**
     * 暂停指定的应用
     */
    public void pauseApp(App app, Cattle cattle) {
        LogWriter.warn2(logger, "尝试暂停应用[%s]，服务器实例[%s]。", app, cattle);
        if (app == null || cattle == null) {
            return;
        }
        RanchMessage msg = new RanchMessage();
        msg.setCattle(cattle);
        msg.setOperator(Operator.APP_PAUSE);
        // 由“协调人”负责向任务分配通道发送消息
        if (this.isCoordinator()) {
            sendMessage(taskDispatcherChannel, null, msg);
        } else {
            sendMessage(ranchMgrChannel, coordinatorAddress, msg);
        }
    }

    /**
     * 设置应用的默认版本
     */
    public void setAppDefaultVersion(App app, AppVersion appVersion, Cattle cattle) {
        LogWriter.warn2(logger, "设置应用[%s]的默认版本[%s]，服务器实例[%s]。", app, appVersion, cattle);
        if (app == null || appVersion == null || cattle == null) {
            return;
        }
        RanchMessage msg = new RanchMessage();
        msg.setCattle(cattle);
        msg.setOperator(Operator.APP_SET_DEFAULT_VERSION);
        msg.setAttibute("defaultAppVersion", appVersion);
        // 由“协调人”负责向任务分配通道发送消息
        if (this.isCoordinator()) {
            sendMessage(taskDispatcherChannel, null, msg);
        } else {
            sendMessage(ranchMgrChannel, coordinatorAddress, msg);
        }
    }
}
