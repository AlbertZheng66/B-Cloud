
package com.xt.bcloud.td7;

import com.xt.bcloud.app.App;
import com.xt.bcloud.app.AppVersion;
import com.xt.bcloud.comm.CloudUtils;
import com.xt.bcloud.comm.PortFactory;
import com.xt.bcloud.td.http.ErrorFactory;
import com.xt.bcloud.td.http.HttpError;
import com.xt.bcloud.td.http.HttpException;
import com.xt.bcloud.resource.ConfService;
import com.xt.bcloud.resource.GroupConf;
import com.xt.bcloud.resource.GroupConfService;
import com.xt.bcloud.session.ClusterSessionProcessor;
import com.xt.bcloud.td.CattleManager;
import com.xt.bcloud.td.DispatcherException;
import com.xt.bcloud.td.Shed;
import com.xt.bcloud.worker.Cattle;
import com.xt.bcloud.worker.RanchMessage;
import com.xt.bcloud.worker.Operator;
import com.xt.core.exception.SystemException;
import com.xt.core.log.LogWriter;
import com.xt.gt.sys.SystemConfiguration;
import com.xt.proxy.Proxy;
import com.xt.proxy.ServiceFactory;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.ExtendedReceiverAdapter;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.View;

/**
 * 和组成员以及其他 TaskDispatcher 进行通信。
 * @author Albert
 */
public class Commnunicator {

    /**
     * 公共主机地址的参数名称
     */
    public static final String PUBLIC_HOSTS = "taskDispatcher.publicHosts";
    public static final String TASK_DISPATCHERS_GROUP_NAME = "TaskDispatchersGroup";
    public static final String TASK_DISPATCHERS_GROUP_CONFIG = "taskDispatchers.GroupConfig";
    /**
     * 用于标识一个组的配置信息（以此字符串开头的一定是任务管理器的相关配置信息）。
     */
    public static final String TASK_DISPATCHER_PREFIX = "taskDispatcher";
    
    /**
     * 当前管理器的唯一标识，注意：不要与其他任务管理器有冲突，否则将发生不可预料的后果。
     */
    private final String uid;
    /**
     * 在GroupConf中标识出当前的组信息。
     */
    public static final String TASK_MANAGER_GROUP_ID = "TASK_MANAGER_GROUP";
    /**
     * 组管理的配置文件
     */
    private URL groupConfigFile;
    
    // 已经注册的可转发的牛, 主键是：待处理的应用的域名或者标识， 键值为其对应的可处理的工作者
    private final Map<String, Shed> hostsMapping = new ConcurrentHashMap<String, Shed>();
    /**
     * 所有应用的管理者(农场主)的地址
     */
    private final Map<Address, Cattle> ranchManagerAddrs = new ConcurrentHashMap();
    private Channel taskDispatcherChannel;
    private final Random random = new Random(System.nanoTime());

    private static final Logger logger = Logger.getLogger(CattleManager.class);

    /**
     * Session 粘滞的特性是否生效
     */
    private final boolean sessionSticky = SystemConfiguration.getInstance().readBoolean("session.sticky", false);
    /**
     * 公共的主机地址，当访问使用这些地址时，需要根据“二级域名”（即请求的第一端路径，以“/”为准）来判断所服务的应用
     */
    private final Set<String> publicHosts = new ConcurrentSkipListSet();
    /**
     * 被暂停的主机地址的集合。
     */
    private final Set<String> pausedHosts = new ConcurrentSkipListSet();
    
    /**
     * the only instance in the JVM
     */
    private static Commnunicator instance = new Commnunicator();
    
    /**
     * 发送心跳包的定时器
     */
    private final java.util.Timer heartBeatingTimer = new java.util.Timer(true);

    private Commnunicator() {
        String _uid = SystemConfiguration.getInstance().readString("taskDispatcher.uid");
        if (StringUtils.isEmpty(_uid)) {
            throw new SystemException("任务管理器必须配置标识编码，通过参数[taskDispatcher.uid]。");
        }
        uid = TASK_DISPATCHER_PREFIX + "." + _uid;
    }
    
    public static Commnunicator getInstance() {
        return instance;
    }

    public void init() throws SystemException {
        
        initPublicHosts();
        // 加载组相关的信息
        final GroupConf groupConf = new GroupConf();
        groupConf.setGroupId(TASK_MANAGER_GROUP_ID);
        groupConf.setEntityId(uid);
        groupConf.setBindAddr(CloudUtils.getLocalHostAddress());
        String port = String.valueOf(PortFactory.getInstance().getPort());
        groupConf.setBindPort(port);
        groupConfigFile = CloudUtils.createArmUrl(ConfService.class,
                ConfService.READ_TASK_MGR_GROUP_CONF, new Object[]{groupConf});
        joinGroup();
        LogWriter.info2(logger, "启动任务分配管理组[%s]", taskDispatcherChannel);

        // 启动心跳线程
        startHeartBeat();
    }
    
    /**
     * 停止管理器的工作（应用退出或者重启）
     */
    public void stop() {
        heartBeatingTimer.cancel();
        // 关闭任务通信
        if (taskDispatcherChannel != null) {
            taskDispatcherChannel.close();
        }
    }

    /**
     * 初始化公共域名
     */
    private void initPublicHosts() {
        String[] hosts = SystemConfiguration.getInstance().readStrings(PUBLIC_HOSTS);
        if (hosts != null && hosts.length > 0) {
            for (int i = 0; i < hosts.length; i++) {
                String host = hosts[i];
                if (!publicHosts.contains(host)) {
                    publicHosts.add(host);
                }
            }
        }
    }

    /**
     * 启动服务器心跳线程。
     */
    private void startHeartBeat() {
        // 心跳的参数配置（单位：秒）,// 默认是“60秒” 发一次心跳
        boolean flag = SystemConfiguration.getInstance().readBoolean("taskDispatcher.heartBeat.flag", true);
        int duration = SystemConfiguration.getInstance().readInt("taskDispatcher.heartBeat.duration", 60) * 1000;
        LogWriter.info2(logger, "启动心跳线程, 间隔为:[%d] 毫秒, Flag:[%s]", duration, flag);
        if (!flag) {
            return;
        }
        heartBeatingTimer.scheduleAtFixedRate(new HeartBeatTask(uid), duration, duration);
    }

    private static class HeartBeatTask extends TimerTask {

        private final String uid;

        public HeartBeatTask(String uid) {
            this.uid = uid;
        }

        @Override
        public void run() {
            try {
                LogWriter.info2(logger, "任务管理器[%s]发送心跳包", uid);
                Proxy proxy = CloudUtils.createArmProxy();
                GroupConfService hbService = ServiceFactory.getInstance().getService(GroupConfService.class, proxy);
                hbService.heartBeat(uid);
            } catch (Throwable t) {
                LogWriter.warn2(logger, t, "任务管理器[%s]发送心跳包时出现异常。", uid);
            }
        }
    }

    /**
     * 根据网络地址找到相应的处理者。
     * @param host
     * @return
     */
    public Cattle findCattle(Request request, Set<Cattle> excluded) {
        if (request == null) {
            throw new HttpException(ErrorFactory.ERROR_404);
        }
        String host = computeHost(request);

        if (StringUtils.isEmpty(host)) {
            throw new HttpException(ErrorFactory.ERROR_404);
        }

        // 首先判断是否被暂停
        // TODO: 是否放开某些拥有特殊标记的请求，以便测试时使用？
        if (pausedHosts.contains(host)) {
            HttpError error = ErrorFactory.getInstance().create("503");
            error.setLocalMessage(String.format("域名[%s]已经被暂停使用。", host));
            throw new HttpException(error);
        }

        Shed shed = hostsMapping.get(host);

        Collection<Cattle> cattles = null;
        if (shed == null) {
            HttpError error = ErrorFactory.getInstance().create("404");
            error.setLocalMessage(String.format("请求的域名[%s]未注册", host));
            throw new HttpException(error);
        }

        // 如果指定了 Session 粘滞的情况，增加
        if (sessionSticky) {
            String processingServer = request.getCookieValue(ClusterSessionProcessor.PROCESSING_SERVER_IN_COOKIE);
            Cattle cattle = shed.findStickedCattle(processingServer);
            if (cattle != null) {
                return cattle;
            }
        }

        // TODO: 是否考虑 Cookie 转换的方式？！！(即在 Cookie 中保存多个服务器的 Session ID)，处理请求时，将当前的
        // JSESSIONID 转换为对应服务器的 SessionID。

        cattles = shed.service(request, excluded);
        if (cattles == null || cattles.isEmpty()) {
            String contextPath = request.getRequestMethod();
            LogWriter.warn2(logger, "未在当前环境[%s](例外实例：%s)找到网址[%s]或者上下文[%s]对应的处理实例。",
                    shed, excluded, host, contextPath);
            HttpError error = ErrorFactory.getInstance().create("503");
            error.setLocalMessage(String.format("请求的域名[%s]已无可用实例。", host));
            throw new HttpException(error);
        }
        int index = random.nextInt(cattles.size());

        // TODO: 用户可以定义选择牛的算法,暂时随机选择一头“牛”
        Cattle selectedCattle = cattles.toArray(new Cattle[cattles.size()])[index];
        // LogWriter.debug2(logger, "为请求[%s]分配实例[%s]" , request, selectedCattle);
        return selectedCattle;
    }

    /**
     * 计算请求使用的域名
     * @param request
     * @return
     */
    private String computeHost(Request request) {
        String host = request.getHeader().getHost();
        if (host != null && publicHosts.contains(host)) {
            // 获得二级域名
            String contextPath = request.getContextPath();
            if (contextPath == null || contextPath.length() < 2) {
                throw new HttpException(ErrorFactory.ERROR_404);
            }
            int index = contextPath.indexOf('/', 2);
            if (index < 0) {
                host = contextPath;
                request.setContextPath("/");
            } else {
                host = contextPath.substring(0, index);
                request.setContextPath(contextPath.substring(index));  // 需要以“/”开头
            }
        }
        return host;
    }

    public void releaseCattle(Cattle cattle) {
    }

    /**
     * 加入一个群组(组主要是为了管理之用)
     */
    private void joinGroup() {
        try {
            LogWriter.info2(logger, "使用配置文件[%s]创建组。", groupConfigFile);
            taskDispatcherChannel = new JChannel(groupConfigFile);
            taskDispatcherChannel.setReceiver(new TaskReceiver());
            taskDispatcherChannel.connect(TASK_DISPATCHERS_GROUP_NAME);
            // 发送一个同步消息(任务管理器将接收到已启动的“服务器实例”的消息)
            RanchMessage synMsg = new RanchMessage();
            synMsg.setOperator(Operator.TASK_SYN);
            LogWriter.info2(logger, "发送一个全局的同步消息。");
            taskDispatcherChannel.send(new Message(null, taskDispatcherChannel.getAddress(), synMsg));
        } catch (Exception ex) {
            throw new DispatcherException(String.format("创建任务转发组[%s]时出现异常。",
                    TASK_DISPATCHERS_GROUP_NAME), ex);
        }
    }

    /**
     * 注册一头“牛”
     * @param cattle
     */
    private void register(Cattle cattle) {
        LogWriter.info(logger, "registering cattle ", cattle);
        // 一头牛可能服务多个域名
        List<String> hosts = getHosts(cattle);
        // synchronized (swapLock) {
        LogWriter.info(logger, "registering hosts ", hosts);
        for (Iterator<String> it = hosts.iterator(); it.hasNext();) {
            String host = it.next();

            LogWriter.info(logger, "registering host ", host);
            Shed shed = hostsMapping.get(host);
            if (shed == null) {
                shed = new Shed(cattle.getApp());
                hostsMapping.put(host, shed);
            }
            shed.addCattle(cattle);
            LogWriter.info(logger, "registered shed ", shed);

        }
        //}
    }

    /**
     * 移除不再工作的服务器。
     * @param cattle
     */
    private void unregister(Cattle cattle) {
        LogWriter.info2(logger, "unregistering cattle[%s]", cattle);
        List<String> hosts = getHosts(cattle);
        // synchronized (swapLock) {
        for (Iterator<String> it = hosts.iterator(); it.hasNext();) {
            String host = it.next();
            if (hostsMapping.containsKey(host)) {
                Shed shed = hostsMapping.get(host);
                if (shed != null) {
                    shed.removeCattle(cattle);
                }
                LogWriter.info2(logger, "unregistered shed=%s", shed);
            }
        }
        //}
    }

    /**
     * 暂停指定服务器实例。
     * @param cattle
     */
    private void pauseApp(Cattle cattle) {
        LogWriter.info2(logger, "暂停应用[%s]......", cattle == null ? null : cattle.getApp());
        List<String> hosts = getHosts(cattle);
        for (Iterator<String> it = hosts.iterator(); it.hasNext();) {
            String host = it.next();
            LogWriter.info2(logger, "域名[%s]被暂停使用。", host);
            if (host != null && !pausedHosts.contains(host)) {
                pausedHosts.add(host);
            }
        }
    }

    /**
     * 停止指定服务器的所有实例。
     * @param cattle
     */
    private void stopApp(Cattle cattle) {
        LogWriter.info2(logger, "stop app = %s", cattle == null ? null : cattle.getApp());
        List<String> hosts = getHosts(cattle);
        // synchronized (swapLock) {
        for (Iterator<String> it = hosts.iterator(); it.hasNext();) {
            String host = it.next();
            if (host != null) {
                hostsMapping.remove(host);
            }
        }
    }

    /**
     * 重新指定服务器的所有实例。
     * @param cattle
     */
    private void restartApp(Cattle cattle) {
        LogWriter.info2(logger, "restart app = %s", cattle == null ? null : cattle.getApp());
        List<String> hosts = getHosts(cattle);
        for (Iterator<String> it = hosts.iterator(); it.hasNext();) {
            String host = it.next();
            if (host != null) {
                pausedHosts.remove(host);
            }
        }
    }

    /**
     * 设置指定应用的版本。
     * @param cattle
     */
    private void setDefaultVersion(Cattle cattle, AppVersion version) {
        LogWriter.info2(logger, "将应用[%s]的默认版本设定为[%s]", cattle == null ? null : cattle.getApp(), version);
        if (version == null) {
            return;
        }
        List<String> hosts = getHosts(cattle);
        for (Iterator<String> it = hosts.iterator(); it.hasNext();) {
            String host = it.next();
            if (hostsMapping.containsKey(host)) {
                Shed shed = hostsMapping.get(host);
                if (shed != null) {
                    shed.setDefaultVersion(version);
                } else {
                    LogWriter.warn2(logger, "当前映射[%s]含有的域名[%s]对应的值为空。", hostsMapping, host);
                }
            } else {
                LogWriter.warn2(logger, "当前映射[%s]的不包含域名[%s]。", hostsMapping, host);
            }
        }
    }

    private void makeLoad(Cattle cattle, List<Cattle> cattles) {
        LogWriter.info2(logger, "makeLoading coordinator:[%s], cattles:[%s] ", cattle, cattles);
        if (cattles == null || cattles.isEmpty()) {
            LogWriter.warn(logger, String.format("读取地址负载时出现非正常数据（cattles=%s）。", cattles));
            return;
        }
        // 用于交换的映射表
        List<String> hosts = getHosts(cattle);
        for (Iterator<String> it = hosts.iterator(); it.hasNext();) {
            String host = it.next();
            Shed shed = hostsMapping.get(host);
            LogWriter.info2(logger, "makeLoading host:[%s], shed:[%s] ", host, shed);
            if (shed == null) {
                shed = new Shed(cattle.getApp());
                hostsMapping.put(host, shed);
            }
            shed.reload(cattles);
        }
    }

    /**
     * 返回此牛对应的网络地址
     * @param cattle
     * @return
     */
    private List<String> getHosts(Cattle cattle) {
        if (cattle == null) {
            LogWriter.warn(logger, String.format("读取地址负载时出现非正常数据（cattle=%s）。", cattle));
            return Collections.EMPTY_LIST;
        }
        App app = cattle.getApp();
        if (app == null) {
            LogWriter.warn(logger, String.format("构造负载时出现非正常数据（app=%s）。", app));
            return Collections.EMPTY_LIST;
        }
        List<String> hosts = app.getHosts();
        if (hosts == null || hosts.isEmpty()) {
            LogWriter.warn(logger, String.format("构造负载时出现非正常数据，无主机地址（app=%s）。", app));
        }
        return hosts;
    }

    private class TaskReceiver extends ExtendedReceiverAdapter {

        public TaskReceiver() {
        }

        @Override
        public void suspect(Address suspected_mbr) {
            // 如果应用管理者出现问题，通过此方式移除其处理程序
            if (ranchManagerAddrs.containsKey(suspected_mbr)) {
                Cattle failedCattle = ranchManagerAddrs.get(suspected_mbr);
                ranchManagerAddrs.remove(suspected_mbr);
                unregister(failedCattle);
            }
        }

        @Override
        public void viewAccepted(View newView) {
            // newView.get
            LogWriter.info(logger, String.format("current members of view = %s;", newView.getMembers()));
        }

        @Override
        public void receive(Message msg) {
            LogWriter.info2(logger, "接收到消息[%s];", msg);
            if (msg == null) {
                return;
            }
            Object obj = msg.getObject();
            if (obj == null) {
                return;
            }
            if (obj instanceof RanchMessage) {
                Address managerAddr = msg.getSrc();
                RanchMessage message = (RanchMessage) obj;
                if (managerAddr != null && message.getCattle() != null) {
                    ranchManagerAddrs.put(managerAddr, message.getCattle());
                }
                Operator op = message.getOperator();
                LogWriter.info2(logger, "消息[%s]的类型是[%s];", message, op);
                Cattle cattle = message.getCattle();
                switch (op) {
                    case REGISTER:
                        register(cattle);
                        break;
                    case UNREGISTER:
                        unregister(cattle);
                        break;
                    case SUSPECT:
                        // register(message.getCattle());
                        break;
                    case RANCH_LOAD:
                        List<Cattle> cattles = (List<Cattle>) message.getAttibute(RanchMessage.CATTLES_LOAD);
                        makeLoad(cattle, cattles);
                        break;
                    case APP_PAUSE:
                        pauseApp(cattle);
                        break;
                    case APP_RESTART:
                        restartApp(cattle);
                        break;
                    case APP_STOP:
                        stopApp(cattle);
                        break;
                    case APP_SET_DEFAULT_VERSION:
                        AppVersion version = (AppVersion) message.getAttibute("defaultAppVersion");
                        setDefaultVersion(cattle, version);
                        break;
                    default:
                    // warning
                }
            }
        }
    }
}


