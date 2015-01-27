
package com.xt.bcloud.app;

import com.xt.core.exception.SystemException;

/**
 * 和应用部署相关的异常.
 * @author albert
 */
public class AppException extends SystemException {

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }

    public AppException(String message) {
        super(message);
    }

}
