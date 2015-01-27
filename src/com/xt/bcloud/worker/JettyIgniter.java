package com.xt.bcloud.worker;

import com.xt.core.exception.SystemException;
import com.xt.core.log.LogWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
//import org.eclipse.jetty.server.Connector;
//import org.eclipse.jetty.server.Server;
//import org.eclipse.jetty.server.nio.SelectChannelConnector;
//import org.eclipse.jetty.webapp.WebAppContext;

/**
 *  使用独立进程的方式启动Jetty.
 * @author albert
 */
public class JettyIgniter {

    private static final Logger logger = Logger.getLogger(JettyIgniter.class);

    /**
     * @param args the command line arguments
     * e.g. -a appId -d "E:\work\xthinker\B-Cloud\web"  -c "/gt_demo" -p 8899
     */
    public static void main(String[] args) {

        String ip = "unknown";
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex1) {
            LogWriter.warn(logger, "获取本地地址失败。", ex1);
        }

        Param param = parse(args);
        LogWriter.info(logger, String.format("启动服务器(ip:%s; appId:%s, port:%d, contextPath:%s, resourceBase:%s)......", ip,
                param.appId, param.port, param.contextPath, param.resourceBase));

//        Server server = new Server();
//        Connector connector = new SelectChannelConnector();
//
//        connector.setPort(param.port);
//        server.setConnectors(new Connector[]{connector});
//
//        WebAppContext webapp = new WebAppContext();
//        webapp.setContextPath(param.contextPath);
//        webapp.setResourceBase(param.resourceBase);
//        server.setHandler(webapp);
//        try {
//            server.start();
//            server.join();
//        } catch (Exception ex) {
//            LogWriter.warn(logger, String.format("服务器(ip:%s; appId:%s, port:%d, contextPath:%s, resourceBase:%s)启动失败。", ip,
//                    param.appId, param.port, param.contextPath, param.resourceBase), ex);
//        }
    }

    /**
     * 解析系统参数
     * @param args
     * @return
     */
    static private Param parse(String[] args) {

        Param param = new Param();

        LogWriter.info(logger, "正在解析参数", StringUtils.join(args, ","));

        Options options = new Options();

        // 系统启动时将用到这个参数
        Option appIdOpt = new Option("a", "appid", true, "应用 ID");
        Option deployOpt = new Option("d", "deploy", true, "发布路径");
        Option contextOpt = new Option("c", "context", true, "上下文路径");
        Option portOpt = new Option("p", "port", true, "使用端口");
        options.addOption(appIdOpt).addOption(deployOpt).addOption(contextOpt).addOption(portOpt);
        BasicParser bp = new BasicParser();
        String portString = null;
        try {
            CommandLine commandLine = bp.parse(options, args);

            String appId = commandLine.getOptionValue(appIdOpt.getOpt());
            LogWriter.info(logger, "appId", appId);
            param.appId = appId;

            String deployPath = commandLine.getOptionValue(deployOpt.getOpt());
            LogWriter.info(logger, "deployPath", deployPath);
            param.resourceBase = deployPath;

            String contextPath = commandLine.getOptionValue(contextOpt.getOpt());
            LogWriter.info(logger, "contextPath", contextPath);
            param.contextPath = contextPath;

            portString = commandLine.getOptionValue(portOpt.getOpt());
            LogWriter.info(logger, "portString", portString);
            if (StringUtils.isNotEmpty(portString)) {
                param.port = Integer.parseInt(portString);
            }
            if (param.port < 1024 || param.port > 65535) {
                 throw new SystemException(String.format("端口[%s]非法。", portString));
            }
        } catch (ParseException ex) {
            throw new SystemException(String.format("解析命令行参数[%s]异常。", StringUtils.join(args, ",")), ex);
        } catch (NumberFormatException ex) {
            throw new SystemException(String.format("端口[%s]非数字异常。", portString), ex);
        }
        return param;
    }
}

/**
 * 启动参数
 * @author albert
 */
class Param {

    String appId = "";
    String resourceBase = "E:/work/xthinker/B-Cloud/web/"; //web工程目录
    int port = -1;
    String contextPath = "gt_demo";
}
