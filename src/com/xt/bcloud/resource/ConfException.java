

package com.xt.bcloud.resource;

import com.xt.core.exception.SystemException;

/**
 * ����Դ���ó��ִ���ʱ, ϵͳ���׳����쳣.
 * @author albert
 */
public class ConfException extends SystemException {
    private static final long serialVersionUID = 2643905858992806565L;

    public ConfException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfException(String message) {
        super(message);
    }

}
