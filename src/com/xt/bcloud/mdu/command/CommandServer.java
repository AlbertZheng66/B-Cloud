
package com.xt.bcloud.mdu.command;

import com.xt.bcloud.mdu.MduException;
import com.xt.core.log.LogWriter;
import com.xt.gt.sys.SystemConfiguration;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

/**
 *
 * @author Albert
 */
public class CommandServer {
    
    private final Logger logger = Logger.getLogger(CommandServer.class);
    
    private final int maxLineLength = SystemConfiguration.getInstance().readInt("commandServer.maxLineLength", 512*1024);
    
    private final int managerPort;
    
    private IoAcceptor acceptor = null;

    public CommandServer(int managerPort) {
        this.managerPort = managerPort;
    }

    public void startServer() {
        LogWriter.info2(logger, "start server by binding port[%d].", managerPort);
        acceptor = new NioSocketAcceptor();

        // acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        TextLineCodecFactory tlcFactory = new TextLineCodecFactory(Charset.forName("UTF-8"));
        tlcFactory.setDecoderMaxLineLength(maxLineLength);
        tlcFactory.setEncoderMaxLineLength(maxLineLength);
        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(tlcFactory));

        acceptor.setHandler(new CommandHandler());
        acceptor.getSessionConfig().setReadBufferSize(2048);
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
        try {
            LogWriter.info2(logger, "���������ʹ�ö˿�[%d]��������", managerPort);
            acceptor.bind(new InetSocketAddress(managerPort));
        } catch (IOException ex) {
            throw new MduException(String.format("�󶨶˿�[%d]�ǳ����쳣��", managerPort), ex);
        }
    }
    
    public void stop() {
        if (acceptor != null) {
            acceptor.dispose(true);
            acceptor = null;
        }
    }
    
}
