
package com.xt.bcloud.td7;

import java.io.IOException;

/**
 *
 * @author Albert
 */
public interface SyncTransmitter extends Transmitter {
    /**
     * 返回相应消息
     * @param msg
     * @return
     * @throws IOException 
     */
    public Response send(Request msg)  throws IOException ;
}
