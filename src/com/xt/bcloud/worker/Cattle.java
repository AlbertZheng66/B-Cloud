package com.xt.bcloud.worker;

import com.xt.bcloud.app.App;
import com.xt.bcloud.app.AppVersion;
import java.io.Serializable;
import org.apache.commons.lang.StringUtils;

/**
 * TODO:管理接口是通过“组”还是“Socket”？！
 * 一个线程占用大约 1M RAM
 * @author albert
 */
public class Cattle implements Serializable {

    private static final long serialVersionUID = -2856672157276250389L;
    private final String id;          // 唯一标识
    private final String ip;          // 所在服务器的 IP 地址
    private final int port;           // 占用的端口号
//    private boolean created = false;  // 标识是否已经创建
//    private String contextPath;       // 应用使用的上下文
//    private Ranch ranch;              // 所在的农场
    private int maxTimeout = 0;       // 最多的延迟（秒），如果任务超时，则停止此任务，以避免死锁。
    /**
     * 此字段标识出当前状态
     */
    private CattleState state = CattleState.NONE;
    /**
     * 当前所在的服务器编码
     */
    private String serverOid;
    /**
     * 正在服务的应用信息
     */
    private final App app;
    /**
     * 应用的版本
     */
    private final AppVersion appVersion;
    /**
     * 对此服务器的性能评分，评分越高的“奶牛”或者执行的可能性越大。
     */
    private volatile int score;
//    /**
//     * 和组绑定相关的参数
//     */
//    private BindParameter bindParameter;
    /**
     * 优先级(后续使用)
     */
    private int priority;
    /**
     * 应用的发布路径
     */
    private String deployPath;

    /**
     * 应用实例的编码
     */
    private final String appInstanceOid;

    // ScoreThread 评分维护线程（注意: 线程使用合适的优先级）
    // Rancher     线程（只有当前服务器是管理者的时候才维护此线程）
    
    // 启动管理线程
    public Cattle(String id, App app, AppVersion appVersion, String appInstanceOid, String ip, int port) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.app = app;
        this.appVersion = appVersion;
        this.appInstanceOid = appInstanceOid;

//        // 自动计算的上下文
//        String _contextPath = app.getId() + "_" + app.getVersionOid();  // 按照规则生成一个应用。
//        // 将版本号中的点替换为下划线"_"
////        this.contextPath = _contextPath.replaceAll("[.]", "_");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Cattle other = (Cattle) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder strBld = new StringBuilder();
        strBld.append(super.toString()).append("[");
        strBld.append("id=").append(id).append("; ");
        strBld.append("app=").append(app).append("; ");
        strBld.append("ip=").append(ip).append("; ");
        strBld.append("port=").append(port).append("; ");
        strBld.append("deployPath=").append(deployPath);
        //strBld.append("contextPath=").append(contextPath);
        strBld.append("]");
        return strBld.toString();
    }

    public String getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public App getApp() {
        return app;
    }

//    /**
//     * 注意：此上下文路径只在构造函数内进行了计算，因此，构造之后改变“app”对象的值将不会引起上下文路径的变化。
//     * @return
//     */
//    public String getContextPath() {
//        return contextPath;
//    }
    public String getServerOid() {
        return serverOid;
    }

    public void setServerOid(String serverOid) {
        this.serverOid = serverOid;
    }

    public AppVersion getAppVersion() {
        return appVersion;
    }

//    public BindParameter getBindParameter() {
//        return bindParameter;
//    }
//
//    public void setBindParameter(BindParameter bindParameter) {
//        this.bindParameter = bindParameter;
//    }

    public String getDeployPath() {
        return deployPath;
    }

    public void setDeployPath(String deployPath) {
        this.deployPath = deployPath;
    }

    /**
     * 返回当前的上下文信息。
     * 如果版本的上下文不为空，则使用版本的上下文；否则，使用在应用中定义的上下文。
     * 注意：ContextPath的形式是：以“/”开头，不能以“/”结尾，否则发布后不能访问。例如："/gt_demo"
     * @return 上下文字符串。如果应用或者应用版本为空，则返回空。
     */
    public String getContextPath() {
        if (app == null || appVersion == null) {
            return null;
        }
        String contextPath = StringUtils.isEmpty(appVersion.getContextPath()) ?
                         app.getContextPath() : appVersion.getContextPath();
        // FIXME: 应用程序的上下文路径不能为空！
        return contextPath;
    }

    public String getAppInstanceOid() {
        return appInstanceOid;
    }
}

