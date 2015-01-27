package com.xt.bcloud.td7.impl;

import com.xt.bcloud.td.http.ErrorFactory;
import com.xt.bcloud.td.http.HttpError;
import com.xt.bcloud.td.http.HttpException;
import com.xt.bcloud.td7.Commnunicator;
import com.xt.bcloud.td7.Request;
import com.xt.bcloud.td7.Chooser;
import com.xt.bcloud.worker.Cattle;
import java.util.Collections;
import org.apache.log4j.Logger;

/**
 *
 * @author Albert
 */
public class DefaultSelector implements Chooser {

    private final Logger logger = Logger.getLogger(DefaultSelector.class);
    
    private final Commnunicator communicator = Commnunicator.getInstance();

    public Cattle select(Request request) {
        
        Cattle cattle = communicator.findCattle(request, Collections.EMPTY_SET);
        if (cattle == null) {
            HttpError error = ErrorFactory.getInstance().create("503");
            error.setLocalMessage(String.format("请求的域名[%s]已无可用实例。", request.getHeader().getHost()));
            throw new HttpException(error);
        }
        return cattle;
    }
}
