
package com.xt.bcloud.resource;

import com.xt.core.exception.SystemException;

/**
 * 和资源相关的异常信息
 * @author albert
 */
public class ResourceException extends SystemException{

    public ResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceException(String message) {
        super(message);
    }
}
