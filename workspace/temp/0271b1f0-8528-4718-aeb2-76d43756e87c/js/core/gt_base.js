
/**
 * 默认情况下，IE 浏览器不支持indexOf函数，在此给其增加一个。
 *
 */
if(!Array.indexOf){
    Array.prototype.indexOf = function(obj){
        for(var i = 0; i< this.length; i++){
            if(this[i] == obj){
                return i;
            }
        }
        return -1;
    }
}

if (!String.startsWith) {
    String.prototype.startsWith = function(obj){
        if (obj == null || (typeof obj !== 'string')) {
            return false;
        }
        if (obj.length === 0) {
            return true;
        }
        if (obj.length > this.length) {
            return false;
        }
        return (this.substring(0, obj.length) === obj);
    }
}

var Interface = function(name, methods) {
    if(arguments.length != 2) {
        throw new Error("Interface constructor called with " + arguments.length +
            "arguments, but expected exactly 2.");
    }
    this.name = name;
    this.methods = [];
    for(var i = 0, len = methods.length; i < len; i++) {
        if(typeof methods[i] !== 'string') {
            throw new Error("Interface constructor expects method names to be "
                + "passed in as a string.");
        }
        this.methods.push(methods[i]);
    }
};

// Static class method.
Interface.ensureImplements = function(object) {
    if(arguments.length < 2) {
        throw new Error("Function Interface.ensureImplements called with " +
            arguments.length + " arguments, but expected at least 2.");
    }
    for(var i = 1, len = arguments.length; i < len; i++) {
        var _interface = arguments[i];
        if(_interface.constructor !== Interface) {
            throw new Error("Function Interface.ensureImplements expects arguments"
                + "two and above to be instances of Interface.");
        }
        for(var j = 0, methodsLen = _interface.methods.length; j < methodsLen; j++) {
            var method = _interface.methods[j];
            if(!object[method] || typeof object[method] !== 'function') {
                throw new Error("Function Interface.ensureImplements: object "
                    + "does not implement the " + _interface.name
                    + " interface. Method " + method + " was not found.");
            }
        }
    }
};

/**
 * 定义了系统的配置参数, "_sc" 是其简写形式。
 */
var SystemConfiguration = _sc = {
    _params : {
        "format.date" : 'yyyy-MM-dd'  // 用于进行日期格式化的模式
    },

    /**
     * 读取一个系统参数，如果参数为定义，则返回默认的系统参数。
     */
    read : function (name, defaultValue) {
        if (name == null) {
            return null;
        }
        return (this._params[name] || defaultValue);
    },

    /**
    * 注册一个系统参数。
    */
    register : function (name, value) {
        if (name == null) {
            throw new Error("The name of parameter can not be null.");
        }
        this._params[name] = value;
    },

    /**
     * TODO: 从服务器端加载系统参数。
     */
    load : function () {

    }

}


/**
 * 类装载器，通过此方法对类进行装载
 */
var ClassLoader = {

    basePackage : '',  // 如果类名采用缩写，此作为基础类路径

    /**
     * 类装载服务
     */
    classLoaderService : 'com.xt.gt.html.service.ClassService',

    /**
     * 类装载方法
     */
    classLoaderMethod : 'loadClass',


    /**
     * 在客户端缓存的类信息。键值为类的全称，值为其类描述。
     */
    classPool : {},

    /**
     * 通过类名称返回一个类描述对象。
     * @param className 类名称（可能是全称，可能是缩写，默认是缩写）。
     * @param full 指示 className 是否为全称，还是缩写。
     */
    getClass : function (className, full) {
        if (className == null || className == '') {
            // 抛出类未发现异常
            throw new Error("Parameter className can not be empty.");
        }
        var fullName = this.getFullName(className, full);

        var classDesc = this.classPool[fullName];
        if (classDesc == null) {
            classDesc = this.loadClass(fullName);
            this.classPool[fullName] = classDesc;
        }
        return classDesc;
    },

    /**
     * 返回类的指定方法的描述信息。
     * @param className 类名称，不能为空
     * @param methodName 方法名称，不能为空
     * @param full  类名称是否是全称
     *
     */
    getMethod : function (className, methodName, full) {
        var classDesc = this.getClass(className, full);
        if (methodName == null || methodName == '') {
            throw new Error("Parameter methodName(2) can not be null.");
        }
        for (var i = 0; i < classDesc.methods.length; i++) {
            if (methodName === classDesc.methods[i].name) {
                return classDesc.methods[i];
            }
        }
        throw new Error("Class [" + className + "] does not contain method[" + methodName + "] ");
    },
    /**
     * 通过类名称返回一个类全路径名称（应用中可能采用缩写方式）。
     * @param className 类名称（可能是全称，可能是缩写，默认是缩写）。
     * @param full 指示 className 是否为全称，还是缩写。
     */
    getFullName : function (className, full) {
        var fullName = className;
        if (full == null) {
            // 自行判断是否是全称
            if (this.basePackage != null && !className.startsWith(this.basePackage)) {
                fullName = this.basePackage + className;
            }
        } else if(!full) {
            fullName = this.basePackage + className;
        }
        return fullName;
    },

    /**
     * 从服务器装载一个类的描述信息。
     */
    loadClass : function (className) {
        if (className == null || className == '') {
            // 抛出类未发现异常
            throw new Error("Parameter className can not be empty.");
        }
        var req = new Request();
        req.type = 'common';  // 采用通用的调用方式
        req.serviceClassName = this.classLoaderService;
        req.methodName       = this.classLoaderMethod;
        req.params[0]        = className;
        var res = ServiceInvoker.invoke(req);
        return res;
    }
};

/**
 * 用于创建服务类的工厂。此工厂将产生一个代理功能，将需要调用的服务类映射为服务器调用。
 * 这样处理可以使得客户端调用和服务器端的调用的机理保持一致。
 */
var ServiceFactory = {
    getService : function(className, full) {
        var classDesc = ClassLoader.getClass(className, full);  // 得到类描述，注意此处的className可能是类名称的缩写
        var service = new ServiceMock (classDesc.className);
        var methods = classDesc.methods;
        for (var i = 0; i < methods.length; i++) {
            var methodDesc = methods[i];
            var methodName = methodDesc.name;
            if (AnnotationHelper.getAnnotation(methodDesc, 'LocalMethod') != null) {
                // 调用本地方法
                var localMethodInfo = new LocalMethodInfo(methodName);
                var localMethod = new InvokedMethod (service, localMethodInfo);
                service[methodName] = localMethod.invokeLocally;
            } else {
                // 调用运程方法，增加一个动态模拟（代理）方法
                var methodProxy = new MethodProxy(service, classDesc.className, methodDesc);
                service[methodName] = methodProxy.doAction;
            }
        }
        return service;
    }
}

/**
 * 服务器端的请求对象，此对象将包装所有的服务类参数、调用方法和方法参数。
 */
function Request(serviceClassName, methodName, params, invokedMethods) {

    /**
     * 服务类的名称
     */
    this.serviceClassName = serviceClassName;

    /**
     * 调用的方法名称
     */
    this.methodName = methodName;

    this.invokedMethods = invokedMethods;

    // 调用的本地服务对象本身
    this.serviceObject  = null;

    // 调用的类型，可取值：download，common，submitFiles，用户可以指定采用哪种调用方式，如果未指定，
    // 系统将自动测试这种方式。
    this.type           = null;

    /**
     * 调用的方法的参数值
     */
    if (params != null && params instanceof Array) {
        this.params = params;
    } else {
        this.params = [];
        if (params != null) {
            this.params[this.params.length] = params;
        }
    }
}

var Response = {

    /**
     * 返回的参数
     */
    returnValue : null,


    exception : null

}

/**
 * 用此类模拟所有的服务类基础类。
 */
function ServiceMock(className) {
    this.className = className;

    this.invokedMethods = new Array();

}

/**
 * 被调用的方法及其参数的封装类。
 */
function LocalMethodInfo(methodName) {
    this.localMethodName = methodName;
    this.localParams = new Array();
}

function InvokedMethod (service, localMethodInfo) {
    this.service         = service;
    this.localMethodInfo = localMethodInfo;

    this.invokeLocally = function() {
        for(var i = 0; i < arguments.length; i++) {
            // TODO: 进行类型转换...
            localMethodInfo.localParams[i] = arguments[i];
        }
        // 剔除重复的调用方法
        for (i = 0; i < service.invokedMethods.length; i++) {
            var method = service.invokedMethods[i];
            if (method.localMethodName == localMethodInfo.localMethodName) {
                service.invokedMethods.splice(i, 1);
                break;
            }
        }
        service.invokedMethods.push(localMethodInfo);
    }
}

/**
 * 模拟方法（代理远程方法的本地方法）。
 */
function MethodProxy (service, className, methodDesc) {
    this.className = className;
    this.methodDesc = methodDesc;
    this.service    = service;

    this.doAction = function () {
        var req = new Request();
        req.serviceClassName = className;
        req.serviceObject    = service;
        // TODO: 检查参数
        for(var i = 0; i < arguments.length; i++) {
            req.params[i] = arguments[i];
        }
        req.methodName = methodDesc.name;

        if (service.invokedMethods != null && service.invokedMethods.length > 0) {
            req.invokedMethods = service.invokedMethods;
        }

        // 处理 Service 的内置函数
        var ret = ServiceInvoker.invoke(req, function(response) {
            var serviceObject = response["serviceObject"];
            jQuery.extend(this.service, serviceObject);
        });

        return ret;
    }
}

/**
 * 服务调用的中心处理类。
 */
var ServiceInvoker = {

    /**
     * action
     */
    action : __contextPath__ + 'jsonClient.action',

    /**
     *
     */
    baseUrl : __contextPath__ + 'jsonClient.action?jsonValue=',

    /**
     * 正常的情况下是一个Response对象，异常的情况可能是一个Exception，其他情况为异常。
     */
    ret : null,

    /**
     * 返回值属于流（InputStream）类型的名称。
     */
    streamTypes : {
        "java.io.InputStream" : true,
        "java.io.ByteArrayInputStream" : true,
        "java.io.FileInputStream" : true,
        "java.io.FilterInputStream" : true,
        "java.io.ObjectInputStream" : true,
        "com.xt.gt.jt.proc.result.DownloadedFileInfo" : true,
        "java.io.StringBufferInputStream" : true
    },

    /**
     * 用户可以定义一定数量的拦截器(interceptor)，在服务调用之前和之后进行相应的
     * 业务处理，比如：登录用户的校验，自动登录等等。
     */
    // onInvokes   : SystemConfiguration.read('interceptor.onInvokes'),    // 调用前使用处理此回调函数
    onCompletes : SystemConfiguration.read('interceptor.onCompletes'),  // 方法调用后回调此函数（目前在下载和完成时不调用此函数）
    // onErrors    : SystemConfiguration.read('interceptor.onErrors'),     // 错误发生时的回调函数(主要动态读取)

    // 类型转换函数（默认的转换函数只对日期类型进行转换，将其转换为1970-01-01至今的毫秒数）
    converter :  function (key, value) {
        //  MessageBox.debug("key=" + key + "; value=" + value);
        if (value instanceof Date) {
            return new String(value.getTime());
        } else {
            return value;
        }
    },

    /**
     * 对外调用的接口。这个接口将判断是否有文件上传，还是文件下载、或者是只是通常的
     * 方法调用。根据这些情况，调用相应的处理函数。
     */
    invoke : function (request, resProcess) {
        var uploadedFiles = [];
        var retValue = null;
        var type = request.type;

        // 处理拦截器函数
        var _onInvokes = SystemConfiguration.read('interceptor.onInvokes');
        if (_onInvokes != null && _onInvokes.length > 0) {
            for(var i = 0; i < _onInvokes.length; i++) {
                var _break = _onInvokes[i](request);
                // 引起中断后续操作
                if (!!_break) {
                    return retValue;
                }
            }
        }
        if (type === 'download' || (type == null && this.isDownload(request))) {
            // 下载操作
            this.download(request, resProcess);
        } else if (type === 'submitFiles' || (type == null && this._hasAttachment(request.params, uploadedFiles))) {
            // 带有附件的提交操作
            retValue =  this.invokeWithAttachments(request, uploadedFiles, resProcess);
        } else {
            // 其他通用调用方式
            retValue = this._invoke0(request, resProcess);
        }
        return retValue;
    },


    /**
     * 向服务器端发送处理请求，此发送采用同步方式进行处理。
     * @param resProcess 结果处理函数
     * @param request req 对象，请求参数不能为空。
     */
    _invoke0 : function (request, resProcess) {
        var _url = this.createURL(this.baseUrl, request);
        jQuery.ajax({
            url: _url,
            type: 'POST',
            dataType: 'json',
            cache : false,
            async: false,
            // timeout: 1000,
            error: function(request, textStatus, errorThrown){
                throw new Error('Error loading JSON document', errorThrown || textStatus);
            },
            success: function(jsonText) {
                ServiceInvoker.response = jsonText;
            },
            complete : function (request, textStatus) {
            // alert('complete textStatus=' + textStatus);
            }
        });

        //
        var retValue = this.processResponse(request, this.response, resProcess);

        // 处理拦截器函数
        if (this.onCompletes != null && this.onCompletes.length > 0) {
            for(var i = 0; i < this.onCompletes.length; i++) {
                this.onCompletes[i](request, retValue);
            }
        }

        // MessageBox.debug('retValue=', retValue);

        return retValue;
    },

    /**
     * 处理结果
     */
    processResponse : function (request, response, resProcess) {
        if (response == null) {
            throw new Error("The response can not be null.");
        }
        if (response['__className'] != 'com.xt.proxy.event.Response') {
            ServiceInvoker.processServerError(request, response);
            return null;
        }

        // 如果定义了结果处理函数，则可以先行进行处理
        if (resProcess != null) {
            resProcess(response);
        }

        // MessageBox.debug('1111111111111111=', "error");
        // 复制本地变量的值（采用值复制的方式，不复制方法）
        if (request != null && response != null) {
            Utils.valueCopy(response.serviceObject, request.serviceObject);
        }

        var retValue = response.returnValue;

        // 对值进行转换
        retValue = ServiceInvoker.responseConvert(retValue);
        // MessageBox.debug('retValue=', retValue);

        return retValue;
    },

    /**
     * 处理服务器端发回的异常信息
     */
    processServerError : function(request, response) {
        // 异常处理
        // var msg = "The exception was thrown by the server.";
        // msg += '||' + response['value'];
        // alert('JSON.stringify(response)=' +  JSON.stringify(response));
        var error = null;
        if (response) {
            var code = ErrorManager.prefix + IdGenerator.getId();
            ErrorManager.register(code, response);
            error = new Error(code);
            error.description = code;
        } else {
            var msg = "The exception was thrown by the server.";
            error = new Error(msg);
            error.description = msg;
        }
        

        var consumed = false;  // 异常是否被处理
        // 动态处理异常捕获函数
        var _onErrors = SystemConfiguration.read('interceptor.onErrors');
        // MessageBox.debug('_onErrors=', _onErrors);
        if (_onErrors != null && _onErrors.length > 0) {
            for (var i = 0; i < _onErrors.length; i++) {
                consumed = !!_onErrors[i](error, request, response);
                if (consumed) {
                    break;
                }
            }
        }
        // alert('processResponse=' + error.stackTrace);
        error.consumed = consumed;  // 标识出此错误已经处理
        // MessageBox.debug('processServerError=', error);
        throw error;
    },

    /**
     * 根据给定的请求创建一个URL，此 URL 符合json请求的标准。
     */
    createURL : function (baseUrl, request) {
        if (request == null) {
            throw new Error("Parameter(2) request can not be null.");
        }

        // 进行函数转换
        request.params = this.requestConvert(request.params);
        if (request.params == null) {
            request.params = [];
        }

        // convert request object to json string
        var requestJSONText = JSON.stringify(request, this.converter);

        // make a service call
        // TODO: 如果 URL 过长将导致请求失败！！！
        // MessageBox.debug('this.baseUrl=' + this.baseUrl + requestJSONText);
        if (baseUrl == null) {
            baseUrl = this.baseUrl;
        }
        return baseUrl + requestJSONText;
    },

    //
    invokeMethod : function(className, methodName) {
        if (className == null || methodName == null) {
            throw "服务类和方法名称都不能为空。";
        }
        var req = new Request();
        req.serviceClassName = className;
        req.methodName = methodName;
        for(var i = 2; i < arguments.length; i++) {
            req.params[i - 2] = arguments[i];
        }
        return this.invoke(req);
    },

    /**
     * 参数中存在附件的情况
     */
    invokeWithAttachments : function (request, uploadedFiles, resProcess) {
        var ajaxUpload = null;  // 第一个文件作为提交用的控件
        for(var i = 0; i < uploadedFiles.length; i++) {
            if (ajaxUpload == null) {
                ajaxUpload = uploadedFiles[i].ajaxUpload;
            } else {
                ajaxUpload.append(uploadedFiles[i].ajaxUpload);
            }
        }
        // 合并多个AjaxUploader
        var jsonValue = ServiceInvoker.createURL('', request);
        ajaxUpload.setData({
            'jsonValue': jsonValue
        });
        ajaxUpload.setOption('responseType', 'json');
        var processResponse = this.processResponse;
        Utils.run(function() {

            // MessageBox.debug('request.serviceObject', request.serviceObject.onComplete);
            if (request.serviceObject.onComplete != null) {
                var _onComplete = request.serviceObject.onComplete;
                ajaxUpload.setOnComplete(function (file, response) {
                    // MessageBox.debug('response=' + response);
                    var retValue = processResponse(request, response, resProcess);  // 先处理结果
                    // MessageBox.debug("retValue=", retValue);
                    _onComplete(retValue);
                });
            }

            ajaxUpload.submit();

            // 提交结束后需要将上传文件清理一下
            for(i = 0; i < uploadedFiles.length; i++) {
                if (ajaxUpload != uploadedFiles[i]) {
                    uploadedFiles[i].reset();
                } else {
                    ajaxUpload.resetValue();
                }
            }
        });  // end of Utils.run
    //this.onComplete = null;
    },

    /**
     * 直接使用上传已定义的Form标签上传
     * @depreciated
     */
    submitFiles : function (form, serviceClassName, methodName, params) {
        if (form == null || _createJQuery(form).length == 0) {
            throw new Error("Parameter form(1) can not be null.");
        }
        if (serviceClassName == null || methodName == null) {
            throw new Error("Parameters serviceClassName(2) and methodName(3) can not be null.");
        }
        var _form =  _createJQuery(form);

        // 检查一下编码类型，没有的话将其自动补充
        var enctype = _form.attr('enctype');
        if (enctype == null || enctype == '' /*|| 'multipart/form-data' != form.attr('enctype').toLowerCase()*/) {
            // throw new Error("form attribute ");
            _form.attr('enctype', 'multipart/form-data');
        }

        _form.attr('action', this.action);
        var request = new Request();
        request.serviceClassName = ClassLoader.getFullName(serviceClassName);
        request.methodName       = methodName;
        request.params           = params || [];
        var jsonValue = this.createURL('', request);
        // 要判断一下这个节点是否已经存在
        jQuery("<input type='hidden' name='jsonValue' />").appendTo(_form).val(jsonValue);
        _form.submit();
    },

    /**
     * 判断一个请求中是否有附件（上传文件）。
     */
    _hasAttachment : function (obj, uploadedFiles) {
        if (obj == null) {
            return false;
        } else if (obj instanceof UploadedFile) {
            uploadedFiles.push(obj);
            return true;
        } else if (obj instanceof Array) {
            var flag = false;
            for (var i = 0; i < obj.length; i++) {
                var ret = this._hasAttachment(obj[i], uploadedFiles);
                flag = flag || ret;
            }
            return flag;
        } else if (typeof obj === 'object') {
            // 采用递归查找方式(TODO: 需要有递归次数限制)
            var _flag = false;
            for (var prop in obj) {
                var _ret = this._hasAttachment(obj[prop], uploadedFiles);
                _flag = flag || _ret;
            }
            return _flag;
        }
        return false;
    },

    /**
     * 将请求进行数据结构的转换（客户端-->服务器）。
     * TODO:此递归需要有递归层次限制，避免引起死循环。
     */
    requestConvert : function (obj) {
        if (obj == null) {
            return null;
        }
        // 基本类型无须转换
        if (typeof obj !== 'object') {
            return obj;
        }

        // 数组类型要逐一转换
        if (obj instanceof Array) {
            var changed = false;
            var newArray = [];
            for (var i = 0; i < obj.length; i++) {
                var elem = obj[i];
                newArray[i] = this.requestConvert(elem);
                changed = changed || (newArray[i] === elem);  // 判断一下是否真正改变
            }
            // return (changed ? newArray : obj);
            return newArray;
        } else {
            return this._objConvert(obj);
        }
    },

    _objConvert : function (obj) {
        if (obj instanceof Date) {
            return "" + obj.getTime();
        } else if (obj instanceof UploadedFile) {
            return {
                propertyName : obj.propertyName,
                fileName : obj.input.val()
            };
        } else {
            var convertedObj = new Object();
            for (var prop in obj) {
                var convertedProp = this.requestConvert(obj[prop]);
                convertedObj[prop] = convertedProp;
            }
            return convertedObj;
        }
    },

    /**
     * 将响应进行转换（服务器-->客户端）的数据结构的转换
     */
    responseConvert : function (obj) {
        // 基本类型无须转换
        if (typeof obj !== 'object') {
            return obj;
        }
        // 数组类型要逐一转换
        if (obj instanceof Array) {
            var changed = false;
            var newArray = [];
            for (var i = 0; i < obj.length; i++) {
                var elem = obj[i];
                newArray[i] = this.responseConvert(elem);
                changed = changed || (newArray[i] === elem);  // 判断一下是否真正改变
            }
            // return (changed ? newArray : obj);
            return newArray;
        } else if (obj['__className'] != null){
            // 表示这是一个纯粹的 Java 类
            var __className = obj['__className'];
            // 找到转换函数，注意：只有特定的类型需要转换（即注册了的类型）
            var converter = ConverterFactory.getConverter(__className);
            if (converter != null) {
                return converter.convert(obj);
            } else {
                return this._convert0(obj);
            }
        } else {
            return this._convert0(obj);
        }
    },

    _convert0 : function (obj) {
        var _changed = false;
        var convertedObj = new Object();
        for (var prop in obj) {
            var convertedProp = this.responseConvert(obj[prop]);
            _changed = _changed || (convertedProp === obj[prop]);  // 判断一下是否真正改变
            convertedObj[prop] = convertedProp;
        }
        // return (_changed ? convertedObj : obj);
        return convertedObj;
    },

    /**
     * 判断一个请求是不是下载函数。判断的规则是，如果一个方法的返回值是：InputStream 及其子类（用户可自行定义），
     * 则认为此方法为返回一个下载流。
     */
    isDownload : function (request) {
        if (request == null || request.serviceClassName == null || request.methodName == null) {
            return false;
        }
        var methodDesc = ClassLoader.getMethod(request.serviceClassName, request.methodName, true);
        return this.streamTypes[methodDesc.returnType];
    },

    /**
     * 使用异步方式下载文件。
     */
    download : function (request, resProcess) {
        var requestJSONText = JSON.stringify(request, this.converter);
        var iframe = this._createIframe();
        var process = this.processResponse;
        jQuery(iframe).bind('load', function (){
            var retValue = this.contentDocument.body.textContent
            || this.contentDocument.body.outerText;
            // MessageBox.debug('download retValue=', retValue);
            // TODO: 应该仔细考虑如果返回的不是合法的 JSON 字符串
            // 对响应代码进行判断
            Utils.run(function() {
                var response = null;
                if (retValue !== undefined
                    && retValue !== '' && retValue.startsWith("{")) {
                   response = JSON.parse(retValue);
                } else if(retValue) {
                    response = new Error('The return value do not define.');
                    response.description = retValue;
                }
		// 正常时没有返回结果，只有服务器端发生异常时返回结果，才需要使用处理函数
		if (response) {
                    process(request, response, resProcess);
		}
                // document.body.removeChild(iframe);
            }, function() {
                // FIXME: 是否出错都要移除此Frame?!不移除的话是否会产生内存泄漏
                // alert('finally..555555555......')
                // document.body.removeChild(iframe);
            });
        });
        var form = this._createForm(iframe, this.action);
        // 必须以这种方式传递参数？！，否则在服务器端读取不到
        jQuery("<input type='hidden' name='jsonValue' />").appendTo(form).val(requestJSONText);
        form.submit();
    },

    /**
	 * Creates iframe with unique name
	 */
    _createIframe : function(){
        var id = '_iframe_' + IdGenerator.getId();

        var iframe = jQuery('<iframe src="javascript:false" name="' + id + '" />');

        iframe.attr('id', id);
        iframe.css("display","none");
        document.body.appendChild(iframe[0]);
        return iframe[0];
    },

    /**
	 * Creates form, that will be submitted to iframe
	 */
    _createForm : function(iframe, action){
        var form = jQuery('<form method="post" enctype="multipart/form-data"></form>');
        //form.css("display","none");
        form.attr('action', action);
        form.attr('target', iframe.name);
        document.body.appendChild(form[0]);
        return form;
    }
}

/**
 * 服务器异常，用于包装有服务器端发出的异常信息，通常都是一个异常堆栈。
 * @depreciated
 */
function ServerError(msg, stackTrace) {
    this.msg        = msg;  // 异常信息
    this.stackTrace = stackTrace;  // 异常堆栈
}

var ImageHelper = {
    /**
     * 在指定的控件上显示图片。
     * 注意：此函数只是构建了一个URL，因此即使服务端发生了错误，在前台也是不能显示的。
     */
    showPicture : function (pictureId, serviceClassName, methodName, params) {
        if (pictureId == null) {
            throw new Error("图片ID不能为空。");
        }
        var picture = _createJQuery(pictureId);

        var _url = this.getImageUrl(serviceClassName, methodName, params);
        picture.attr('src', _url);
    },

    /**
     * 构建一个用于读取图片的 URL
     */
    getImageUrl : function (serviceClassName, methodName, params) {
        if (serviceClassName == null || methodName == null) {
            // throw new Error("服务类和方法名称都不能为空。");
            throw new Error("The service class and method name can not be null.");
        }
        var request = new Request();
        request.serviceClassName = ClassLoader.getFullName(serviceClassName);
        request.methodName = methodName;
        request.params     = params || [];
        var _url = ServiceInvoker.createURL(null, request);
        return _url;
    }

}

/**
 * 和取得标注相关的助手类
 */
var AnnotationHelper = {
    getAnnotation : function (declaredObj, annoName) {
        if (declaredObj == null || annoName == null) {
            throw new "标注描述和待查找的标注名称都不能为空。";
        }
        var annos = declaredObj.annotations;
        if (annos == null || annos.length == 0) {
            return null;
        }
        if (!(annos instanceof Array)) {
            throw new "标注必须是数组类型。";
        }
        for (var i =0; i < annos.length; i++) {
            var anno = annos[i];
            if (anno != null && anno.name != null && anno.name.indexOf(annoName) >=0) {
                return anno;
            }
        }
        return null;
    },

    /**
     * 从一个类的指定域上取得指定的标注。
     * @_clazz 指定的类，不能为空。
     * @_name  域的名称，不能为空。
     */
    getFieldAnnotations : function (_clazz, _name) {
        if (_clazz == null || _name == null) {
            throw new Error("The parameters can not be null.");
        }

        // 找到指定的域，未找到则抛出异常
        var field = null;
        if (_clazz.fields != null) {
            for(var i = 0; i < _clazz.fields.length; i++) {
                if (_name === _clazz.fields[i].name) {
                    field = _clazz.fields[i];
                    break;
                }
            }
        }
        if (field == null) {
            throw new Error ('Can not find the field named [' + _name + '] in class [' + _clazz + ']');
        }
        return (field.annotations || []);
    },

    /**
     * 从一个类的指定域上取得指定的标注
     */
    getFieldAnnotation : function (_class, _name, annotationName) {
        var anno = null;
        var annos = this.getFieldAnnotations(_class, _name);
        for (var i = 0; i < annos.length; i++) {
            var _anno = annos[i];
            if (_anno != null && _anno.name != null && _anno.name.indexOf(annotationName) >=0) {
                anno = _anno;
                break;
            }
        }
        return anno;
    }

}

/**
 * 定义所有字典的服务。
 */
var DictionaryService = {

    // 远程字典服务的名称
    serviceName : 'com.xt.core.utils.dic.RemoteDictionaryService',

    // 装置字典服务的方法名称
    methodName : 'getDictionary',

    // 缓存字典服务的池
    pool : {},

	/**
	  * 根据指定的字典名称返回一个字典，如果字典不存在，系统将抛出异常。
	  */
    getDictionary : function (dictionaryName) {
        if (dictionaryName == null) {
            throw new Error("Parameter dictionaryName(1) can not be null.");
        }
        var dic = this.pool[dictionaryName];
        if (dic == null) {
            // 装载字典
            dic = ServiceInvoker.invokeMethod(this.serviceName, this.methodName, dictionaryName);
            this.pool[dictionaryName] = dic;
        }
        return dic;
    },

	/**
	  * 注册一个字典自定义的字典，系统不对字典是否重复进行校验，如果同名，后注册将覆盖新注册的字典。
	  * @param  dicName 字典名称
	  * @param  items     字典项数组
	  */
    register : function (dicName, items) {
	    if (dicName == null || items == null) {
		    MessageBox.shwoError('字典名称及其字典项均不能为空。');
		    return;
		}
		var dic = {'items' : items};
		this.pool[dicName] = dic;
    }

}

/**
  * 字典帮助类，用于根据字典构建下拉列表框、复选框、单选按钮等控件。
  *  TODO: 调整参数的形式，用{}代替目前的多参数。
  */
var DicUtils = {

    decorate : function (id, dicName, nullable, nullValue, nullTitle, contrlStr) {
        if (id == null || dicName == null) {
            throw new Error("Parameters id(1) and dicName(2) can not be null. ");
        }
        // 清除所有子节点
        var comp = _createJQuery(id);

        comp.empty();

        // 处理“空”节点
        if (nullable) {
            nullValue = nullValue == null ? '' : nullValue;
            nullTitle = nullTitle == null ? '' : nullTitle;
            var output = contrlStr(nullValue, nullTitle);
            comp.append(jQuery(output));
        }
        var dic = DictionaryService.getDictionary(dicName);
        if (dic === undefined) {
            throw new Error('The dictionary named [' + dicName + '] can not be found.');
        }

        // 字典项为空则直接返回
        if (dic.items == null) {
            return;
        }

        for (var i = 0; i < dic.items.length; i++) {
            var dicItem = dic.items[i];
            output = contrlStr(dicItem["value"], dicItem["title"]);
            comp.append(jQuery(output));
        }
    },

    /**
           * 使用“字典”修饰下拉列表框，即使用下拉列表框显示字典的内容。
          */
    decorateComboBox : function (comboBoxId, dicName, nullable, nullValue, nullTitle) {
        this.decorate(comboBoxId, dicName, nullable, nullValue, nullTitle, function (value, title) {
            return "<option value='" + value + "'>" + title + "</option>";
        });
    },

    /**
     * 使用“字典”修饰列表框，即使用列表框显示字典的内容。处理方式和decorateComboBox相同
     */
    decorateList : function (listId, dicName) {
        this.decorateComboBox(listId, dicName, false, null, null);
    },

    /**
     * 使用“字典”修饰列表框，即使用列表框显示字典的内容。处理方式和decorateComboBox相同
     */
    decorateCheckBoxes : function (divId, dicName, name) {
        this.decorate(divId, dicName, false, null, null, function (value, title) {
            return "<input name='" + name + "' type='checkbox' value='" + value + "'>" + title + "</input>";
        });
    },

    /**
     * 使用“字典”修饰列表框，即使用列表框显示字典的内容。处理方式和decorateComboBox相同
     */
    decorateRadioes : function (divId, dicName, nullable, nullValue, nullTitle, name) {
        this.decorate(divId, dicName, nullable, nullValue, nullTitle, function (value, title) {
            return "<input name='" + name + "' type='radio' value='" + value + "'>" + title + "</input>";
        });
    },

    /**
     * 字典类型的结构比较复杂，因此推荐使用此方式进行字典的转换。
     */
    getTitle : function (dic, value) {
        if (dic == null) {
            throw "字典不能为空。";
        }
        if (value == null || dic.items == null) {
            return "";
        }
        // alert('dic=' + dic);
        // alert('dic.items=' + dic.items);
        for (var i = 0; i < dic.items.length; i++) {
            if (value == dic.items[i].value) {
                return dic.items[i].title;
            }
        }
        return value;  // 没有找到合适的字典项。
    }
}

/**
 * 可编辑的控件标签的名称
 */
var EDITABLE_CONTROL_TAG = ['input', 'select', 'textarea'];

/**
 * 可编辑的控件标签的名称
 */
var EDITABLE_CONTROL_TYPE = ['text', 'password', 'checkbox', 'radio'];

var BindUtils = {

    /**
     * 将当前值按照名称与所有被选中的控件进行绑定
     * @param selector 选择器（需要遵循jQuery的标准模式）。
     * @param data 必须是对象类型
     * @param params 参数，其中包括：
     *     type     : 数据对应的类型（注意，应该是Java定义的类型，因为此类型关系到格式化和反格式化处理）
     *     converter : 用户自定义转换方式的回调处理，如果未定义，且传入了type 参数，将采用系统默认的转换方式。
     *     formater  : 对数据进行格式化和解析的回调处理，如果未定义，且传入了type 参数，将采用系统默认的转换方式。
     */
    bind : function (selector, data, params) {
        if (data == null) {
            return;
        }
        var _params = params || {};
        // 绑定对象对应的Java类型
        var type      = _params['type'];
        var converter  = _params['converter'];
        var formater   = _params['formater'];
        var exclusions = _params['exclusions'] || [];  // 不处理的字段


        var comps = ControlUtils.getNamedComp(selector);
        var _clazz = null;
        if (type != null) {
            _clazz = ClassLoader.getClass(type);
        }

        // TODO: 对日期，字典值和Lookup的自动绑定


        // MessageBox.debug('_class=', _clazz)
        jQuery.each(comps, function (index, comp) {
            var _name = comp.name;  // 控件的名称
            if (_name != null && exclusions.indexOf(_name) < 0) {
                var _value = null;
                if (converter != null) {
                    _value = converter.convertTo(comp, _name, data, params);
                } else {
                    // 进行自动转换
                    _value = BindUtils.convert(_clazz, _name, data[_name]);
                }
				// MessageBox.debug('_name=', _name);
				// MessageBox.debug('_value=', _value);

                // 对数据进行格式化
                if (formater != null) {
                    _value = formater.format(comp, _name, data, params);
                } else {
                    // 进行自动格式化
                    _value = BindUtils.format(_clazz, _name, data[_name]);
                }

                // 绑定到控件
                jQuery(comp).val(_value || '');

                // 当控件发生变化时，使用此方法控件的值同步回对象
                comp.onchange = function() {
                    var _value = jQuery(comp).val();
                    // 对数据进行解析（反格式化）（将显示内容变为实际值）
                    if (formater != null && formater.parse) {
                        _value = formater.parse(comp, _name, _value, params);
                    } else {
                        _value = BindUtils.parse(_clazz, _name, _value);
                    }
                    // 对数据进行反向转换（将显示值编码）
                    data[_name] = _value;
                }
            }
        });
    },

    convert : function (_clazz, name, value) {
        var field = BindUtils._getField(_clazz, name);
        // 未找到对应的域
        if (field == null) {
            return value;
        }

        // MessageBox.debug('field=', field);
        // 未找到对应的域
        if (field == null) {
            return value;
        }
        var converter = ConverterFactory.getConverter(field.type);
        if (converter != null) {
            return converter.convert(value);
        }
        return value;
    },

    _getField : function (_clazz, name) {
        if (_clazz == null || name == null || _clazz.fields == null) {
            return null;
        }
        var fields = _clazz.fields;
        var field = null;
        for (var i = 0; i < fields.length; i++) {
            if (fields[i].name == name) {
                field = fields[i];
                break;
            }
        }
        return field;
    },

    /**
     * 根据参数类型和格式化标注自动进行格式化工作。
     */
    format : function (_clazz, name, value) {
        var field = BindUtils._getField(_clazz, name);
        // 未找到对应的域
        if (field == null) {
            return value;
        }
        var formater = FormaterFactory.getFormater(field.type);
        // MessageBox.debug('formater=' + formater);
        if (formater != null) {
            return formater.format(_clazz, name, value);
        }
        return value;
    },

    /**
     * 根据参数类型和格式化标注自动进行格式化工作。
     */
    parse : function (_clazz, name, value) {
        var field = BindUtils._getField(_clazz, name);
        // 未找到对应的域
        if (field == null) {
            return value;
        }
        var formater = FormaterFactory.getFormater(field.type);
        // MessageBox.debug('formater=' + formater);
        if (formater != null && formater.parse) {
            return formater.parse(_clazz, name, value);
        }
        return value;
    }
}


/**
 * 用于校验的帮助类。
 */
var Validator = function (_settings) {
    this.settings = {
        /**
         * 用于装载“PO”对象相关校验的服务类。
         */
        validatorLoaderService : "com.xt.gt.html.service.ValidatorLoaderService",

        /**
         * 用于装载“PO”对象相关校验的方法名称。
         */
        methodName : "getValidators"
    }
    jQuery.extend(this.settings, _settings);
}

/**
 * 用于组件校验的静态方法。
 * @param comp 组件实例
 * @param validatables 可校验接口
 */
Validator.compValidate = function (comp, validatables) {
    var msgs = [];   // 错误消息
    for (var i = 0; i < validatables.length; i++) {
        var validatable = validatables[i];
        if (validatable == null) {
            continue;
        }
        var _vms = validatable.validate($(comp).val());
        if (_vms != null && _vms.length > 0) {
            for (var j = 0; j < _vms.length; j++) {
                msgs.push(_vms[j]);
            }
        }
    }
    return msgs;
}

var MessageDisplay = {
    /**
     * 显示指定控件相关信息。首先清除与此控件相关的所有信息，然后在控件的后面
     * 将信息显示出来（使用“span”控件）。
     * @param source 源控件，为空，则不进行任何处理。
     * @param vms 校验信息的数组。
     */
    showMessages : function(source, vms) {
        // 清除当前显示的错误信息
        this.clear(source);

        if (vms == null || vms.length == 0) {
            return;
        }
        if (source == null) {
            // 使用提示框显示消息
            return;
        }

        //
        jQuery(source).after('<span class="validation_fail">' + vms[0].message + "</span>");
    },

    /**
     * 清除与此控件相关的所有信息。
     */
    clear : function (source) {
        if (source == null) {
            return;
        }
        jQuery(source).siblings('.validation_fail').remove();
    }
};

jQuery.extend(Validator.prototype, {
    /**
     * 控件及其名称的校验映射表.
     */
    compValidators : {},

    /**
     * 消息显示器,用于确定具体的消息显示方式.
     */
    display : MessageDisplay,

    /**
     * 将指定容器的校验信息和类信息绑定到一起。
     */
    bindContainer : function (containerId, className, full) {
        var fullName = ClassLoader.getFullName(className, full);
        var validators = ServiceInvoker.invokeMethod(this.settings.validatorLoaderService,
            this.settings.methodName, fullName);
        var comps = ControlUtils.getNamedComp(containerId);
        var _compValidators = {};
        var onValidate = this.onValidate;
        var _convert = this.convert;
        var _display = this.display;
        jQuery.each(validators, function(name, validatables){
            if (comps[name] != null && validatables.length > 0) {
                // 将服务端校验转换为客户端校验
                var _validatables = _convert(validatables);

                // 被校验的错误控件
                var comp = jQuery(comps[name]);

                // 自动产生一个id
                if (comp.attr('id') == null || comp.attr('id') == '') {
                    comp.attr('id', '_val_' + IdGenerator.getId());
                }

                // 在事情焦点和文本变化时进行校验
                comp.bind('keyup',   {
                    validatables : _validatables,
                    display : _display
                }, onValidate);
                comp.bind('change', {
                    validatables : _validatables,
                    display : _display
                }, onValidate);
                _compValidators[comp.attr('id')] = _validatables;
            }
        });
        for(var compId in _compValidators) {
            // 校验方式要累计(TODO: 怎样剔除重复的校验类？)
            var _validators = this.compValidators[compId];
            if (_validators == null) {
                _validators = [];
            }
            for (var i = 0; i < _compValidators[compId].length; i++) {
                _validators.push(_compValidators[compId][i]);
            }
            this.compValidators[compId] = _validators;
        }
    },

    onValidate : function(event) {
        var validatables = event.data.validatables;
        var msgs = Validator.compValidate(this, validatables);
        //
        var display  = event.data.display;
        if (msgs.length > 0) {
            display.showMessages(this, msgs);
        } else {
            display.clear(this);
        }
    },

    /**
     * 卸载当前所有已注册控件的校验事件监听器。注意：因为无法区分时间的类型，所以此操作将解除
     * 已经注册的所有控件的“keyup”，“change”事件。如果有非校验用途的此类
     * 事件，需要重新注册一次。
     */
    unbind : function () {
        if (this.compValidators == null) {
            return
        }
        jQuery.each(this.compValidators, function(compId, validatables) {
            var _comp = jQuery('#' + compId);
            _comp.unbind('keyup');
            _comp.unbind('change');
        });
        this.compValidators = {};
    },

    /**
     * 将指定控件和可校验接口绑定到一起。
     */
    bindComponent : function (comp, validatable) {
        if (comp == null) {
            throw new Error('The component that be bound can not be null.');
        }
        if (validatable == null) {
            return;
        }
        var _comp = jQuery(comp);

        // 自动产生一个id, 确保组件都有ID
        var id = _comp.attr('id');
        if (id == null || id == '') {
            id = '_val_' + IdGenerator.getId();
            _comp.attr('id', id);
        }
        var validatables = this.compValidators[id];
        if (validatables == null) {
            validatables = [];
        }
        validatables.push(validatable);
        this.compValidators[id] = validatables;
    },

    /**
     * 校验所有的已经注册的控件是否合法
     */
    validate : function () {
        var success = true;
        // MessageBox.debug("this.compValidators=", this.compValidators);
        jQuery.each(this.compValidators, function(compId, validatables) {
            var _comp = jQuery('#' + compId);
            _comp.change();  // 触发控件的改变事件，显示错误信息
            var msgs = Validator.compValidate(_comp, validatables);
            if (msgs != null && msgs.length > 0) {
                success = false;
            }
        });

        return success;
    },

    /**
     * 将服务器端的校验定义转换为客户端（JS形式）的校验定义。
     */
    convert : function (validatables) {
        var _validatables = [];
        jQuery.each(validatables, function(i, validatable) {
            if (validatable != null) {
                var _className = validatable['__className'];
                if (_className == null) {
                    throw new Error('The property[__className] of validatable can not be null.');
                }
                var converter = ValidatorConverterFactory.getConverter(_className);
                var validator = converter.getValidator(validatable);
                if (validator == null) {
                    throw new Error('The validatable of property[' + _className +'] can not be found.');
                }
                _validatables.push(validator);
            }
        });
        return _validatables;
    }
});

/**
 * 校验关系转换器工厂，用于定义客户端和服务器端校验关系的转换方式。
 */
var ValidatorConverterFactory = {

    /**
     * 转换器映射关系，主键是：类名；键值是转换器实例。
     */
    _converters : {
        "com.xt.gt.html.val.RequiredValidator" : new RequiredValidatorConverter(),
        "com.xt.gt.html.val.LengthValidator"   : new LengthValidatorConverter(),
        "com.xt.gt.html.val.RangeValidator"    : new RangeValidatorConverter(),
        "com.xt.gt.html.val.EmailValidator"    : new CommonValidatorConverter(),
        "com.xt.gt.html.val.HTTPValidator"     : new CommonValidatorConverter(),
        "com.xt.gt.html.val.PhoneValidator"    : new CommonValidatorConverter(),
        "com.xt.gt.html.val.MobileValidator"   : new CommonValidatorConverter(),
        "com.xt.gt.html.val.SSNValidator"      : new CommonValidatorConverter(),
        "com.xt.gt.html.val.ChineseValidator"  : new CommonValidatorConverter(),
        "com.xt.gt.html.val.IntegerValidator"  : new CommonValidatorConverter(),
        "com.xt.gt.html.val.LongValidator"  : new CommonValidatorConverter(),
        "com.xt.gt.html.val.PositiveIntegerValidator" : new CommonValidatorConverter(),
        "com.xt.gt.html.val.FloatValidator"    : new FloatValidatorConverter(),
        "com.xt.gt.html.val.DoubleValidator"    : new FloatValidatorConverter()
    },

    /**
     * 根据类名得到转换器。
     */
    getConverter : function (_className) {
        var converter = this._converters[_className];
        if (converter == null) {
            throw new Error('The converter for class[' + _className + '] can not be found.');
        }
        return converter;
    },

    /**
     * 注册一个转换器。
     * @param _className 类名称
     * @param _converter 转换器
     */
    register : function (_className, _converter) {
        if (_className == null || _converter == null) {
            throw new Error('The parameters can not be null.');
        }
        this._converters[_className] = _converter;
    }
}

/**
 * 校验消息。
 * @param message 消息内容
 * @param level    错误的级别, 默认为Level.ERROR。
 * @param tip     用于对错误提供友善的提示信息。
 */
function ValidateMessage (message, level, tip) {
    this.message = message;
    this.level   = level || 'Level.ERROR';
    this.tip     = tip;
}

function Validatable (){
    /**
     * 对当前值进行校验，如果出现错误，将错误信息以“ValidateMessage”数组的方式返回。
     * 否则，返回长度为 0 的数组。
     */
    this.prototype.validate = function (value) {}
}

/**
 * 不能为空校验。
 */
var RequiredValidator = function (message) {
    this.message = message;
}

jQuery.extend(RequiredValidator.prototype, {
    validate : function (value) {
        var vms = [];
        var vm = new ValidateMessage(this.message);
        if (value == null) {
            vms.push(vm);
        }
        if (typeof value === 'string' && value == '') {
            vms.push(vm);
        }
        if (!value) {
            vms.push(vm);
        }
        return vms;
    }
});


function RequiredValidatorConverter() {
    this.getValidator = function(_validatable) {
        return new RequiredValidator('此输入域为必填项。')
    }
}

/**
 * 最大长度校验。
 */
var LengthValidator = function (length, msg) {
    this.length = length;
    this.message = msg;
}

jQuery.extend(LengthValidator.prototype, {
    validate : function (value) {
        var vms = [];
        var vm = new ValidateMessage(this.message);
        if (value == null) {
            return vms;
        }
        if (typeof value !== 'string') {
            vms.push(vm);
        }
        if (value.length > this.length) {
            vms.push(vm);
        }
        return vms;
    }
});

function LengthValidatorConverter() {
    this.getValidator = function(_validatable) {
        var validator = new LengthValidator(_validatable.length,
            '此输入域的最大长度为' + _validatable.length + '。');
        return validator;
    }
}

/**
 * 进行最大值和最小值校验，被校验的值要大于等于最小值，小于等于最大值。
 */
var RangeValidator = function(min, max, msg) {
    this.min = min;
    this.max = max;
    this.message = msg;
}

jQuery.extend(RangeValidator.prototype, {
    validate : function (value) {
        var vms = [];
        var vm = new ValidateMessage(this.message);
        if (value == null) {
            return vms;
        }
        if (typeof value === 'number' && (value > this.max || value < this.min)) {
            vms.push(vm);
        } else if (typeof value == 'string') {
            var _value = parseFloat(value);
            if (_value > this.max || _value < this.min) {
                vms.push(vm);
            }
        }
        return vms;
    }
});

function RangeValidatorConverter() {
    this.getValidator = function(_validatable) {
        return new RangeValidator(_validatable.min, _validatable.max,
            '此输入域的最大长度为[' + _validatable.max + ']。');
    }
}

/**
 * 正则表达式校验。
 */
var RegExpValidator = function (regExp, msg) {
    this.regExp = regExp;
    this.message = msg;
}

jQuery.extend(RegExpValidator.prototype, {
    validate : function (value) {
        var vms = [];
        var vm = new ValidateMessage(this.message);
        if (value == null) {
            return vms;
        }
        if (typeof value !== 'string') {
            vms.push(vm);
            return vms;
        }
        if (this.regExp == null) {
            // throw new Error("用于校验的正则表达式不能为空。");
            throw new Error("The regular expression which was used on validating can not be null.");
        }
        var _pattern = this.regExp;
        if (typeof this.regExp == 'string') {
            _pattern = new RegExp(this.regExp);
        }
        if (!_pattern.test(value)) {
            vms.push(vm);
        }
        return vms;
    }
});

/**
 * eMail 校验器, 默认格式为: ^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*.\\w+([-.]\\w+)*$，
 * 可通过参数 validation.email 进行配置。
 */
var EmailValidator = function (msg) {
    this.message = msg || '电子邮件的格式错误。';
    var exp = SystemConfiguration.read('validation.email')
    || '^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*.\\w+([-.]\\w+)*$';
    this.regExpValidator  = new RegExpValidator(exp, msg);
}

jQuery.extend(EmailValidator.prototype, {
    validate : function (value) {
        return this.regExpValidator.validate(value);
    }
});

/**
 * 通用的校验转换器, 此转换器安装校验类的名称(不包括包名), 加上"Validator" 作为客户端校验类的名称，
 * 并且，参数只有“message”一个的情况下。
 */
function CommonValidatorConverter() {
    this.getValidator = function(_validatable) {
        var _className = _validatable['__className'];

        // MessageBox.debug('CommonValidatorConverter _className=', _className);
        if (_className == null || _className == '') {
            throw new Error('The name of validator can not be null.');
        }
        var segs = _className.split('.');
        var name = segs[segs.length - 1];  // 取类的名称
        // MessageBox.debug('CommonValidatorConverter name=', name);
        var exp = 'new ' + name + '(\"' + _validatable.message + '\")';
        return eval(exp);
    }
}

/**
 * URL 地址校验器, 默认格式为: ^https?://([\\w\\-]+.)+[\\w\\-]+(/[\\w\\-\\./?%&=]*)?$，
 * 可通过参数 validation.http 进行配置。
 */
var HTTPValidator = function (msg) {
    this.message = msg || 'HTTP 地址的格式不正确。';
    var exp = SystemConfiguration.read('validation.http')
    || '^https?://([\\w-]+.)+[\\w-]+(/[\\w\\-\\./?%&=]*)?$';
    this.regExpValidator  = new RegExpValidator(exp, msg);
}

jQuery.extend(HTTPValidator.prototype, {
    validate : function (value) {
        return this.regExpValidator.validate(value);
    }
});


/**
 * 电话号码（不包括手机）校验器, 默认格式为: ^((\d{3,4})|\d{3,4}-)?\d{7,8}$，
 * 可通过参数 validation.phone 进行配置。
 */
var PhoneValidator = function (msg) {
    this.message = msg || '电话号码的格式不正确。';
    var exp = SystemConfiguration.read('validation.phone')
    || '^((\\d{3,4})|\\d{3,4}-)?\\d{7,8}$';
    this.regExpValidator  = new RegExpValidator(exp, msg);
}

jQuery.extend(PhoneValidator.prototype, {
    validate : function (value) {
        return this.regExpValidator.validate(value);
    }
});

/**
 * 移动电话号码校验器, 默认格式为: ^1\\d{12,12}$，
 * 可通过参数 validation.mobile 进行配置。
 */
var MobileValidator = function (msg) {
    this.message = msg || '移动电话的格式不正确。';
    var exp = SystemConfiguration.read('validation.mobile')
    || '^1\\d{10,10}$';
    this.regExpValidator  = new RegExpValidator(exp, msg);
}

jQuery.extend(MobileValidator.prototype, {
    validate : function (value) {
        return this.regExpValidator.validate(value);
    }
});


/**
 * 身份证校验器, 默认格式为: ^\\d{15}|\\d{18}$，
 * 可通过参数 validation.ssn 进行配置。
 */
var SSNValidator = function (msg) {
    this.message = msg || '身份证号码的格式不正确。';
    var exp = SystemConfiguration.read('validation.ssn')
    || '^\\d{15}|\\d{18}$';
    this.regExpValidator  = new RegExpValidator(exp, msg);
}

jQuery.extend(SSNValidator.prototype, {
    validate : function (value) {
        return this.regExpValidator.validate(value);
    }
});

/**
 * 中文校验器, 默认格式为: ^[\u4e00-\u9fa5]*$，
 * 可通过参数 validation.chinese 进行配置。
 */
var ChineseValidator = function (msg) {
    this.message = msg || '只允许输入中文字符。';
    var exp = SystemConfiguration.read('validation.chinese')
    || '^[\u4e00-\u9fa5]*$';
    this.regExpValidator  = new RegExpValidator(exp, msg);
}

jQuery.extend(ChineseValidator.prototype, {
    validate : function (value) {
        return this.regExpValidator.validate(value);
    }
});

/**
 * 整数类型校验。最大值为:999999999。 默认格式为: ^(\\+|\\-)?([1-9]\\d{0,8})|0$，
 * 可通过参数 validation.integer 进行配置。
 */
var IntegerValidator = function (msg) {
    var exp = SystemConfiguration.read('validation.integer')
    || '^((\\+|\\-)?([1-9]\\d{0,8})|0)$';
    this.regExpValidator = new RegExpValidator(exp, msg);
    this.message = msg || '输入值必须为整数。';
}

jQuery.extend(IntegerValidator.prototype, {
    validate : function (value) {
        return this.regExpValidator.validate(value);
    }
});

/**
 * 长整数类型校验。最大值为:999999999999999999。 默认格式为: ^(\\+|\\-)?([1-9]\\d{0,17})|0$，
 * 可通过参数 validation.long 进行配置。
 */
var LongValidator = function (msg) {
    var exp = SystemConfiguration.read('validation.long')
    || '^((\\+|\\-)?([1-9]\\d{0,17})|0)$';
    this.regExpValidator = new RegExpValidator(exp, msg);
    this.message = msg || '输入值必须为长整数。';
}

jQuery.extend(LongValidator.prototype, {
    validate : function (value) {
        return this.regExpValidator.validate(value);
    }
});

/**
 * 正整数类型校验。最大值为:99999999999。默认格式为: ^([1-9]\\d{0,8})|0$，
 * 可通过参数 validation.positiveInteger 进行配置。
 */
var PositiveIntegerValidator = function (msg) {
    var exp = SystemConfiguration.read('validation.positiveInteger')
    || '^(([1-9]\\d{0,9})|0)$';
    this.regExpValidator = new RegExpValidator(exp, msg);
    this.message = msg || '输入值必须为正整数。';
}

jQuery.extend(PositiveIntegerValidator.prototype, {
    validate : function (value) {
        return this.regExpValidator.validate(value);
    }
});

/**
 * 浮点数类型校验。最大值为:99999999999.99。默认格式为: ^(\\+|\\-)?(([1-8]\\d{0,9})|0)(\\.\\d\\d{0,${scale}})?$，
 * 可通过参数 validation.float 进行配置。
 */
var FloatValidator = function (msg, scale) {
    this.message = msg || '输入值必须为合法的小数。';
    this.scale = scale  || 2;  // 小数点后的位数
    var exp = SystemConfiguration.read('validation.float')
    || '^(\\+|\\-)?(([1-9]\\d{0,9})|0)(\\.\\d\\d{0,${scale}})?$';

    exp = exp.replace(/\$\{scale\}/, this.scale - 1);  // 替换变量
    // MessageBox.debug('FloatValidator exp=' + exp);
    this.regExpValidator = new RegExpValidator(exp, msg);
}

jQuery.extend(FloatValidator.prototype, {
    validate : function (value) {
        return this.regExpValidator.validate(value);
    }
});

function FloatValidatorConverter() {
    this.getValidator = function(_validatable) {
        return new FloatValidator(_validatable.message, _validatable.scale);
    }
}

/**
 * 双精度小数类型校验。最大值为:9999999999999999999999.99。
 * 默认格式为: ^(\\+|\\-)?(([1-17]\\d{0,9})|0)(\\.\\d\\d{0,${scale}})?$，
 * 可通过参数 validation.double 进行配置。
 */
var DoubleValidator = function (msg, scale) {
    this.message = msg || '输入值必须为合法的双精度小数。';
    this.scale = scale  || 2;  // 小数点后的位数
    var exp = SystemConfiguration.read('validation.double')
    || '^(\\+|\\-)?(([1-9]\\d{0,17})|0)(\\.\\d\\d{0,${scale}})?$';

    exp = exp.replace(/\$\{scale\}/, this.scale - 1);  // 替换变量
    // MessageBox.debug('FloatValidator exp=' + exp);
    this.regExpValidator = new RegExpValidator(exp, msg);
}

jQuery.extend(DoubleValidator.prototype, {
    validate : function (value) {
        return this.regExpValidator.validate(value);
    }
});

function DoubleValidatorConverter() {
    this.getValidator = function(_validatable) {
        return new DoubleValidator(_validatable.message, _validatable.scale);
    }
}

/**
 * 用于参数组建和解析的助手类。TODO: 考虑 URL 中存在“#”的情况。
 */
var ParameterHelper = {
    /**
     * 表示一个值是 JSON 表达式的前缀。
     */
    JSON_PREFIX : '__json__:',

    /*
     *  从当前文档的连接（href）中读取所有参数
     *  @return  参数 Map，如果参数为多个值，则返回的是数组。
     */
    parse : function (href) {
        if (href == null) {
            return {};
        }

        // 去掉后缀（#）
        var segs = href.split('#');
        var cleanHref = href;
        if (segs.length > 0) {
            cleanHref = segs[0];
        }

        // alert('href=' + href);
        var args  = cleanHref.split("?");
        var retVal = {};

        /*参数为空*/
        if(args[0] == cleanHref) {
            return retVal; /*无需做任何处理*/
        }
        var str = args[1];
        args = str.split("&");
        for(var i = 0; i < args.length; i ++) {
            str = args[i];
            var arg = str.split("=");
            if(arg.length <= 1) continue;
            var name  = arg[0];
            // TODO: 此处存在一个限制，普通参数不能以“__json__:”开头
            var _val = decodeURI(arg[1]);
            var value = (new RegExp('^' + ParameterHelper.JSON_PREFIX + '.+')).test(_val) ?
            JSON.parse(_val.substring(ParameterHelper.JSON_PREFIX.length))
            : _val;  // 值可能是复杂的，比如：JSON 表达式
            // MessageBox.debug('name=' + name + '; value=' + value);
            if (retVal[name] != null) {
                // 处理多值
                if (retVal[name] instanceof Array) {
                    retVal[name].push(value);
                } else {
                    var values = [];
                    values.push(retVal[name], value);
                    retVal[name] = values;
                }
            } else {
                retVal[name] = value;
            }
        }
        return retVal;
    },

    build : function (params) {
        if (params == null) {
            return '';
        }
        var paramsStr = '';
        jQuery.each(params, function (name, value) {
            if (value != null) {
                var _value = value;
                if (typeof value === 'object') {
                    _value = ParameterHelper.JSON_PREFIX + JSON.stringify(value);
                }
                // 进行编码
                paramsStr += '&' + encodeURI(name) + '=' + encodeURI(_value);
            }
        });
        return paramsStr;
    },

    /**
     * 在指定的 URL 后面缀上参数。
     */
    append : function (url, params) {
        var ret = url || '';
        if (ret.indexOf('?') < 0) {
            ret += '?_null_=0';  // 占位符
        }
        return (ret + this.build(params));
    }

}


/**
 * 日期格式化函数，用于将日期类型转换为指定格式。指定的“Pattern”是“java.text.SimpleDateFormat”
 * 的一个子集。注意：本算法只是采用简单的正则表达式替换，因此可能重复（错误）的情况。
 * 目前，此函数只支持格式化，不支持解析。
 */
var DateFormat = {
    replaceSingleQuotation : true,  // 是否替换掉模板中的单引号

    months : ['一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月'],

    // 注意：patternNames 和 patterns 要完全一致
    patternNames : ['yyyy',  'yy', 'MMM',  'MM', 'M', 'dd', 'd',
    'HH', 'H', 'mm', 'm',  'ss', 's'],
    patterns : {
        'yyyy'  : '1970',
        'yy'    : '01',
        'MMM'   : '一月',
        'MM'    : '01',
        'M'     : '1',
        'dd'    : '01',
        'd'     : '1',
        'HH'    : '00',  // 24 小时进制
        'H'    : '0',    //
        'mm'    : '00',
        'm'     : '0',
        'ss'    : '00',
        's'     : '0'
    },

    format : function(date, patternStr) {
        if (patternStr == null || patternStr == '') {
            // 使用系统默认的模式
            patternStr = SystemConfiguration.read('format.date', 'yyyy-MM-dd');
        }
        if (date == null) {
            return null;
        }

        // 同步当前日期
        this.patterns['yyyy'] = date.getFullYear();
        this.patterns['yy']   = date.getFullYear() % 100 < 10 ? '0' + date.getFullYear() % 100 : date.getFullYear() % 100;
        var month = date.getMonth() + 1;
        this.patterns['MMM']  = this.months[date.getMonth()];
        this.patterns['MM']   = month < 10 ? '0' + month : month;
        this.patterns['M']    = month;
        this.patterns['dd']   = date.getDate() < 10 ? '0' + date.getDate() : date.getDate();
        this.patterns['d']    = date.getDate();
        this.patterns['HH']   = date.getHours() < 10 ? '0' + date.getHours() : date.getHours();        // 24 小时进制
        this.patterns['H']   =  date.getHours() ;
        this.patterns['mm']   = date.getMinutes() < 10 ? '0' + date.getMinutes() : date.getMinutes();
        this.patterns['m']    = date.getMinutes();
        this.patterns['ss']   = date.getSeconds() < 10 ? '0' + date.getSeconds() : date.getSeconds();
        this.patterns['s']    = date.getSeconds();

        var formatedStr = patternStr;
        var _patterns = this.patterns;
        jQuery.each(this.patternNames, function(index, regexp){
            var value = _patterns[regexp];
            if (value == null) {
                throw new Error("No value of Pattern[" + regexp + "].");
            }
            formatedStr = formatedStr.replace(regexp, value);
        });

        // 替换单引号部分
        while (this.replaceSingleQuotation && /[']/.test(formatedStr)) {
            formatedStr = formatedStr.replace("'", "");
        }

        return formatedStr;
        },

        /**
         * 根据指定模式解析字符串。
         * @param value 被解析的数据
         * @param patternStr 模式字符串，如果为空，则使用系统默认模式
         */
        parse : function (value, patternStr) {
            if (value == null || value === '') {
                return null;
            }
            if (patternStr == null || patternStr == '') {
                // 使用系统默认的模式
                patternStr = SystemConfiguration.read('format.date', 'yyyy-MM-dd');
            }

            var year = 1970;
            var month = 1;
            var day   = 1;
            var hour  = 0;
            var minute = 0;
            var second = 0;
            // 暂时忽略毫秒

            var _pattern = '';
            var valueIndex = 0;  // 被解析值所在的位置
            for (var i = 0; i < patternStr.length; i++) {
                var _char     = patternStr.charAt(i) || patternStr[i];  // 当前char
                _pattern += _char;
                var _longPattern = _pattern;  // 尽量使用长匹配
                if (i < patternStr.length - 1) {
                    _longPattern += (patternStr.charAt(i + 1) || patternStr[i + 1]);
                    if (this.isPartMatched(_longPattern)) {
                        continue;  // 继续匹配
                    }
                }
                // 结尾没匹配时有点问题
                if (this.patterns[_pattern] == null) {
                    if (!(this.replaceSingleQuotation && _pattern == "'")) {
                        // 当作格式化字符处理
                        valueIndex += _pattern.length; // 向后移动指针
                    }
                    _pattern = '';
                    continue;
                }

                // 短匹配
                var shortMatched = (_pattern.length < 2);
                // 在短模式的情况下（'M','d','H','m', 's'）可能产生变长，此时需要用此标记进行控制
                var shift = 0;
                if (_pattern === 'MMM') {
                    shift = this.getMonthShift(value, valueIndex);
                } else if (shortMatched && valueIndex < value.length - 1) {
                    // 一般情况下，要求短模式必须有分隔符，否则将造成解析错误
                    if (/^\d$/.test(value.charAt(valueIndex + 1) || value[valueIndex + 1])) {
                        shift = 1;
                    }
                }
                var endIndex = valueIndex + _pattern.length + shift;
                // MessageBox.debug('endIndex=' + endIndex + "; valueIndex=" + valueIndex);

                if (endIndex >= valueIndex.length) {
                    throw new Error('日期格式解析错误。');
                }

                var _matchedValue = value.substring(valueIndex, endIndex);  // 当前匹配的值
                // MessageBox.debug('_matchedValue=' + _matchedValue + "; _pattern=" + _pattern);
                switch (_pattern) {
                    case 'yyyy':
                        year = this.getInt(_matchedValue, _pattern, 9999, 'year');
                        break;
                    case 'yy':
                        year = 2000 + this.getInt(_matchedValue, _pattern, 99, 'year');
                        break;
                    case 'MMM':
                        month = this.months.indexOf(_matchedValue);
                        break;
                    case 'MM':
                        month = this.getInt(_matchedValue, _pattern, 13, 'month') - 1;
                        break;
                    case 'M':
                        month = this.getInt(_matchedValue, _pattern, 13, 'month') - 1;
                        break;
                    case 'dd':
                        day = this.getInt(_matchedValue, _pattern, 31, 'day');
                        break;
                    case 'd':
                        day = this.getInt(_matchedValue, _pattern, 31, 'day');
                        break;
                    case 'HH':
                        hour = this.getInt(_matchedValue, _pattern, 24, 'hour');
                        break;
                    case 'H':
                        hour = this.getInt(_matchedValue, _pattern, 24, 'hour');
                        break;
                    case 'mm':
                        minute = this.getInt(_matchedValue, _pattern, 60, 'minute');
                        break;
                    case 'm':
                        minute = this.getInt(_matchedValue, _pattern, 60, 'minute');
                        break;
                    case 'ss':
                        second = this.getInt(_matchedValue, _pattern, 60, 'second');
                        break;
                    case 's':
                        second = this.getInt(_matchedValue, _pattern, 60, 'second');
                        break;
                    default :
                        throw new Error ('The pattern [' + _pattern + '] can not be parsed.');
                }
                valueIndex += _matchedValue.length; // 向后移动指针
                _pattern = '';
            }  // end of for

            return new Date(year, month, day, hour, minute, second);
        },

        /**
         * 返回一个整数，如果此数非整数，则返回一个异常
         */
        getInt : function(_int, patternName, max, errorMsg) {
            // 去掉前导零
            if (_int.length > 0 && _int.substring(0, 1) === '0') {
                _int = _int.substring(1);
            }
            if (isNaN(_int)) {
                throw new Error('The segment[' + _int + '] corresponding to ['
                    + patternName + '] must be integer.');
            }
            var val = parseInt(_int);
            // 集中校验
            if (val >= max) {
                throw new Error('The ' + errorMsg +' is bad, it must be less than ' + max + '.');
            }
            return val;
        },

        /**
         * 计算一个模式串是否是部分匹配
         */
        isPartMatched  : function(pattern) {
            if (pattern == null) {
                return false;
            }
            if (this.patterns[pattern] != null) {
                return true;
            }
            for (var i = 0; i < this.patternNames.length; i++) {
                if (pattern.length < this.patternNames[i].length
                    && this.patternNames[i].substring(0, pattern.length) === pattern) {
                    return true;
                }
            }
            return false;
        },

        /**
         * 返回中文月份所占的字节数与"MMM"的差值
         */
        getMonthShift : function (value, valueIndex) {
            var month = value.substring(valueIndex, valueIndex + 2);
            if (this.months.indexOf (month) >= 0) {
                return -1;
            } else if (value.length > valueIndex + 2) {
                // 尝试3个字符的情况
                month = value.substring(valueIndex, valueIndex + 3);
                if (this.months.indexOf (month) >= 0) {
                    return 0;
                }
            }
            throw new Error('The format of month is bad.');
        }
    }

    /**
 * 数字格式化函数。用于将数值类型转换为指定格式。指定的“Pattern”是“java.text.SimpleDateFormat”
 * 的一个子集。注意：本算法只是采用简单的正则表达式替换，因此可能重复（错误）的情况。
 * 目前，此函数只支持格式化，不支持解析。
 */
    var NumberFormat = {
        sep : ',',  // 分隔符，用于确定整数部分按照多少位进行分割
        exp : '.',  // 定位符，用于确定小数点后面保留几位小数

        format : function(value, patternStr) {
            if (value == null) {
                value = 0;
            } else {
                value = new Number(value);
            }
            if (patternStr == null || patternStr == '') {
                // 使用根据数值的类型自动判断格式化的模式（整数或者浮点数）
                patternStr = (parseInt(value,10)===value) ?
                (SystemConfiguration.read('format.int') || '#,##0') :
                (SystemConfiguration.read('format.float') || '#,##0.00');
            }
            // 计算分隔符和定位符的位置
            var expIndex = patternStr.indexOf(this.exp);
            var sepIndex = patternStr.indexOf(this.sep);
            if(sepIndex > -1 ) {
                if (expIndex > -1) {
                    sepIndex = expIndex - sepIndex - 1;  // 要减去其本身所占的位置
                } else {
                    sepIndex = patternStr.length - sepIndex - 1;
                }
            }

            if (expIndex > -1) {
                expIndex = patternStr.length - expIndex - 1;  // 要减去其本身所占的位置
            }

            var valueSegs = ('' + value).split(this.exp);  // 将整数转换为字符串形式并切分为：整数部分和小数部分
            var intSeg        = valueSegs[0];
            var result = '';
            if (sepIndex > 0) {
                for (var i = intSeg.length - 1; i >=0; i--) {
                    // MessageBox.debug('charAt(i)=' + charAt(i));
                    result = (intSeg.charAt(i) || intSeg[i]) + result;   // IE 使用charAt 方法, FF 使用"数组方式";
                    if (i > 0 && (intSeg.length - i) % sepIndex == 0) {
                        result = this.sep + result;
                    }
                }
            } else {
                // 无分界符则原样输出
                result = intSeg;
            }
            var fractionalSeg = '';
            if (valueSegs.length > 1) {
                fractionalSeg = valueSegs[1];
            }
            if (expIndex > 0) {
                result += this.exp;
                for (var j = 0; j < expIndex; j++) {
                    if (j < fractionalSeg.length) {
                        result += (fractionalSeg.charAt(j) || fractionalSeg[j]);  // IE 使用charAt 方法, FF 使用"数组方式";
                    } else {
                        result += '0';
                    }
                }
            }
            return result;
        },

        /**
         * 根据模式自动判断解析的方法，如果模式字符串为空，或者含有小数点“.”，
         * 则使用 parseFloat 进行解析；否则使用parseInt进行解析。
         * @param value 被解析的值
         * @param pattern 匹配模式
         */
        parse : function (value, pattern) {
            if (value == null || value.length == 0) {
                return 0;
            }
            if (pattern == null || pattern.indexOf('.') >= 0) {
                return this.parseFloat(value);
            }
            return this.parseInt(value);
        },

        /**
     * 将值转换为整数型数值（包括长整型）。
     */
        parseInt : function (value) {
            if (value == null || value == '') {
                return null;
            }
            var _intValue = '';
            for(var i = 0; i < value.length; i++) {
                // 剔除非字符
                var _char = value[i] || value.charAt(i);
                if (_char === '.') {
                    // 遇到小数点则抛弃后面的数据
                    break;
                }
                if (/[+\-\d]/.test(_char)) {
                    _intValue += _char;
                }
            }
            return parseInt(_intValue);
        },

        /**
     * 将值转换为浮点型数值（包括双精度）。
     */
        parseFloat : function (value, patterStr) {
            if (value == null || value == '') {
                return null;
            }
            var _intValue = '';
            for(var i = 0; i < value.length; i++) {
                // 剔除非字符
                var _char = value[i] || value.charAt(i);
                if (/[+\-\d.]/.test(_char)) {
                    _intValue += _char;
                }
            }
            return parseFloat(_intValue);

        }
    }

    /**
 * ID生成器，用于产生页面唯一的ID。
 */
    var IdGenerator = {
        /**
     * ID 的内部编码
     */
        id : 0,

        /**
     * 返回一个页面唯一的 ID。
     */
        getId : function () {
            this.id += 1;
            return this.id;
        }
    }

    Event = function (source, name) {
        this.source = source;
        this.name   = name;  // 事件的名称，这个名称很重要，需要按照名称调用相应的事件。
    }

    /**
 * 用于事件处理的接口
 */
    Observable = function () {
        }

    jQuery.extend(Observable.prototype, {

        /**
     * 注册监听器；如果注册的监听器为空，则不进行如何处理。
     */
        addListener : function(listener) {
            if (this.listeners == null) {
                this.listeners = [];
            }
            if (listener != null) {
                this.listeners.push(listener);
            }
        },

        /**
     * 同：addListener。
     */
        on : this.addListener,

        /**
     * 移除监听器；如果指定的监听器为空，则不进行如何处理。
     */
        removeLinstener : function(listener) {
            if (listener == null || this.listeners == null) {
                return;
            }
            var removed = -1;
            for (var i = 0; i < this.listeners.length; i++) {
                if (listener == this.listeners[i]) {
                    removed = i;
                }
            }
            if (removed > -1) {
                this.listeners.splice(removed, 1);
            }
        },

        /**
     * 同：removeLinstener。
     */
        un : this.removeLinstener,

        /**
     * 触发一个事件。
     */
        fire : function (event) {
            if (this.listeners == null) {
                return;
            }
            for (var i = 0; i < this.listeners.length; i++) {
                var listener = this.listeners[i];
                var method = "on" + event.name;
                if (listener[method] != null) {
                    listener[method](event);
                }
            }
        /**
        var _event = event;
        jQuery.each(this.listeners, function(i, listener) {
            var method = "on" + _event.name;
            if (listener[method] != null) {
                listener[method](_event);
            }
        });*/
        }
    });


    var ControlUtils = {
        /**
     * 返回指定容器下所有的已经命名的可编辑控件
     * @ containerId 容器（一般是 div、form 等控件）的ID
     */
        getNamedComp : function (selector) {
            var comps = {};
            if (selector == null) {
                return comps;
            }
            var container = _createJQuery(selector);
            jQuery.each(EDITABLE_CONTROL_TAG, function (index, ctrlName) {
                container.find(ctrlName).each(function() {
                    var _name = this.name;  // 控件的名称
                    if (_name != null) {
                        comps[_name] = this;
                    }
                });
            });
            return comps;
        },

		/**
		  * 使用对象数组中创建下拉列表框
		  * @param id    comboBox 对象的ID
		  * @param objs 对象数组
		  * @param {}    其他相关参数，其中，需要可定义的参数有：
		  *                       1. value, 从对象中取值的属性名，用作选项（或者字典）的值
		  *                       2. title, 从对象中取标题的属性名，用作选项（或者字典）的显示标题
		  *                       3. nullable, 是否允许为空，布尔类型，默认为“false”。
		  *                       4. nullTitle, 允许为空时显示的标题。
		  *                       5. nullValue, 允许为空时，空项对应的值。
		  */
		createComboBox : function(id, objs, params) {
		    if (id == null || objs == null || params == null) {
			    MessageBox.showError('创建下拉列表框的参数均不能为空。');
				return;
			}
			var dicName = '_dicName_' + IdGenerator.getId();  // 自动产生一个 ID

			// 将对象转换为字典项
			var items = [];

			// MessageBox.debug('objs=' + objs);
			for(var i = 0; i < objs.length; i++) {
			    var obj = objs[i];
			    var item = {index : i, value : obj[params.value],
				   title : obj[params.title]};
				items.push(item);
			}
			// MessageBox.debug('items=', items);
		    DictionaryService.register(dicName, items);

			//MessageBox.debug('params[nullable]=' + params['nullable']);
			// 使用字典方法构建下拉列表框
			DicUtils.decorateComboBox(id, dicName,
			    params['nullable'] || false, params['nullValue'], params['nullTitle']);
        }
    }

    /*** Lookup(查找) 相关代码  **/
    var LookupUtils = {

        /**
     * 查询表对话框的页面，这个页面提供了一个基础框，需要用来填写选择数据。
     */
        lookupTableDlg : 'lookup_dialog.html',

        /**
     * 绑定单一控件
     * @param lookupBtn 查询按钮实例，不能为空。如果是字符串，则认为其是“ID”，也可以是其他合法的jQuery对象。
     * @param url       嵌入的URL实例，不能为空。
     * @param params    参数表实例，可能为空。
     */
        bindInput : function(lookupBtn, url, params) {
            if (lookupBtn == null || lookupBtn === '' || url == null) {
                throw new Error("Parameters lookupBtn(1) and url(2) can not be empty.");
            }

            // 程序员可以在自定义查找页面的模板，也可以使用默认模板
            var _url = params['lookupTemplate'] || this.lookupTableDlg;

            if (_url.indexOf('?') < 0) {
                _url += '?__null__=0';  // 查询中没有条件的话，则使用一个占位符。
            }

            if (params == null) {
                params = {};
            }
            // 追加 Lookup 数据装载参数。
            params['lookupUrl'] = url;

            var paramsStr = ParameterHelper.build(params);

            _url += paramsStr;
            var width  = params['width']  != null ? params['width'] : 600;     // 默认宽度是：600
            var height = params['height'] != null ? params['height'] : 450;    // 默认高度是：450
            _createJQuery(lookupBtn).bind("click", function(){
                showDialog(_url, true, width, height);
            });
        },

        init : function () {
            // 解析参数
            var href = document.location.href;
            var params = ParameterHelper.parse(href);

            // 装载页面

            // 注入参数
            if (lookupInit) {
                lookupInit(params);
            }
        }
    }

    /**
 * 这是一个用内部约定创建的jQuery对象。
 * 如果参数是字符串，则认为其为控件的“ID”，返回此“ID”代表的jQuery对象；其他则直接将其包装为jQuery对象。
 */
    var _createJQuery = function (queryDom) {
        if(queryDom == null) {
            return null;
        }
        if (typeof queryDom === 'string' && queryDom.charAt(0) != '#') {
            queryDom = '#' + queryDom;
        }
        return jQuery(queryDom);
    }


    /*** Lookup(查找) 相关代码  **/

    function showDialog(url, model, width, height) {
        if (model && false /* jQuery.browser.msie */) { // IE 下还有点问题
            var returnValues = window.showModalDialog(url, 'popup', 'directories=no, scrollbars=1, location=no, '
                + 'menubar=no, status=no, toolbar=no, center=yes  dialogWidth=' + width + ', dialogHeight='
                + height + ', dialogTop=200, dialogLeft=200');
        } else {
            dialog = window.open(url, 'popup', 'directories=no, scrollbars=1, location=no,'
                + 'menubar=no, status=no, toolbar=no, width='
                + width + ', height=' + height + ', top=200, left=200');
            dialog.focus();
        }
    }

    /**
 * 存放一个向导页的相关信息。
 * @param name 向导页的名称，必须唯一。
 * @param url 向导页的地址，如果是指定的页面内的某个区域，必须以“#”开头。
 * @param params 参数表。
 */
    function WizardPage(name, url, params) {
        this.name   = name;
        this.url    = url;
        this.params = params;
    }

    /**
 * 创建向导的相关类。
 */
    var Wizard = function() {

    }

    Wizard.prototype  = {

        /**
     * 显示区域的编码
     */
        displayAreaName : '__wizardArea',

        /**
     * 向导的模板文件
     */
        wizardTemplate : __contextPath__ + 'gt_base/wizard_frm.html',

        pages : [],

        /**
     * 当前显示的页面
     */
        currentPage : null,

        /**
     * 增加加入一个向导页面
     * @param name 向导页的名称
     * @param url  向导页的链接地址（或者 div 的id）。
     * @param params  页面参数。
     */
        addPage : function (name, url, params) {
            if (name == null || url == null) {
                throw new Error("Parameters name(1) and url(2) can not be empty.");
            }
            this.pages.push(new WizardPage(name, url, params));
        },

        /**
     * 判断指定页之后是否还有下一页数据需要填写
     */
        hasNextPage : function(name) {

        },

        /**
     * 判断指定页之前是否存在上一页信息。
     */
        hasPrevPage : function(name) {

        },

        nextPage : function () {
            if (this.pages == null || this.pages.length == 0) {
                throw new Error("Wizard need some pages.");
            }
            if (this.currentPage == null) {
                this.currentPage = this.pages[0];
            } else {
                for (var i = 0; i < this.pages.length; i++) {
                    if (this.currentPage['name'] == this.pages[i]['name']
                        && i < (this.pages.length - 1)) {
                        this.currentPage = this.pages[i + 1];
                        break;
                    }
                }
            }
            return this.currentPage;
        },

        /**
     * 向导初始化操作
     */
        init : function(containerId) {
            var _pages = this.pages;
            var _cp =  this.currentPage;
            jQuery(containerId).load(this.wizardTemplate, {}, function () {
                //Wizard.pages = _pages;  // 注意：此处要重新赋值
                //Wizard.currentPage = _cp;
                nextPage();  // 调用wizard.html页面的nextPage()方法
            });
        },

        prevPage : function () {
            if (this.pages == null || this.pages.length == 0) {
                throw new Error("Wizard need some pages.");
            }
            if (this.currentPage == null) {
                this.currentPage = null;
            } else {
                for (var i = 0; i < this.pages.length; i++) {
                    if (this.currentPage['name'] == this.pages[i]['name']
                        && i > 0) {
                        this.currentPage = this.pages[i - 1];
                        break;
                    }
                }
            }
            return this.currentPage;
        }
    }

    /**
 * 帮助类
 */
    var Utils  = {

        /**
     * 将源对象的所有属性值拷贝到目标对象中，只拷贝属性值，不拷贝功能。
     * @param source 源对象，如果为空，不进行任何处理
     * @param dest   目标对象，如果为空，不进行任何处理
     * @param propertyNames  属性集合，指定复制的属性名称，注意，因为复制是递归的，所以对子对象亦生效。
     */
        valueCopy : function (source, dest, propertyNames) {
            if (source == null || dest == null) {
                return;
            }
            for (var prop in source) {
                var type = typeof source[prop];
                //MessageBox.debug('prop=' + prop + ";type=" + type + ";dest[prop]=" + dest[prop] + ";source[prop]=" + source[prop]);
                if (type === 'function'
                    || (propertyNames != null && jQuery.inArray(prop, propertyNames))) {
                    continue;
                }
                if (type === 'object') {
                    if (dest[prop] != undefined) {
                        Utils.valueCopy(source[prop], dest[prop]);
                    } else {
                        dest[prop] = source[prop];
                    }
                } else {
                    dest[prop] = source[prop];
                }
            }
        },

        /**
     * 判断当前鼠标的位置是否在指定的控件之上
     */
        isMouseOver : function (mouseEvent, elem) {
            if(mouseEvent == null || elem == null) {
                return false;
            }
            var posi = jQuery(elem).position();
            var width = jQuery(elem).width();
            var height = jQuery(elem).height();
            if (width == 0 || height == 0) {
                return false;
            }

            return (mouseEvent.clientX >= posi.left && mouseEvent.clientX <= posi.left + width
                && mouseEvent.clientY >= posi.top && mouseEvent.clientY <= posi.top + height);
        },

        /**
     * 用于运行应用的代码, 用此运行代码的好处是:
     *     1. 自动进行了文档准备处理(jQuery.ready)
     *     2. 自动捕获并显示异常
     */
        run : function (runnable, onFinally) {
            if (runnable == null) {
                return;
            }
            if (!jQuery.isFunction(runnable) || (onFinally && !jQuery.isFunction(onFinally))) {
                MessageBox.showError('系统错误', '参数必须是函数。');
            }

            jQuery().ready(function(){  // TODO: 在弹出对话框时有问题, 不能执行该方法
                try {
                    runnable();
                } catch(e) {
                    if (!e.consumed) {
                        var sMsg = e.description;
                        //MessageBox.debug("sMsg=", sMsg);
                        // TODO: 这部分代码和 boot.js 里的部分代码有重叠
                        if (sMsg != null && sMsg.startsWith(ErrorManager.prefix)) {
                            var _error = ErrorManager.errors[sMsg];
                            var msg = sMsg;
                            var detail = null;
                            if (_error) {
                                var _fe = ErrorUtils.findFriendlyException(_error);
                                msg = _fe.message;
                                detail = _fe.stackTrace;
                            }
                            MessageBox.showError(msg, detail);
                        } else {
                            MessageBox.showError(sMsg, e);
                        }
                    }
                // throw e; 错误不再继续处理
                } finally {
                    if (onFinally) {
                        onFinally();
                    }
                }
            });
        },

        /**
     * 创建URL , 在输入的url上加上绝对路径。
     */
        createUrl : function (url) {
            var cp = ContextPath.getPath();
            return cp ? cp + url : url;
        }
    }

    var UploadedFile = function (_input) {
        this.__className = 'UploadedFile'; // 固定的类名称
        if (_input == null) {
            // throw new Error("文件输入控件不能为空。");
            throw new Error("Input file control can not be null.");
        }
        this.input = _input = _createJQuery(_input);

        this.propertyName = this.input.attr('name');
        if (this.propertyName == null) {
            throw new Error("The name of uploaded file control can not be null.");
        }
        this.ajaxUpload = new AjaxUpload(this.input, {
            action: ServiceInvoker.action,
            autoSubmit: false,
            name : this.propertyName,
            responseType: 'json',
            onChange : function(file, extension){
                _input.val(file);
            }

        });

        /**
     * 重新设置状态
     */
        this.reset = function () {
            this.input.val('');
            this.ajaxUpload.reset();
        }

        /**
     * 清空当前输入域的值
     */
        this.resetValue = function() {
            this.input.val('');
        }
    }

    var MessageBox = {
        _errorDialog   : null,  // 错误消息对话框
        _msgCtrl       : null,  // 错误消息控件
        _detailMsgCtrl : null,  // 详细信息控件

        debug : function () {
            var ret = '';
            for (var i = 0; i < arguments.length; i++) {
                var arg = arguments[i];
                ret += this._stringify(arg);
            }
            alert(ret);
        },

        /**
     * 这个方法将使用递归方式，输出对象及其所有属性的字符串表示。如果是原生对象（非object），则直接输出其值；
     * 如果是数组，则以“[]”形式输出其值；如果是对象，以“{}”形式输出其值。
     */
        _stringify : function (obj) {
            var ret = '';
            if (obj == null) {
                ret = 'null';
            } else if (typeof obj == 'object') {
                ret += '{'
                for(var prop in obj) {
                    ret += '' + prop + ':' + MessageBox._stringify(obj[prop]) + ';';
                }
                ret += '}'
            } else if (obj instanceof Array) {
                ret += '['
                for(prop in obj) {
                    ret += MessageBox._stringify(obj[prop]) + ',';
                }
                ret += ']'
            }else {
                ret += obj;
            }
            return ret;
        },

        /**
     * 需要用户进行确认的信息。
     * @return 确认返回true，否则返回false。
     */
        confirm : function (message) {
            return confirm(message);
        },

        /**
     * 给用户显示一个提示性的信息，一般用于显示不是特别重要的通知，如：保存成功等情况，
     * 以不影响用户继续操作为宜。
     */
        inform : function (message) {
            alert (message);
        },

        /**
     * 提示给用户的警告信息，一般需要用户确认才能继续处理。
     */
        alert : function (message) {
            alert(message);
        },

        /**
     * 显示错误信息
     */
        showError : function (msg, detailMsg) {
            if (jQuery) {
                if (this._errorDialog == null) {
                    this._errorDialog = this._createDialog();
                } else {
                    this._errorDialog.dialog( 'open' );
                }
                msg       = '' + msg;      // 需要转换为字符串
                detailMsg = '' + detailMsg;
                this._msgCtrl.text(msg);
                this._detailMsgCtrl.text(detailMsg);
            } else {
                var _msg = '系统发生异常: \n' + msg;
                if (detailMsg != null) {
                    _msg += '\n 详细信息:' + detailMsg;
                }
                // _msg += '\n URL:' + sUrl + '\nlineNumber:' + sLine;
                alert(_msg);
            }
        },

        _createDialog : function () {
            var _dialog = jQuery("<div style='width:800px; height:400px;' > </div>");
            _dialog.attr('title', '异常信息');


            this._msgId = '_msg_' + IdGenerator.getId();
            // 提示信息
            var msgDiv = jQuery("<div><p>错误信息如下：</p>" +
                // '<p> 地址:' + sUrl + ' 行号:' + sLine + "</p>" +
                "</div>");
            this._msgCtrl = jQuery("<p id='" + this._msgId + "'></p>");
            this._msgCtrl.appendTo(msgDiv);
            _dialog.append(msgDiv);

            this._detailMsgId = '_detailMsg_' + IdGenerator.getId();
            var id = '_dialog_error_' + IdGenerator.getId();
            var detail = jQuery("<div> " +
                "<a href='#' onclick=\"jQuery('#" + id + "').toggle('blind',{},500);\"" +
                "'>详细信息</a>" +
                "</div>");
            this._detailMsgCtrl = jQuery("<div id='" + id + "' style='overflow:scroll; width:100%; height:300px;display:none'></div>" );
            this._detailMsgCtrl.appendTo(detail);
            detail.insertAfter(msgDiv);

            _dialog.dialog({
                modal : true,
                width: 500,
                height : 280,

                buttons: {
                    Ok: function() {
                        $(this).dialog('close');
                    }
                }
            });
            return _dialog;
        }

    }

    /**
 * Boolean 类型的转换器。
 */
    var BooleanConverter = {
        convert : function (value) {
            if (value == null) {
                return false;
            }
            // 待检查
            if (value['__className'] == 'java.lang.Boolean' ) {
                return 'true' == value; //
            }
            return !!value;
        }
    }

    /**
 * 日期类型的包装器，主要是将Java的日期类型（java.sql.Date,java.util.Date,java.util.timestamp,java.util.Calendar）
 * 转换为对应的日期类型。
 */
    var DateConverter = {
        /**
     * 根据当前值，返回其可转换的 日期类型的值，如果参数为空，则返回空。
     * 如果传入的参数类型不能识别，则抛出“不能识别”的类型异常。
     */
        convert : function (value) {
            if (value == null) {
                return null;
            }
            // 待检查
            var date = new Date();
            var __className = value['__className'];
            if (__className === 'java.util.Date' || __className === 'java.sql.Date'
                || __className === 'java.sql.Timestamp') {
                date.setTime(parseInt(value['time']));
            } else if (__className === 'java.util.Calendar') {
                date.setTime(value);
            } else if (!isNaN(value)){
                // 是数值的话，则认为其是时间值
                date.setTime(new Number(value));
            } else if (typeof value === 'string') {
            //TODO: 尝试用日期格式进行解析
            } else {
                throw new Error("The type[" + value + "] can not be converted to date.");
            }
            return date;
        }
    }

    /**
 * 数值类型的转换器，主要是将Java包装类转换为对应的JavaScript的Number类型。
 */
    var NumberConverter = {

        /**
     * 根据当前值，返回其可转换的 日期类型的值，如果参数为空，则返回空。
     * 如果传入的参数类型不能识别，则抛出“不能识别”的类型异常。
     */
        convert : function (value) {
            if (value == null) {
                return null;
            }
            // 待检查
            var ret = 0;
            var __className = value['__className'];
            if (__className === 'java.lang.Integer' || __className === 'java.lang.Long'
                || __className === 'java.lang.Short' || __className === 'java.math.BigInteger') {
                ret = parseInt(value['value']);
            } else if (__className === 'java.lang.Float' || __className === 'java.lang.Double'
                || __className === 'java.util.BigDecimal') {
                ret = parseFloat(value['value']);
            } else {
                throw new Error("Can not know the type to date.");
            }
            return ret;
        }
    }

    var ConverterFactory = {
        /**
     * 已注册的所有转换器
     */
        converters : {
            'java.lang.Boolean'  :  BooleanConverter,
            'java.util.Date'     :  DateConverter,
            'java.sql.Date'      :  DateConverter,
            'java.sql.Timestamp' :  DateConverter,
            'java.util.Calendar' :  DateConverter,
            'java.lang.Long'     :  NumberConverter,
            'java.lang.Integer'  :  NumberConverter,
            'java.lang.Short'    :  NumberConverter,
            'java.math.BigInteger' :  NumberConverter,
            'java.lang.Float'      :  NumberConverter,
            'java.lang.Double'     :  NumberConverter,
            'java.util.BigDecimal' :  NumberConverter
        },

        getConverter : function (type) {
            if (type == null) {
                return null;
            }
            return this.converters[type];
        },

        /**
     * 注册一个类型转换器
     */
        register : function (type, converter) {
            if (type == null || converter == null) {
                throw new Error("Parameters type[1] and converter[2] can not be null.");
            }
            this.converters[type] = converter;
        }
    }



    /**
 * 格式化接口，用于处理数据显示时的格式化。此接口必须实现两个方法：
 * format(_class, name, value) : 将实际值转换为对应的显示值，返回值为显示数据。
 * parse(_class, name, value)  : 将显示值转换为相应的实际值，显示值为实际数据。
 */
    function Formater () {
    }

    Formater.prototype.format = function (value) {}

    Formater.prototype.parse = function (value) {}


    var FormaterUtils = {
        getAnnotation : function (_class, _name) {
            var formatAnno = null;
            if (_class != null &&  _name != null) {
                formatAnno = AnnotationHelper.getFieldAnnotation(_class, _name, 'Formater');
            }
            var patternStr = null;
            if (formatAnno != null) {
                patternStr = formatAnno.value;
            }
            return patternStr;
        }
    }
    /**
 * 对日期类型进行格式化。
 * @param _class Java 类型（这个类型上可能定义了标注）
 * @param _name  此值对应的属性名称
 */
    var DateFormater = {
        /**
     * 对值进行格式化
     */
        format : function (_class, _name, value) {
            if (value == null) {
                return null;
            }
            var ret = null;
            if (typeof value === 'string') {
                ret = value;
            } else {
                var patternStr = FormaterUtils.getAnnotation(_class, _name);
                if (value instanceof Date) {
                    ret = DateFormat.format(value, patternStr);
                } else if (!isNaN(value)) {
                    ret = new Date();
                    ret.setTime(value);
                    ret = DateFormat.format(value, patternStr);
                } else {
                    throw new Error('The type of value must be Date, string or number.');
                }
            }
            return ret;
        },

        /**
     * 对值进行解析
     */
        parse : function (_class, _name, value) {
            // MessageBox.debug('parse value', value)
            if (value == null) {
                return null;
            }
            var ret = null;
            if (value instanceof Date) {
                ret = value;
            } else if (typeof value === 'string') {
                var patternStr = FormaterUtils.getAnnotation(_class, _name);
                ret = DateFormat.parse(value, patternStr);
            } else if (!isNaN(value)) {
                ret = new Date();
                ret.setTime(value);
            } else {
                throw new Error('The type of value must be Date, string or number.');
            }
            return ret;
        }
    }

    /**
 * 对数字类型进行格式化。
 * @param _class Java 类型（这个类型上可能定义了标注）
 * @param _name  此值对应的属性名称
 */
    var NumberFormater /* implements Formater */ = {
        format : function (_class, _name, value) {
            if (value == null) {
                return null;
            }
            var ret = null;
            if (!isNaN(value)) {
                ret = value;
            } else if (typeof value === 'string') {
                var patternStr = FormaterUtils.getAnnotation(_class, _name);
                NumberFormat.parse(value, patternStr);
            } else {
                throw new Error('The type of value must be string or number.');
            }
            return ret;
        }
    }

    /**
 * 格式化转换器工厂，根据工厂的类型对数据进行格式化处理。
 */
    var FormaterFactory = {

        /**
     * 已注册的所有转换器
     */
        formaters : {
            // 'java.lang.Boolean'  :  BooleanConverter,
            'java.util.Date'       :  DateFormater,
            'java.sql.Date'        :  DateFormater,
            'java.sql.Timestamp'   :  DateFormater,
            'java.util.Calendar'   :  DateFormater,
            'java.lang.Long'       :  DateFormater,
            'java.lang.Integer'    :  DateFormater,
            'java.lang.Short'      :  NumberConverter,
            'java.math.BigInteger' :  NumberConverter,
            'java.lang.Float'      :  NumberConverter,
            'java.lang.Double'     :  NumberConverter,
            'java.util.BigDecimal' :  NumberConverter
        },

        getFormater : function (type) {
            if (type == null) {
                return null;
            }
            return this.formaters[type];
        },

        /**
     * 注册一个类型转换器
     */
        register : function (type, formater) {
            if (type == null || formater == null) {
                throw new Error("Parameters type[1] and formater[2] can not be null both.");
            }
            this.formaters[type] = formater;
        }
    }

/**
 * 根据给定的枚举类型名称，模拟一个字典对象。（注意：枚举名称必须和Java类定义的一致，但是可采用缩写）。
 */
var EnumFactory = {
    /**
     * 根据给定的枚举类型名称，创建一个字典对象。
     * @param enumName 枚举名称，不能为空，必须和Java类定义的一致，但是可采用缩写方式
     */
    create : function (enumName) {
        if (enumName == null) {
            throw new Error ("The name of enum can not be null.");
        }
        var dic = DictionaryService.getDictionary(enumName);
        if (dic == null) {
            throw new Error ("Enum [" + enumName + "] can not be found.");
        }
        var items = dic.items;
        var _enum = {'__className':'java.lang.Enum', 'className' : dic.name};
        for (var i = 0; i < items.length; i++) {
            var item = items[i];
			var _item = {'__className':'java.lang.Enum', 'className' : dic.name}
			_item.name  = item.value;  // 为了和服务器传的值保持一致
			_item.title = item.title;
            _enum[item.value] = _item;
        }
        return _enum;
    },

	/**
	   * 判断两个枚举类型是否相等
	   */
	equals : function (enum1, enum2) {
	    if(enum1 == null || enum1.name == null
    		|| enum2 == null) {
			return false;
		}
		return enum1.name === enum2.name;
	}
}

/**
 * 服务器端返回的异常堆栈：
 * throwable={"message":"asdlad","cause":{"message":"sfasl","__className":"com.xt.gt.html.ExceptionWrapper","cause":{"__className":"com.xt.gt.html.ExceptionWrapper","className":"java.lang.NullPointerException"},"className":"com.xt.core.exception.POException"},"__className":"com.xt.gt.html.ExceptionWrapper","className":"com.xt.core.exception.SystemException"}
 */
var ErrorUtils = {
    /**
     * 对于用户来说，优先级的排序
     */
    friendlyExceptions : ['BusinessException' , 'ServiceException' , 'POException', 'SystemException', 'Exception', 'Throwable'],

    getFriendlyMessage : function (_error) {
        if (_error == null) {
            return null;
        }
        var fe = this.findFriendlyException(_error);
        if (fe != null) {
            return fe.message;
        }
        return "";
    },

    /**
     * 找到用户比较友好的异常。
     */
    findFriendlyException : function (_error) {
        if (_error == null) {
            return null;
        }
        for(var i = 0; i < this.friendlyExceptions.length; i++) {
            var feName = this.friendlyExceptions[i];
            var ew = _error;
            while (ew) {
                // 以给定的类名结尾
                var regexp = new RegExp("^.*" + feName + "$");
                if (regexp.test(ew.className)) {
                     return ew;
                }
                ew = ew.cause;
            }
        }
        return _error;
    }
}

/**
 * 用于管理当前的异常信息。
 */
var ErrorManager = {

    /**
     * 错误编码的前缀，用于区分一般错误和编码错误
     */
    prefix : '_$_',

    /**
     * 所有的异常编码。
     */
    errors : {},

    /**
     * 注册一个异常信息
     */
    register : function(code, msg) {
        if (code != null && msg != null) {
            this.errors[code] = msg;
        }
    }

    //getError : function
}