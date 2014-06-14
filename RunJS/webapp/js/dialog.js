/**
 * 该类用来处理弹出框，使用 <a href="http://www.oschina.net/p/zebra_dialog" target="_blank">Zebra Dialog</a> 插件实现
 * 
 * @class Dialog
 */
Dialog = (function() {

	var PT = Dialog.prototype;

	var instance;

	PT.defaultParams = {
		viewLink : editor_path + "/dialog",
		ident : ""
	}

	PT.Events = {};

	/**
	 *@class Dialog 
	 *@constructor
	 */
	function Dialog() {
		instance = this;
		this.arg = arguments;
		this.clazz = className(this);
		g_utils.initParams.call(this, this.defaultParams);
		return this;
	}

	/**
	 * 
	 * 获取一个 dialog 并显示，第一个参数为 dialog 的标识名称，对应 option 中的一条配置信息以及 dialog 模版内容；
	 * 第二个参数之后，遇到 string 则将其当作候补 dialog 模版内容：若 ajax 加载远端模版内容为空时，则当作候补内容显示；
	 * 遇到 function 或者 array 则将其中的 function 全部作为 events 中的元素，按照 option 中的 buttons 的 callback 依次注册事件
	 * @method get
	 * @param {String} name 对话框名称
	 * @param {String} msg 对话框中显示的描述信息（当无法从远端加载对应的内容时显示，也可以在公用一个对话框时用来显示描述信息，例如确认操作窗口）
	 * @param {Function} callback 对话框中的按钮对应的处理事件，顺序与界面上的排序相反
	 * @return {Object} dialog 返回 Zebra Dialog 实例
	 * @example
	 * 	var d = dialog.get("confirm2",doNothing,doNothing).dialog;//弹出一个Zebra Dialog，并获取 Dialog 的 HTML 内容
	 */
	PT.get = function() {
		if (arguments.length < 1 || !typeOf(arguments[0], "string"))
			return;
		var name = arguments[0].split("/")[0];
		var events = [];
		var content;
		for ( var i = 1; i < arguments.length; i++) {
			var cur = arguments[i];
			if (isFunc(cur))
				events.push(cur);
			if (isArray(cur)) {
				$.each(cur, function(i, k) {
					if (isFunc(k))
						events.push(k);
				})
			}
			if (typeOf(cur, "string"))
				content = cur;
		}
		if (isArray(content))
			events = content;
		var opt = this.option[name];
		if (isNotEmpty(opt)) {
			// 为dialog加载显示内容
			var html = g_utils.load(this.viewLink + "/" + arguments[0], false);
			if (isEmpty($.trim(html)) && isNotArray(content) && isNotFunc(content)) {
				html = content;
			}
			// 为dialog注册事件
			var btns = opt.buttons;
			if (isArray(btns)) {
				$.each(btns, function(key, value) {
					if (isFunc(events[key]))
						value.callback = events[key];
				})
			}
			this.cur_dialog = new $.Zebra_Dialog(html, opt);
			plugins.fireEvent("onDialogLoad", this.cur_dialog);
			return this.cur_dialog;
		}
	};

	/**
	 * 从对话框中寻找某控件或元素
	 * @method getField
	 * @param {String} d Zebra Dialog 实例的 html 内容
	 * @param {String} type 查找条件，jQuery选择器
	 * @param {Number} idx 索引
	 * @return {Object} element 返回该控件或元素，未找到则返回 undefined
	 * @example
	 * 	var d = dialog.get("confirm2",doNothing,doNothing).dialog;//弹出一个Zebra Dialog，并获取 Dialog 的 HTML 内容
	 * 	dialog.getField(d,"input[type='checkbox']",0);//从对话框中查找第一个(索引为0)的 checkbox 控件
	 */
	PT.getField = function(d, type, idx) {
		if (isNotEmpty(d) && isNotEmpty(type)) {
			if (isEmpty(idx) || !typeOf(idx, 'number')) {
				idx = 0;
			}
			try {
				return $(d).find(type + ":eq(" + idx + ")");
			} catch (e) {
				return;
			}
		}
	}

	/**
	 * 通过 getField 拿到对话框中的某元素 并获取它的 value
	 * @method valueOf
	 * @param {String} d Zebra Dialog 实例的 html 内容
	 * @param {String} type 查找条件，jQuery选择器
	 * @param {Number} idx 索引
	 * @return {String} value 返回当前元素的value 未找到元素则返回 undefined
	 * @example
	 * 	var d = dialog.get("confirm2",doNothing,doNothing).dialog;//弹出一个Zebra Dialog，并获取 Dialog 的 HTML 内容
	 * 	dialog.valueOf(d,"input[type='checkbox']",0)//在对话框中查找第一个(索引为0)的 checkbox 控件，并获得其 value
	 */
	PT.valueOf = function(d, type, idx) {
		var e = instance.getField(d, type, idx);
		return isNotEmpty(e) ? e.val() : undefined;
	}

	/**
	 * 通过 getField 拿到对话框中的某元素 并设置它的 value
	 * @method setValueOf
	 * @param {String} d Zebra Dialog 实例的 html 内容
	 * @param {String} type 查找条件，jQuery选择器
	 * @param {Number} idx 索引
	 * @param {Object} value 设置值
	 * @example
	 * 	var d = dialog.get("confirm2",doNothing,doNothing).dialog;//弹出一个Zebra Dialog，并获取 Dialog 的 HTML 内容
	 * 	dialog.setValueOf(d,"input[type='checkbox']",0,"1212")//在对话框中查找第一个(索引为0)的 checkbox 控件，并获得其 value
	 */
	PT.setValueOf = function(d, type, idx, value) {
		var e = instance.getField(d, type, idx);
		if (isNotEmpty(e))
			e.val(value);
	}

	/**
	 * 在对话框中显示错误信息
	 * @method errorMsg
	 * @param {String} d Zebra Dialog 实例的 html 内容
	 * @param {String} msg 要显示的错误信息
	 * @param {String} sel 在该选择器下寻找错误显示区域
	 * @return {Boolean} 正常显示错误返回true，否则返回false
	 * @example
	 * 	var d = dialog.get("confirm2",doNothing,doNothing).dialog;//弹出一个Zebra Dialog，并获取 Dialog 的 HTML 内容
	 * 	dialog.errorMsg(d,"代码名称不能为空");//在对话框中显示错误信息
	 */
	PT.errorMsg = function(d, msg, sel) {
		if (isEmpty(msg) || isEmpty(d))
			return false;
		try {
			var em;
			if (isEmpty(sel)) {
				em = d.find(".errorMsg");
			} else {
				em = d.find(sel + " .errorMsg");
			}
			em.html(msg);
			return true;
		} catch (e) {
			return false;
		}
	}

	/**
	 * 用来处理设置菜单左侧选项被点击的动作
	 * @method onSettingItemClick
	 * @param {String} d Zebra Dialog 实例的 html 内容
	 * @param {Number} step 激活第 step 个选项
	 * @example
	 * 	var d = dialog.get("confirm2",doNothing,doNothing).dialog;//弹出一个Zebra Dialog，并获取 Dialog 的 HTML 内容
	 * 	dialog.onSettingItemClick(d,0);//激活设置窗口第一个选项
	 */
	PT.onSettingItemClick = function(d, step) {
		var left_ul = dialog.getField(d, ".setting_content .setting_list");
		var right_content = dialog.getField(d, ".setting_right").find(".right_content");
		var arrow = dialog.getField(d, ".list_item_focus");
		var lis = left_ul.find("li").removeClass("item_focus");
		var cur = lis.eq(step).addClass("item_focus");
		right_content.removeClass("setting_on").addClass("setting_off");
		right_content.eq(step).removeClass("setting_off").addClass("setting_on");
		arrow.css('top', (10 + step * 41));
	}

	PT.events = {

	};

	/**
	 * 定义了所有的Zebra Dialog对话框的配置参数
	 * @property option 
	 * @type {JSON}
	 */
	PT.option = {
		success : {
			'single' : true,
			'buttons' : false,
			'modal' : false,
			'type' : 'confirmation',
			'auto_close' : 1000
		},
		success2 : {
			'title' : '操作成功',
			'modal' : false,
			'type' : 'confirmation',
			'buttons' : [ {
				caption : '确认',
				enter : true,
				callback : function(d) {
					return dialog.action.success_confirm(d);
				}
			} ]
		},
		error : {
			'buttons' : false,
			'modal' : false,
			'type' : 'error',
			'auto_close' : 2000
		},
		jserror : {
			'title' : '脚本错误',
			'modal' : false,
			'type' : 'error',
			'buttons' : [ {
				caption : '取消'
			} ]
		},
		error2 : {
			'title' : '错误提示',
			'modal' : false,
			'type' : 'error',
			'buttons' : [ {
				caption : '确认',
				callback : function(d) {
					return dialog.action.error_confirm(d);
				}
			} ]
		},
		create_project : {
			'title' : '创建新代码',
			'modal' : true,
			'type' : 'question',
			'buttons' : [ {
				caption : '取消',
				callback : function(d) {
					if (typeof dialog.action.cancel != 'undefined')
						return dialog.action.cancel(d);
					else
						return true;
				}
			}, {
				caption : '创建',
				enter : true,
				callback : function(d) {
					return dialog.action.create(d)
				}
			} ]
		},
		login : {
			'title' : '登录方式',
			'modal' : true,
			'width' : 460,
			'type' : false,
			'buttons' : [ {
				caption : '取消'
			} ]
		},
		fork : {
			'title' : 'Fork 当前代码',
			'modal' : true,
			'type' : 'question',
			'buttons' : [ {
				caption : '取消',
				callback : function(d) {
					if (typeof dialog.action.cancel != 'undefined')
						return dialog.action.cancel(d);
					else
						return true;
				}
			}, {
				caption : '确认',
				enter : true,
				callback : function(d) {
					return dialog.action.create(d)
				}
			} ]
		},
		delete_file : {
			'title' : '删除',
			'modal' : true,
			'type' : 'question',
			'buttons' : [ {
				caption : '取消',
				callback : function(d) {
					d.close(true);
				}
			}, {
				caption : '确认删除',
				enter : true,
				callback : function(d) {
					return dialog.action.delete_(d);
				}
			} ]
		},
		delete_code : {
			'title' : '删除',
			'modal' : true,
			'type' : 'question',
			'buttons' : [ {
				caption : '取消',
				callback : function(d) {
					d.close(true);
				}
			}, {
				caption : '确认删除',
				enter : true,
				callback : function(d) {
					return dialog.action.delete_(d);
				}
			} ]
		},
		rename : {
			'title' : '重命名',
			'modal' : true,
			'type' : 'question',
			'buttons' : [ {
				caption : '取消'
			}, {
				caption : '确认',
				enter : true,
				callback : function(d) {
					return dialog.action.rename(d);
				}
			} ]
		},
		rename_catalog : {
			'title' : '重命名',
			'modal' : true,
			'type' : 'question',
			'buttons' : [ {
				caption : '取消'
			}, {
				caption : '确认',
				enter : true,
				callback : function(d) {
					return dialog.action.rename(d);
				}
			} ]
		},
		f1_help : {
			'title' : '帮助',
			'modal' : true,
			'type' : false,
			'width' : 710,
			'single' : true,
			'custom_class' : "SettingsBody",
			'buttons' : [ {
				caption : '确认'
			} ]
		},
		share : {
			'title' : '分享',
			'modal' : true,
			'width' : 550,
			'type' : false,
			'single' : true,
			'buttons' : [ {
				caption : '确认',
				enter : true,
				callback : function(d) {
					d.close(true);
				}
			} ]
		},
		// 项目设置
		project_setting : {
			'title' : '项目管理',
			'modal' : true,
			'type' : 'information',
			'width' : 600,
			'buttons' : [ {
				caption : '取消',
				callback : function(d) {
					return dialog.action.cancel2(d);
				}
			}, {
				caption : '确认',
				enter : true,
				callback : function(d) {
					return dialog.action.doSetting(d);
				}
			} ]
		},
		// 编辑器设置
		editor_setting : {
			'title' : '编辑器设置',
			'modal' : true,
			'type' : false,
			'width' : 710,
			'custom_class' : "SettingsBody",
			'buttons' : [ {
				caption : '确认',
				callback : function(d) {
					return dialog.action.doSetting(d);
				}
			} ]
		},
		version_setting : {
			'title' : '版本管理',
			'modal' : true,
			'type' : 'information',
			'width' : 600,
			'buttons' : [ {
				caption : '取消',
				callback : function(d) {
					return dialog.action.cancel(d);
				}
			}, {
				caption : '确认',
				enter : true,
				callback : function(d) {
					return dialog.action.doSetting(d);
				}
			} ]
		},
		publish_version : {
			'title' : '发布当前代码',
			'modal' : true,
			'type' : false,
			'width' : 400,
			'buttons' : [ {
				caption : '取消',
				callback : function(d) {
					d.close(true);
				}
			}, {
				caption : '发布',
				enter : true,
				callback : function(d) {
					return dialog.action.publish(d);
				}
			} ]
		},
		update_publish : {
			'title' : '更新发布信息',
			'modal' : true,
			'type' : false,
			'width' : 400,
			'buttons' : [ {
				caption : '取消',
				callback : function(d) {
					d.close(true);
				}
			}, {
				caption : '更新',
				enter : true,
				callback : function(d) {
					return dialog.action.publish(d);
				}
			} ]
		},
		doConfirm : {
			'title' : '确认',
			'modal' : true,
			'width' : 350,
			'type' : "question",
			'buttons' : [ {
				caption : '取消',
				callback : function(d) {
					return dialog.action.cancel(d);
				}
			}, {
				caption : '确认',
				enter : true,
				callback : function(d) {
					return dialog.action.doIt(d);
				}
			} ]
		},
		doConfirm2 : {
			'title' : '确认',
			'modal' : true,
			'width' : 300,
			'type' : "question",
			'buttons' : [ {
				caption : '取消',
				callback : function(d) {
					return dialog.action.cancel(d);
				}
			}, {
				caption : '确认',
				enter : true,
				callback : function(d) {
					return dialog.action.doIt(d);
				}
			} ]
		},
		doConfirm3 : {
			'title' : '确认',
			'modal' : true,
			'width' : 400,
			'type' : "question",
			'buttons' : [ {
				caption : '取消'
			}, {
				caption : '不保存',
				callback : function(d) {
					return dialog.action.cancel(d);
				}
			}, {
				caption : '保存',
				enter : true,
				callback : function(d) {
					return dialog.action.doIt(d);
				}
			} ]
		},
		doConfirm4 : {
			'title' : '确认',
			'modal' : true,
			'width' : 300,
			'type' : "question",
			'buttons' : [ {
				caption : '确认'
			} ]
		},
		saveForce : {
			'title' : '强制保存',
			'modal' : true,
			'width' : 300,
			'type' : "question",
			'buttons' : [ {
				caption : '取消',
				callback : function(d) {
					return dialog.action.cancel(d);
				}
			}, {
				caption : '确认',
				enter : true,
				callback : function(d) {
					return dialog.action.doIt(d);
				}
			} ]
		},
		upload : {
			'title' : '资源上传',
			'modal' : true,
			'width' : 450,
			'type' : false,
			'buttons' : [ {
				caption : '取消',
				callback : function(d) {
					d.close(true);
				}
			}, {
				caption : '上传',
				enter : true,
				callback : function(d) {
					return dialog.action.upload(d);
				}
			} ]
		},
		upload_plugin : {
			'title' : '插件上传',
			'modal' : true,
			'width' : 450,
			'type' : false,
			'buttons' : [ {
				caption : '取消',
				callback : function(d) {
					d.close(true);
				}
			}, {
				caption : '上传',
				enter : true,
				callback : function(d) {
					return dialog.action.upload(d);
				}
			} ]
		},
		create_sys_plugin : {
			'title' : '创建插件',
			'modal' : true,
			'type' : 'question',
			'buttons' : [ {
				caption : '取消',
				callback : function(d) {
					d.close(true);
				}
			}, {
				caption : '创建',
				enter : true,
				callback : function(d) {
					return dialog.action.upload(d);
				}
			} ]
		},
		create_wizard : {
			'title' : '项目创建向导',
			'modal' : true,
			'width' : 450,
			'type' : false,
			'buttons' : [ {
				caption : '取消',
				callback : function(d) {
					d.close(true);
				}
			} ]
		},
		create_catalog : {
			'title' : '创建分类',
			'modal' : true,
			'type' : false,
			'buttons' : [ {
				caption : '取消',
				callback : function(d) {
					d.close(true);
				}
			}, {
				caption : '创建',
				enter : true,
				callback : function(d) {
					return dialog.action.upload(d);
				}
			} ]
		},
		choose_catalog : {
			'title' : '选择分类',
			'width' : 450,
			'modal' : true,
			'type' : false,
			'buttons' : [ {
				caption : '取消',
				callback : function(d) {
					d.close(true);
				}
			}, {
				caption : '确定',
				enter : true,
				callback : function(d) {
					return dialog.action.upload(d);
				}
			} ]
		},
		choose_code_type : {
			'title' : '代码视图类别',
			'width' : 450,
			'modal' : true,
			'type' : false,
			'buttons' : [ {
				caption : '取消',
				callback : function(d) {
					d.close(true);
				}
			}, {
				caption : '确定',
				enter : true,
				callback : function(d) {
					return dialog.action.upload(d);
				}
			} ]
		},
		view_notify : {
			'title' : '查看提醒',
			'modal' : true,
			'type' : false,
			'width' : 710,
			'single' : true,
			'custom_class' : "SettingsBody",
			'buttons' : [ {
				caption : '确认'
			}, {
				caption : '全部标记已阅'
			} ]
		},
		wizards : {
			'title' : '创建向导',
			'modal' : true,
			'type' : false,
			'width' : 710,
			'single' : true,
			'custom_class' : "SettingsBody",
			'buttons' : [ {
				caption : '取消'
			}, {
				caption : '完成',
				enter : true
			}, {
				caption : '下一步'
			}, {
				caption : '上一步'
			} ]
		},
		action : {}
	};

	return Dialog;

})();