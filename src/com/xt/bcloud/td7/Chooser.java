
package com.xt.bcloud.td7;

import com.xt.bcloud.worker.Cattle;
import java.net.Socket;

/**
 *
 * @author Albert
 */
public interface Chooser {
    
    /**
     * —°‘Ò‘≠ º
     * @param header 
     */
    public Cattle select(Request request);
    
}
