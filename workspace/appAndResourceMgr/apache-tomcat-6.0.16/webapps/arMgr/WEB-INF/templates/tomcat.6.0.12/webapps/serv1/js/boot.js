/**
  * 用于加载其他代码的启动程序。此程序负责动态加载所有的其他相关的JS 和 CSS。 
  */

 /**
  * 系统参数
  */
 var __System = {

 }


/**
 * 自动计算__contextPath__
 */
var ContextPath = {
    // 上下文路径的匹配模式
    pathPattern : new RegExp('^/?[\\w_\\-][\\w_\\-\\d]*?/'),

    init : function () {
        if (window.__contextPath__) {
            return;
        }
        // TODO: 在端口号为80的时候是否需要特殊处理待验证
        var path = '/';  // 默认字符串
        var pathName = window.location.pathname;  // 在 IE 的弹出窗口情况下，没有前导的“斜线”
        // alert('window.location.pathName=' + pathName);
        var matcher = this.pathPattern.exec(pathName);
        if (matcher) {
            path = matcher[0];
        }
        // 如果路径以非“斜线”开头（IE 可能），则将其补全
        if ((path.charAt(0) || path[0]) != '/') {
            path = '/' + path;
        }
        window.__contextPath__ = path;
    },
    
    /**
     * 返回当前应用的上下文路径
     */
    getPath : function () {
        this.init ();
        return window.__contextPath__;
    },

    /**
     * 手工设置上下文路径
     */
    setPath : function(path) {
        if (path == null) {
            throw new Error("The context path can not be null.");
        }
        window.__contextPath__ = path;
    }
}

// 初始化上下文路径
ContextPath.init();


// 监听遗漏的错误信息
window.onerror = function(sMsg, sUrl, sLine) {
    var plainMsg = sMsg;
	var detailMsg = null;
	var msgs = sMsg.split('||');
	if (msgs.length && msgs.length > 1) {
	    plainMsg  = msgs[0];
		detailMsg = msgs[1];
	}
	//if (MessageBox) {
    //    MessageBox.showError(plainMsg, detailMsg);
	//} else {
	    var _msg = '系统发生异常:\n' + plainMsg;
		if (detailMsg != null) {
		    _msg += '\n 详细信息:' + detailMsg;
		}
		_msg += '\n URL:' + sUrl + '\nlineNumber:' + sLine;
	    alert(_msg);
	//}
	return false;
}

/**
 * 核心的 JavaScript 文件数组。
 * cfg.js 是用户配置文件（系统初始化方法，项目可在此处进行自定义的初始化操作）。
 */
var _coreJavaScriptFiles = ['core/jquery-1.3.2.js', 'core/jquery-ui-1.7.2.custom.min.js',
                            'core/i18n/ui.datepicker-zh-CN.js',
                            'core/json2.js', 'core/gt_base.js', 'core/gt_table.js',/**/
	                        'core/ajaxupload.3.2.js', 'conf.js'];
	

/**
 * 核心的 JavaScript 文件数组。
 */
var _coreCSSFiles = ['ui-lightness/jquery-ui-1.7.2.custom.css', 'gt_demo.css'];
 
// 核心代码是必须加载的代码，是基础代码
var _coreJSPath = (__contextPath__ || '') + 'js/';

// 核心代码是必须加载的代码，是基础代码
var _coreCSSPath = (__contextPath__ || '') + 'css/';

// 启动项目的通用JS文件。一般情况下，在 “conf.js” 中的 beforeLoading 方法里配置。
var commonJavaScriptFiles = [];

var commonCSSFiles = [];

 /**
    * 按照要求加载系统下的所有代码
    */
var Loader = {
    
    /**
	  * 加载指定子包的配置文件
	  */
    importJS : function (subPackages) {
        // 1. 加载核心代码
        for (var i = 0; i < _coreJavaScriptFiles.length; i++) {
            // alert('loading js file=' + _coreJSPath + _coreJavaScriptFiles[i]);
	        this.__loadJS(_coreJSPath + _coreJavaScriptFiles[i]);
        }
		
		// 2. 启动初始化配置
		if (__System != null && __System.beforeLoading != null) {
		    __System.beforeLoading();
		}
		
		// 3. 启动项目的通用JS文件。一般情况下，在 “conf.js” 中的 beforeLoading 方法里配置。
		if (commonJavaScriptFiles !== null) {
		    for (i = 0; i < commonJavaScriptFiles.length; i++) {
			    this.__loadJS(_coreJSPath + commonJavaScriptFiles[i]);
			}
		}

	    // 4. 加载子包的代码
	    if (subPackages != null) {
            if (typeof subPackages === 'string') {
                // 子包只有一个名称
                this.__loadSubPackage(subPackages);
            } else {
                // 子包是多个（数组）
                for (i = 0; i < subPackages.length; i++) {
                    this.__loadSubPackage(subPackages[i]);
                }
            }
	    }
		
		// 5. 启动初始化配置
		if (__System !== null  && __System.afterLoading) {
		    __System.afterLoading();
		}
    },

    /**
     * 导入子包的脚本文件
     */
    __loadSubPackage : function (subPackage) {
        if (__System === null || __System.jsPackages === null
                || __System.jsPackages[subPackage] === null) {
            throw new Error('The name of subPackage[' + subPackage + '] can not be found in jsPackages.');
        }
        for (i = 0; i < __System.jsPackages[subPackage].length; i++) {
            var packageName = __System.jsPackages[subPackage][i];
            this.__loadJS(_coreJSPath + packageName);
        }
    },

    importCSS  : function (subPackages) {
        // 1. 加载核心代码
        for (var i = 0; i < _coreCSSFiles.length; i++) {
	        this.__loadCSS(_coreCSSPath + _coreCSSFiles[i]);
        }
		
		// 2. 启动项目通用的 CSS 文件。一般情况下，在 “conf.js” 中的 beforeLoading 方法里配置。
		if (commonCSSFiles !== null) {
		    for (i = 0; i < commonCSSFiles.length; i++) {
			    this.__loadCSS(_coreCSSPath + commonCSSFiles[i]);
			}
		}		

	    if (subPackages == null) {
	        return;
	    }

	    // 3. 加载各个子模块的 CSS 文件
	    if (subPackages == 'sub1'
    	    || jQuery.inArray("sub1", subPackages) >= 0) {
	        // 加载子模块1 的相关文件
	    }
		
    },
	
	__loadJS : function(jsFileName) {
        // alert('loding 正在导入=' + jsFileName);
        /*var scriptEle = document.createElement("script");
    	scriptEle.src = jsFileName;
	    document.getElementsByTagName("head")[0].appendChild(scriptEle);
    	return scriptEle;*/
        $import(jsFileName);
    },

    __loadCSS : function(cssFileName) {
        // alert('loding cssFileName=' + cssFileName);
        var fileRef = document.createElement("link");
        fileRef.setAttribute("rel", "stylesheet");
        fileRef.setAttribute("type", "text/css");
        fileRef.setAttribute("href", cssFileName);
        document.getElementsByTagName("head")[0].appendChild(fileRef);
    }
}

/***
 * 来自javaEye的加载方法
 */
var $_JS_LOADED = {};   //已载入的JavaScript文件

//动态加载JavaScript文件  
function $import(src){  
    if(!$_JS_LOADED[src]){  
        try{  
            var xhr = window.ActiveXObject ? new ActiveXObject("Microsoft.XMLHTTP") : new XMLHttpRequest();  
            xhr.open("GET", src, false);  
            xhr.send(null);  
      
           if(200 == xhr.status || 0 == xhr.status){  
               if (window.execScript)  
                   window.execScript(xhr.responseText);  
               else window.eval.call(window, xhr.responseText);  
           }  
           $_JS_LOADED[src] = true;  
        } catch(e) {
           alert('系统错误: 加载文件"' + src + '"出错! \n[在第 ' + e.lineNumber + ' 行出现 ' + e.message + ' 错误]');  
        }  
    }  
}

/***
 * 来自javaEye的加载方法
 */
    JSLoader=function (){
        var cbArr = new Array();
        function execCBs(){
            for(var i=0;i<cbArr.length;i++){
                if(cbArr[i].cb){
                    try{
                        if(typeof(cbArr[i].cb) == "function"){
                            if(typeof(cbArr[i].scope)!="undefined")
                                cbArr[i].cb.call(cbArr[i].scope)
                           else
                               cbArr[i].cb();
                       }
                   }
                   catch(e){alert('script error!')};
                   cbArr.splice(i--,1);
               }
           }
       };
       return {
           //加载后的回调
           onReady : function(callBack,d){
               cbArr.push({cb : callBack,scope : d});
           },
           //回调方法的运行
           execFcs : function(){
               execCBs();
           },
           Ajax : {
               //AJAX实现
           }
       };
   }();

   JSLoader.jsCache = {currentLevel:1,levelCounter:1,ready:false};
   JSLoader.require=function(fileName){

       var inStack = false;
       if(JSLoader.jsCache[fileName]){
           return;
       }else{
           var js = new Js(fileName);
           JSLoader.jsCache[fileName] = js;
           JSLoader.jsCache.ready = false;
           var currentLevel = JSLoader.jsCache.levelCounter++;
           JSLoader.Ajax.request('GET',fileName,{
                   success:function(response){
                           JSLoader.jsCache[fileName].loaded = true;
                           JSLoader.jsCache[fileName].content =  response.responseText;
                           JSLoader.jsCache[currentLevel] = JSLoader.jsCache[fileName]
                           executeScript(currentLevel);
                       },
                   scope:this
               }

           );
       }

       function executeScript(executeLevel){
           if(executeLevel == JSLoader.jsCache.currentLevel
               && JSLoader.jsCache[executeLevel].loaded){

               executeJs(JSLoader.jsCache[executeLevel])

               if(executeLevel == (JSLoader.jsCache.levelCounter - 1))
                   JSLoader.execFcs();

               if(++JSLoader.jsCache.currentLevel < JSLoader.jsCache.levelCounter
                       && JSLoader.jsCache[JSLoader.jsCache.currentLevel]
                       && JSLoader.jsCache[JSLoader.jsCache.currentLevel].loaded){
                   executeScript(JSLoader.jsCache.currentLevel);
               }
           }
       };

       function executeJs(jsObj){
           if(window.executeScript){
               window.executeScript(jsObj.content);
           }else if(window.execScript){
               window.execScript(jsObj.content)
           }else{
               window.eval(jsObj.content);
           }
           jsObj.content = null;
           jsObj.excuted = true;
           jsObj.content = false;
       }

       function Js(fn){
           this.fileName=fn;
           this.loaded=false;
           this.content=null;
           this.excuted=false;
       }
   }

