package com.xt.bcloud.bg.impl;

import com.xt.bcloud.app.AppInstance;
import com.xt.bcloud.app.AppService;
import com.xt.bcloud.bg.Inspectable;
import com.xt.bcloud.comm.CloudUtils;
import com.xt.core.log.LogWriter;
import com.xt.core.proc.impl.fs.FileService;
import com.xt.core.proc.impl.fs.FileServiceAware;
import com.xt.core.proc.impl.fs.LocalFileService;
import com.xt.core.service.LocalMethod;
import com.xt.gt.sys.SystemConfiguration;
import com.xt.proxy.Proxy;
import com.xt.proxy.ServiceFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * �����������ɾ��Ӧ����ֹͣ��"������"�������ļ�ϵͳ������ѹ����
 * Ӧ���ڷ�����ʵ�����ڵĻ��������С�
 * @author albert
 */
public class DeployedPackageInspector implements Inspectable, FileServiceAware {

    private final Logger logger = Logger.getLogger(DeployedPackageInspector.class);
    /**
     * ʵ��ɾ���󣬷���Ŀ¼������ʱ�䡣
     */
    private final int reserverdDays = SystemConfiguration.getInstance().readInt("deployedPathInspector.reserverdDays", 3);
    /**
     * ֻ�����صģ�IP��ַ�ͱ���IP��ַ�����Ӧ��ʵ���������ڷǷֲ�ʽ�ļ�ϵͳʱ�������⣩��
     */
    private final boolean localOnly = SystemConfiguration.getInstance().readBoolean("deployedPathInspector.localOnly", true);
    private String localAddress;

    public DeployedPackageInspector() {
        try {
            localAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            LogWriter.warn2(logger, ex, "δ�ܻ�ȡ���ص� IP ��ַ��");
        }
    }
    /**
     * �ļ�������ʵ��
     */
    private transient FileService fileService;

    public void excecute() {
        if (localAddress == null) {
            return;
        }

        // ȥ����������϶�ȡ�Ѿ�ֹͣ�ķ�����ʵ��
        Proxy proxy = CloudUtils.createArmProxy();
        AppService appService = ServiceFactory.getInstance().getService(AppService.class, proxy);
        List<AppInstance> instances = appService.readStopedInstances(localAddress, reserverdDays);
        for (Iterator<AppInstance> it = instances.iterator(); it.hasNext();) {
            AppInstance appInstance = it.next();
            String deployPath = appInstance.getDeployPath();
            if (StringUtils.isNotEmpty(deployPath)) {
                LogWriter.info2(logger, "�������Ӧ��[%s]�Ĳ����ļ�[%s]��", appInstance, deployPath);
                if (localOnly && !localAddress.equals(appInstance.getIp())) {
                    // ֻ�����ص��ļ�ϵͳ
                    continue;
                }
                // ��ǰ·���Ǳ����ļ�����ʱ��Ҫɾ����ǰ·����ǰ׺��
                if (fileService instanceof LocalFileService) {
                    LocalFileService lfs = ((LocalFileService) fileService);
                    LogWriter.info2(logger, "����·����·��Ϊ[%s]��", lfs.getRootPath());
                    if (lfs.getRootPath() != null) {
                        String rootPath = lfs.getRootPath().getAbsolutePath();
                        if (deployPath.startsWith(rootPath)) {
                            deployPath = deployPath.substring(rootPath.length());
                            LogWriter.info2(logger, "������·��ȥ��ǰ׺��ת��Ϊ[%s]��", deployPath);
                        }
                    }
                }
                if (fileService.exists(deployPath)) {
                    LogWriter.info2(logger, "����ɾ��·��[%s]��", deployPath);
                    // FIXME: ·������Ϊ�ղ���ɾ��
                    if (fileService.delete(deployPath)) {
                        // ϵͳ�����ݿ�ļ�¼����Ϊ�գ������ظ�����
                        appService.clearDeployedPath(appInstance);
                    }
                } else {
                    LogWriter.warn2(logger, "����·��[%s]�Ѿ������ڡ�", deployPath);
                }
            }
        }
    }

    @LocalMethod
    public void setFileService(FileService _fileService) {
        this.fileService = _fileService;
    }

    @LocalMethod
    public FileService getFileService() {
        return fileService;
    }
}
