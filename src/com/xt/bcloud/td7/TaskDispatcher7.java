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
     * 日志实例
     */
    private final static Logger logger = Logger.getLogger(TaskDispatcher7.class);

    private final MduService mduService = CloudUtils.createMduService();
    /**
     * 连接器的参数名称
     */
    public static final String PARAM_TD_CONNECTORS = "td.connectors";
    /**
     * 注册失败时进行重试的间隔（毫秒）
     */
    public static final int TASK_DISPATCHER_RETRY_INTERVAL = 5000;
    
    /**
     * 任务分派器的ID
     */
    private static final String TASK_DISPATCHER_ID = SystemConfiguration.getInstance().readString("td.id",
            CloudUtils.getComputerName());
    
    /**
     * 任务管理器的JMX端口
     */
    private final int customizedJmxRmiPort = SystemConfiguration.getInstance().readInt("td.jmx.rmi.port", -1);
    
    
    private final ProfilingManager profilingManager = null;
    
    /**
     * 启动的连接器对象
     */
    private final Connector[] connectors;
    
    /**
     * 任务管理器所在的组
     */
    private final ThreadGroup tdThreadGroup = new ThreadGroup("td-" + this.getClass().getSimpleName());
    
    /**
     * 代表当前的任务分派器实例
     */
    private TaskDispatcher taskDispatcher;
    
    /**
     * 是否注册成功的标记
     */
    private boolean registered = false;
    
    /**
     * 注册可管理的MBeans
     */
    private final MBeansRegister beansRegister = new MBeansRegister();
    
    /**
     * 用于发送心跳信息的定时器
     */
    private final HeartBeating heartBeating = new HeartBeating();

    public TaskDispatcher7() {
        Connector[] _connectors = SystemConfiguration.getInstance().readObjects(PARAM_TD_CONNECTORS, Connector.class);
        if (CollectionUtils.isEmpty(_connectors)) {
            // 默认的连接器：Http连接器
            HttpIOConnector ioConnector = new HttpIOConnector();
            ioConnector.setPort(4900);
            _connectors = new Connector[]{ioConnector};
        }
        connectors = _connectors;
    }

    /**
     * 开始初始化参数
     */
    public boolean init() {
        // 初始化Mbean注册器
        beansRegister.onInit();

        // 初始化组管理器
        Commnunicator.getInstance().init();
        for (Connector conn : connectors) {
            LogWriter.info2(logger, "开始初始化连接器[%s]", conn);
            conn.init();
        }
        //  如果注册失败（ARM可能宕机），需要循环注册
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    // 向服务器进行注册
                    register();

                    // 注册成功
                    registered = true;
                    timer.cancel();

                    // 启动心跳
                    heartBeating.start(new Runnable() {

                        public void run() {
                            mduService.tdBeat(taskDispatcher);
                        }
                        
                    });
                } catch (Throwable t) {
                    LogWriter.warn2(logger, t, "任务分配器[%s]注册出现异常，应用将稍后[%d]进行重试。",
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
            LogWriter.info2(logger, "开始启动连接器[%s]", conn);
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
     * 到资源分配器进行注册。 如果注册失败，需要重新注册。
     */
    private void register() {
        // templateOid：即AppServerTemplate的OID，编译应用关联使用的发布模板。
        // 通过MDU发布的应用有此ID，自行启动的则没有此ID
        String templateOid = SystemConfiguration.getInstance().readString("td.templateOid");
        taskDispatcher = new TaskDispatcher();
        AppServerTemplate template = mduService.getTaskDispatcherTemplate(templateOid);
        if (template != null) {
            taskDispatcher.setName(template.getName());
        } else {
            LogWriter.warn2(logger, "未找到任务管理器模板[%s]", templateOid);
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
     * 到资源分配器进行注销。
     */
    private void unregister() {
        if (!registered) {
            return;
        }
        try {
            mduService.unregisterTaskDispatcher(taskDispatcher);
        } catch (Throwable t) {
            LogWriter.warn2(logger, t, "注销资源分配器[%s]时出现异常。", taskDispatcher);
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
