/**
 * 常用 JavaScript 库。
 * 
 * @class Resource
 * @type {Object}
 */
var Resource2 = Resource = {
	/**
	 * <a href="http://www.oschina.net/p/windjs" target="_blank">Wind.js</a>
	 * 是一个异步编程的JS库，作者为老赵
	 * 
	 * @property Wind.js
	 * @type {Object}
	 */
	wind : {
		text : "Wind.js",
		scripts : [ {
			text : "Wind.js 0.7.0",
			ident : "wind_070",
			url : [ "/js/sandbox/wind.js/wind-core.js",
					"/js/sandbox/wind.js/wind-compiler.js",
					"/js/sandbox/wind.js/wind-builderbase.js",
					"/js/sandbox/wind.js/wind-async.js" ]
		} ]
	},
	/**
	 * <a href="http://www.oschina.net/p/bootstrap" target="_blank">Bootstrap</a>
	 * 是一个Twitter开发的web前端框架 *
	 * 
	 * @property Bootstrap
	 * @type {Object}
	 */
	bootstrap : {
		text : "Bootstrap",
		requires : [ 'jquery_172' ],
		scripts : [ {
			ident : 'bootstrap_221',
			text : "Bootstrap v2.2.1",
			url : "/js/sandbox/bootstrap-2.2.1/js/bootstrap.min.js",
			style : "/js/sandbox/bootstrap-2.2.1/css/bootstrap.min.css"
		} ]
	},
	/**
	 * <a href="http://www.oschina.net/p/yui" target="_blank">YUI</a>
	 * 雅虎出品的web前端框架
	 * 
	 * @property YUI
	 * @type {Object}
	 */
	yui : {
		text : "YUI",
		scripts : [ {
			text : "YUI 3.3.0",
			ident : 'yui_330',
			url : "/js/sandbox/yui/yui-3.3.0-min.js"
		}, {
			text : "YUI 2.8.2",
			ident : 'yui_282',
			url : "/js/sandbox/yui/yuiloader-2.8.2-min.js"
		} ]
	},
	/**
	 * <a href="http://www.oschina.net/p/mootools" target="_blank">MooTools</a>
	 * MooTools是一个简洁，模块化，面向对象的JavaScript框架。
	 * 
	 * @property MooTools
	 * @type {Object}
	 */
	mootools : {
		text : "MooTools",
		scripts : [
				{
					ident : "mootools_145",
					text : "Mootools 1.4.5",
					ident : "mootools_145",
					url : "/js/sandbox/mootools/mootools-core-1.4.5-full-nocompat-yc.js"
				}, {
					ident : "mootools_125",
					text : "Mootools 1.2.5",
					ident : "mootools_125",
					url : "/js/sandbox/mootools/mootools-1.2.5-core-yc.js"
				} ]
	},
	/**
	 * <a href="http://www.oschina.net/p/prototype" target="_blank">Prototype</a>
	 * Prototype.js 是一个由Sam Stephenson写的JavaScript包,一个Ajax框架。
	 * 
	 * @property Prototype
	 * @type {Object}
	 */
	prototype : {
		text : "Prototype",
		scripts : [ {
			ident : "prototype_1710",
			text : "Prototype 1.7.1.0",
			url : "/js/sandbox/prototype/1.7.1.0/prototype.js"
		}, {
			ident : "prototype_1700",
			text : "Prototype 1.7.0.0",
			url : "/js/sandbox/prototype/1.7.0.0/prototype.js"
		}, {
			ident : "prototype_1610",
			text : "Prototype 1.6.1.0",
			url : "/js/sandbox/prototype/1.6.1.0/prototype.js"
		} ]
	},
	/**
	 * <a href="http://www.oschina.net/p/jquery" target="_blank">jQuery</a>
	 * jQuery 一个很常用Ajax框架。
	 * 
	 * @property jQuery
	 * @type {Object}
	 */
	jquery : {
		text : "jQuery",
		scripts : [ {
			ident : 'jquery_183',
			text : "jQuery 1.8.3",
			url : "/js/sandbox/jquery/jquery-1.8.3.min.js"
		}, {
			ident : 'jquery_182',
			text : "jQuery 1.8.2",
			url : "/js/sandbox/jquery/jquery-1.8.2.min.js"
		}, {
			ident : 'jquery_180',
			text : "jQuery 1.8.0",
			url : "/js/sandbox/jquery/jquery-1.8.0.min.js"
		}, {
			ident : 'jquery_172',
			text : "jQuery 1.7.2",
			url : "/js/sandbox/jquery/jquery-1.7.2.min.js"
		}, {
			ident : 'jquery_164',
			text : "jQuery 1.6.4",
			url : "/js/sandbox/jquery/jquery-1.6.4.min.js"
		}, {
			ident : 'jquery_151',
			text : "jQuery 1.5.1",
			url : "/js/sandbox/jquery/jquery-1.5.1.min.js"
		}, {
			ident : 'jquery_144',
			text : "jQuery 1.4.4",
			url : "/js/sandbox/jquery/jquery-1.4.4.min.js"
		} ]
	},
	/**
	 * <a href="http://www.oschina.net/p/jquery+ui" target="_blank">jQuery UI</a>
	 * jQuery UI 一个很基于jQuery的web前端UI框架。
	 * 
	 * @property jQuery UI
	 * @type {Object}
	 */
	jqueryui : {
		text : "jQuery UI",
		requires : [ 'jquery_182' ],
		scripts : [ {
			ident : "jqueryui_191",
			text : "jQuery UI 1.9.1",
			url : "/js/sandbox/jquery-ui/1.9.1/js/jquery-ui-1.9.1.custom.min.js",
			style : "/js/sandbox/jquery-ui/1.9.1/css/smoothness/jquery-ui-1.9.1.custom.min.css"
		} ]
	},
	/**
	 * <a href="http://www.oschina.net/p/jquerymobile" target="_blank">jQuery
	 * Mobile</a> jQuery Mobile 一个很基于jQuery的移动前端UI框架。
	 * 
	 * @property jQuery Mobile
	 * @type {Object}
	 */
	jquerymobile : {
		text : "jQuery Mobile",
		requires : [ 'jquery_182' ],
		scripts : [
				{
					ident : "jquerymobile_120",
					text : "jQuery Mobile 1.2.0",
					url : "/js/sandbox/jquery-mobile/jquery.mobile-1.2.0/jquery.mobile-1.2.0.min.js",
					style : "/js/sandbox/jquery-mobile/jquery.mobile-1.2.0/jquery.mobile-1.2.0.css"
				},
				{
					ident : "jquerymobile_111",
					text : "jQuery Mobile 1.1.1",
					url : "/js/sandbox/jquery-mobile/jquery.mobile-1.1.1/jquery.mobile-1.1.1.min.js",
					style : "/js/sandbox/jquery-mobile/jquery.mobile-1.1.1/jquery.mobile-1.1.1.css"
				},
				{
					ident : "jquerymobile_101",
					text : "jQuery Mobile 1.0.1",
					url : "/js/sandbox/jquery-mobile/jquery.mobile-1.0.1/jquery.mobile-1.0.1.min.js",
					style : "/js/sandbox/jquery-mobile/jquery.mobile-1.0.1/jquery.mobile-1.0.1.css"
				} ]
	},
	/**
	 * <a href="http://www.oschina.net/p/dojo" target="_blank">Dojo</a> Dojo
	 * 一个很优雅的ajax框架。
	 * 
	 * @property Dojo
	 * @type {Object}
	 */
	dojo : {
		text : "Dojo",
		scripts : [ {
			ident : "dojo_181",
			text : "Dojo 1.8.1",
			url : "/js/sandbox/dojo/dojo-1.8.1.min.js"
		}, {
			ident : "dojo_180",
			text : "Dojo 1.8.0",
			url : "/js/sandbox/dojo/dojo-1.8.0.min.js"
		}, {
			ident : "dojo_160",
			text : "Dojo 1.6.0",
			url : "/js/sandbox/dojo/dojo-1.6.0.min.js"
		}, {
			ident : "dojo_141",
			text : "Dojo 1.4.1",
			url : "/js/sandbox/dojo/dojo-1.4.1.min.js"
		} ]
	},
	jqueryplugins : {
		text : "jQuery 插件",
		scripts : [
				/**
				 * <a href="http://www.oschina.net/p/cookie">jQuery cookie</a>
				 * jQuery cookie 一个操作cookie的jQuery插件
				 * 
				 * @property jquery cookie
				 * @type {Object}
				 */
				{
					ident : "jqueryplugins1_jquery_cookie_13",
					text : "jQuery cookie",
					url : "/js/sandbox/jquery-plugins/jquery.cookie-1.3.js",
					requires : [ "jquery_182" ]
				},
				/**
				 * <a href="http://www.oschina.net/p/form+plugin">jQuery form</a>
				 * jQuery cookie 一个操作表单的jQuery插件
				 * 
				 * @property jquery form
				 * @type {Object}
				 */
				{
					ident : "jqueryplugins2_jqueryform_282",
					text : "jQuery forms 2.8.2",
					url : "/js/sandbox/jquery-plugins/jquery.form-2.82.js",
					requires : [ "jquery_182" ]
				},
				/**
				 * <a href="http://www.oschina.net/p/cookies">jQuery cookies</a>
				 * jQuery cookies 另一个操作cookie的jQuery插件
				 * 
				 * @property jquery cookies
				 * @type {Object}
				 */
				{
					ident : "jqueryplugins3_jquery_cookies_220",
					text : "jQuery cookies 2.2.0",
					url : "/js/sandbox/jquery-plugins/jquery.cookies.2.2.0.min.js",
					requires : [ "jquery_182" ]
				},
				/**
				 * <a href="http://www.oschina.net/p/validity">jQuery validity</a>
				 * jQuery validity 一个jQuery验证插件
				 * 
				 * @property jquery validity
				 * @type {Object}
				 */
				{
					ident : "jqueryplugins4_jquery_validity_120",
					text : "jQuery validity 1.2.0",
					url : "/js/sandbox/jquery-plugins/jquery-validity/jQuery.validity.min.js",
					style : "/js/sandbox/jquery-plugins/jquery-validity/jquery.validity.css",
					requires : [ "jquery_182" ]
				},
				/**
				 * <a href="http://www.oschina.net/p/uploadify">jQuery uploadify</a>
				 * jQuery uploadify 一个jQuery上传插件
				 * 
				 * @property jquery uploadify
				 * @type {Object}
				 */
				{
					ident : "jqueryplugins5_jquery_uploadify_32",
					text : "jQuery uploadify 3.2",
					url : "/js/sandbox/jquery-plugins/uploadify/jquery.uploadify.min.js",
					style : "/js/sandbox/jquery-plugins/uploadify/uploadify.css",
					requires : [ "jquery_182" ]
				},
				/**
				 * <a href="http://www.oschina.net/p/jqgrid">jQuery jqGrid</a>
				 * jqGrid 一个jQuery网格插件
				 * 
				 * @property jquery jqGrid
				 * @type {Object}
				 */
				{
					ident : "jqueryplugins6_jquery_jqgrid_441",
					text : "jQuery jqGrid 4.4.1",
					url : "/js/sandbox/jquery-plugins/jqGrid/js/jquery.jqGrid.min.js",
					style : "/js/sandbox/jquery-plugins/jqGrid/css/ui.jqgrid.css",
					requires : [ "jquery_182" ]
				},
				/**
				 * <a href="http://www.oschina.net/p/colorbox">jQuery colorbox</a>
				 * colorbox 一个jQuery自定义灯箱插件
				 * 
				 * @property jquery colorbox
				 * @type {Object}
				 */
				{
					ident : "jqueryplugins7_jquery_colorbox_132",
					text : "jQuery ColorBox 1.3.2",
					url : "/js/sandbox/jquery-plugins/jquery.colorbox-min.js",
					requires : [ "jquery_182" ]
				},
				/**
				 * <a href="http://www.oschina.net/p/fancybox">jQuery fancybox</a>
				 * fancybox 一个jQuery对话框插件
				 * 
				 * @property jquery fancybox
				 * @type {Object}
				 */
				{
					ident : "jqueryplugins8_fancybox_213",
					text : "jQuery fancyBox 2.1.3",
					url : "/js/sandbox/jquery-plugins/fancybox/jquery.fancybox.pack.js",
					style : "/js/sandbox/jquery-plugins/fancybox/jquery.fancybox.css",
					requires : [ "jquery_182" ]
				},
				/**
				 * <a
				 * href="http://www.oschina.net/p/wilq32.rotateimage">jQueryRotate</a>
				 * jQueryRotate 旋转图片的 jQuery 插件
				 * 
				 * @property jquery jQueryRotate
				 * @type {Object}
				 */
				{
					ident : "jqueryplugins9_jquery_rotate_22",
					text : "jQuery Rotate 2.2",
					url : "/js/sandbox/jquery-plugins/jQueryRotate.2.2.js",
					requires : [ "jquery_182" ]
				} ]
	},
	/**
	 * <a href="http://www.oschina.net/p/jqmobi">jQ.Mobi</a>
	 * jQ.Mobi是jQuery的部分重写版本，但针对HTML5和移动设备做了优化
	 * 
	 * @property jquery jQ.Mobi
	 * @type {Object}
	 */
	jqmobi : {
		text : "jqMobi",
		scripts : [ {
			text : "jqMobi.min.js",
			ident : "jqmobi_min",
			url : "/js/sandbox/jqmobi/jq.mobi.min.js",
			style : "/js/sandbox/jqmobi/icons.css"
		}, {
			text : "jq.ui.min.js",
			ident : "jqmobi1_ui",
			requires : [ "jqmobi_min" ],
			url : "/js/sandbox/jqmobi/jq.ui.min.js",
			style : "/js/sandbox/jqmobi/jq.ui.css"
		}, {
			text : "jq.popup.js",
			ident : "jqmobi2_ui",
			requires : [ "jqmobi_min" ],
			url : "/js/sandbox/jqmobi/plugins/jq.popup.js",
			style : "/js/sandbox/jqmobi/plugins/css/jq.popup.css"
		} ]
	},
	others : {
		text : "Others",
		scripts : [
				/**
				 * <a href="http://www.oschina.net/p/dwz">DWZ UI</a>
				 * DWZ富客户端框架(jQuery RIA framework), 是中国人自己开发的基于jQuery实现的Ajax
				 * RIA开源框架.
				 * 
				 * @property DWZ UI
				 * @type {Object}
				 */
				{
					text : "DWZ UI 1.4.4",
					ident : "others_dwzui_144",
					url : "/js/sandbox/dwz-ria-1.4.4/js/dwz.min.js",
					style : [ "/js/sandbox/dwz-ria-1.4.4/themes/css/core.css",
							"/js/sandbox/dwz-ria-1.4.4/themes/css/print.css",
							"/js/sandbox/dwz-ria-1.4.4/themes/css/ieHack.css",
							"/js/sandbox/dwz-ria-1.4.4/themes/css/login.css" ]
				},
				/**
				 * <a href="http://www.oschina.net/p/ztree">zTree</a> zTree 是利用
				 * JQuery 的核心代码，实现一套能完成大部分常用功能的 Tree 插件
				 * 
				 * @property zTree
				 * @type {Object}
				 */
				{
					text : "zTree 3.4",
					ident : "others_ztree_34",
					url : "/js/sandbox/ztree-3.4/jquery.ztree.all-3.4.min.js",
					requires : [ "jquery_182" ]
				},
				/**
				 * <a href="http://www.oschina.net/p/extjs">ExtJS</a> ExtJS
				 * 主要用来开发RIA富客户端的AJAX应用，主要用于创建前端用户界面，与后台技术无关的前端ajax框架。
				 * 
				 * @property ExtJS
				 * @type {Object}
				 */
				{
					ident : "others_extjs_411a",
					text : "ExtJS 4.1.1a",
					url : "/js/sandbox/extjs/ext-all.js",
					style : "/js/sandbox/extjs/resources/css/ext-all.css"
				},
				/**
				 * <a href="http://www.oschina.net/p/backbone">Backbone</a>
				 * Backbone.js 是一种重量级javascript MVC 应用框架
				 * 
				 * @property Backbone.js
				 * @type {Object}
				 */
				{
					text : "Backbone 0.9.1",
					ident : "others_backbone_091",
					url : "/js/sandbox/other/backbone-min.js",
					requires : [ 'others_underscore_133' ]
				},
				/**
				 * <a href="http://www.oschina.net/p/coffeescript">coffeescript</a>
				 * CoffeeScript这一门编程语言构建在JavaScript之上，其被编译成高效的JavaScript
				 * 
				 * @property coffeescript
				 * @type {Object}
				 */
				{
					ident : "others_coffeescript",
					text : "CoffeeScript",
					url : "/js/sandbox/other/coffee-script.js"
				},
				/**
				 * <a href="http://www.oschina.net/p/coffeescript">coffeescript</a>
				 * CoffeeScript这一门编程语言构建在JavaScript之上，其被编译成高效的JavaScript
				 * 
				 * @property coffeescript
				 * @type {Object}
				 */
				{
					ident : "others_es5_shim_124",
					text : "ES5 shim 1.2.4",
					url : "/js/sandbox/other/es5-shim.min.js"
				},
				/**
				 * <a href="http://www.oschina.net/p/lesscss">less</a> Less CSS
				 * 是一个使用广泛的 CSS 预处理器，通过简单的语法和变量对 CSS 进行扩展，可减少很多 CSS 的代码量。
				 * 
				 * @property less
				 * @type {Object}
				 */
				{
					ident : "others_less_130",
					text : "Less 1.3.0",
					url : "/js/sandbox/other/less-1.3.0.min.js"
				},
				/**
				 * <a href="http://www.oschina.net/p/modernizr">Modernizr</a>
				 * Modernizr 是一个利用 JS 和 CSS 来检测浏览器说支持功能的小工具。
				 * 
				 * @property Modernizr
				 * @type {Object}
				 */
				{
					ident : "others_modernizr_262",
					text : "Modernizr 2.6.2",
					url : "/js/sandbox/other/modernizr.min.js"
				},
				/**
				 * <a href="http://www.oschina.net/p/processing_js">processing</a>
				 * Processing.js是一个开放的编程语言，在不使用Flash或Java小程序的前提下,
				 * 可以实现程序图像、动画和互动的应用。
				 * 
				 * @property Modernizr
				 * @type {Object}
				 */
				{
					ident : "others_processing_141",
					text : "Processing 1.4.1",
					url : "/js/sandbox/other/processing-1.4.1.min.js"
				},
				/**
				 * <a href="http://www.oschina.net/p/raphael">Raphaël</a>
				 * Raphaël 是一个小型的 JavaScript 库，用来简化在页面上显示向量图的工作。
				 * 
				 * @property Modernizr
				 * @type {Object}
				 */
				{
					ident : "others_raphael_210",
					text : "Raphael 2.1.0",
					url : "/js/sandbox/other/raphael-min.js"
				},
				/**
				 * <a href="http://www.oschina.net/p/sammy-js">Sammy.js</a>
				 * Sammy.js 是一个微型的 JavaScript 框架用来简化 JavaScript 应用程序的编写
				 * 
				 * @property Sammy.js
				 * @type {Object}
				 */
				{
					ident : "others_sammy_063",
					text : "Sammy 0.6.3",
					url : "/js/sandbox/other/sammy.min.js"
				},
				/**
				 * <a href="http://www.oschina.net/p/jquery+easyui">jQuery
				 * easyui</a> jQuery easyui
				 * 为网页开发提供了一堆的常用UI组件，包括菜单、对话框、布局、窗帘、表格、表单等等.
				 * 
				 * @property jQuery easyui
				 * @type {Object}
				 */
				{
					ident : "others_jquery_easyui_131",
					text : "jQuery easy UI 1.3.1",
					url : "/js/sandbox/jquery-easyui/jquery.easyui.min.js",
					style : "/js/sandbox/jquery-easyui/themes/default/easyui.css"
				},
				/**
				 * <a href="http://www.oschina.net/p/sencha-touch">Sencha Touch</a>
				 * Sencha Touch是专门为移动设备开发应用的Javascript框架。
				 * 
				 * @property Sencha Touch
				 * @type {Object}
				 */
				{
					ident : "others_sencha_touch_2011",
					text : "Sencha Touch 2.0.1.1",
					url : "/js/sandbox/sencha-touch-2.0.1.1/sencha-touch.js",
					style : "/js/sandbox/sencha-touch-2.0.1.1/resources/css/sencha-touch.css"
				},

				{
					ident : "others_twitterlib",
					text : "TwitterLib",
					url : "/js/sandbox/other/twitterlib.min.js"
				},
				/**
				 * <a
				 * href="http://www.oschina.net/p/underscore_js">Underscore.js</a>
				 * Underscore.js 是一个实用的JavaScript工具库，提供了类似 Prototype
				 * 功能的编程支持，但没有对 JavaScript 内置的对象进行扩展。
				 * 
				 * @property Underscore.js
				 * @type {Object}
				 */
				{
					ident : "others_underscore_133",
					text : "underscore 1.3.3",
					url : "/js/sandbox/other/underscore-min.js"
				},
				/**
				 * <a href="http://www.oschina.net/p/zeptojs">Zepto.js</a>
				 * Zepto.js 是支持移动WebKit浏览器的JavaScript框架，具有与jQuery兼容的语法。
				 * 
				 * @property Underscore.js
				 * @type {Object}
				 */
				{
					ident : "others_zepto_10rc1",
					text : "Zepto 1.0rc1",
					url : "/js/sandbox/other/zepto.min.js"
				}, {
					ident : "others_script_aculo_us_183",
					text : "script.aculo.us 1.8.3",
					url : "/js/sandbox/prototype/scriptaculous.js",
					requires : [ "prototype_1710" ]
				},
				/**
				 * <a href="http://www.oschina.net/p/canvastext">CanvasText</a>
				 * CanvasText就是一个Javascript的类库，能让你像使用HTML和CSS的语法来操作canvas绘制文字，使用CanvasText可以少写很多代码就能实现漂亮的效果。
				 * 
				 * @property CanvasText
				 * @type {Object}
				 */
				{
					ident : "others_canvastext_041",
					text : "CanvasText-0.4.1",
					url : "/js/sandbox/other/CanvasText-0.4.1.js"
				},
				/**
				 * <a href="http://www.oschina.net/p/jcanvascript">jCanvaScript</a>
				 * jCanvaScript是一个面向HTML5画布（canvas）的Javascript类库，它提供了许多方法用于简化处理HTML5画布（canvas）元素的内容，只要支持canvas和Javascript的浏览器都可以使用它，包括iPhone、iPad和Android等平台。
				 * 
				 * @property jCanvaScript
				 * @type {Object}
				 */
				{
					ident : "others_jcanvascript_1518",
					text : "jCanvaScript-1.5.18",
					url : "/js/sandbox/other/jCanvaScript.1.5.18.min.js"
				},
				/**
				 * <a
				 * href="http://www.oschina.net/p/jquery-html5-uploader">jQuery
				 * HTML5 Uploader</a> jQuery HTML5 Uploader 是一个轻量级的 jQuery
				 * 插件用来直接从电脑中拖放文件到浏览器并实现上传的功能，目前该功能只支持 Firefox 和 Chrome 浏览器。
				 * 
				 * @property jQuery HTML5 Uploader
				 * @type {Object}
				 */
				{
					ident : "others_jquery_html5_uploader",
					text : "jQuery HTML5 Uploader",
					url : "/js/sandbox/other/jquery.html5uploader.min.js",
					requires : [ "jquery_183" ]
				},
				/**
				 * <a href="http://www.oschina.net/p/kineticjs">jQuery HTML5
				 * Uploader</a> jQuery HTML5 Uploader 是一个轻量级的 jQuery
				 * 插件用来直接从电脑中拖放文件到浏览器并实现上传的功能，目前该功能只支持 Firefox 和 Chrome 浏览器。
				 * 
				 * @property jQuery HTML5 Uploader
				 * @type {Object}
				 */
				{
					ident : "others_kinetic_v412",
					text : "kinetic-v4.1.2",
					url : "/js/sandbox/other/kinetic-v4.1.2.min.js"
				},
				/**
				 * <a href="http://www.oschina.net/p/angularjs">AngularJS</a>
				 * AngularJS (Angular.JS) 是一组用来开发Web页面的框架、模板以及数据绑定和丰富UI组件。
				 * 
				 * @property jQuery HTML5 Uploader
				 * @type {Object}
				 */
				{
					ident : "others_angular_103",
					text : "AngularJS-1.0.3",
					url : "/js/sandbox/other/angular.min.js"
				} ]
	}
};