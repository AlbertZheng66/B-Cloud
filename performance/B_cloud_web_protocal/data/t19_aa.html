/**
  * 这个文件定义了用户校验和登录的相关类。
  */

var _dialog = null;

/**
 * 处理重新登录（当后台重新启动时，使用此方法弹出一个对话框，让用户重新登录）。
 */
function relogin(error, request, response) {
    var reg = /.*com\.xt\.gt\.demo\.aa\.AAException.*/;
    // alert('relogin...reg.test(error)=' + reg.test(error));
    var sMsg = error.description || error.message;
    if (sMsg.startsWith(ErrorManager.prefix)) {
        var _error = ErrorManager.errors[sMsg];
        if (reg.test(_error.className)) {
            if (_dialog == null) {
            _dialog = _createDialog();
        } else {
            _dialog.dialog('open');
        }
        return true;
        }
    } else {
        return false;
    }
}

/**
 * 每次操作之前检查用户是否已经登录，如果未登录，则提示用户进行登录操作
 */
function preLogin (request) {
    // 登录操作不检测
    if ('aa.LoginService' == request.serviceClassName) {
        return;
    }
//    var userName = jQuery.cookie('user_in_cookie');
//    // alert('preLogin userName=' + userName);
//    if (userName == null) {
//        if (_dialog == null) {
//            _dialog = _createDialog();
//        } else {
//            _dialog.dialog('open');
//        }
//        // window.location.href = Utils.createUrl('aa/login.html');
//    }
//    return (userName == null);
//    // throw new Error('com.xt.gt.demo.aa.AAException:用户尚未登录。');
//    alert('preLogin end..........');
}

var _createDialog = function () {
    var html = "<table style='text-align: left; width: 100%; height: 100%;' id='loginPanel' border='0' cellpadding='0' cellspacing='0'>" +
    "    <tbody>" +
    "        <tr>" +
    "            <td style='text-align: right;'>用户名：</td>" +
    "            <td><input name='userName' id='userName' value='admin' type='text' /></td>" +
    "        </tr>" +
    "        <tr>" +
    "            <td style='text-align: right;'>密码：</td>" +
    "            <td><input name='passwd' id='passwd' value='admin' type='password' /></td>" +
    "        </tr>" +
    "    </tbody>" +
    "</table>";
    var _dialog = jQuery(html);
    _dialog.attr('title', '请登录');
    _dialog.dialog({
        modal : true,
        width: 400,
        height : 260,

        buttons: {
            Ok: function() {
                Utils.run(function() {
                    // 登录操作
                    var service = ServiceFactory.getService('aa.LoginService');
                    var userInfo = {userName : jQuery('#userName').val(),
                                    passwd   : jQuery('#passwd').val()};

                    if (service.login(userInfo)) {
                        jQuery.cookie('user_in_cookie', jQuery('#userName'));
                        _dialog.dialog('close');
                    } else {
                        alert('登录失败，请确认你输入的用户名和密码相同!');
                    }
                });
            }
        }
    });
    return _dialog;
}
