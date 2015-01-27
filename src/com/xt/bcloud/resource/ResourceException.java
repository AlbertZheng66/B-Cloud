
package com.xt.bcloud.resource;

import com.xt.core.exception.SystemException;

/**
 * ����Դ��ص��쳣��Ϣ
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
