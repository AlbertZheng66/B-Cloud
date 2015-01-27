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
     * ţ�������е�ţ��
     */
    private final Set<Cattle> cattles = new HashSet<Cattle>();

    // �������д��ʵ��
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    // ����
    private final Lock readLock = rwl.readLock();
    // д��
    private final Lock writeLock = rwl.writeLock();
    /**
     * �� Map ���ڴ洢�汾�͡�ţ����������ʵ����ӳ���ϵ��1:N��
     */
    private final Multimap<AppVersion, Cattle> versionsMap = ArrayListMultimap.create();
    /**
     * TODO: ����ָ��Ӧ�õġ��������������ע�⣺ÿ��Ӧ�ÿ���ָ����ͬ����������������Ϊ��Դ���д�����
     * ���Ƶ��ļ���С��ÿ��Ӧ�÷ֱ����ƣ�
     */
    /**
     * �汾ѡ��ӿ�
     */
    private final VersionSelectable versionSelectable =
            (VersionSelectable) SystemConfiguration.getInstance().readObject("taskDispatcher.versionSelection", new DefaultVersionSelection());
    /**
     * ȱʡ�İ汾
     */
    private AppVersion defaultVersion;
    /**
     * ��ǰ��Ӧ��
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
     * ����ͷ��Ϣ��ѡ������ṩ����ġ�ţ����
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
            LogWriter.info2(logger, "��ǰ�汾[%s]��Ӧ�Ŀ�ѡ��ʵ��[%s]��", selectedVer, selectedCattles);
            if (excluded == null || excluded.isEmpty()) {
                return selectedCattles;
            }

            // �޳����ų�����ʹ��ʵ��
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
     * ����һͷţ��
     * @param cattle
     */
    public void addCattle(Cattle cattle) {
        if (cattle == null) {
            return;
        }
        AppVersion appVersion = cattle.getAppVersion();
        if (appVersion == null) {
            LogWriter.warn2(logger, "������ʵ��[%s]δ����汾��", cattle);
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
            LogWriter.warn2(logger, "������ʵ��[%s]δ����汾��", cattle);
            return;
        }

        writeLock.lock();
        try {
            cattles.remove(cattle);
            versionsMap.remove(appVersion, cattle);
//            if (defaultVersion != null
//                    && defaultVersion.equals(appVersion)
//                    && !versionsMap.containsKey(defaultVersion)) {
            // ע�⣺Ĭ�ϰ汾ֻ��������֪ͨ���ܸ��ģ���ţ��û�ˣ���һ���ǰ汾����
//                //Ĭ�ϰ汾��ɾ����
//                if (versionsMap.size() < 1) {
//                    this.defaultVersion = null;
//                } else {
//                    // Ĭ��ʹ�����µİ汾��
//                    this.defaultVersion = Collections.max(versionsMap.keys());
//                }
//            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * �ҵ���������ַ��Ӧ�Ĵ�����ʵ����һ������ Session ճ�͵������������ʹ����ͬ�ķ���������
     * ͬһ���ͻ��˵�����
     * @param serverURL �������ĵ�ַ����ʽΪ��127.0.0.1:888.
     * @return �����Ӧ�����ַ�ķ�������Ȼ���ڣ��򷵻ش˷�����ʵ�������򷵻ؿա�
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
     * ����װ�����е�ţ��
     * TODO: ������Ӱ��ܴ���Ҫ��ϸ����
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
            LogWriter.info2(logger, "��Ӧ��[%s]��Ĭ�ϰ汾�趨Ϊ[%s]", app, defaultVersion);
        }

    }
}
