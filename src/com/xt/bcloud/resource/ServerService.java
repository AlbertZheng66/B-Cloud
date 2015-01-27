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
 * �����������࣬���ڹ������ķ���Ľӿ��ࡣ
 * @author albert
 */
public class ServerService implements IService {

    private final Logger logger = Logger.getLogger(ServerService.class);

    public ServerService() {
    }

    /**
     * TODO: Ŀǰ�������и�©���������û�����ͨ������֮�������ô˷������Ժ��ٴ����������
     * ���������ƶ˿ڵķ�ʽ�ɽ��������⣿����
     * @param app
     */
    public Cattle deploy(App app, AppVersion appVersion, String appInstanceOid, Capacity capacity) {
        LogWriter.info(logger, String.format("���ڲ���Ӧ��[%s], �汾[%s]������Ϊ[%s]��",
                app, appVersion, capacity));

        // ��Ҫͨ�� Profile ��ȡ����·��
        Profile profile = ServerManager.getInstance().getProfile();
        if (profile == null || profile.getWorkspace() == null) {
            throw new ResourceException("δ��ȡ�����ص� Profile�������ж���Ĺ���Ŀ¼����Ϊ�ա�");
        }

        // ��������·��
        File deployPath = createDeployPath(profile, app);


        // ��Ӧ�ù��������뷢����
        Proxy proxy = CloudUtils.createArmProxy();
        AppService appSerivce = ServiceFactory.getInstance().getService(AppService.class, proxy);
        // ������ָ��·��(���ϴ����ļ���ѹ��)
        InputStream is = appSerivce.getDeployedPackage(app, appVersion);
        if (is == null) {
            // δ��ȡ��Ӧ�÷�������Ϣ
            throw new ResourceException(String.format("δ��ȡ���汾Ϊ[%s]��Ӧ��[%s]�ķ�������", appVersion, app));
        }
        // test............
        try {
            LogWriter.debug(logger, "��ȡ�ֽ���========" + is.available());
            // FileOutputStream fos = new FileOutputStream("e:\\test.war", false);
            BufferedInputStream bis = new BufferedInputStream(is);
            // TeeInputStream tis = new TeeInputStream(bis, fos, false);
            UnZip unzip = new UnZip();
            unzip.unZip(bis, deployPath);  // �ͷŵ�ѹ��·���£�ע���ѹ��·����
            // fos.close();
        } catch (IOException ex) {
            throw new ResourceException(String.format("��ѹ�����ļ�[%s]�쳣��", deployPath), ex);
        }

        // FIXME: ��Ѩ����ʱӦ�ô���һ��
        String host = null;
        try {
            host = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            throw new ResourceException("��ȡ���ص�ַʱ�����쳣��", ex);
        }
        // ����������
        Cattle cattle = createInstance(app, appVersion, appInstanceOid, capacity,
                host, deployPath.getAbsolutePath());
        return cattle;
    }

    /**
     * ��������·��
     * @param profile
     * @param app
     * @return
     */
    private File createDeployPath(Profile profile, App app) {
        // File deployPath = new File(profile.getWorkspace(), app.getOid() + "_" + app.getId() + "_" + app.getVersionOid());
        File deployPath = new File(profile.getWorkspace(), CloudUtils.generateOid());
        LogWriter.debug(logger, "Ӧ�õķ���·��Ϊ��", deployPath.getAbsolutePath());
        //
        if (deployPath.exists()) {
            logger.warn(String.format("ɾ���Ѿ����ڵ�·��[%s]��", deployPath.getAbsolutePath()));
            deployPath.delete();
        }
        deployPath.mkdirs();
        return deployPath;
    }

    /**
     * ����һ������ʵ����
     * @return
     */
    synchronized public Cattle createInstance(final App app, final AppVersion appVersion, String appInstanceOid,
            final Capacity capacity, final String host, final String resourceBase) {
        LogWriter.debug(logger, String.format("����Ӧ�÷�����ʵ��[%s]������Ϊ[%s]...", app, capacity));

        // ����Ӧ�÷���
        int port = PortFactory.getInstance().getPort(); // �Զ�����һ���˿ں�
        LogWriter.debug(logger, String.format("������ʵ��[%s]ʹ�ö˿ں�[%d]��", app, port));

        String id = UUID.randomUUID().toString();
        final Cattle cattle = new Cattle(id, app, appVersion, appInstanceOid, host, port);

        // ͨ������������������
        ServerController controller = new TomcatServerController();
        controller.init(cattle, resourceBase);
        ServerControllerFactory.getInstance().register(cattle, controller);
        controller.start();

        // TODO: ���������Ƿ�ɹ�������Ӧ�ò��ԣ����ݿ���ԣ��ȵȣ�

        // �������߼�����(�ԡ�daemon����ʽ���д��߳�)
        Ranch ranch = new Ranch(cattle);
        Thread ranchThread = new Thread(ranch);
        ranchThread.setDaemon(true);
        ranchThread.start();

        cattle.setDeployPath(resourceBase);

        // �����������ע���飨������´������飩
        LogWriter.info(logger, String.format("[%s]�Ѿ��ɹ�������", cattle));
        return cattle;
    }

    /**
     * ж��ĳ��ָ����Ӧ��ʵ��,
     * @param cattleId ʵ����Ψһ����
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
            LogWriter.warn(logger, String.format("δ�ҵ�������ʵ��[%s]��Ӧ�Ŀ�������", cattleId));
        }
        return serverController;
    }

    /**
     * ����������������
     * @param cattleId
     */
    public void restart(String cattleId) {
        ServerController serverController = getServerController(cattleId);
        if (serverController != null) {
            serverController.restart();
        } else {
            LogWriter.warn2(logger, "δ�ҵ� cattleId:[%s]��Ӧ�Ŀ�������", cattleId);
        }
    }

    /**
     * ��ͣһ��Ӧ�ó���
     * @param app
     * @param cattleId
     */
    public boolean pauseApp(App app, String cattleId) {
        LogWriter.warn2(logger, "������ͣӦ��[%s]��������ʵ��Ϊ[%s]��", app, cattleId);
        if (app == null || cattleId == null) {
            return false;
        }
        Ranch ranch = RanchManager.getInstance().find(cattleId);
        if (ranch == null) {
            LogWriter.warn2(logger, "���ݹ�����ʵ��[%s]δ�ҵ���Ӧ�Ĺ����顣", cattleId);
            return false;
        }
        Cattle cattle = ranch.getManagedCattle();
        ranch.pauseApp(app, cattle);
        return true;
    }

    public void unregister(App app, AppVersion version, String cattleId) {
        LogWriter.warn2(logger, "������Ӧ��[%s]�汾[%s]�ķ�����ʵ��[%s]��ע�ᡣ", app, version, cattleId);
        if (app == null || cattleId == null || version == null) {
            return;
        }
        Ranch ranch = RanchManager.getInstance().find(cattleId);
        if (ranch == null) {
            LogWriter.warn2(logger, "���ݹ�����ʵ��[%s]δ�ҵ���Ӧ�Ĺ����顣", cattleId);
            return;
        }
       
        Cattle cattle = ranch.getManagedCattle();
        ranch.unregister(cattle);
    }

    /**
     * ��ָ���汾����ΪӦ�õ�Ĭ�ϰ汾
     * @param app
     * @param cattleId
     * @return
     */
    public boolean setAppDefaultVersion(App app, AppVersion appVersion, String cattleId) {
        LogWriter.warn2(logger, "��Ӧ��[%s]�İ汾����Ϊ[%s]��������ʵ��Ϊ[%s]��", app, appVersion, cattleId);
        if (app == null || cattleId == null) {
            return false;
        }
        Ranch ranch = RanchManager.getInstance().find(cattleId);
        if (ranch == null) {
            LogWriter.warn2(logger, "���ݹ�����ʵ��[%s]δ�ҵ���Ӧ�Ĺ����顣", cattleId);
            return false;
        }
        Cattle cattle = ranch.getManagedCattle();
        ranch.setAppDefaultVersion(app, appVersion, cattle);
        return true;
    }
}
