

package com.xt.bcloud.td;

import com.xt.core.exception.SystemException;

/**
 * ����ת��ʱ��ʹ�ô��쳣.
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
