
package com.xt.bcloud.app;

import com.xt.core.exception.SystemException;

/**
 * ��Ӧ�ò�����ص��쳣.
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
