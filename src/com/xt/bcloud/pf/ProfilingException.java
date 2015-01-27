
package com.xt.bcloud.pf;

import com.xt.core.exception.SystemException;

/**
 *
 * @author Albert
 */
public class ProfilingException extends SystemException {
    
    private static final long serialVersionUID = -5186310550644373210L;

    public ProfilingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProfilingException(String message) {
        super(message);
    }
    
    
    
}
