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
 * 此类的任务是删除应用已停止的"发布包"，减少文件系统带来的压力。
 * 应该在服务器实例所在的机器上运行。
 * @author albert
 */
public class DeployedPackageInspector implements Inspectable, FileServiceAware {

    private final Logger logger = Logger.getLogger(DeployedPackageInspector.class);
    /**
     * 实例删除后，发布目录保留的时间。
     */
    private final int reserverdDays = SystemConfiguration.getInstance().readInt("deployedPathInspector.reserverdDays", 3);
    /**
     * 只处理本地的（IP地址和本地IP地址相符的应用实例，避免在非分布式文件系统时出现问题）。
     */
    private final boolean localOnly = SystemConfiguration.getInstance().readBoolean("deployedPathInspector.localOnly", true);
    private String localAddress;

    public DeployedPackageInspector() {
        try {
            localAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            LogWriter.warn2(logger, ex, "未能获取本地的 IP 地址。");
        }
    }
    /**
     * 文件服务器实例
     */
    private transient FileService fileService;

    public void excecute() {
        if (localAddress == null) {
            return;
        }

        // 去请求服务器上读取已经停止的服务器实例
        Proxy proxy = CloudUtils.createArmProxy();
        AppService appService = ServiceFactory.getInstance().getService(AppService.class, proxy);
        List<AppInstance> instances = appService.readStopedInstances(localAddress, reserverdDays);
        for (Iterator<AppInstance> it = instances.iterator(); it.hasNext();) {
            AppInstance appInstance = it.next();
            String deployPath = appInstance.getDeployPath();
            if (StringUtils.isNotEmpty(deployPath)) {
                LogWriter.info2(logger, "正在清除应用[%s]的部署文件[%s]。", appInstance, deployPath);
                if (localOnly && !localAddress.equals(appInstance.getIp())) {
                    // 只处理本地的文件系统
                    continue;
                }
                // 当前路径是本地文件服务时，要删除当前路径的前缀。
                if (fileService instanceof LocalFileService) {
                    LocalFileService lfs = ((LocalFileService) fileService);
                    LogWriter.info2(logger, "本地路径跟路径为[%s]。", lfs.getRootPath());
                    if (lfs.getRootPath() != null) {
                        String rootPath = lfs.getRootPath().getAbsolutePath();
                        if (deployPath.startsWith(rootPath)) {
                            deployPath = deployPath.substring(rootPath.length());
                            LogWriter.info2(logger, "将本地路径去除前缀，转换为[%s]。", deployPath);
                        }
                    }
                }
                if (fileService.exists(deployPath)) {
                    LogWriter.info2(logger, "正在删除路径[%s]。", deployPath);
                    // FIXME: 路径必须为空才能删除
                    if (fileService.delete(deployPath)) {
                        // 系统将数据库的记录设置为空，避免重复处理
                        appService.clearDeployedPath(appInstance);
                    }
                } else {
                    LogWriter.warn2(logger, "发布路径[%s]已经不存在。", deployPath);
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
