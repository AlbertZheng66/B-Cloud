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
 * ���ط�����,���ڽ����ص�ǰӦ�õ����з������ĸ���������͸������������
 * @author albert
 */
public class Ranch implements Runnable {

    /**
     * ���������������������ļ�
     */
    private final URL taskDispatchersGroupConfig;
    /**
     * ũ��������������ļ�
     */
    private URL ranchMgrConfig;
    private final Logger logger = Logger.getLogger(Ranch.class);
    /**
     * ֹͣ���
     */
    private volatile boolean stoped = false;
    /**
     * ���ڷ��񣨴�����Ӧ����Ϣ
     */
    private final App app;
    /**
     * ���ڡ����ء����͵�ͨ��ͨ�����������������������ǰ��ͨ����
     */
    private JChannel taskDispatcherChannel;
    /**
     * ����ũ�������ͨ��
     */
    private JChannel ranchMgrChannel;
    /**
     * ��ִ����������з���ʵ�����������ֻ��Э���˲ż�¼�����ݣ�
     */
    private final Map<Cattle, Load> cattleLoad = Collections.synchronizedMap(new LinkedHashMap());
    /**
     * ��ִ����������з�����ʵ���ĵ�ַ��Ϣ��ֻ��Э���˲ż�¼�����ݣ�
     */
    private final Map<Address, Cattle> cattleAddress = Collections.synchronizedMap(new LinkedHashMap());
    /**
     * �Դ˷��������������֣�������ֽ�Ӱ����Դ������ͷ��䡣
     * ���磺����Դ����һ������ֵʱ��ϵͳ���Զ�����Դ������������Դ��
     */
    private volatile int score;
    /**
     * ���ص�ַ
     */
    private Address localAddress;
    /**
     * Э���ˣ�ͨ���ǵ�һ�������ߣ�
     */
    private Address coordinatorAddress = null;
    /**
     * ���صġ�ţ��
     */
    private Cattle managedCattle;
    /**
     * �Ƿ������������Ϣ
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
     * ���ʼ������
     */
    private void init() {
        RanchManager.getInstance().register(this);

        // ��ȡ�����
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
        LogWriter.debug(logger, String.format("��������ʵ��[%s]�����ص�ַ[%s]",
                managedCattle, localAddress));
    }

    /**
     * ֹͣ��һͷţ���Ĺ�����
     * @param cattle
     */
    synchronized public void unregister(Cattle cattle) {
        LogWriter.info2(logger, "������Ӧ��ʵ��[%s]��", cattle);
        if (cattle == null) {
            return;
        }
        
        RanchMessage unregisterMessage = new RanchMessage();
        unregisterMessage.setCattle(cattle);
        unregisterMessage.setOperator(Operator.UNREGISTER);

        try {
            // ����һ������ע����Ϣ
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
            logger.warn(String.format("ע��Ӧ��ʵ��[%s]ʧ�ܡ�", managedCattle), ex);
        }
        
        // ע��ʱ��Ӧ�ùر����еġ���ͨ����
        closeChannels();
    }

    /**
     * ����һ��Ⱥ�飬���������Ⱥ�顣(ֻ��ũ���Ĵ�������Ҫ��������)��
     */
    private void joinTaskGroup() {
        if (taskDispatcherChannel != null) {
            return;
        }
        try {
            taskDispatcherChannel = new JChannel(taskDispatchersGroupConfig);

            // ϵͳ�˳�ʱ�ر�ͨ��
            CleanerManager.getInstance().register(new ChannelCleaner(ranchMgrChannel));

            taskDispatcherChannel.setReceiver(new ExtendedReceiverAdapter() {

                @Override
                public void suspect(Address suspected_mbr) {
                    LogWriter.warn2(logger, "ͨ��[%s]���ܳ��������⡣", suspected_mbr);
                }

                @Override
                public void receive(Message msg) {
                    Object value = msg.getObject();
                    if (value == null || !(value instanceof RanchMessage)) {
                        LogWriter.warn(logger, String.format("���ܴ�����յ��Ķ���[%s]��", value));
                        return;
                    }
                    RanchMessage message = (RanchMessage) value;
                    if (message.getOperator() == Operator.TASK_SYN) {
                        try {
                            LogWriter.info2(logger, "��Դ[%s]���յ�һ��ͬ����Ϣ[%s]��", msg.getSrc(), message);
                            // ����ͣһ�ᣬ��������ô����
                            Thread.sleep(5 * 1000);
                            sendRanchLoad(msg.getSrc());  // ֻ�������˷��ظ�����Ϣ(TODO: �е����Ⱑ����)
                            // sendRanchLoad(null); // sendRegisterMessage(taskDispatcherChannel, msg.getSrc(), managedCattle);
                            // sendRegisterMessage(taskDispatcherChannel, msg.getSrc(), managedCattle);
                        } catch (InterruptedException ex) {
                            java.util.logging.Logger.getLogger(Ranch.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        LogWriter.warn2(logger, "��Դ[%s]���յ�һ�����ܴ������Ϣ[%s]��", msg.getSrc(), message);
                    }
                }
            });
            taskDispatcherChannel.connect(CattleManager.TASK_DISPATCHERS_GROUP_NAME);

            // ����Э������Ϣ
            // sendRegisterMessage(taskDispatherChannel, null, managedCattle);
        } catch (Exception ex) {
            // TODO: �������ũ��ʧ���ˣ��Ѿ�������Ӧ��ʵ��Ӧ��ȡ������
            throw new SystemException(String.format("����[%s]���������ʧ�ܡ�", this), ex);
        }
    }

    /**
     * ����һ��ӵ��ũ���ڲ�������顣
     */
    private synchronized Address joinRanchGroup() {
        if (ranchMgrChannel != null) {
            return this.localAddress;
        }
        try {
            ranchMgrChannel = new JChannel(ranchMgrConfig);
            // ϵͳ�˳�ʱ�ر���ͨ��
            CleanerManager.getInstance().register(new ChannelCleaner(ranchMgrChannel));

            ranchMgrChannel.setReceiver(new ExtendedReceiverAdapter() {

                @Override
                public void receive(Message msg) {
                    // ���е�ע����Ϣ����Э���˷��͵����������
                    processReceivedMessage(msg);
                }

                @Override
                public void suspect(Address suspected_mbr) {
                    // ��������������ͻ�����Ϣ
                    Cattle failedCattle = cattleAddress.get(suspected_mbr);
                    if (isCoordinator()) {
                        if (failedCattle != null) {
                            unregister(failedCattle);
                        }
                    } else {
                        Vector<Address> members = ranchMgrChannel.getView().getMembers();
                        if (!members.isEmpty()) {
                            LogWriter.info2(logger, "��ǰ�ĵ�һ����ַ�ǣ�[%s]", members.get(0));
                            if (members.size() == 1 || (members.get(0) == localAddress)) {
                                // ���Լ��л�Ϊ��Э���ˡ�
                                coordinatorAddress = localAddress;
                                joinTaskGroup();
                                // ����ʧ���ߵ�ע����Ϣ(��Э���˵ķ���ʱ����һ��)
                                if (failedCattle != null) {
                                    unregister(failedCattle);
                                }
                                // ���������ע����Ϣ
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
            // ����һ����Ա���� join ֪ͨ(����֪ͨһ���ڡ�viewAccepted��֮��ô��)
            sendRegisterMessage(ranchMgrChannel, coordinatorAddress, managedCattle);
            return _localAddress;
        } catch (Exception ex) {
            // TODO: �������ũ��ʧ���ˣ��Ѿ�������Ӧ��ʵ��Ӧ��ȡ������
            throw new SystemException(String.format("������ߴ���Ӧ��ʵ��[%s]������ʧ�ܡ�", this), ex);
        }
    }

    /**
     * ����һ��Э������Ϣ����Э�����Զ�������
     * @param target
     */
    private void sendCoordinatorMessage(Address target) {
        RanchMessage msg = new RanchMessage();
        msg.setOperator(Operator.COORDINATOR);
        msg.setCattle(managedCattle);
        sendMessage(ranchMgrChannel, target, msg);
    }

    /**
     * ������յ���ũ����������Ϣ
     * @param msg
     */
    private void processReceivedMessage(Message msg) {
        LogWriter.info(logger, String.format("��������յ���Ϣ[%s]", msg));
        if (msg == null) {
            return;
        }
        Object value = msg.getObject();
        if (value == null || !(value instanceof RanchMessage)) {
            LogWriter.warn(logger, String.format("���ܴ�����յ��Ķ���[%s]��", value));
            return;
        }
        RanchMessage message = (RanchMessage) value;
        if (message.getOperator() == Operator.RANCH_LOAD) {
            return;
        }
        // һ��ũ��ֻ����һ���ض���Ӧ��(ÿ��Ӧ�ô��ڲ�ͬ���飬��˲������������)
//                    if (managedCattle == null
//                            || managedCattle.getApp() == null
//                            || load.getCattle() == null
//                            || managedCattle.getApp().equals(load.getCattle().getApp())) {
//                        return;
//                    }

        // ��¼��ַ��Ϣ
        if (msg.getSrc() != null && message.getCattle() != null) {
            cattleAddress.put(msg.getSrc(), message.getCattle());
        }

        // ���нڵ㶼���¼������Ϣ
        if (message.getOperator() == Operator.LOAD) {
            LogWriter.info2(logger, "�յ��ڵ�[%s]�ĸ�����Ϣ[%s]��", message.getCattle(), message.getLoad());
            if (message.getCattle() != null && message.getLoad() != null) {
                cattleLoad.put(message.getCattle(), message.getLoad());
            }
            return;
        }

        // ֻҪ�����Լ����͵���Ϣ������Ҫ�л�Э���ˡ�
        if (message.getOperator() == Operator.COORDINATOR) {
            if (!localAddress.equals(msg.getSrc())) {
                this.coordinatorAddress = msg.getSrc();
            }
            return;
        }

        // ���е�ע����Ϣ����Э���˷��͵����������
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
     * �жϱ��ص�ַ�Ƿ���Э����
     * @return
     */
    private boolean isCoordinator() {
        return (localAddress != null && localAddress.equals(coordinatorAddress));
    }

    /**
     * ������ͼ�仯�����
     * TODO: ����¼���ġ���ͼ������������������ܺ������һ�������������������������ˣ���
     * ��Ҫ����ǰ��������ʵ������Ϣ����ע�ᣬ����ʹ�á�
     * @param new_view
     */
    private void prcessViewAccepted(View new_view) {
        Vector<Address> members = new_view.getMembers();
        if (members.isEmpty()) {
            return;
        }
        coordinatorAddress = members.get(0);
        // ��һ�δ���������ʱ�����ص�ַ����Ϊ�ա�
        if (members.size() == 1 || coordinatorAddress.equals(localAddress)) {
            joinTaskGroup();
            // ���Ա�����˱仯�����������������һ��֪ͨ(���磺ĳ����Ա�Ѿ���������)
            List<Address> missingAddrs = new ArrayList();
            for (Iterator<Address> it = cattleAddress.keySet().iterator(); it.hasNext();) {
                Address missingAddr = it.next();  // �����е��ַ��ʧ��
                if (!members.contains(missingAddr)) {
                    // ����һ��ע����Ϣ
                    RanchMessage msg = new RanchMessage();
                    Cattle missingCattle = cattleAddress.get(missingAddr);
                    msg.setCattle(missingCattle);
                    cattleLoad.remove(missingCattle);  // ȥ����Ӧ�ĸ�����Ϣ
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
            // �ͷ�ԭ��ͨ��(����Э���˵Ĺ���Ȩ)
            if (taskDispatcherChannel != null && taskDispatcherChannel.isConnected()) {
                taskDispatcherChannel.close();
            }
            taskDispatcherChannel = null;
        }
    }

    /**
     * �����ƣ�ÿ��Ӧ��һ���顣
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
                logger.warn("�̱߳��жϡ�", ex);
            }

            // ��Э��������������鷢�����帺����Ϣ
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

        // ע���Լ�
        RanchManager.getInstance().unregister(this);
    }

    synchronized public void stop() {
        logger.info("�ر���Ϣ��.........");
        if (stoped) {
            return;
        }
        unregister(managedCattle);
        closeChannels();
        stoped = true;
    }

    /**
     * �ر����е���ͨ����
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
     * ֻ��Э���˷���������Ϣ
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
                 LogWriter.warn2(logger, "����������Ĺ���ͨ��[%s]��״̬[connected=%s;open=%s]����",
                        ranchMgrChannel,
                        ranchMgrChannel.isConnected(), ranchMgrChannel.isOpen());
                return;
            }
            ranchMgrChannel.send(coordinatorAddress, localAddress, loadMsg);
        } catch (Exception ex) {
            logger.warn(String.format("���͸�����Ϣʧ�ܡ�", this), ex);
        }
    }

    /**
     * Э����������������������帺����Ϣ
     * TODO: Ϊ�˼�������������Ӧ����Ŀ�ĵķ���, �������û�б�Ҫ֪ͨ����Ҫ���з��ͣ�
     * �Լ��ٷ������˵�ѹ����
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
            // TODO: �����ش�С����������
            ArrayList<Cattle> cattles = new ArrayList(cattleLoad.keySet());
            if (!cattles.contains(managedCattle)) {  // ���Լ�Ҳ����
                cattles.add(managedCattle);
            }
            loadMsg.setAttibute(RanchMessage.CATTLES_LOAD, cattles);
            LogWriter.info2(logger, "��Ŀ��[%s]����һ��������Ϣ[%s]��",
                    target == null ? "all" : target, loadMsg);

            if (!taskDispatcherChannel.isConnected()
                    || !taskDispatcherChannel.isOpen()) {
                LogWriter.warn2(logger, "����������Ĺ���ͨ��[%s]��״̬[connected=%s;open=%s]����",
                        taskDispatcherChannel,
                        taskDispatcherChannel.isConnected(), taskDispatcherChannel.isOpen());
                return;
            }
            taskDispatcherChannel.send(target, localAddress, loadMsg);
        } catch (Exception ex) {
            LogWriter.warn2(logger, ex, "���͸�����Ϣʧ�ܡ�", loadMsg, ex);
        }
    }

    /**
     * ���㱾�ط������ĸ������
     * @return
     */
    private Load calculteLoad() {
        Load load = new LoadCalculator().calculate();
        return load;
    }

    /**
     * ��ָ��ͨ������һ��ע����Ϣ��
     */
    private void sendRegisterMessage(JChannel channel, Address target, Cattle cattle) {
        RanchMessage msg = new RanchMessage();
        msg.setOperator(Operator.REGISTER);
        msg.setCattle(cattle);
        sendMessage(channel, target, msg);
    }

    private void sendMessage(JChannel channel, Address target, Serializable msg) {
        if (channel == null) {
            LogWriter.info(logger, "����ͨ����δ������");
            return;
        }
        try {
            LogWriter.info(logger, String.format("��λ��[%s]��ͨ��[%s]����ע����Ϣ[%s]��", target, channel, msg));
            channel.send(new Message(target, localAddress, msg));
        } catch (Exception ex) {
            LogWriter.warn2(logger, ex, "��ͨ��[%s]������Ϣ[%s]ʱ�����쳣��", channel, msg);
        }
    }

    public Cattle getManagedCattle() {
        return managedCattle;
    }

    /**
     * ��ָͣ����Ӧ��
     */
    public void pauseApp(App app, Cattle cattle) {
        LogWriter.warn2(logger, "������ͣӦ��[%s]��������ʵ��[%s]��", app, cattle);
        if (app == null || cattle == null) {
            return;
        }
        RanchMessage msg = new RanchMessage();
        msg.setCattle(cattle);
        msg.setOperator(Operator.APP_PAUSE);
        // �ɡ�Э���ˡ��������������ͨ��������Ϣ
        if (this.isCoordinator()) {
            sendMessage(taskDispatcherChannel, null, msg);
        } else {
            sendMessage(ranchMgrChannel, coordinatorAddress, msg);
        }
    }

    /**
     * ����Ӧ�õ�Ĭ�ϰ汾
     */
    public void setAppDefaultVersion(App app, AppVersion appVersion, Cattle cattle) {
        LogWriter.warn2(logger, "����Ӧ��[%s]��Ĭ�ϰ汾[%s]��������ʵ��[%s]��", app, appVersion, cattle);
        if (app == null || appVersion == null || cattle == null) {
            return;
        }
        RanchMessage msg = new RanchMessage();
        msg.setCattle(cattle);
        msg.setOperator(Operator.APP_SET_DEFAULT_VERSION);
        msg.setAttibute("defaultAppVersion", appVersion);
        // �ɡ�Э���ˡ��������������ͨ��������Ϣ
        if (this.isCoordinator()) {
            sendMessage(taskDispatcherChannel, null, msg);
        } else {
            sendMessage(ranchMgrChannel, coordinatorAddress, msg);
        }
    }
}
