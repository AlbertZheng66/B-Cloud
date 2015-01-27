package com.xt.bcloud.mdu.command;

import com.xt.bcloud.comm.PortFactory;
import com.xt.bcloud.comm.PortRange;
import com.xt.bcloud.comm.ValueReader;
import com.xt.core.utils.DateUtils;
import com.xt.core.utils.RandomUtils;
import java.util.Calendar;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 *
 * @author Albert
 */
public class CommandValueReader implements ValueReader {

    private final Map params;

    public CommandValueReader(Map params) {
        this.params = params;
    }

    public String readValue(String name) {
        if ("_random".equals(name)) {
            // ����һ�������
            return RandomUtils.generateRandomKeyCode();
        } else if (name.matches("^_range\\[\\d+,\\d+\\]$")) {
            // ���ض���Χ�ڲ���һ����
            String subName = name.substring("_range[".length(), name.length() - 1);
            PortRange pr = parse(subName);
            return RandomUtils.randomRange(pr.getStartIndex(), pr.getEndIndex());
        } else if (name.matches("^_port\\[\\d+,\\d+\\]$")) {
            // ���ض���Χ�ڲ���һ����
            String subName = name.substring("_port[".length(), name.length() - 1);
            PortRange pr = parse(subName);
            return String.valueOf(PortFactory.getInstance().getPort(pr.getStartIndex(), pr.getEndIndex()));
        } else if (name.matches("^_inc\\[[_a-zA-Z][_a-zA-Z0-9]*\\]$")) {
            String incName = name.substring("_inc[".length(), name.length() - 1);
            return readNext(incName);
            // �������ж�����ʼֵ��Ȼ���һ
        } else if ("_date".equals(name)) {
            // ���ص�ǰ��־
            return DateUtils.toDateStr(Calendar.getInstance(), "yyyyMMdd");
        } else if ("_time".equals(name)) {
            return DateUtils.toDateStr(Calendar.getInstance(), "yyyyMMdd_HHmmss");
        } else {
            // �Ӳ�����ȡֵ
            Object _value = params.get(name);
            if (_value != null) {
                return String.valueOf(_value);
            }
        }
        return "${" + name + "}";
    }

    private String readNext(String incName) {
        Preferences node = Preferences.userRoot().node("inc");
        String next = String.valueOf(node.getInt(incName, -1) + 1);
        node.put(incName, next);
        return next;
    }

    private PortRange parse(String rangeStr) {
        String[] segs = rangeStr.split("[,]");
        int startIndex = Integer.parseInt(segs[0]);
        int endIndex = Integer.parseInt(segs[1]);
        PortRange pr = new PortRange();
        pr.setStartIndex(startIndex);
        pr.setEndIndex(endIndex);
        return pr;
    }
}
