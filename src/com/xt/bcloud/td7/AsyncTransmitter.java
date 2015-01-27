
package com.xt.bcloud.td7;

import java.io.IOException;

/**
 *
 * @author Albert
 */
public interface AsyncTransmitter extends Transmitter {
    
    
    
    public interface Callable {
        public void execute(Response response);
        
        public void handle(Throwable t);
    }
    
    /**
     * 返回相应消息
     * @param msg
     * @return
     * @throws IOException 
     */
    public void asyncSend(Request msg, Callable callable)  throws IOException;
    
}
