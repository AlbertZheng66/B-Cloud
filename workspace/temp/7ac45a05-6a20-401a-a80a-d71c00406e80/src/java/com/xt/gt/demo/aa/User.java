
package com.xt.gt.demo.aa;

/**
 *
 * @author albert
 */
public class User {
    public static final String USER_IN_SESSION = "USER_IN_SESSION";

    private String userName;

    private String passwd;

    public User() {
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String userCode) {
        this.passwd = userCode;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
