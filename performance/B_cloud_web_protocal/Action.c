Action()
{

	web_url("www.bc_demo1.com:4900", 
		"URL=http://www.bc_demo1.com:4900/", 
		"Resource=0", 
		"RecContentType=text/html", 
		"Referer=", 
		"Snapshot=t1.inf", 
		"Mode=HTTP", 
		LAST);

	web_concurrent_start(NULL);

	web_url("top.html", 
		"URL=http://www.bc_demo1.com:4900/top.html", 
		"Resource=0", 
		"RecContentType=text/html", 
		"Referer=http://www.bc_demo1.com:4900/", 
		"Snapshot=t2.inf", 
		"Mode=HTTP", 
		LAST);

	web_url("main.html", 
		"URL=http://www.bc_demo1.com:4900/main.html", 
		"Resource=0", 
		"RecContentType=text/html", 
		"Referer=http://www.bc_demo1.com:4900/", 
		"Snapshot=t3.inf", 
		"Mode=HTTP", 
		LAST);

	web_concurrent_end(NULL);

	web_concurrent_start(NULL);

	web_url("nav.html", 
		"URL=http://www.bc_demo1.com:4900/nav.html", 
		"Resource=0", 
		"RecContentType=text/html", 
		"Referer=http://www.bc_demo1.com:4900/main.html", 
		"Snapshot=t4.inf", 
		"Mode=HTTP", 
		LAST);

	web_url("welcome.html", 
		"URL=http://www.bc_demo1.com:4900/welcome.html", 
		"Resource=0", 
		"RecContentType=text/html", 
		"Referer=http://www.bc_demo1.com:4900/main.html", 
		"Snapshot=t6.inf", 
		"Mode=HTTP", 
		LAST);

	web_concurrent_end(NULL);

	web_concurrent_start(NULL);

	web_url("banner.jpg", 
		"URL=http://www.bc_demo1.com:4900/images/banner.jpg", 
		"Resource=1", 
		"RecContentType=image/jpeg", 
		"Referer=http://www.bc_demo1.com:4900/top.html", 
		"Snapshot=t5.inf", 
		LAST);

	web_url("boot.js", 
		"URL=http://www.bc_demo1.com:4900/js/boot.js", 
		"Resource=1", 
		"RecContentType=text/javascript", 
		"Referer=http://www.bc_demo1.com:4900/nav.html", 
		"Snapshot=t7.inf", 
		LAST);

	web_concurrent_end(NULL);

	lr_think_time(30);

	web_url("dolphin.jpg", 
		"URL=http://www.bc_demo1.com:4900/images/dolphin.jpg", 
		"Resource=1", 
		"RecContentType=image/jpeg", 
		"Referer=http://www.bc_demo1.com:4900/welcome.html", 
		"Snapshot=t8.inf", 
		LAST);

	web_concurrent_start(NULL);

	web_url("read.getParams", 
		"URL=http://www.bc_demo1.com:4900/read.getParams?name=contextPath", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/nav.html", 
		"Snapshot=t9.inf", 
		LAST);

	web_url("conf.js", 
		"URL=http://www.bc_demo1.com:4900/js/conf.js", 
		"Resource=1", 
		"RecContentType=text/javascript", 
		"Referer=http://www.bc_demo1.com:4900/nav.html", 
		"Snapshot=t10.inf", 
		LAST);

	web_url("jquery-1.3.2.js", 
		"URL=http://www.bc_demo1.com:4900/js/core/jquery-1.3.2.js", 
		"Resource=1", 
		"RecContentType=text/javascript", 
		"Referer=http://www.bc_demo1.com:4900/nav.html", 
		"Snapshot=t11.inf", 
		LAST);

	web_url("jquery-ui-1.7.2.custom.min.js", 
		"URL=http://www.bc_demo1.com:4900/js/core/jquery-ui-1.7.2.custom.min.js", 
		"Resource=1", 
		"RecContentType=text/javascript", 
		"Referer=http://www.bc_demo1.com:4900/nav.html", 
		"Snapshot=t12.inf", 
		LAST);

	web_url("ui.datepicker-zh-CN.js", 
		"URL=http://www.bc_demo1.com:4900/js/core/i18n/ui.datepicker-zh-CN.js", 
		"Resource=1", 
		"RecContentType=text/javascript", 
		"Referer=http://www.bc_demo1.com:4900/nav.html", 
		"Snapshot=t13.inf", 
		LAST);

	web_url("json2.js", 
		"URL=http://www.bc_demo1.com:4900/js/core/json2.js", 
		"Resource=1", 
		"RecContentType=text/javascript", 
		"Referer=http://www.bc_demo1.com:4900/nav.html", 
		"Snapshot=t14.inf", 
		LAST);

	web_url("gt_base.js", 
		"URL=http://www.bc_demo1.com:4900/js/core/gt_base.js", 
		"Resource=1", 
		"RecContentType=text/javascript", 
		"Referer=http://www.bc_demo1.com:4900/nav.html", 
		"Snapshot=t15.inf", 
		LAST);

	web_url("gt_table.js", 
		"URL=http://www.bc_demo1.com:4900/js/core/gt_table.js", 
		"Resource=1", 
		"RecContentType=text/javascript", 
		"Referer=http://www.bc_demo1.com:4900/nav.html", 
		"Snapshot=t16.inf", 
		LAST);

	web_url("ajaxupload.3.2.js", 
		"URL=http://www.bc_demo1.com:4900/js/core/ajaxupload.3.2.js", 
		"Resource=1", 
		"RecContentType=text/javascript", 
		"Referer=http://www.bc_demo1.com:4900/nav.html", 
		"Snapshot=t17.inf", 
		LAST);

	web_url("jquery.cookie.js", 
		"URL=http://www.bc_demo1.com:4900/js/demo/jquery.cookie.js", 
		"Resource=1", 
		"RecContentType=text/javascript", 
		"Referer=http://www.bc_demo1.com:4900/nav.html", 
		"Snapshot=t18.inf", 
		LAST);

	web_url("aa.js", 
		"URL=http://www.bc_demo1.com:4900/js/demo/aa.js", 
		"Resource=1", 
		"RecContentType=text/javascript", 
		"Referer=http://www.bc_demo1.com:4900/nav.html", 
		"Snapshot=t19.inf", 
		LAST);

	web_url("jquery-ui-1.7.2.custom.css", 
		"URL=http://www.bc_demo1.com:4900/css/ui-lightness/jquery-ui-1.7.2.custom.css", 
		"Resource=1", 
		"RecContentType=text/css", 
		"Referer=http://www.bc_demo1.com:4900/nav.html", 
		"Snapshot=t20.inf", 
		LAST);

	web_url("gt_demo.css", 
		"URL=http://www.bc_demo1.com:4900/css/gt_demo.css", 
		"Resource=1", 
		"RecContentType=text/css", 
		"Referer=http://www.bc_demo1.com:4900/nav.html", 
		"Snapshot=t21.inf", 
		LAST);

	web_url("ui-bg_highlight-soft_100_eeeeee_1x100.png", 
		"URL=http://www.bc_demo1.com:4900/css/ui-lightness/images/ui-bg_highlight-soft_100_eeeeee_1x100.png", 
		"Resource=1", 
		"RecContentType=image/png", 
		"Referer=http://www.bc_demo1.com:4900/nav.html", 
		"Snapshot=t22.inf", 
		LAST);

	web_url("ui-bg_glass_100_f6f6f6_1x400.png", 
		"URL=http://www.bc_demo1.com:4900/css/ui-lightness/images/ui-bg_glass_100_f6f6f6_1x400.png", 
		"Resource=1", 
		"RecContentType=image/png", 
		"Referer=http://www.bc_demo1.com:4900/nav.html", 
		"Snapshot=t23.inf", 
		LAST);

	web_url("ui-icons_ef8c08_256x240.png", 
		"URL=http://www.bc_demo1.com:4900/css/ui-lightness/images/ui-icons_ef8c08_256x240.png", 
		"Resource=1", 
		"RecContentType=image/png", 
		"Referer=http://www.bc_demo1.com:4900/nav.html", 
		"Snapshot=t24.inf", 
		LAST);

	web_url("ui-bg_glass_65_ffffff_1x400.png", 
		"URL=http://www.bc_demo1.com:4900/css/ui-lightness/images/ui-bg_glass_65_ffffff_1x400.png", 
		"Resource=1", 
		"RecContentType=image/png", 
		"Referer=http://www.bc_demo1.com:4900/nav.html", 
		"Snapshot=t25.inf", 
		LAST);

	web_concurrent_end(NULL);

	lr_think_time(84);

	web_url("test_table_template.html", 
		"URL=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		"Resource=0", 
		"RecContentType=text/html", 
		"Referer=http://www.bc_demo1.com:4900/main.html", 
		"Snapshot=t26.inf", 
		"Mode=HTTP", 
		LAST);

	web_url("boot.js_2", 
		"URL=http://www.bc_demo1.com:4900/js/boot.js", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_concurrent_start(NULL);

	web_url("read.getParams_2", 
		"URL=http://www.bc_demo1.com:4900/base/read.getParams?name=contextPath", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		"Snapshot=t27.inf", 
		LAST);

	web_url("conf.js_2", 
		"URL=http://www.bc_demo1.com:4900/js/conf.js", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("jquery-1.3.2.js_2", 
		"URL=http://www.bc_demo1.com:4900/js/core/jquery-1.3.2.js", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("gt_demo.css_2", 
		"URL=http://www.bc_demo1.com:4900/css/gt_demo.css", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("jquery-ui-1.7.2.custom.css_2", 
		"URL=http://www.bc_demo1.com:4900/css/ui-lightness/jquery-ui-1.7.2.custom.css", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("jquery-ui-1.7.2.custom.min.js_2", 
		"URL=http://www.bc_demo1.com:4900/js/core/jquery-ui-1.7.2.custom.min.js", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("ui.datepicker-zh-CN.js_2", 
		"URL=http://www.bc_demo1.com:4900/js/core/i18n/ui.datepicker-zh-CN.js", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("json2.js_2", 
		"URL=http://www.bc_demo1.com:4900/js/core/json2.js", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("gt_base.js_2", 
		"URL=http://www.bc_demo1.com:4900/js/core/gt_base.js", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("gt_table.js_2", 
		"URL=http://www.bc_demo1.com:4900/js/core/gt_table.js", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("ajaxupload.3.2.js_2", 
		"URL=http://www.bc_demo1.com:4900/js/core/ajaxupload.3.2.js", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("jquery.cookie.js_2", 
		"URL=http://www.bc_demo1.com:4900/js/demo/jquery.cookie.js", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("aa.js_2", 
		"URL=http://www.bc_demo1.com:4900/js/demo/aa.js", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_concurrent_end(NULL);

	lr_think_time(49);

	web_custom_request("jsonClient.action", 
		"URL=http://www.bc_demo1.com:4900/jsonClient.action?jsonValue={\"serviceClassName\":\"com.xt.gt.html.service.ClassService\",\"methodName\":\"loadClass\",\"serviceObject\":null,\"type\":\"common\",\"params\":[\"com.xt.gt.demo.service.TableService\"]}", 
		"Method=POST", 
		"Resource=0", 
		"RecContentType=text/html", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		"Snapshot=t28.inf", 
		"Mode=HTTP", 
		"EncType=", 
		LAST);

	web_custom_request("jsonClient.action_2", 
		"URL=http://www.bc_demo1.com:4900/jsonClient.action?jsonValue={\"serviceClassName\":\"com.xt.gt.demo.service.TableService\",\"methodName\":\"list\",\"serviceObject\":{\"className\":\"com.xt.gt.demo.service.TableService\",\"invokedMethods\":[]},\"type\":null,\"params\":[]}", 
		"Method=POST", 
		"Resource=0", 
		"RecContentType=text/html", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		"Snapshot=t29.inf", 
		"Mode=HTTP", 
		"EncType=", 
		LAST);

	web_concurrent_start(NULL);

	web_url("ui-icons_222222_256x240.png", 
		"URL=http://www.bc_demo1.com:4900/css/ui-lightness/images/ui-icons_222222_256x240.png", 
		"Resource=1", 
		"RecContentType=image/png", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		"Snapshot=t30.inf", 
		LAST);

	web_url("ui-bg_diagonals-thick_20_666666_40x40.png", 
		"URL=http://www.bc_demo1.com:4900/css/ui-lightness/images/ui-bg_diagonals-thick_20_666666_40x40.png", 
		"Resource=1", 
		"RecContentType=image/png", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		"Snapshot=t31.inf", 
		LAST);

	web_url("ui-icons_ffffff_256x240.png", 
		"URL=http://www.bc_demo1.com:4900/css/ui-lightness/images/ui-icons_ffffff_256x240.png", 
		"Resource=1", 
		"RecContentType=image/png", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		"Snapshot=t32.inf", 
		LAST);

	web_url("ui-bg_gloss-wave_35_f6a828_500x100.png", 
		"URL=http://www.bc_demo1.com:4900/css/ui-lightness/images/ui-bg_gloss-wave_35_f6a828_500x100.png", 
		"Resource=1", 
		"RecContentType=image/png", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		"Snapshot=t33.inf", 
		LAST);

	web_url("ui-bg_glass_100_fdf5ce_1x400.png", 
		"URL=http://www.bc_demo1.com:4900/css/ui-lightness/images/ui-bg_glass_100_fdf5ce_1x400.png", 
		"Resource=1", 
		"RecContentType=image/png", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		"Snapshot=t34.inf", 
		LAST);

	web_concurrent_end(NULL);

	lr_think_time(103);

	web_custom_request("jsonClient.action_3", 
		"URL=http://www.bc_demo1.com:4900/jsonClient.action?jsonValue={\"serviceClassName\":\"com.xt.gt.html.service.ClassService\",\"methodName\":\"loadClass\",\"serviceObject\":null,\"type\":\"common\",\"params\":[\"com.xt.gt.demo.aa.LoginService\"]}", 
		"Method=POST", 
		"Resource=0", 
		"RecContentType=text/html", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		"Snapshot=t35.inf", 
		"Mode=HTTP", 
		"EncType=", 
		LAST);

	web_custom_request("jsonClient.action_4", 
		"URL=http://www.bc_demo1.com:4900/jsonClient.action?jsonValue={\"serviceClassName\":\"com.xt.gt.demo.aa.LoginService\",\"methodName\":\"login\",\"serviceObject\":{\"className\":\"com.xt.gt.demo.aa.LoginService\",\"invokedMethods\":[]},\"type\":null,\"params\":[{\"userName\":\"admin\",\"passwd\":\"admin\"}]}", 
		"Method=POST", 
		"Resource=0", 
		"RecContentType=text/html", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		"Snapshot=t36.inf", 
		"Mode=HTTP", 
		"EncType=", 
		LAST);

	lr_think_time(10);

	web_url("test_table_template.html_2", 
		"URL=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		"Resource=0", 
		"RecContentType=text/html", 
		"Referer=http://www.bc_demo1.com:4900/main.html", 
		"Snapshot=t37.inf", 
		"Mode=HTTP", 
		LAST);

	web_url("boot.js_3", 
		"URL=http://www.bc_demo1.com:4900/js/boot.js", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_concurrent_start(NULL);

	web_url("read.getParams_3", 
		"URL=http://www.bc_demo1.com:4900/base/read.getParams?name=contextPath", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		"Snapshot=t38.inf", 
		LAST);

	web_url("conf.js_3", 
		"URL=http://www.bc_demo1.com:4900/js/conf.js", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("jquery-ui-1.7.2.custom.css_3", 
		"URL=http://www.bc_demo1.com:4900/css/ui-lightness/jquery-ui-1.7.2.custom.css", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("jquery-1.3.2.js_3", 
		"URL=http://www.bc_demo1.com:4900/js/core/jquery-1.3.2.js", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("gt_demo.css_3", 
		"URL=http://www.bc_demo1.com:4900/css/gt_demo.css", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("jquery-ui-1.7.2.custom.min.js_3", 
		"URL=http://www.bc_demo1.com:4900/js/core/jquery-ui-1.7.2.custom.min.js", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("ui.datepicker-zh-CN.js_3", 
		"URL=http://www.bc_demo1.com:4900/js/core/i18n/ui.datepicker-zh-CN.js", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("json2.js_3", 
		"URL=http://www.bc_demo1.com:4900/js/core/json2.js", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("gt_base.js_3", 
		"URL=http://www.bc_demo1.com:4900/js/core/gt_base.js", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("gt_table.js_3", 
		"URL=http://www.bc_demo1.com:4900/js/core/gt_table.js", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("ajaxupload.3.2.js_3", 
		"URL=http://www.bc_demo1.com:4900/js/core/ajaxupload.3.2.js", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("jquery.cookie.js_3", 
		"URL=http://www.bc_demo1.com:4900/js/demo/jquery.cookie.js", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("aa.js_3", 
		"URL=http://www.bc_demo1.com:4900/js/demo/aa.js", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_concurrent_end(NULL);

	lr_think_time(49);

	web_custom_request("jsonClient.action_5", 
		"URL=http://www.bc_demo1.com:4900/jsonClient.action?jsonValue={\"serviceClassName\":\"com.xt.gt.html.service.ClassService\",\"methodName\":\"loadClass\",\"serviceObject\":null,\"type\":\"common\",\"params\":[\"com.xt.gt.demo.service.TableService\"]}", 
		"Method=POST", 
		"Resource=0", 
		"RecContentType=text/html", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		"Snapshot=t39.inf", 
		"Mode=HTTP", 
		"EncType=", 
		LAST);

	web_custom_request("jsonClient.action_6", 
		"URL=http://www.bc_demo1.com:4900/jsonClient.action?jsonValue={\"serviceClassName\":\"com.xt.gt.demo.service.TableService\",\"methodName\":\"list\",\"serviceObject\":{\"className\":\"com.xt.gt.demo.service.TableService\",\"invokedMethods\":[]},\"type\":null,\"params\":[]}", 
		"Method=POST", 
		"Resource=0", 
		"RecContentType=text/html", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		"Snapshot=t40.inf", 
		"Mode=HTTP", 
		"EncType=", 
		LAST);

	web_concurrent_start(NULL);

	web_url("ui-bg_gloss-wave_35_f6a828_500x100.png_2", 
		"URL=http://www.bc_demo1.com:4900/css/ui-lightness/images/ui-bg_gloss-wave_35_f6a828_500x100.png", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("ui-icons_222222_256x240.png_2", 
		"URL=http://www.bc_demo1.com:4900/css/ui-lightness/images/ui-icons_222222_256x240.png", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("ui-bg_diagonals-thick_20_666666_40x40.png_2", 
		"URL=http://www.bc_demo1.com:4900/css/ui-lightness/images/ui-bg_diagonals-thick_20_666666_40x40.png", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("ui-icons_ffffff_256x240.png_2", 
		"URL=http://www.bc_demo1.com:4900/css/ui-lightness/images/ui-icons_ffffff_256x240.png", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("ui-bg_glass_100_fdf5ce_1x400.png_2", 
		"URL=http://www.bc_demo1.com:4900/css/ui-lightness/images/ui-bg_glass_100_fdf5ce_1x400.png", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_concurrent_end(NULL);

	lr_think_time(69);

	web_custom_request("jsonClient.action_7", 
		"URL=http://www.bc_demo1.com:4900/jsonClient.action?jsonValue={\"serviceClassName\":\"com.xt.gt.html.service.ClassService\",\"methodName\":\"loadClass\",\"serviceObject\":null,\"type\":\"common\",\"params\":[\"com.xt.gt.demo.aa.LoginService\"]}", 
		"Method=POST", 
		"Resource=0", 
		"RecContentType=text/html", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		"Snapshot=t41.inf", 
		"Mode=HTTP", 
		"EncType=", 
		LAST);

	web_custom_request("jsonClient.action_8", 
		"URL=http://www.bc_demo1.com:4900/jsonClient.action?jsonValue={\"serviceClassName\":\"com.xt.gt.demo.aa.LoginService\",\"methodName\":\"login\",\"serviceObject\":{\"className\":\"com.xt.gt.demo.aa.LoginService\",\"invokedMethods\":[]},\"type\":null,\"params\":[{\"userName\":\"admin\",\"passwd\":\"admin\"}]}", 
		"Method=POST", 
		"Resource=0", 
		"RecContentType=text/html", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		"Snapshot=t42.inf", 
		"Mode=HTTP", 
		"EncType=", 
		LAST);

	lr_think_time(11);

	web_url("test_table_template.html_3", 
		"URL=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		"Resource=0", 
		"RecContentType=text/html", 
		"Referer=http://www.bc_demo1.com:4900/main.html", 
		"Snapshot=t43.inf", 
		"Mode=HTTP", 
		LAST);

	web_url("boot.js_4", 
		"URL=http://www.bc_demo1.com:4900/js/boot.js", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_concurrent_start(NULL);

	web_url("read.getParams_4", 
		"URL=http://www.bc_demo1.com:4900/base/read.getParams?name=contextPath", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		"Snapshot=t44.inf", 
		LAST);

	web_url("conf.js_4", 
		"URL=http://www.bc_demo1.com:4900/js/conf.js", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("jquery-1.3.2.js_4", 
		"URL=http://www.bc_demo1.com:4900/js/core/jquery-1.3.2.js", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("gt_demo.css_4", 
		"URL=http://www.bc_demo1.com:4900/css/gt_demo.css", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("jquery-ui-1.7.2.custom.css_4", 
		"URL=http://www.bc_demo1.com:4900/css/ui-lightness/jquery-ui-1.7.2.custom.css", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("jquery-ui-1.7.2.custom.min.js_4", 
		"URL=http://www.bc_demo1.com:4900/js/core/jquery-ui-1.7.2.custom.min.js", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("ui.datepicker-zh-CN.js_4", 
		"URL=http://www.bc_demo1.com:4900/js/core/i18n/ui.datepicker-zh-CN.js", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("json2.js_4", 
		"URL=http://www.bc_demo1.com:4900/js/core/json2.js", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("gt_base.js_4", 
		"URL=http://www.bc_demo1.com:4900/js/core/gt_base.js", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("gt_table.js_4", 
		"URL=http://www.bc_demo1.com:4900/js/core/gt_table.js", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("ajaxupload.3.2.js_4", 
		"URL=http://www.bc_demo1.com:4900/js/core/ajaxupload.3.2.js", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("jquery.cookie.js_4", 
		"URL=http://www.bc_demo1.com:4900/js/demo/jquery.cookie.js", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("aa.js_4", 
		"URL=http://www.bc_demo1.com:4900/js/demo/aa.js", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_concurrent_end(NULL);

	lr_think_time(47);

	web_custom_request("jsonClient.action_9", 
		"URL=http://www.bc_demo1.com:4900/jsonClient.action?jsonValue={\"serviceClassName\":\"com.xt.gt.html.service.ClassService\",\"methodName\":\"loadClass\",\"serviceObject\":null,\"type\":\"common\",\"params\":[\"com.xt.gt.demo.service.TableService\"]}", 
		"Method=POST", 
		"Resource=0", 
		"RecContentType=text/html", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		"Snapshot=t45.inf", 
		"Mode=HTTP", 
		"EncType=", 
		LAST);

	web_custom_request("jsonClient.action_10", 
		"URL=http://www.bc_demo1.com:4900/jsonClient.action?jsonValue={\"serviceClassName\":\"com.xt.gt.demo.service.TableService\",\"methodName\":\"list\",\"serviceObject\":{\"className\":\"com.xt.gt.demo.service.TableService\",\"invokedMethods\":[]},\"type\":null,\"params\":[]}", 
		"Method=POST", 
		"Resource=0", 
		"RecContentType=text/html", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		"Snapshot=t46.inf", 
		"Mode=HTTP", 
		"EncType=", 
		LAST);

	web_concurrent_start(NULL);

	web_url("ui-bg_gloss-wave_35_f6a828_500x100.png_3", 
		"URL=http://www.bc_demo1.com:4900/css/ui-lightness/images/ui-bg_gloss-wave_35_f6a828_500x100.png", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("ui-bg_diagonals-thick_20_666666_40x40.png_3", 
		"URL=http://www.bc_demo1.com:4900/css/ui-lightness/images/ui-bg_diagonals-thick_20_666666_40x40.png", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("ui-icons_222222_256x240.png_3", 
		"URL=http://www.bc_demo1.com:4900/css/ui-lightness/images/ui-icons_222222_256x240.png", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_url("ui-icons_ffffff_256x240.png_3", 
		"URL=http://www.bc_demo1.com:4900/css/ui-lightness/images/ui-icons_ffffff_256x240.png", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_table_template.html", 
		LAST);

	web_concurrent_end(NULL);

	lr_think_time(12);

	web_url("test_bind.html", 
		"URL=http://www.bc_demo1.com:4900/base/test_bind.html", 
		"Resource=0", 
		"RecContentType=text/html", 
		"Referer=http://www.bc_demo1.com:4900/main.html", 
		"Snapshot=t47.inf", 
		"Mode=HTTP", 
		LAST);

	web_url("boot.js_5", 
		"URL=http://www.bc_demo1.com:4900/js/boot.js", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_bind.html", 
		LAST);

	web_url("read.getParams_5", 
		"URL=http://www.bc_demo1.com:4900/base/read.getParams?name=contextPath", 
		"Resource=1", 
		"Referer=http://www.bc_demo1.com:4900/base/test_bind.html", 
		"Snapshot=t48.inf", 
		LAST);

	return 0;
}