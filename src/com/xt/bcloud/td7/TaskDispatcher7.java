package com.xt.bcloud.td7;

import com.xt.bcloud.comm.CloudUtils;
import com.xt.bcloud.comm.Constants;
import com.xt.bcloud.comm.HeartBeating;
import com.xt.bcloud.mdu.AppServerTemplate;
import com.xt.bcloud.mdu.MduService;
import com.xt.bcloud.pf.server.mbeans.MBeansRegister;
import com.xt.bcloud.resource.TaskDispatcher;
import com.xt.bcloud.resource.server.ServerState;
import com.xt.bcloud.td7.impl.HttpIOConnector;
import com.xt.core.app.Startable;
import com.xt.core.log.LogWriter;
import com.xt.core.utils.CollectionUtils;
import com.xt.gt.sys.SystemConfiguration;
import com.xt.gt.sys.SystemConstants;
import com.xt.gt.sys.loader.SystemLoader;
import com.xt.gt.sys.loader.SystemLoaderManager;
import com.xt.proxy.ServiceFactory;
import java.io.File;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import org.apache.log4j.Logger;

/**
 *
 * @author Albert
 */
public class TaskDispatcher7 implements Startable {
    
    /**
     * ��־ʵ��
     */
    private final static Logger logger = Logger.getLogger(TaskDispatcher7.class);

    private final MduService mduService = CloudUtils.createMduService();
    /**
     * �������Ĳ�������
     */
    public static final String PARAM_TD_CONNECTORS = "td.connectors";
    /**
     * ע��ʧ��ʱ�������Եļ�������룩
     */
    public static final int TASK_DISPATCHER_RETRY_INTERVAL = 5000;
    
    /**
     * �����������ID
     */
    private static final String TASK_DISPATCHER_ID = SystemConfiguration.getInstance().readString("td.id",
            CloudUtils.getComputerName());
    
    /**
     * �����������JMX�˿�
     */
    private final int customizedJmxRmiPort = SystemConfiguration.getInstance().readInt("td.jmx.rmi.port", -1);
    
    
    private final ProfilingManager profilingManager = null;
    
    /**
     * ����������������
     */
    private final Connector[] connectors;
    
    /**
     * ������������ڵ���
     */
    private final ThreadGroup tdThreadGroup = new ThreadGroup("td-" + this.getClass().getSimpleName());
    
    /**
     * ����ǰ�����������ʵ��
     */
    private TaskDispatcher taskDispatcher;
    
    /**
     * �Ƿ�ע��ɹ��ı��
     */
    private boolean registered = false;
    
    /**
     * ע��ɹ����MBeans
     */
    private final MBeansRegister beansRegister = new MBeansRegister();
    
    /**
     * ���ڷ���������Ϣ�Ķ�ʱ��
     */
    private final HeartBeating heartBeating = new HeartBeating();

    public TaskDispatcher7() {
        Connector[] _connectors = SystemConfiguration.getInstance().readObjects(PARAM_TD_CONNECTORS, Connector.class);
        if (CollectionUtils.isEmpty(_connectors)) {
            // Ĭ�ϵ���������Http������
            HttpIOConnector ioConnector = new HttpIOConnector();
            ioConnector.setPort(4900);
            _connectors = new Connector[]{ioConnector};
        }
        connectors = _connectors;
    }

    /**
     * ��ʼ��ʼ������
     */
    public boolean init() {
        // ��ʼ��Mbeanע����
        beansRegister.onInit();

        // ��ʼ���������
        Commnunicator.getInstance().init();
        for (Connector conn : connectors) {
            LogWriter.info2(logger, "��ʼ��ʼ��������[%s]", conn);
            conn.init();
        }
        //  ���ע��ʧ�ܣ�ARM����崻�������Ҫѭ��ע��
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    // �����������ע��
                    register();

                    // ע��ɹ�
                    registered = true;
                    timer.cancel();

                    // ��������
                    heartBeating.start(new Runnable() {

                        public void run() {
                            mduService.tdBeat(taskDispatcher);
                        }
                        
                    });
                } catch (Throwable t) {
                    LogWriter.warn2(logger, t, "���������[%s]ע������쳣��Ӧ�ý��Ժ�[%d]�������ԡ�",
                            TaskDispatcher7.this, TASK_DISPATCHER_RETRY_INTERVAL);
                }
            }
        }, 0, TASK_DISPATCHER_RETRY_INTERVAL);

        return true;
    }

    public void start() {
        for (Connector conn : connectors) {
            Thread connThread = new Thread(tdThreadGroup, conn);
            connThread.setName("Connector." + conn.getClass().getSimpleName());
            connThread.setDaemon(true);
            LogWriter.info2(logger, "��ʼ����������[%s]", conn);
            connThread.start();
        }
    }

    public void stop() {
        unregister();
        Commnunicator.getInstance().stop();
        for (Connector conn : connectors) {
            conn.stop();
        }
        heartBeating.cancel();
        beansRegister.onDestroy();
    }

    /**
     * ����Դ����������ע�ᡣ ���ע��ʧ�ܣ���Ҫ����ע�ᡣ
     */
    private void register() {
        // templateOid����AppServerTemplate��OID������Ӧ�ù���ʹ�õķ���ģ�塣
        // ͨ��MDU������Ӧ���д�ID��������������û�д�ID
        String templateOid = SystemConfiguration.getInstance().readString("td.templateOid");
        taskDispatcher = new TaskDispatcher();
        AppServerTemplate template = mduService.getTaskDispatcherTemplate(templateOid);
        if (template != null) {
            taskDispatcher.setName(template.getName());
        } else {
            LogWriter.warn2(logger, "δ�ҵ����������ģ��[%s]", templateOid);
        }
        taskDispatcher.setOid(UUID.randomUUID().toString());
        taskDispatcher.setId(TASK_DISPATCHER_ID);
        taskDispatcher.setName(CloudUtils.getComputerName());
        taskDispatcher.setIp(CloudUtils.getLocalHostAddress());
        taskDispatcher.setValid(true);
        taskDispatcher.setState(ServerState.AVAILABLE);
        taskDispatcher.setJmxRmiPort(CloudUtils.getJmxRmiPort(customizedJmxRmiPort));
        taskDispatcher.setTemplateId(templateOid);
        taskDispatcher.setInvalidTime(Constants.INVALID_TIME);
        taskDispatcher.setLastUpdatedTime(Calendar.getInstance());
        mduService.registerTaskDispatcher(taskDispatcher);
    }

    /**
     * ����Դ����������ע����
     */
    private void unregister() {
        if (!registered) {
            return;
        }
        try {
            mduService.unregisterTaskDispatcher(taskDispatcher);
        } catch (Throwable t) {
            LogWriter.warn2(logger, t, "ע����Դ������[%s]ʱ�����쳣��", taskDispatcher);
        }
    }

    public static void main(String[] argv) {
        System.setProperty("java.net.preferIPv4Stack", "true");
        // -l com.xt.gt.sys.loader.CommandLineSystemLoader -m CLIENT_SERVER -p local -f conf\gt-config.xml
        if (argv.length == 0) {
            argv = new String[]{"-l", "com.xt.gt.sys.loader.CommandLineSystemLoader",
                "-m", "CLIENT_SERVER",
                "-p", "local", "-f", "conf\\gt-config.xml"};
        }
        SystemLoaderManager slManager = SystemLoaderManager.getInstance();
        slManager.init(argv);
        SystemConfiguration.getInstance().set(SystemConstants.APP_CONTEXT, new File("").getAbsolutePath());
        SystemLoader loader = slManager.getSystemLoader();
        if (loader.getConfigFile() != null) {
            SystemConfiguration.getInstance().load(loader.getConfigFile(), false);
        }

        final TaskDispatcher7 td = new TaskDispatcher7();
        td.init();
        td.start();
    }
}
