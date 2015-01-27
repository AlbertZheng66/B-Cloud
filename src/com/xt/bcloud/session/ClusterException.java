

package com.xt.bcloud.session;

import com.xt.core.exception.SystemException;

/**
 * 封装集群出现问题时产生的异常。
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
