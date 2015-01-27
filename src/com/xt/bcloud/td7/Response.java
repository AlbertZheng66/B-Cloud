/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xt.bcloud.td7;

/**
 *
 * @author Albert
 */
public class Response extends Message {
    
     /**
     * ×´Ì¬ÐÐ£¨Status Line£©
     */
    private String statusLine;

    public Response() {
    }

    public String getStatusLine() {
        return statusLine;
    }

    public void setStatusLine(String statusLine) {
        this.statusLine = statusLine;
    }

    public String getCookieValue(String name) {
        return null;
    }
}
