

package com.xt.bcloud.session;

import com.xt.core.exception.SystemException;

/**
 * ��װ��Ⱥ��������ʱ�������쳣��
 * @author albert
 */
public class ClusterException extends SystemException {
    
    private static final long serialVersionUID = 4174247854066740797L;

    public ClusterException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClusterException(String message) {
        super(message);
    }

}
