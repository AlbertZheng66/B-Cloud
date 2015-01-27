
package com.xt.bcloud.mdu.command;

import com.xt.core.exception.SystemException;

/**
 *
 * @author Albert
 */
public class BadCommandException extends SystemException {
    private static final long serialVersionUID = 2122926150348747219L;

    public BadCommandException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadCommandException(String message) {
        super(message);
    }
    
}
