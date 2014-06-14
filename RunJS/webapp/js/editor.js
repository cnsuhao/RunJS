/**
 * 按视图划分，Editor负责编辑器视图的显示及操作
 * 
 * @class Editor
 */
Editor = (function() {

	var PT = Editor.prototype;

	var instance;

	/**
	 * 默认配置参数
	 * 
	 * @attribute defaultParams
	 * @private
	 */
	PT.defaultParams = {
		viewLink : buildString(editor_path, "/editor"),
		tplLink : buildString(editor_path, "/template"),
		pluginTplLink : buildString(editor_path, "/plugin_tpl"),
		ident : "",
		theme : "night"
	};

	/**
	 * 事件绑定规则定义，详情见 {{#crossLink "Utils"}}{{/crossLink}} 类中的 {{#crossLink
	 * "Utils/binder"}}{{/crossLink}} 方法
	 * 
	 * @property Events
	 * @type {JSON}
	 */
	PT.Events = {
		"mouseover->.editor" : "show_quick_tools",
		"mouseout->.editor" : "hide_quick_tools"
	};

	/**
	 * 显示视图右上角快速工具条
	 * 
	 * @method show_quick_tools
	 */
	PT.show_quick_tools = function() {
		$(this).find(".quick_tools").show();
	};

	/**
	 * 隐藏视图右上角快速工具条
	 * 
	 * @method hide_quick_tools
	 */
	PT.hide_quick_tools = function() {
		$(this).find(".quick_tools").hide();
	};

	/**
	 * @class Editor
	 * @constructor
	 */
	function Editor() {
		instance = this;
		this.arg = arguments;
		this.clazz = className(this);
		g_utils.initParams.call(this, this.defaultParams);
		this.initView.call(this);
		return this;
	}

	/**
	 * 编辑器视图初始化
	 * 
	 * @method initView
	 * @param {String}
	 *            ident 代码唯一标识
	 * @param {Boolean}
	 *            async 是否采取异步方式初始化视图
	 */
	PT.initView = function(ident, async) {
		if (isEmpty(ident))
			ident = g_status.ident;
		g_utils.initView.call(this, ident, function(ident, view) {
			this.initCodeMirror(view);
			this.editorSets = $(".editorSet");
			this.setTheme(Setting.theme);
			this.setCMFont();
			$(window).resize();
			this.updatePreview("", true);
			instance.refreshEditors();
		}, async);
		g_utils.binder.call(this);
		if (isNotEmpty(g_status.codename))
			$("title").html(buildString(g_status.codename, "-RunJS"));
	};

	/**
	 * 选择JS编辑类型：JavaScript或CoffeeScript
	 * 
	 * @method chooseJsType
	 */
	PT.chooseJsType = function() {
		var doNow = function(d, rd) {
			var jstype = dialog.getField(d, "input[name='choose_js_type']:checked", 0);
			if (jstype.length == 0) {
				dialog.errorMsg(d, "请选择其中一个类别");
				return false;
			}
			var success = function(msg) {
				runjs.initStatus(g_status.ident);
				instance.initView(g_status.ident);
				rd.close();
			};
			var fail = function(msg) {
				dialog.errorMsg(d, msg.msg);
			};
			var callback = function(msg) {
				g_utils.errorHandler(msg, success, fail, true);
			};
			Api.ajax.set_code_type(g_status.codeid, "js", jstype.val(), callback);
			return false;
		};
		dialog.get(buildString("choose_code_type/js/", g_status.ident), doNothing, doNow);
	};

	/**
	 * 选择CSS编辑类型：CSS 或 LESS
	 * 
	 * @method chooseCssType
	 */
	PT.chooseCssType = function() {
		var doNow = function(d, rd) {
			var csstype = dialog.getField(d, "input[name='choose_css_type']:checked", 0);
			if (csstype.length == 0) {
				dialog.errorMsg(d, "请选择其中一个类别");
				return false;
			}
			var success = function(msg) {
				runjs.initStatus(g_status.ident);
				instance.initView(g_status.ident);
				rd.close();
			};
			var fail = function(msg) {
				dialog.errorMsg(d, msg.msg);
			};
			var callback = function(msg) {
				g_utils.errorHandler(msg, success, fail, true);
			};
			Api.ajax.set_code_type(g_status.codeid, "css", csstype.val(), callback);
			return false;
		};
		dialog.get(buildString("choose_code_type/css/", g_status.ident), doNothing, doNow);
	};

	/**
	 * 获取当前代码的MD5值
	 * 
	 * @method codeMD5
	 */
	PT.codeMD5 = function() {
		return CryptoJS.MD5(buildString(instance.editorHtml.getValue(), instance.editorJs.getValue(), instance.editorCss.getValue())).toString();
	}
	/**
	 * 给三个视图异步加载代码
	 * 
	 * @method loadTemplate
	 * @param {String}
	 *            ident 代码唯一标识，ident为空则加载默认模版代码，ident不为空则尝试加载用户的对应代码
	 */
	PT.loadTemplate = function(ident) {
		var cur = this;
		if (isEmpty(ident) && isEmpty(g_status.ident))
			ident = "";
		else
			ident = g_status.ident;
		$.get(buildString(this.tplLink, "/html/", ident), function(html) {
			cur.editorHtml.setValue(html);
		})
		$.get(buildString(this.tplLink, "/css/", ident), function(css) {
			cur.editorCss.setValue(css);
		})
		$.get(buildString(this.tplLink, "/js/", ident), function(js) {
			cur.editorJs.setValue(js);
		})
	}

	/**
	 * 加载插件模版代码
	 * 
	 * @method loadPlugin
	 * @param {String}
	 *            ident 代码唯一标识
	 * @param {String}
	 *            pname 插件类名
	 * @param {Number}
	 *            onMethod {{#crossLink "Plugins/onEvents"}}{{/crossLink}} 个数
	 */
	PT.loadPlugin = function(ident, pname, onMethod) {
		if (isEmpty(ident))
			ident = "noident";
		var js = g_utils.load(buildString(this.pluginTplLink, "/", ident, "/", pname, "/", onMethod), false);
		return {
			html : "",
			css : "",
			js : js
		};
	}

	/**
	 * 加载插件模版代码
	 * 
	 * @method loadPluginTpl
	 * @param {String}
	 *            ident 代码唯一标识
	 * @param {Number}
	 *            onMethod {{#crossLink "Plugins/onEvents"}}{{/crossLink}} 个数
	 */
	PT.loadPluginTpl = function(ident, onMethod) {
		var codes = instance.loadPlugin(ident, "PluginName", onMethod);
		instance.editorJs.setValue(codes.js);
		instance.editorHtml.setValue(codes.html);
		instance.editorCss.setValue(codes.css);
	};

	/**
	 * 检查并设置编辑器编辑状态
	 * 
	 * @method setEditedStatus
	 */
	PT.setEditedStatus = function() {
		var curHash = instance.codeMD5();
		if (isEmpty(instance.contentHash) || instance.contentHash.length == 0) {
			// 首次加载初始化hash
			instance.contentHash = buildString(curHash, "_", g_status.ident);
			instance.edited = false;
		} else {
			var ident = instance.contentHash.split("_")[1];
			var hash = instance.contentHash.split("_")[0];
			if (ident == g_status.ident && hash != curHash) {
				instance.edited = true;
				$("title").html(buildString("*", $("title").html().replace(/[*]/g, "")));
			} else {
				if (ident != g_status.ident) {
					instance.contentHash = buildString(curHash, "_", g_status.ident);
				}
				instance.edited = false;
				$("title").html($("title").html().replace(/[*]/g, ""));
			}
		}
	}

	/**
	 * 移除编辑状态
	 * 
	 * @method removeEditedStatus
	 */
	PT.removeEditedStatus = function() {
		instance.edited = false;
		instance.contentHash = buildString(instance.codeMD5(), "_", g_status.ident);
		$("title").html($("title").html().replace(/[*]/g, ""));
	}

	/**
	 * 以同步方式加载模版代码
	 * 
	 * @method getRemoteCode
	 * @param {String}
	 *            ident 代码唯一标识
	 * @param {String}
	 *            type 代码片段类型'html'|'css'|'js'
	 * @return {JSON} code 返回{html:"",css:"",js:""}结构数据
	 */
	PT.getRemoteCode = function(ident, type) {
		return this.loadTplSnippet(ident, type, false);
	}

	/**
	 * 从远程加载代码片段，如果ident为空，则加载模版代码片段
	 * 
	 * @method loadTplSnippet
	 * @param {String}
	 *            ident 代码唯一标识
	 * @param {String}
	 *            type 代码片段类型'html'|'css'|'js'
	 * @param {Boolean}
	 *            async 是否采用异步方式加载
	 * @param {Function}
	 *            callback 异步方式的回调函数
	 * @return {JSON} code 返回{html:"",css:"",js:""}结构数据
	 */
	PT.loadTplSnippet = function(ident, type, async, callback) {
		var cur = this;
		if (isEmpty(ident))
			ident = "";
		if (isEmpty(async))
			async = true;
		switch (type) {
		case "html":
		case "css":
		case "js":
			return $.ajax({
				url : buildString(this.tplLink, "/", type + "/", ident),
				success : function(html) {
					if (isFunc(callback))
						callback.call(cur, html);
				},
				async : async
			}).responseText
			break;
		default:
			return {
				html : this.loadTplSnippet(ident, "html", async),
				css : this.loadTplSnippet(ident, "css", async),
				js : this.loadTplSnippet(ident, "js", async)
			};
		}
	}

	/**
	 * 获取当前编辑器内textarea代码
	 * 
	 * @method getCodeOfView
	 * @param {Object}
	 *            view 视图选择器
	 * @param {String}
	 *            type 编辑器类型 'html'|'css'|'js'
	 */
	PT.getCodeOfView = function(view, type) {
		if (isNotEmpty(view) && isNotEmpty(type)) {
			return view.find("#code_" + type).val();
		}
	}

	/**
	 * 初始化CodeMirror
	 * 
	 * @method initCodeMirror
	 * @param {Object}
	 *            view 视图选择器
	 * @param {Boolean}
	 *            是否使用编辑器textarea的默认代码
	 */
	PT.initCodeMirror = function(view, defCode) {
		var cur = this;
		var delay;
		var html, css, js;
		if (isEmpty(defCode) || defCode) {
			html = this.getDefaultEditorValue("html");
			css = this.getDefaultEditorValue("css");
			js = this.getDefaultEditorValue("js");
		} else {
			html = this.getCodeOfView(view, "html");
			css = this.getCodeOfView(view, "css");
			js = this.getCodeOfView(view, "js");
		}
		if (isNotEmpty(view)) {
			this.editorHtml = new CodeMirror(document.getElementById("code_html").parentElement, {
				mode : "text/html",
				tabSize : 2,
				indentWithTabs : true,
				value : html,
				lineNumbers : true,
				lineWrapping : true,
				matchBrackets : true,
				onChange : function(cm) {
					if (isNotEmpty(delay))
						clearTimeout(delay);
					delay = setTimeout(function() {
						cur.updatePreview();
					}, 300)
					if (isFunc(instance.onEditorHtmlChange))
						instance.onEditorHtmlChange(cm);
					plugins.fireEvent.call(cm, "onHtmlEditorChange", cm);
				},
				onCursorActivity : function(cm) {
					plugins.fireEvent.call(cm, "onHtmlCursorActivity", cm);
				}
			});
			this.editorCss = new CodeMirror(document.getElementById("code_css").parentElement, {
				mode : g_status.cssType == 1 ? "css" : "less",
				tabSize : 2,
				indentWithTabs : true,
				value : css,
				lineNumbers : true,
				lineWrapping : true,
				matchBrackets : true,
				onChange : function(cm) {
					if (isNotEmpty(delay))
						clearTimeout(delay);
					delay = setTimeout(function() {
						cur.updatePreview();
					}, 300)
					if (isFunc(instance.onEditorCssChange))
						instance.onEditorCssChange(cm);
					plugins.fireEvent.call(cm, "onCssEditorChange", cm);
				},
				onCursorActivity : function(cm) {
					plugins.fireEvent.call(cm, "onCssCursorActivity", cm);
				}
			});
			this.editorJs = new CodeMirror(document.getElementById("code_js").parentElement, {
				mode : g_status.jsType == 1 ? "javascript" : "coffeescript",
				tabSize : 2,
				indentWithTabs : true,
				value : js,
				lineNumbers : true,
				lineWrapping : true,
				matchBrackets : true,
				onChange : function(cm) {
					if (isNotEmpty(delay))
						clearTimeout(delay);
					delay = setTimeout(function() {
						cur.updatePreview();
					}, 300)
					if (isFunc(instance.onEditorJsChange))
						instance.onEditorJsChange(cm);
					plugins.fireEvent.call(cm, "onJsEditorChange", cm);
				},
				onCursorActivity : function(cm) {
					plugins.fireEvent.call(cm, "onJsCursorActivity", cm);
				}
			});
			// '隐藏'textarea
			this.target.find("textarea").attr("style", "position: absolute; left: -10000px; width: 10px;");
			// 编辑器大小调整
			this.h_handler = $(".handler_horizontal").TextAreaResizer({
				vertical : false,
				html : this.editorHtml,
				css : this.editorCss,
				js : this.editorJs
			});
			this.v_handler = $(".handler_vertical").TextAreaResizer({
				vertical : true,
				html : this.editorHtml,
				css : this.editorCss,
				js : this.editorJs
			});
		} else {

		}
	};

	/**
	 * 获取CodeMirro编辑器代码
	 * 
	 * @method getEditorCode
	 * @param {String}
	 *            type 编辑器类型 'html'|'css'|'js'
	 */
	PT.getEditorCode = function(type) {
		switch (type) {
		case "html":
			return this.editorHtml.getValue();
		case "css":
			return this.editorCss.getValue();
		case "js":
			return this.editorJs.getValue();
		default:
			return {
				html : this.getEditorCode("html"),
				css : this.getEditorCode("css"),
				js : this.getEditorCode("js")
			}
		}
	}

	/**
	 * 获取HTML+JS+CSS组合过后的最终页面代码，用于在预览视图中实时显示
	 * 
	 * @method getCombinedHtml
	 */
	PT.getCombinedHtml = function() {
		var js = "", css = "";
		var html = this.editorHtml.getValue();
		var temp = "";
		if (html.indexOf("</body>") > -1) {
			var body = [];
			body.push(html.substring(0, html.lastIndexOf("</body>")));
			body.push(html.substring(html.lastIndexOf("</body>")));
			html = body[0];
			temp = body.length == 2 && body[1] ? body[1] : "";
		}
		try {
			if (g_status.cssType == 1) {
				css = this.editorCss.getValue();
			} else if (g_status.cssType == 2) {
				css = g_utils.load("/action/ajax/less_compile", false, undefined, this.editorCss.getValue());
			}
		} catch (e) {
			return html + temp;
		}
		try {
			if (g_status.jsType == 1) {
				js = this.editorJs.getValue();
			} else if (g_status.jsType == 2) {
				js = CoffeeScript.compile(this.editorJs.getValue());
			}
		} catch (e) {
			return buildString(html, "<style>", css, "</style>", temp);
		}
		return buildString(html, "<script>try{\n", js, "\n}catch(e){\n}</script>", "<style>", css, "</style>", temp);
	}

	/**
	 * 更新视图的定时器实例，用来实现延时更新
	 * 
	 * @attribute update_delay
	 * @private
	 */
	var update_delay;

	/**
	 * 更新预览视图的内容
	 * 
	 * @method updatePreview
	 * @param {String}
	 *            chtml 组合过后的HTML+CSS+JS代码
	 * @param {Boolean}
	 *            是否强制更新
	 */
	PT.updatePreview = function(chtml, update) {
		instance.setEditedStatus();
		if (isNotEmpty(update_delay) && g_status.cssType == 2) {
			Console.log(buildString("clear timeout(" , update_delay , ")"));
			clearTimeout(update_delay);
		}
		update_delay = setTimeout(function() {
			if (!g_status.isOtherCode && (g_status.mode == "plugin" && isNotEmpty(update) || g_status.mode == "code")) {
				var pre_wrapper = $(".preview");
				pre_wrapper.find("iframe").remove();
				var previewFrame;
				if (g_status.mode == "plugin" && g_mode == "code" && isNotEmpty(update)) {
					var src = buildString(g_status.host , editor_path , '/' , g_status.ident , "?mode=plugin");
					previewFrame = pre_wrapper.append(buildString('<iframe id="preview" src="' , src , '" frameborder="0"></iframe>')).find("iframe")[0];
				} else {
					previewFrame = pre_wrapper.append('<iframe id="preview" frameborder="0"></iframe>').find("iframe")[0];
					var preview = previewFrame.contentDocument || previewFrame.contentWindow.document;
					preview.open();
					var html = isNotEmpty(chtml) && typeOf(chtml, 'string') ? chtml : instance.getCombinedHtml();
					preview.write(html);
					preview.close();
				}
			}
		}, g_status.cssType == 2 ? 1000 : 1);
	}

	/**
	 * 获取默认代码
	 * 
	 * @method getDefaultEditorValue
	 * @param {String}
	 *            type 代码类型 'html'|'css'|'js'
	 */
	PT.getDefaultEditorValue = function(type) {
		return isNotEmpty(this[type]) ? this[type] : $("#code_" + type).val();
	}

	/**
	 * 根据全局变量 Setting 中的theme属性设置主题，当前只是 default 和 night两种主题
	 * 
	 * @method setTheme
	 */
	PT.setTheme = function() {
		if (isNotEmpty(Setting.theme) && (Setting.theme == "default" || Setting.theme == "night")) {
			this.editorHtml.setOption("theme", Setting.theme);
			this.editorCss.setOption("theme", Setting.theme);
			this.editorJs.setOption("theme", Setting.theme);
			if (Setting.theme == "night")
				$("body").attr("class", "NightTheme");
			else
				$("body").attr("class", "DefaultTheme");
		}
	}

	/**
	 * 设置CodeMirror字体，根据全局变量 Setting 中的fontfamily和fontsize属性设置字体
	 * 
	 * @method setCMFont
	 */
	PT.setCMFont = function() {
		$(".CodeMirror").ready(function() {
			// 初始化字体
			var fontsize = 12;
			var fontfamily = "consolas";

			if (typeof Setting.fontsize != 'undefined') {
				fontsize = Setting.fontsize;
			} else {
				Setting.fontsize = fontsize;
			}

			if (typeof Setting.fontfamily != 'undefined') {
				fontfamily = Setting.fontfamily;
			} else {
				Setting.fontfamily = fontfamily;
			}

			$(".CodeMirror").css({
				"font-family" : fontfamily,
				"font-size" : fontsize + "px"
			});

			instance.refreshEditors();
		});
	}

	/**
	 * 刷新编辑器，当编辑器外观或大小被改变时调用
	 * 
	 * @method refreshEditors
	 */
	PT.refreshEditors = function() {
		this.editorHtml.refresh()
		this.editorCss.refresh()
		this.editorJs.refresh()
	};

	return Editor;
})();