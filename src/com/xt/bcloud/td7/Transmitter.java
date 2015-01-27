
package com.xt.bcloud.td7;

/**
 * 与应用服务器进行通信，转发HTTP请求。
 * @author Albert
 */
public interface Transmitter {
    
    public boolean open();
    
    public void end();
    
}
