
package com.xt.bcloud.td7;

import java.io.IOException;

/**
 *
 * @author Albert
 */
public interface SyncTransmitter extends Transmitter {
    /**
     * ������Ӧ��Ϣ
     * @param msg
     * @return
     * @throws IOException 
     */
    public Response send(Request msg)  throws IOException ;
}
