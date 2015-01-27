
package com.xt.bcloud.mdu;

import com.xt.core.exception.SystemException;

/**
 *
 * @author Albert
 */
public class MduException extends SystemException {
    private static final long serialVersionUID = -6362598819172750220L;

    public MduException(String message, Throwable cause) {
        super(message, cause);
    }

    public MduException(String message) {
        super(message);
    }
    
    
}
