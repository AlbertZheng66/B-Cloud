
package com.xt.bcloud.pf.connector;

import com.xt.core.exception.SystemException;

/**
 *
 * @author Albert
 */
public class RmiException extends SystemException {

    public RmiException(String message, Throwable cause) {
        super(message, cause);
    }

    public RmiException(String message) {
        super(message);
    }
    
}
