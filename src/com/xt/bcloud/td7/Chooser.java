
package com.xt.bcloud.td7;

import com.xt.bcloud.worker.Cattle;
import java.net.Socket;

/**
 *
 * @author Albert
 */
public interface Chooser {
    
    /**
     * ѡ��ԭʼ
     * @param header 
     */
    public Cattle select(Request request);
    
}
