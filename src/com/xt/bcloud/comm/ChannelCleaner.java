
package com.xt.bcloud.comm;

import com.xt.comm.Cleanable;
import com.xt.core.log.LogWriter;
import org.apache.log4j.Logger;
import org.jgroups.Channel;

/**
 *
 * @author albert
 */
public class ChannelCleaner implements Cleanable {
    private final Logger logger = Logger.getLogger(ChannelCleaner.class);

    private Channel channel;

    public ChannelCleaner(Channel channel) {
        this.channel = channel;
    }

    public void clean() {
        if (channel != null) {
            LogWriter.info2(logger, "正在关闭通道[%s]...", channel);
            channel.close();
            channel = null;
        }
    }



}
