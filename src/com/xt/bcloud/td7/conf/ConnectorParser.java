package com.xt.bcloud.td7.conf;

import com.xt.bcloud.td7.Connector;
import com.xt.bcloud.td7.TaskDispatcher7;
import com.xt.core.utils.ClassHelper;
import com.xt.gt.sys.ParameterParser;
import com.xt.gt.sys.SystemConfigurationException;
import com.xt.gt.sys.SystemParameter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 * This class is used to load and create the information of connectors from the conf files.
 * @author Albert
 */
public class ConnectorParser implements ParameterParser {

    public static final String TAG_DATABASES = "connectors";
    public static final String TAG_DATABASE = "connector";
    public static final String ATTR_CLASS_NAME = "className";
    public static final String ATTR_PORT = "port";

    public String getParameterName() {
        return TaskDispatcher7.PARAM_TD_CONNECTORS;
    }

    public Object parse(SystemParameter systemParameter) {
        List<Connector> connectors = new ArrayList();
        List<SystemParameter> paramsElem = systemParameter.getChildren();
        for (SystemParameter param : paramsElem) {
            Connector conn = loadConnector(param);
            if (conn != null) {
                connectors.add(conn);
            }
        }
        return connectors.toArray(new Connector[connectors.size()]);
    }

    private Connector loadConnector(SystemParameter param) {
        String className = param.getAttributeValue(ATTR_CLASS_NAME);
        if (StringUtils.isEmpty(className)) {
            throw new SystemConfigurationException("连接器的类名称不能为空。");
        }
        String port = param.getAttributeValue(ATTR_PORT);
        if (StringUtils.isEmpty(port)) {
            throw new SystemConfigurationException("连接器的端口不能为空。");
        }
        int portInt = Integer.parseInt(port);
        Connector conn = (Connector)ClassHelper.newInstance(className);
        conn.setPort(portInt);
        return conn;
    }
}
