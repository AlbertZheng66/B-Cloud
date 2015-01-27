
package com.xt.bcloud.mdu.command;

import com.xt.bcloud.comm.Constants;
import com.xt.bcloud.mdu.MduException;
import com.xt.bcloud.mdu.PhyServer;
import com.xt.core.log.LogWriter;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import org.apache.log4j.Logger;
import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

/**
 *
 * @author Albert
 */
public class CommandClient {

    private final Logger logger = Logger.getLogger(CommandClient.class);

    public CommandClient() {
    }

    public Object execute(PhyServer phyServer, final Command command) {
        LogWriter.info2(logger, "Start to connect to the server[%s:%d]",
                phyServer.getIp(), phyServer.getManagerPort());
        NioSocketConnector connector = new NioSocketConnector();

        // Configure the service.
        connector.setConnectTimeoutMillis(Constants.DEPLOYING_SERVICE_CONNECT_TIMEOUT);

        connector.getFilterChain().addLast("codec",
                new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));

        //connector.getFilterChain().addLast("logger", new LoggingFilter());
        CommandAdapter commandAdapter = new CommandAdapter(command);
        connector.setHandler(commandAdapter);

        IoSession session = null;
        for (int i = 0; i < 5; i++) {
            try {
                ConnectFuture future = connector.connect(new InetSocketAddress(phyServer.getIp(),
                        phyServer.getManagerPort()));
                future.awaitUninterruptibly();
                session = future.getSession();
                break;
            } catch (RuntimeIoException e) {
                LogWriter.warn2(logger, e, "Failed to connect to the server[%s:%d]",
                        phyServer.getIp(), phyServer.getManagerPort());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    // do nothing...
                }
            }
        }
        if (session == null) {
            throw new MduException(String.format("Failed to connect to the server[%s:%d]",
                phyServer.getIp(), phyServer.getManagerPort()));
        }

        // wait until the summation is done
        session.getCloseFuture().awaitUninterruptibly();
        connector.dispose();
        if (commandAdapter.result instanceof Throwable) {
            MduException me = new MduException(String.format("执行命令[%s]时出现错误", command),
                    (Throwable)commandAdapter.result);
            throw me;
        }
        return commandAdapter.result;
    }

    class CommandAdapter extends IoHandlerAdapter {

        private final Command command;
        private Object result;  // 命令运行的结果

        public CommandAdapter(Command command) {
            this.command = command;
        }
        
        @Override
        public void sessionOpened(IoSession is) throws Exception {
            LogWriter.info2(logger, "Opens a session [%s]", is);
            // send the command to the server
            String commandStr = CommandFactory.getInstance().build(command);
            is.write(commandStr);
        }

        @Override
        public void sessionClosed(IoSession is) throws Exception {
            super.sessionClosed(is);
            LogWriter.info2(logger, "The session [%s] is closed...", is);
        }

        @Override
        public void exceptionCaught(IoSession is, Throwable thrwbl) throws Exception {
            LogWriter.info2(logger, "The session [%s] is closed...", is);
            super.exceptionCaught(is, thrwbl);
            // 处理异常的情况 
            is.close(true);
            result = thrwbl;
        }

        @Override
        public void messageReceived(IoSession is, Object o) throws Exception {
            LogWriter.info2(logger, "The message [%s] has been received.", o);
            String message = (String) o;
            try {
                System.out.println("messageReceived=" + message);
                Command resultCmd = CommandFactory.getInstance().parse(message);
                if (resultCmd instanceof ExceptionCommand) {
                    throw new MduException((String) resultCmd.getParam());
                } else if (resultCmd instanceof OkCommand) {
                    result = resultCmd.getParam();
                    LogWriter.info2(logger, "The command [%s] has been executed successfully.", resultCmd);
                } else {
                    throw new MduException(String.format("不能处理的命令[%s]", resultCmd));
                }
            } finally {
                is.close(true);
            }
        }
        
    }
}
