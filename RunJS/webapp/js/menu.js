/**
 * 按视图划分，Menu负责处理顶部菜单视图的显示及操作
 * @class Menu
 */
Menu = (function() {

	var PT = Menu.prototype;
	
	var instance;

	/**
	 * 默认配置参数
	 * @attribute defaultParams
	 * @private
	 */
	PT.defaultParams = {
		viewLink : editor_path+"/menu",
		msgLink:"/msg",
		interval_time:1000*60
	}

	/**
	 * 事件绑定规则定义，详情见 {{#crossLink "Utils"}}{{/crossLink}} 类中的 {{#crossLink
	 * "Utils/binder"}}{{/crossLink}} 方法
	 * 
	 * @property Events
	 * @type {JSON}
	 */
	PT.Events = {
		"click->#save" : function() {
			flow.of(runjs).save(1)
		},
		"click->#share" : function() {
			flow.of(runjs).share(1)
		},
		"click->#editor_setting" : "editor_setting",
		"mouseover->#logout" : function() {
			$(this).find("ul").show()
		},
		"mouseout->#logout" : function() {
			$(this).find("ul").hide()
		},
		"click->#help" : "help",
		"change->.fontsize select" : "setFont",
		"click->#msg_count":"view_notify"
	};

	/** 
	 * @class Menu
	 * @constructor
	 */
	function Menu() {
		this.arg = arguments;
		this.clazz = className(this);
		g_utils.initParams.call(this, this.defaultParams);
		this.initView.call(this);
		instance = this;
		this.checkMsg = setInterval(function(){
			checkMsg();
		},instance.interval_time);
		return this;
	}
	
	/**
	 * 检查是否有最新的消息提醒
	 * @method checkMsg
	 * @private
	 */
	var checkMsg = function(){
		g_utils.load(instance.msgLink,true,function(msg){
			var count = parseInt(msg);
			if(count==0){
				$("#msg_count").hide();
			}else{
				instance.showMsg(count);
			}
		});
	};

	/**
	 * 菜单视图初始化
	 * @method initView
	 * @param {String} ident 代码唯一标识
	 * @param {Boolean} async 是否采取异步方式初始化视图
	 */
	PT.initView = function(ident,async) {
		if (isEmpty(ident))
			ident = g_status.ident;
		g_utils.initView.call(this, ident, function(ident, view) {

		},async);
		g_utils.binder.call(this);
	}

	/**
	 * 显示编辑器菜单，并处理其中的设置
	 * @method editor_setting
	 */
	PT.editor_setting = function() {
		var d = dialog.get("editor_setting", doNothing).dialog;
		$("#setting_list li").click(function() {
			var idx = $(this).index();
			dialog.onSettingItemClick(d,idx);
		});
		// 初始化设置值
		// 主题初始化
		$(".theme_list li").removeClass("current");
		$(".theme_list li." + Setting.theme).addClass("current");
		// 字体初始化
		var ff = dialog.getField(d, ".fontsize select", 0);
		var fs = dialog.getField(d, ".fontsize select", 1);
		if (isNotEmpty(Setting.fontfamily))
			ff.val(Setting.fontfamily);
		if (isNotEmpty(Setting.fontsize))
			fs.val(Setting.fontsize);
	}
	
	/**
	 * 显示消息提醒
	 * @method showMsg
	 * @param {Number} count 显示提醒条数
	 */
	PT.showMsg = function(count){
		$("#msg_count").show().html(count);
	};
	
	/**
	 * 标记提醒已读
	 * @method ready_msg
	 * @param {Array} ids 需要标记的id数组或唯一id
	 */
	PT.read_msg = function(ids){
		var success = function(msg){
			var ids2 = $.makeArray(ids)
			$.each(ids2,function(i,id){
				$("#m_"+id).replaceWith('<span class="btn_read">已阅</span>');
			});
			checkMsg();
		};
		var callback = function(msg) {
			g_utils.errorHandler(msg, success, doNothing);
		}
		Api.ajax.read_msg(ids,callback);
	};
	
	/**
	 * 查看消息提醒
	 * @method view_notify
	 */
	PT.view_notify=function(){
		var clear_all_msg = function(d,rd){
			var success = function(msg){
				$("#msg_count").hide();
				rd.close();
			};
			var fail = function(msg){
				rd.close();
			};
			var callback = function(msg) {
				g_utils.errorHandler(msg, success, fail);
			}
			Api.ajax.read_all_msg(0,callback);
			return false;
		};
		var d = dialog.get("view_notify",doNothing,clear_all_msg).dialog;
		$(".setting_list li").click(function() {
			var idx = $(this).index();
			dialog.onSettingItemClick(d,idx);
		});
	}

	/**
	 * 显示帮助菜单
	 * @method help
	 */
	PT.help = function() {
		dialog.get("f1_help", doNothing);
		$(".setting_list li").click(function() {
			var idx = $(this).index();
			$(".setting_list li").removeClass("item_focus");
			$(this).addClass("item_focus");
			$(".setting_right .right_content ").removeClass("setting_on").addClass("setting_off");
			$(".setting_right .right_content :eq(" + idx + ")").removeClass("setting_off").addClass("setting_on");
			$(".list_item_focus").css({
				top : 10 + idx * 41
			});
		});
	}
	
	/**
	 * 设置编辑器主题
	 * @method setTheme
	 * @param {String} theme 主题类型
	 * @param {Boolean} store 是否保存到服务器
	 */
	PT.setTheme = function(theme, store) {
		if (isNotEmpty(store) && store && g_status.login) {
			var success = function(msg) {
				Setting.theme = theme;
				runjs.editor.setTheme();
				$(".theme_list li").removeClass("current");
				$(".theme_list li." + Setting.theme).addClass("current");
			};
			var fail = function(msg) {
			};
			var callback = function(msg) {
				g_utils.errorHandler(msg, success, fail);
			}
			Api.ajax.setting("theme", theme, callback);
		} else {
			Setting.theme = theme;
			runjs.editor.setTheme();
			$(".theme_list li").removeClass("current");
			$(".theme_list li." + Setting.theme).addClass("current");
		}

	}

	/**
	 * 设置字体，字体根据当前绑定的select元素值来设置
	 * @method setFond
	 */
	PT.setFont = function() {
		var cur = $(this);
		if (cur.attr("class").indexOf("size") > -1) {
			// 设置font-size
			Setting.fontsize = $(cur).val();
			runjs.editor.setCMFont();
			if (g_status.login) {
				var callback = function(msg) {
					g_utils.errorHandler(msg, doNothing, doNothing);
				}
				Api.ajax.setting("fontsize", Setting.fontsize, callback);
			}
		} else {
			// 设置font-family
			Setting.fontfamily = $(cur).val();
			runjs.editor.setCMFont();
			if (g_status.login) {
				var callback = function(msg) {
					g_utils.errorHandler(msg, doNothing, doNothing);
				}
				Api.ajax.setting("fontfamily", Setting.fontfamily, callback);
			}
		}
	}

	/**
	 * 添加建议，该方法必须在弹出help窗口后才能被调用
	 * @method addAdvice
	 */
	PT.addAdvice = function() {
		var d = $("#editorSetting");
		var email = dialog.getField(d, "input", 0);
		var content = dialog.getField(d, "textarea", 0);
		var captcha = dialog.getField(d, "input", 1);
		if ($.trim(email.val()).length == 0) {
			dialog.errorMsg(d, "Email 不能为空", ".advice");
			email.focus();
			return false;
		} else if (email.val().match(/^\w+([-+.']\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/) == null) {
			dialog.errorMsg(d, "Email 格式错误", ".advice");
			email.focus();
			return false;
		}
		if ($.trim(content.val()).length == 0 || $.trim(content.val()).length > 1000) {
			dialog.errorMsg(d, "反馈内容长度为1~1000字符", ".advice");
			content.focus();
			return false;
		}
		if ($.trim(captcha.val()).length == 0) {
			dialog.errorMsg(d, "验证码不能为空", ".advice");
			captcha.focus();
			return false;
		}
		var success = function(msg) {
			d.find(".advice").html('<div style="text-align:center;color:#40AA53;font-size:20px;font-weight:bold;top:150px;position:relative;">感谢您的反馈和意见！</div>');
		};
		var fail = function(msg) {
			dialog.errorMsg(d, msg.msg, ".advice");
		};
		var callback = function(msg) {
			g_utils.errorHandler(msg, success, fail,true);
		};
		Api.ajax.add_advice(captcha.val(), g_status.ident, email.val(), content.val(), callback);
	}

	return Menu;

})();