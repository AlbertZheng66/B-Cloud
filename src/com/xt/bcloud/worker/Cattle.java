package com.xt.bcloud.worker;

import com.xt.bcloud.app.App;
import com.xt.bcloud.app.AppVersion;
import java.io.Serializable;
import org.apache.commons.lang.StringUtils;

/**
 * TODO:����ӿ���ͨ�����顱���ǡ�Socket������
 * һ���߳�ռ�ô�Լ 1M RAM
 * @author albert
 */
public class Cattle implements Serializable {

    private static final long serialVersionUID = -2856672157276250389L;
    private final String id;          // Ψһ��ʶ
    private final String ip;          // ���ڷ������� IP ��ַ
    private final int port;           // ռ�õĶ˿ں�
//    private boolean created = false;  // ��ʶ�Ƿ��Ѿ�����
//    private String contextPath;       // Ӧ��ʹ�õ�������
//    private Ranch ranch;              // ���ڵ�ũ��
    private int maxTimeout = 0;       // �����ӳ٣��룩���������ʱ����ֹͣ�������Ա���������
    /**
     * ���ֶα�ʶ����ǰ״̬
     */
    private CattleState state = CattleState.NONE;
    /**
     * ��ǰ���ڵķ���������
     */
    private String serverOid;
    /**
     * ���ڷ����Ӧ����Ϣ
     */
    private final App app;
    /**
     * Ӧ�õİ汾
     */
    private final AppVersion appVersion;
    /**
     * �Դ˷��������������֣�����Խ�ߵġ���ţ������ִ�еĿ�����Խ��
     */
    private volatile int score;
//    /**
//     * �������صĲ���
//     */
//    private BindParameter bindParameter;
    /**
     * ���ȼ�(����ʹ��)
     */
    private int priority;
    /**
     * Ӧ�õķ���·��
     */
    private String deployPath;

    /**
     * Ӧ��ʵ���ı���
     */
    private final String appInstanceOid;

    // ScoreThread ����ά���̣߳�ע��: �߳�ʹ�ú��ʵ����ȼ���
    // Rancher     �̣߳�ֻ�е�ǰ�������ǹ����ߵ�ʱ���ά�����̣߳�
    
    // ���������߳�
    public Cattle(String id, App app, AppVersion appVersion, String appInstanceOid, String ip, int port) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.app = app;
        this.appVersion = appVersion;
        this.appInstanceOid = appInstanceOid;

//        // �Զ������������
//        String _contextPath = app.getId() + "_" + app.getVersionOid();  // ���չ�������һ��Ӧ�á�
//        // ���汾���еĵ��滻Ϊ�»���"_"
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
//     * ע�⣺��������·��ֻ�ڹ��캯���ڽ����˼��㣬��ˣ�����֮��ı䡰app�������ֵ����������������·���ı仯��
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
     * ���ص�ǰ����������Ϣ��
     * ����汾�������Ĳ�Ϊ�գ���ʹ�ð汾�������ģ�����ʹ����Ӧ���ж���������ġ�
     * ע�⣺ContextPath����ʽ�ǣ��ԡ�/����ͷ�������ԡ�/����β�����򷢲����ܷ��ʡ����磺"/gt_demo"
     * @return �������ַ��������Ӧ�û���Ӧ�ð汾Ϊ�գ��򷵻ؿա�
     */
    public String getContextPath() {
        if (app == null || appVersion == null) {
            return null;
        }
        String contextPath = StringUtils.isEmpty(appVersion.getContextPath()) ?
                         app.getContextPath() : appVersion.getContextPath();
        // FIXME: Ӧ�ó����������·������Ϊ�գ�
        return contextPath;
    }

    public String getAppInstanceOid() {
        return appInstanceOid;
    }
}

