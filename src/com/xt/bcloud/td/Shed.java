package com.xt.bcloud.td;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.xt.bcloud.app.App;
import com.xt.bcloud.app.AppVersion;
import com.xt.bcloud.td.impl.DefaultVersionSelection;
import com.xt.bcloud.td7.CookieReader;
import com.xt.bcloud.worker.Cattle;
import com.xt.core.log.LogWriter;
import com.xt.gt.sys.SystemConfiguration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author albert
 */
public class Shed {

    private final Logger logger = Logger.getLogger(Shed.class);
    /**
     * 牛栏中所有的牛。
     */
    private final Set<Cattle> cattles = new HashSet<Cattle>();

    // 可重入读写锁实例
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    // 读锁
    private final Lock readLock = rwl.readLock();
    // 写锁
    private final Lock writeLock = rwl.writeLock();
    /**
     * 此 Map 用于存储版本和“牛”（服务器实例）映射关系（1:N）
     */
    private final Multimap<AppVersion, Cattle> versionsMap = ArrayListMultimap.create();
    /**
     * TODO: 限制指定应用的“最大连接数”。注意：每个应用可以指定不同的连接数（连接作为资源进行处理）。
     * 限制的文件大小（每个应用分别限制）
     */
    /**
     * 版本选择接口
     */
    private final VersionSelectable versionSelectable =
            (VersionSelectable) SystemConfiguration.getInstance().readObject("taskDispatcher.versionSelection", new DefaultVersionSelection());
    /**
     * 缺省的版本
     */
    private AppVersion defaultVersion;
    /**
     * 当前的应用
     */
    private final App app;

    public Shed(App app) {
        this.app = app;
    }

    @Override
    public String toString() {
        StringBuilder strBld = new StringBuilder();
        strBld.append(super.toString()).append("[");
        strBld.append("cattles=").append(cattles);
        strBld.append("]");
        return strBld.toString();
    }

    /**
     * 根据头信息，选择可以提供服务的“牛”。
     * @param request
     * @return
     */
    public Collection<Cattle> service(CookieReader cookieReader, Set<Cattle> excluded) {
        if (versionSelectable == null) {
            return Collections.unmodifiableCollection(cattles);
        }
        readLock.lock();
        try {
            AppVersion selectedVer = versionSelectable.select(cookieReader, defaultVersion, versionsMap.keySet());
            if (selectedVer == null) {
                selectedVer = defaultVersion;
            }
            Collection<Cattle> selectedCattles = versionsMap.get(selectedVer);
            LogWriter.info2(logger, "当前版本[%s]对应的可选择实例[%s]。", selectedVer, selectedCattles);
            if (excluded == null || excluded.isEmpty()) {
                return selectedCattles;
            }

            // 剔除“排除”的使用实例
            List<Cattle> availableCattles = new ArrayList(selectedCattles.size());
            for (Iterator<Cattle> it = selectedCattles.iterator(); it.hasNext();) {
                Cattle cattle = it.next();
                if (!excluded.contains(cattle)) {
                    availableCattles.add(cattle);
                }
            }
            return availableCattles;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 增加一头牛。
     * @param cattle
     */
    public void addCattle(Cattle cattle) {
        if (cattle == null) {
            return;
        }
        AppVersion appVersion = cattle.getAppVersion();
        if (appVersion == null) {
            LogWriter.warn2(logger, "服务器实例[%s]未定义版本。", cattle);
            return;
        }
        writeLock.lock();
        try {
            cattles.add(cattle);
            versionsMap.put(appVersion, cattle);
            if (defaultVersion == null) {
                defaultVersion = appVersion;
            }
        } finally {
            writeLock.unlock();
        }
    }

    public void removeCattle(Cattle cattle) {
        if (cattle == null) {
            return;
        }
        AppVersion appVersion = cattle.getAppVersion();

        if (appVersion == null) {
            LogWriter.warn2(logger, "服务器实例[%s]未定义版本。", cattle);
            return;
        }

        writeLock.lock();
        try {
            cattles.remove(cattle);
            versionsMap.remove(appVersion, cattle);
//            if (defaultVersion != null
//                    && defaultVersion.equals(appVersion)
//                    && !versionsMap.containsKey(defaultVersion)) {
            // 注意：默认版本只能是主动通知才能更改，“牛”没了，不一定是版本升级
//                //默认版本都删除了
//                if (versionsMap.size() < 1) {
//                    this.defaultVersion = null;
//                } else {
//                    // 默认使用最新的版本。
//                    this.defaultVersion = Collections.max(versionsMap.keys());
//                }
//            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 找到服务器地址对应的处理器实例，一般用于 Session 粘滞的情况，即尽量使用相同的服务器处理
     * 同一个客户端的请求。
     * @param serverURL 服务器的地址，形式为：127.0.0.1:888.
     * @return 如果对应传入地址的服务器仍然存在，则返回此服务器实例，否则返回空。
     */
    public Cattle findStickedCattle(String serverURL) {
        if (StringUtils.isEmpty(serverURL)) {
            return null;
        }
        readLock.lock();
        try {
            for (Iterator<Cattle> it = cattles.iterator(); it.hasNext();) {
                Cattle cattle = it.next();
                if (serverURL.startsWith(cattle.getIp())
                        && serverURL.endsWith(String.valueOf(cattle.getPort()))) {
                    return cattle;
                }
            }
        } finally {
            readLock.unlock();
        }
        return null;
    }

    /**
     * 重新装载所有的牛。
     * TODO: 对性能影响很大，需要仔细考虑
     * @param cattles
     */
    public void reload(Collection<Cattle> _cattles) {
        if (_cattles == null) {
            return;
        }
        writeLock.lock();

        try {
            cattles.clear();
            versionsMap.clear();
            for (Iterator<Cattle> it = _cattles.iterator(); it.hasNext();) {
                Cattle cattle = it.next();
                addCattle(cattle);
            }
        } finally {
            writeLock.unlock();

        }
    }

    public void setDefaultVersion(AppVersion defaultVersion) {
        if (defaultVersion != null) {
            this.defaultVersion = defaultVersion;
            LogWriter.info2(logger, "将应用[%s]的默认版本设定为[%s]", app, defaultVersion);
        }

    }
}
