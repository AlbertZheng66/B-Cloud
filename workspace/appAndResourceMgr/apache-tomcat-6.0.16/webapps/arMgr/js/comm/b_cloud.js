
/**
  * 将数值转换为以“兆（M）”的形式表示。数据以对终端用户更友好的方式展现。
  × human readable formatter
  */
var HRFormatter = {
    toM : function (value) {
        return Math.floor(value / (1024 * 1024));
    },
	
	toK : function (value) {
        return Math.floor(value / 1024);
    },
	
	toDate : function (value) {
	    if (value < 1000) {
            return "小于1秒";
        }
	    var seconds = Math.floor(value / 1000);
        var minutes = Math.floor(seconds / 60);
        var hours = Math.floor(minutes / 60);
        var days = Math.floor(hours / 24);
        var str = "";
        if (days > 1) {
            str += days + "天"
        }
        if (hours > 1) {
            str += (hours % 24) + "小时"
        }
        if (minutes > 1) {
            str += (minutes % 60) + "分"
        }
        if (seconds > 1) {
            str += (seconds % 60) + "秒"
        }
                           
	}
}

var setCurrentTime = function(id) {
    var currentTime = DateFormat.format(new Date(), 'yyyy-MM-dd HH:mm:ss');
    $(id).val(currentTime);
}

var VarTemplate = {
    // 变量的匹配模式
    varPattern : /\$\{([a-zA-Z_]\w*)\}/g,
	
	format : function (id, obj, process) {
        var text = jQuery(id).html();
        // 实施参数替换
        var matches = text.match(this.varPattern);
        if (matches == null) {
            return text;
        }
        var resultStr = text;
        for (var i = 0; i < matches.length; i++) {
            var varName = matches[i].substring(2, matches[i].length - 1);
            var value = obj[varName];
			if (process != null) {
			    value = process(varName, value); 
			}
            resultStr = resultStr.replace(matches[i], value);
        }
        return resultStr;
    }
}