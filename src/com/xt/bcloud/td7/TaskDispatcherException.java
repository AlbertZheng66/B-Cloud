
package com.xt.bcloud.td7;

import com.xt.core.exception.SystemException;

/**
 *
 * @author Albert
 */
public class TaskDispatcherException extends SystemException {
    private static final long serialVersionUID = -4218250518812320479L;

    public TaskDispatcherException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskDispatcherException(String message) {
        super(message);
    }
    
}
