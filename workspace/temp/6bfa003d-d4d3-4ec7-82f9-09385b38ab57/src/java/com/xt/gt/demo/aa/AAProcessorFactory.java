
package com.xt.gt.demo.aa;

import com.xt.core.proc.Processor;
import com.xt.core.proc.ProcessorFactory;
import com.xt.core.utils.dic.RemoteDictionaryService;
import com.xt.gt.html.service.TableService;
import com.xt.gt.html.service.ClassService;
import com.xt.gt.html.service.POValidatorLoaderService;
import com.xt.gt.html.service.SystemConfigurationService;
import com.xt.gt.html.service.ValidatorLoaderService;

/**
 *
 * @author albert
 */
public class AAProcessorFactory implements ProcessorFactory {

	public void onInit() {
	}

	public synchronized Processor createProcessor(Class serviceClass) {
        // 不需要进行校验的权限（登录等）
        if (serviceClass.getAnnotation(Public.class) == null
                && !serviceClass.equals(ClassService.class)
                && !serviceClass.equals(TableService.class)
                && !serviceClass.equals(ValidatorLoaderService.class)
                && !serviceClass.equals(POValidatorLoaderService.class)
                && !serviceClass.equals(SystemConfigurationService.class)
                && !serviceClass.equals(RemoteDictionaryService.class)) {
            return new AAProcessor();
        }
		return null;
	}

	public void onDistroy() {
	}

}
