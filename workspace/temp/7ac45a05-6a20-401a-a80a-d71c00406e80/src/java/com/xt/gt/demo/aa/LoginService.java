
package com.xt.gt.demo.aa;

import com.xt.core.proc.impl.SessionAware;
import com.xt.core.service.AbstractService;
import com.xt.core.service.LocalMethod;
import com.xt.core.session.Session;

/**
 *
 * @author albert
 */
@Public
public class LoginService  extends AbstractService implements SessionAware {

    transient private Session session;

    public LoginService() {
    }

    public boolean login(User user) {
        if (user == null) {
            return false;
        }
        
        // 用户名和密码相等即可
        if (user.getUserName() != null && user.getUserName().equals(user.getPasswd())) {
            //TODO: 在此授权
            session.setAttribute(User.USER_IN_SESSION, user);
            return true;
        } else {
            return false;
        }
    }

    @LocalMethod()
    public void setSession(Session session) {
        this.session = session;
    }

    @LocalMethod
	public Session getSession() {
        return session;
    }
}
