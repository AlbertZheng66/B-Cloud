package com.xt.bcloud.resource;

import com.xt.bcloud.app.App;
import com.xt.bcloud.app.AppService;
import com.xt.bcloud.app.AppVersion;
import com.xt.bcloud.comm.Capacity;
import com.xt.bcloud.comm.CloudUtils;
import com.xt.bcloud.comm.PortFactory;
import com.xt.bcloud.comm.UnZip;
import com.xt.bcloud.worker.Cattle;
import com.xt.bcloud.worker.Ranch;
import com.xt.bcloud.worker.RanchManager;
import com.xt.bcloud.worker.ServerController;
import com.xt.bcloud.worker.ServerControllerFactory;
import com.xt.bcloud.worker.TomcatServerController;
import com.xt.core.log.LogWriter;
import com.xt.core.service.IService;
import com.xt.proxy.Proxy;
import com.xt.proxy.ServiceFactory;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * 服务器服务类，用于管理发布的服务的接口类。
 * @author albert
 */
public class ServerService implements IService {

    private final Logger logger = Logger.getLogger(ServerService.class);

    public ServerService() {
    }

    /**
     * TODO: 目前，这里有个漏洞，就是用户可以通过程序之间来调用此方法。稍后再处理这个问题
     * （采用限制端口的方式可解决这个问题？）。
     * @param app
     */
    public Cattle deploy(App app, AppVersion appVersion, String appInstanceOid, Capacity capacity) {
        LogWriter.info(logger, String.format("正在部署应用[%s], 版本[%s]，容量为[%s]。",
                app, appVersion, capacity));

        // 需要通过 Profile 读取本地路径
        Profile profile = ServerManager.getInstance().getProfile();
        if (profile == null || profile.getWorkspace() == null) {
            throw new ResourceException("未读取到本地的 Profile，且其中定义的工作目录必须为空。");
        }

        // 创建发布路径
        File deployPath = createDeployPath(profile, app);


        // 向应用管理器申请发布包
        Proxy proxy = CloudUtils.createArmProxy();
        AppService appSerivce = ServiceFactory.getInstance().getService(AppService.class, proxy);
        // 发布到指定路径(将上传的文件解压缩)
        InputStream is = appSerivce.getDeployedPackage(app, appVersion);
        if (is == null) {
            // 未读取到应用发布的信息
            throw new ResourceException(String.format("未读取到版本为[%s]的应用[%s]的发布包。", appVersion, app));
        }
        // test............
        try {
            LogWriter.debug(logger, "读取字节数========" + is.available());
            // FileOutputStream fos = new FileOutputStream("e:\\test.war", false);
            BufferedInputStream bis = new BufferedInputStream(is);
            // TeeInputStream tis = new TeeInputStream(bis, fos, false);
            UnZip unzip = new UnZip();
            unzip.unZip(bis, deployPath);  // 释放到压缩路径下（注意解压的路径）
            // fos.close();
        } catch (IOException ex) {
            throw new ResourceException(String.format("解压部署文件[%s]异常。", deployPath), ex);
        }

        // FIXME: 多穴主机时应该处理一下
        String host = null;
        try {
            host = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            throw new ResourceException("读取本地地址时出现异常。", ex);
        }
        // 启动服务器
        Cattle cattle = createInstance(app, appVersion, appInstanceOid, capacity,
                host, deployPath.getAbsolutePath());
        return cattle;
    }

    /**
     * 创建发布路径
     * @param profile
     * @param app
     * @return
     */
    private File createDeployPath(Profile profile, App app) {
        // File deployPath = new File(profile.getWorkspace(), app.getOid() + "_" + app.getId() + "_" + app.getVersionOid());
        File deployPath = new File(profile.getWorkspace(), CloudUtils.generateOid());
        LogWriter.debug(logger, "应用的发布路径为：", deployPath.getAbsolutePath());
        //
        if (deployPath.exists()) {
            logger.warn(String.format("删除已经存在的路径[%s]。", deployPath.getAbsolutePath()));
            deployPath.delete();
        }
        deployPath.mkdirs();
        return deployPath;
    }

    /**
     * 创建一个服务实例。
     * @return
     */
    synchronized public Cattle createInstance(final App app, final AppVersion appVersion, String appInstanceOid,
            final Capacity capacity, final String host, final String resourceBase) {
        LogWriter.debug(logger, String.format("创建应用服务器实例[%s]，容量为[%s]...", app, capacity));

        // 启动应用服务
        int port = PortFactory.getInstance().getPort(); // 自动生成一个端口号
        LogWriter.debug(logger, String.format("服务器实例[%s]使用端口号[%d]。", app, port));

        String id = UUID.randomUUID().toString();
        final Cattle cattle = new Cattle(id, app, appVersion, appInstanceOid, host, port);

        // 通过控制器启动服务器
        ServerController controller = new TomcatServerController();
        controller.init(cattle, resourceBase);
        ServerControllerFactory.getInstance().register(cattle, controller);
        controller.start();

        // TODO: 测试启动是否成功（包括应用测试，数据库测试，等等）

        // 创建或者加入组(以“daemon”方式运行此线程)
        Ranch ranch = new Ranch(cattle);
        Thread ranchThread = new Thread(ranch);
        ranchThread.setDaemon(true);
        ranchThread.start();

        cattle.setDeployPath(resourceBase);

        // 向任务管理器注册组（如果是新创建的组）
        LogWriter.info(logger, String.format("[%s]已经成功启动。", cattle));
        return cattle;
    }

    /**
     * 卸载某个指定的应用实例,
     * @param cattleId 实例的唯一编码
     */
    public void undeploy(String cattleId, boolean forcefully) {
        ServerController serverController = getServerController(cattleId);
        if (serverController != null) {
            serverController.stop(forcefully);
        }
        Ranch ranch = RanchManager.getInstance().find(cattleId);
        if (ranch != null) {
            ranch.stop();
        }
    }

    private ServerController getServerController(String cattleId) {
        if (StringUtils.isEmpty(cattleId)) {
            return null;
        }

        ServerController serverController = ServerControllerFactory.getInstance().find(cattleId);
        if (serverController == null) {
            LogWriter.warn(logger, String.format("未找到服务器实例[%s]对应的控制器。", cattleId));
        }
        return serverController;
    }

    /**
     * 重新启动服务器。
     * @param cattleId
     */
    public void restart(String cattleId) {
        ServerController serverController = getServerController(cattleId);
        if (serverController != null) {
            serverController.restart();
        } else {
            LogWriter.warn2(logger, "未找到 cattleId:[%s]对应的控制器。", cattleId);
        }
    }

    /**
     * 暂停一个应用程序
     * @param app
     * @param cattleId
     */
    public boolean pauseApp(App app, String cattleId) {
        LogWriter.warn2(logger, "尝试暂停应用[%s]，工作项实例为[%s]。", app, cattleId);
        if (app == null || cattleId == null) {
            return false;
        }
        Ranch ranch = RanchManager.getInstance().find(cattleId);
        if (ranch == null) {
            LogWriter.warn2(logger, "根据工作项实例[%s]未找到对应的管理组。", cattleId);
            return false;
        }
        Cattle cattle = ranch.getManagedCattle();
        ranch.pauseApp(app, cattle);
        return true;
    }

    public void unregister(App app, AppVersion version, String cattleId) {
        LogWriter.warn2(logger, "撤销对应用[%s]版本[%s]的服务器实例[%s]的注册。", app, version, cattleId);
        if (app == null || cattleId == null || version == null) {
            return;
        }
        Ranch ranch = RanchManager.getInstance().find(cattleId);
        if (ranch == null) {
            LogWriter.warn2(logger, "根据工作项实例[%s]未找到对应的管理组。", cattleId);
            return;
        }
       
        Cattle cattle = ranch.getManagedCattle();
        ranch.unregister(cattle);
    }

    /**
     * 将指定版本设置为应用的默认版本
     * @param app
     * @param cattleId
     * @return
     */
    public boolean setAppDefaultVersion(App app, AppVersion appVersion, String cattleId) {
        LogWriter.warn2(logger, "将应用[%s]的版本设置为[%s]，服务器实例为[%s]。", app, appVersion, cattleId);
        if (app == null || cattleId == null) {
            return false;
        }
        Ranch ranch = RanchManager.getInstance().find(cattleId);
        if (ranch == null) {
            LogWriter.warn2(logger, "根据工作项实例[%s]未找到对应的管理组。", cattleId);
            return false;
        }
        Cattle cattle = ranch.getManagedCattle();
        ranch.setAppDefaultVersion(app, appVersion, cattle);
        return true;
    }
}
