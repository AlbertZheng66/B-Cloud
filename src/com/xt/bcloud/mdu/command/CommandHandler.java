package com.xt.bcloud.mdu.command;

import com.xt.core.log.LogWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

/**
 *
 * @author Albert
 */
public class CommandHandler extends IoHandlerAdapter {
    
   public final int STACK_TRACE_MAX_LENGTH = 4 * 1024;

    private final Logger logger = Logger.getLogger(CommandHandler.class);
    
    public CommandHandler() {
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        super.sessionCreated(session);
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        super.sessionOpened(session);
        LogWriter.info2(logger, "sessionOpened=%s", session);
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        LogWriter.warn2(logger, cause, "服务器捕获到异常。");
        // TODO: 是否写给客户端
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        LogWriter.info2(logger, "messageReceived=%s", message);
        String commandStr = String.valueOf(message).trim();
        try {
            Command command = CommandFactory.getInstance().parse(commandStr);
            Serializable result = command.execute();
            OkCommand okCommand = new OkCommand();
            okCommand.setParam(result);
            session.write(CommandFactory.getInstance().build(okCommand));
        } catch (Throwable t) {
            writeException(t, commandStr, session);
        }
    }

    private void writeException(Throwable t, String commandStr, IoSession session) {
        LogWriter.warn2(logger, t, "执行命令[%s]时出错。", commandStr);
        // 写入异常
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        String stackTrace = sw.toString();
        // 截取超出字符
        if (stackTrace.length() > STACK_TRACE_MAX_LENGTH) {
            stackTrace = stackTrace.substring(0, STACK_TRACE_MAX_LENGTH);
        }
        Command command = new ExceptionCommand();
        command.setParam(stackTrace);
        session.write(CommandFactory.getInstance().build(command));
    }
}
