
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
 * �����Ա�Լ����� TaskDispatcher ����ͨ�š�
 * @author Albert
 */
public class Commnunicator {

    /**
     * ����������ַ�Ĳ�������
     */
    public static final String PUBLIC_HOSTS = "taskDispatcher.publicHosts";
    public static final String TASK_DISPATCHERS_GROUP_NAME = "TaskDispatchersGroup";
    public static final String TASK_DISPATCHERS_GROUP_CONFIG = "taskDispatchers.GroupConfig";
    /**
     * ���ڱ�ʶһ�����������Ϣ���Դ��ַ�����ͷ��һ������������������������Ϣ����
     */
    public static final String TASK_DISPATCHER_PREFIX = "taskDispatcher";
    
    /**
     * ��ǰ��������Ψһ��ʶ��ע�⣺��Ҫ����������������г�ͻ�����򽫷�������Ԥ�ϵĺ����
     */
    private final String uid;
    /**
     * ��GroupConf�б�ʶ����ǰ������Ϣ��
     */
    public static final String TASK_MANAGER_GROUP_ID = "TASK_MANAGER_GROUP";
    /**
     * �����������ļ�
     */
    private URL groupConfigFile;
    
    // �Ѿ�ע��Ŀ�ת����ţ, �����ǣ��������Ӧ�õ��������߱�ʶ�� ��ֵΪ���Ӧ�Ŀɴ���Ĺ�����
    private final Map<String, Shed> hostsMapping = new ConcurrentHashMap<String, Shed>();
    /**
     * ����Ӧ�õĹ�����(ũ����)�ĵ�ַ
     */
    private final Map<Address, Cattle> ranchManagerAddrs = new ConcurrentHashMap();
    private Channel taskDispatcherChannel;
    private final Random random = new Random(System.nanoTime());

    private static final Logger logger = Logger.getLogger(CattleManager.class);

    /**
     * Session ճ�͵������Ƿ���Ч
     */
    private final boolean sessionSticky = SystemConfiguration.getInstance().readBoolean("session.sticky", false);
    /**
     * ������������ַ��������ʹ����Щ��ַʱ����Ҫ���ݡ�������������������ĵ�һ��·�����ԡ�/��Ϊ׼�����ж��������Ӧ��
     */
    private final Set<String> publicHosts = new ConcurrentSkipListSet();
    /**
     * ����ͣ��������ַ�ļ��ϡ�
     */
    private final Set<String> pausedHosts = new ConcurrentSkipListSet();
    
    /**
     * the only instance in the JVM
     */
    private static Commnunicator instance = new Commnunicator();
    
    /**
     * �����������Ķ�ʱ��
     */
    private final java.util.Timer heartBeatingTimer = new java.util.Timer(true);

    private Commnunicator() {
        String _uid = SystemConfiguration.getInstance().readString("taskDispatcher.uid");
        if (StringUtils.isEmpty(_uid)) {
            throw new SystemException("����������������ñ�ʶ���룬ͨ������[taskDispatcher.uid]��");
        }
        uid = TASK_DISPATCHER_PREFIX + "." + _uid;
    }
    
    public static Commnunicator getInstance() {
        return instance;
    }

    public void init() throws SystemException {
        
        initPublicHosts();
        // ��������ص���Ϣ
        final GroupConf groupConf = new GroupConf();
        groupConf.setGroupId(TASK_MANAGER_GROUP_ID);
        groupConf.setEntityId(uid);
        groupConf.setBindAddr(CloudUtils.getLocalHostAddress());
        String port = String.valueOf(PortFactory.getInstance().getPort());
        groupConf.setBindPort(port);
        groupConfigFile = CloudUtils.createArmUrl(ConfService.class,
                ConfService.READ_TASK_MGR_GROUP_CONF, new Object[]{groupConf});
        joinGroup();
        LogWriter.info2(logger, "����������������[%s]", taskDispatcherChannel);

        // ���������߳�
        startHeartBeat();
    }
    
    /**
     * ֹͣ�������Ĺ�����Ӧ���˳�����������
     */
    public void stop() {
        heartBeatingTimer.cancel();
        // �ر�����ͨ��
        if (taskDispatcherChannel != null) {
            taskDispatcherChannel.close();
        }
    }

    /**
     * ��ʼ����������
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
     * ���������������̡߳�
     */
    private void startHeartBeat() {
        // �����Ĳ������ã���λ���룩,// Ĭ���ǡ�60�롱 ��һ������
        boolean flag = SystemConfiguration.getInstance().readBoolean("taskDispatcher.heartBeat.flag", true);
        int duration = SystemConfiguration.getInstance().readInt("taskDispatcher.heartBeat.duration", 60) * 1000;
        LogWriter.info2(logger, "���������߳�, ���Ϊ:[%d] ����, Flag:[%s]", duration, flag);
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
                LogWriter.info2(logger, "���������[%s]����������", uid);
                Proxy proxy = CloudUtils.createArmProxy();
                GroupConfService hbService = ServiceFactory.getInstance().getService(GroupConfService.class, proxy);
                hbService.heartBeat(uid);
            } catch (Throwable t) {
                LogWriter.warn2(logger, t, "���������[%s]����������ʱ�����쳣��", uid);
            }
        }
    }

    /**
     * ���������ַ�ҵ���Ӧ�Ĵ����ߡ�
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

        // �����ж��Ƿ���ͣ
        // TODO: �Ƿ�ſ�ĳЩӵ�������ǵ������Ա����ʱʹ�ã�
        if (pausedHosts.contains(host)) {
            HttpError error = ErrorFactory.getInstance().create("503");
            error.setLocalMessage(String.format("����[%s]�Ѿ�����ͣʹ�á�", host));
            throw new HttpException(error);
        }

        Shed shed = hostsMapping.get(host);

        Collection<Cattle> cattles = null;
        if (shed == null) {
            HttpError error = ErrorFactory.getInstance().create("404");
            error.setLocalMessage(String.format("���������[%s]δע��", host));
            throw new HttpException(error);
        }

        // ���ָ���� Session ճ�͵����������
        if (sessionSticky) {
            String processingServer = request.getCookieValue(ClusterSessionProcessor.PROCESSING_SERVER_IN_COOKIE);
            Cattle cattle = shed.findStickedCattle(processingServer);
            if (cattle != null) {
                return cattle;
            }
        }

        // TODO: �Ƿ��� Cookie ת���ķ�ʽ������(���� Cookie �б������������� Session ID)����������ʱ������ǰ��
        // JSESSIONID ת��Ϊ��Ӧ�������� SessionID��

        cattles = shed.service(request, excluded);
        if (cattles == null || cattles.isEmpty()) {
            String contextPath = request.getRequestMethod();
            LogWriter.warn2(logger, "δ�ڵ�ǰ����[%s](����ʵ����%s)�ҵ���ַ[%s]����������[%s]��Ӧ�Ĵ���ʵ����",
                    shed, excluded, host, contextPath);
            HttpError error = ErrorFactory.getInstance().create("503");
            error.setLocalMessage(String.format("���������[%s]���޿���ʵ����", host));
            throw new HttpException(error);
        }
        int index = random.nextInt(cattles.size());

        // TODO: �û����Զ���ѡ��ţ���㷨,��ʱ���ѡ��һͷ��ţ��
        Cattle selectedCattle = cattles.toArray(new Cattle[cattles.size()])[index];
        // LogWriter.debug2(logger, "Ϊ����[%s]����ʵ��[%s]" , request, selectedCattle);
        return selectedCattle;
    }

    /**
     * ��������ʹ�õ�����
     * @param request
     * @return
     */
    private String computeHost(Request request) {
        String host = request.getHeader().getHost();
        if (host != null && publicHosts.contains(host)) {
            // ��ö�������
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
                request.setContextPath(contextPath.substring(index));  // ��Ҫ�ԡ�/����ͷ
            }
        }
        return host;
    }

    public void releaseCattle(Cattle cattle) {
    }

    /**
     * ����һ��Ⱥ��(����Ҫ��Ϊ�˹���֮��)
     */
    private void joinGroup() {
        try {
            LogWriter.info2(logger, "ʹ�������ļ�[%s]�����顣", groupConfigFile);
            taskDispatcherChannel = new JChannel(groupConfigFile);
            taskDispatcherChannel.setReceiver(new TaskReceiver());
            taskDispatcherChannel.connect(TASK_DISPATCHERS_GROUP_NAME);
            // ����һ��ͬ����Ϣ(��������������յ��������ġ�������ʵ��������Ϣ)
            RanchMessage synMsg = new RanchMessage();
            synMsg.setOperator(Operator.TASK_SYN);
            LogWriter.info2(logger, "����һ��ȫ�ֵ�ͬ����Ϣ��");
            taskDispatcherChannel.send(new Message(null, taskDispatcherChannel.getAddress(), synMsg));
        } catch (Exception ex) {
            throw new DispatcherException(String.format("��������ת����[%s]ʱ�����쳣��",
                    TASK_DISPATCHERS_GROUP_NAME), ex);
        }
    }

    /**
     * ע��һͷ��ţ��
     * @param cattle
     */
    private void register(Cattle cattle) {
        LogWriter.info(logger, "registering cattle ", cattle);
        // һͷţ���ܷ���������
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
     * �Ƴ����ٹ����ķ�������
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
     * ��ָͣ��������ʵ����
     * @param cattle
     */
    private void pauseApp(Cattle cattle) {
        LogWriter.info2(logger, "��ͣӦ��[%s]......", cattle == null ? null : cattle.getApp());
        List<String> hosts = getHosts(cattle);
        for (Iterator<String> it = hosts.iterator(); it.hasNext();) {
            String host = it.next();
            LogWriter.info2(logger, "����[%s]����ͣʹ�á�", host);
            if (host != null && !pausedHosts.contains(host)) {
                pausedHosts.add(host);
            }
        }
    }

    /**
     * ָֹͣ��������������ʵ����
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
     * ����ָ��������������ʵ����
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
     * ����ָ��Ӧ�õİ汾��
     * @param cattle
     */
    private void setDefaultVersion(Cattle cattle, AppVersion version) {
        LogWriter.info2(logger, "��Ӧ��[%s]��Ĭ�ϰ汾�趨Ϊ[%s]", cattle == null ? null : cattle.getApp(), version);
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
                    LogWriter.warn2(logger, "��ǰӳ��[%s]���е�����[%s]��Ӧ��ֵΪ�ա�", hostsMapping, host);
                }
            } else {
                LogWriter.warn2(logger, "��ǰӳ��[%s]�Ĳ���������[%s]��", hostsMapping, host);
            }
        }
    }

    private void makeLoad(Cattle cattle, List<Cattle> cattles) {
        LogWriter.info2(logger, "makeLoading coordinator:[%s], cattles:[%s] ", cattle, cattles);
        if (cattles == null || cattles.isEmpty()) {
            LogWriter.warn(logger, String.format("��ȡ��ַ����ʱ���ַ��������ݣ�cattles=%s����", cattles));
            return;
        }
        // ���ڽ�����ӳ���
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
     * ���ش�ţ��Ӧ�������ַ
     * @param cattle
     * @return
     */
    private List<String> getHosts(Cattle cattle) {
        if (cattle == null) {
            LogWriter.warn(logger, String.format("��ȡ��ַ����ʱ���ַ��������ݣ�cattle=%s����", cattle));
            return Collections.EMPTY_LIST;
        }
        App app = cattle.getApp();
        if (app == null) {
            LogWriter.warn(logger, String.format("���츺��ʱ���ַ��������ݣ�app=%s����", app));
            return Collections.EMPTY_LIST;
        }
        List<String> hosts = app.getHosts();
        if (hosts == null || hosts.isEmpty()) {
            LogWriter.warn(logger, String.format("���츺��ʱ���ַ��������ݣ���������ַ��app=%s����", app));
        }
        return hosts;
    }

    private class TaskReceiver extends ExtendedReceiverAdapter {

        public TaskReceiver() {
        }

        @Override
        public void suspect(Address suspected_mbr) {
            // ���Ӧ�ù����߳������⣬ͨ���˷�ʽ�Ƴ��䴦�����
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
            LogWriter.info2(logger, "���յ���Ϣ[%s];", msg);
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
                LogWriter.info2(logger, "��Ϣ[%s]��������[%s];", message, op);
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


