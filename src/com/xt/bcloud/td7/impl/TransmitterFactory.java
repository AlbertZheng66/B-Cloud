
package com.xt.bcloud.td7.impl;

import com.xt.bcloud.td7.AsyncTransmitter;
import com.xt.bcloud.td7.SyncTransmitter;

/**
 *
 * @author Albert
 */
public class TransmitterFactory {
    
    private static TransmitterFactory instance = new TransmitterFactory();
    
    private TransmitterFactory() {
        
    }
    
    public static TransmitterFactory getInstance() {
        return instance;
    }
    
    /**
     * 可以服用这些IP。
     * @param ip
     * @param port
     * @return 
     */
    public SyncTransmitter getTransmitter(String ip, int port) {
        SyncTransmitter transmitter = new DefaultSyncTransmitter(ip, port);
        transmitter.open();
        return transmitter;
    }
            
     public AsyncTransmitter getAsyncTransmitter(String ip, int port) {
        AsyncTransmitter transmitter = new DefaultAsyncTransmitter(ip, port);
        transmitter.open();
        return transmitter;
    }
    
    /**
     * 探测服务器是否可用。
     * @param ip
     * @param port
     * @return 
     */
    public boolean isAvailable(String ip, int port) {
        return true;
    }
    
}
