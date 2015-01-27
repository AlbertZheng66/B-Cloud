package com.xt.bcloud.pf.app;

import com.xt.bcloud.app.*;
import com.xt.bcloud.pf.AbstractProfilingService;
import com.xt.bcloud.pf.jvm.JVMProfilingInfo;
import com.xt.bcloud.pf.jvm.JVMProfilingService;
import com.xt.bcloud.pf.jvm.Utils;
import com.xt.core.proc.impl.InjectService;
import com.xt.core.service.LocalMethod;
import java.util.*;

/**
 *
 * @author Albert
 */
public class AppProfilingService extends AbstractProfilingService {

    private static final long serialVersionUID = -6342580403379265729L;
    @InjectService
    protected transient AppService appService;
    @InjectService
    protected transient JVMProfilingService jvmProfilingService;

    public AppProfilingService() {
    }

    public List<AppProfilingInfo> listApps() {
        List<App> apps = appService.list();
        if (apps.isEmpty()) {
            return Collections.emptyList();
        }

        List<AppProfilingInfo> appProfilings = new ArrayList();
        Map<String, JVMProfilingInfo> pisMap = getPisMap();
        for (Iterator<App> it = apps.iterator(); it.hasNext();) {
            App app = it.next();
            if (app.getState() != AppState.RUNNING) {
                continue;
            }
            AppProfilingInfo appProfilingInfo = createAppProfilingInfo(app);
            List<AppVersion> versions = appService.listVersions(app);
            for (Iterator<AppVersion> it1 = versions.iterator(); it1.hasNext();) {
                AppVersion version = it1.next();
                if (version.getState() != AppVersionState.RUNNING) {
                    continue;
                }
                List<AppInstance> instances = appService.listInstances(app, version);
                for (Iterator<AppInstance> it2 = instances.iterator(); it2.hasNext();) {
                    AppInstance instance = it2.next();
                    // 对相同应用实例的对象进行汇总
                    JVMProfilingInfo profilingInfo = getJVMProfilingInfo(pisMap, instance);
                    if (profilingInfo != null) {
                        appProfilingInfo.add(profilingInfo.getClassLoadingInfo());
                        appProfilingInfo.add(profilingInfo.getCpuInfo());
                        appProfilingInfo.add(profilingInfo.getMemoryInfo());
                        appProfilingInfo.add(profilingInfo.getThreadInfo());
                        appProfilingInfo.add(profilingInfo.getUptime());
                        appProfilingInfo.addInstanceCount();
                    }
                }
            }
        }
        return appProfilings;
    }

    private AppProfilingInfo createAppProfilingInfo(App app) {
        AppProfilingInfo appProfilingInfo = new AppProfilingInfo();
        AppVersion appVersion = appService.getAppVersion(app.getVersionOid());
        appProfilingInfo.setAppVersion(appVersion.getVersion());
        appProfilingInfo.setId(app.getId());
        appProfilingInfo.setOid(app.getOid());
        appProfilingInfo.setName(app.getName());
        appProfilingInfo.setCpuInfo(Utils.EMPTY_CPU_INFO);
        appProfilingInfo.setMemoryInfo(Utils.EMPTY_MEMORY_INFO);
        appProfilingInfo.setThreadInfo(Utils.EMPTY_THREAD_INFO);
        appProfilingInfo.setClassLoadingInfo(Utils.EMPTY_CLASS_LOADING_INFO);
        return appProfilingInfo;
    }

    private Map<String, JVMProfilingInfo> getPisMap() {
        final List<JVMProfilingInfo> profilingInfos = jvmProfilingService.listServers();
        final Map<String, JVMProfilingInfo> pisMap = new HashMap();
        for (Iterator<JVMProfilingInfo> it = profilingInfos.iterator(); it.hasNext();) {
            JVMProfilingInfo jpi = it.next();
            pisMap.put(jpi.getOid(), jpi);
        }
        return pisMap;
    }

    private JVMProfilingInfo getJVMProfilingInfo(Map<String, JVMProfilingInfo> pis, AppInstance instance) {
        if (instance == null || instance.getServerOid() == null) {
            return null;
        }
        String oid = instance.getServerOid();
        return pis.get(oid);
    }

    @LocalMethod
    public AppService getAppService() {
        return appService;
    }

    @LocalMethod
    public void setAppService(AppService appService) {
        this.appService = appService;
    }

    @LocalMethod
    public JVMProfilingService getJvmProfilingService() {
        return jvmProfilingService;
    }

    @LocalMethod
    public void setJvmProfilingService(JVMProfilingService jvmProfilingService) {
        this.jvmProfilingService = jvmProfilingService;
    }
}
