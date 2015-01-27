

package com.xt.gt.demo.aa;

import com.xt.core.log.LogWriter;
import com.xt.core.proc.Processor;
import com.xt.core.session.Session;
import java.lang.reflect.Method;
import org.apache.log4j.Logger;

/**
 *
 * @author albert
 */
public class AAProcessor implements Processor{

    private final Logger logger = Logger.getLogger(AAProcessor.class);

	private Session session;

	public void onCreate(Class serviceClass, Session session) {
		this.session = session;
	}


	public void onBefore(Object service, Method method, Object[] params) {
        // 不校验公共方法
        if (method.getAnnotation(Public.class) != null) {
            return;
        }
        User user = (User)session.getAttribute(User.USER_IN_SESSION);
        LogWriter.debug(logger, "onBefore user", user);
        if (user == null) {
            throw new AAException("用户尚未登录。");
        }
        
        //TODO: 根据用户的权限信息判断用户是否可以访问此功能

	}


	public void onAfter(Object service, Object ret) {

	}

	public void onFinally() {
		session = null;
	}

	public void onThrowable(Throwable t) {

	}

}
