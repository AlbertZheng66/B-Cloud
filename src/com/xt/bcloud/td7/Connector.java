
package com.xt.bcloud.td7;

/**
 *
 * @author Albert
 */
public interface Connector extends Runnable{
    
    /**
     * ����ʹ�õĶ˿ں�
     * @param port 
     */
    public int getPort();
    
    public void setPort(int port);
    
    public void init();
    
    public void stop();
    
}
