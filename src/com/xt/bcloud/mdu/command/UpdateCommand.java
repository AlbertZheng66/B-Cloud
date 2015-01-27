
package com.xt.bcloud.mdu.command;

import com.xt.core.log.LogWriter;
import java.io.Serializable;
import org.apache.log4j.Logger;

/**
 *
 * @author Albert
 */
public class UpdateCommand extends Command{
    
    public final static String UPDATE = "update";
    
    public UpdateCommand() {
        this.name = UPDATE;
    }

    @Override
    public Serializable execute() {
        LogWriter.info2(logger, "start updating server[%s]", getParam());
        return true;
    }
    
    /**
     * ����һ��Ӧ�÷�������
     */
    public void update(boolean relocatable) {
        // ����ʱĿ¼�����µ�Ӧ�÷�����
        // ֹͣ��ǰ������
    }    
}
