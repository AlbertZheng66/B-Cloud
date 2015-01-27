
package com.xt.bcloud.td7;

/**
 *
 * @author Albert
 */
public interface Connector extends Runnable{
    
    /**
     * 返回使用的端口号
     * @param port 
     */
    public int getPort();
    
    public void setPort(int port);
    
    public void init();
    
    public void stop();
    
}
