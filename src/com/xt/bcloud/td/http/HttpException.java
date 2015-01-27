package com.xt.bcloud.td.http;

import com.xt.core.exception.SystemException;

/**
 *
 * @author albert
 */
public class HttpException extends SystemException {

    private static final long serialVersionUID = -9072010578556725537L;

    /**
     * 对应的错误代码
     */
    private final HttpError error;

     public HttpException(HttpError error) {
        super(error == null ? "unknown" : error.getErrorCode());
        this.error = error;
    }
     
     public HttpException(HttpError error, Throwable cause) {
        super(error.getErrorMessage(), cause);
        this.error = error;
        
    }


    public HttpException(String message, HttpError error) {
        super(message);
        this.error = error;
    }

    public HttpError getError() {
        return error;
    }
}
