package com.xt.bcloud.comm;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Albert
 */
public class VarParser {

    public static String parse(String value, ValueReader reader) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }
        char[] _chars = value.toCharArray();
        StringBuilder actualValue = new StringBuilder();
        StringBuilder varName = new StringBuilder();
        boolean begin = false;
        boolean hasNext = true;  // 是否还有下一个节点
        for (int i = 0; i < _chars.length; i++) {
            hasNext = i < (_chars.length - 1);
            char c = _chars[i];
            if (begin) {
                if (c == '}') {
                    begin = false;
                    //TODO: 在此替换变量
                    String _varName = varName.toString();
                    varName.delete(0, varName.length());  // 清除名称
                    String _value = reader.readValue(_varName);
                    actualValue.append(_value == null ? "" : _value);
                } else {
                    varName.append(c);
                }
                continue;
            }
            if (c == '$' && hasNext && _chars[i + 1] == '{') {
                begin = true;
                i++;  // 跳过“{”
                continue;
            }
            
            actualValue.append(c);
        }
        return actualValue.toString();
    }
}
