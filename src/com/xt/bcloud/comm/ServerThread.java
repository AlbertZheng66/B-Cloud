package com.xt.bcloud.comm;

//import org.eclipse.jetty.server.Connector;
//import org.eclipse.jetty.server.Server;
//import org.eclipse.jetty.server.nio.SelectChannelConnector;
//import org.eclipse.jetty.webapp.WebAppContext;

/**
 * 服务器线程
 * @author albert
 */
public class ServerThread implements Runnable {

//    private Server server;

    private final String resourceBase;  // = "E:/work/xthinker/B-Cloud/web/"; //web工程目录

    private final int port;

    private final String contextPath;

    public ServerThread(int port, String contextPath, String resourceBase) {
        this.port         = port;
        this.contextPath  = contextPath;
        this.resourceBase = resourceBase;
    }

    public void run() {
        try {
//            server = new Server();
//            Connector connector = new SelectChannelConnector();
//
//            connector.setPort(port);
//            server.setConnectors(new Connector[]{connector});
//
//            WebAppContext webapp = new WebAppContext();
//            // String webContext = "/gt_demo"; //上下文路径
//            webapp.setContextPath(contextPath);
//            webapp.setResourceBase(resourceBase);
//            server.setHandler(webapp);
//            server.start();
//            server.join();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
