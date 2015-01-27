

package com.xt.bcloud.td.http;

import java.io.Serializable;

/**
 * 用于表示一个 HTTP 错误相应。
 * @author albert
 */
public class HttpError implements Serializable, Cloneable {
    
    private static final long serialVersionUID = 2654974249358945488L;

    /**
     * 错误编码
     */
    private String errorCode;

    /**
     * 错误消息
     */
    private String errorMessage;

    /**
     * 本地消息
     */
    private String localMessage;

    public HttpError() {
    }

    public HttpError(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HttpError other = (HttpError) obj;
        if ((this.errorCode == null) ? (other.errorCode != null) : !this.errorCode.equals(other.errorCode)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.errorCode != null ? this.errorCode.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder strBld = new StringBuilder();
        strBld.append(super.toString()).append("[");
        strBld.append("errorCode=").append(errorCode).append("; ");
        strBld.append("errorMessage=").append(errorMessage).append("; ");
        strBld.append("localMessage=").append(localMessage);
        strBld.append("]");
        return strBld.toString();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }


    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getLocalMessage() {
        return localMessage;
    }

    public void setLocalMessage(String localMessage) {
        this.localMessage = localMessage;
    }



}
