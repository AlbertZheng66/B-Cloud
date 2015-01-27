package com.xt.bcloud.session;

import com.xt.core.proc.Processor;
import com.xt.core.proc.ProcessorFactory;

/**
 *
 * @author albert
 */
public class ClusterSessionProcessorFactory implements ProcessorFactory {

    public void onInit() {
    }

    public synchronized Processor createProcessor(Class serviceClass) {
        Processor processor = new ClusterSessionProcessor();
        return processor;
    }

    public void onDestroy() {
    }
}

