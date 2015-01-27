
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