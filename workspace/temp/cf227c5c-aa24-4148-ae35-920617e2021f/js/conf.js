
/**
  * 系统初始化方法，项目可在此处进行自定义的初始化操作。
  */
var __System = {
    initialized : false,  // 是否已经初始化过
	
	// 所有的子包JS文件，主键是：子包名称；值是对应的JS 文件，例如：sub1 : ['file1.js', 'file2.js']
	jsPackages : {},   
	
	// 所有的子包CSS文件，主键是：子包名称；值是对应的CSS 文件，例如：sub1 : ['file1.css', 'file2.css']
	cssPackages : {},   
	
	/**
	 * 在加载核心 JS 代码之后，调用此方法
	 */
    beforeLoading : function() { 
	    if (this.initialized) {
		    return;
		}

		// 项目通用的 JavaScript 文件。
        commonJavaScriptFiles = ['demo/jquery.cookie.js', 'demo/aa.js'];

        // 项目通用的 CSS 文件。
        commonCSSFiles = [];

        //  参数配置区
	    ClassLoader.basePackage = 'com.xt.gt.demo.';
		
		// 初始化各个子包相关的JS文件
		// this.jsPackages['aa']  = ['demo/aa.js'];  // 一般情况下，真实文件应该位于：js/sub1/file.js
    },

    
    /**
	 * 在所有 JS 代码之后，调用此方法
	 */
    afterLoading : function() { 
        if (this.initialized) {
		    return;
		}
        
		/*********  参数配置区   **********/
        // 日历设置为中文
        jQuery.datepicker.setDefaults(jQuery.datepicker.regional['zh-CN']);

        // 1. 注册系统的错误拦截器
	    SystemConfiguration.register('interceptor.onErrors', [relogin]);  // 在aa.js 中定义
        
        // 2. 注册系统的调用拦截器
	    SystemConfiguration.register('interceptor.onInvokes', [preLogin]);  // 在aa.js 中定义

        /*********  参数配置区   **********/
		this.initialized = true;
    }
}
