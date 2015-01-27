package com.xt.bcloud.mdu.command;

import com.xt.core.json.JsonBuilder;
import com.xt.gt.sys.SystemConfiguration;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Albert
 */
public final class CommandFactory {

        
    
    
    public static final String SEP = "?";
    /**
     * ��������ִ��
     */
    public final static String OK = "200";
    /**
     * ���ز������쳣
     */
    public final static String CLIENT_EXCEPTION = "500";
    private final static CommandFactory instance = new CommandFactory();
    private Map<String, Command> commands = Collections.synchronizedMap(new HashMap());

    private CommandFactory() {
        commands.put(OK, new OkCommand());
        commands.put(CLIENT_EXCEPTION, new ExceptionCommand());
        //  �Զ�������
        final Command[] appCommands = SystemConfiguration.getInstance().readObjects("commands", Command.class);
        for (Command command : appCommands) {
            register(command.getName(), command);
        }
    }

    static public CommandFactory getInstance() {
        return instance;
    }
    
    public void register(String name, Command command) {
        if (StringUtils.isNotEmpty(name) && command != null) {
            commands.put(name, command);
        }
    }
 
   public Command getCommand(String name) {
        if (StringUtils.isEmpty(name) || !commands.containsKey(name)) {
            throw new BadCommandException(String.format("����ʶ������[%s]", name));
        }
        return commands.get(name);
    }

    public String build(Command command) {
        if (command == null || StringUtils.isEmpty(command.getName())) {
            throw new BadCommandException("��������ƶ�����Ϊ�ա�");
        }
        StringBuilder strBld = new StringBuilder();
        strBld.append(command.getName());
        if (command.getParam() != null) {
            strBld.append(SEP);
            JsonBuilder jsonBuilder = new JsonBuilder();
            strBld.append(jsonBuilder.build(command.getParam()));
        }
        return strBld.toString();
    }

    public Command parse(String commandStr) {
        if (StringUtils.isEmpty(commandStr)) {
            throw new BadCommandException("����Ϊ�ա�");
        }
        String name = commandStr;
        int index = commandStr.indexOf(SEP);
        Serializable param = null;
        if (index >= 0) {
            name = commandStr.substring(0, index);
            String paramsStr = commandStr.substring(index + 1);
            // FIXME:
            param = (Serializable) parseParams(paramsStr);
        }

        Command command = getCommand(name);
        command.setParam(param);
        return command;
    }

    private static Object parseParams(String paramsStr) {
        if (StringUtils.isEmpty(paramsStr)) {
            return null;
        }
        JsonBuilder jsonBuilder = new JsonBuilder();
        return jsonBuilder.parse(paramsStr);
    }
}
