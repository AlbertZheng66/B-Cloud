

package com.xt.bcloud.td;

import com.xt.core.exception.SystemException;

/**
 * 任务转发时都使用此异常.
 * @author albert
 */
public class DispatcherException extends SystemException {

    public DispatcherException(String message, Throwable cause) {
        super(message, cause);
    }

    public DispatcherException(String message) {
        super(message);
    }
}
