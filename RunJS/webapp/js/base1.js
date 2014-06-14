var RunJS = function(opt) {
	var options = {};
	var headerHeight, core, editorSets, editors;
	var delay;

	var codeMD5 = function() {
		return CryptoJS.MD5(
				runjs.editorHtml.getValue() + runjs.editorJs.getValue()
						+ runjs.editorCss.getValue()).toString();
	}

	var sendIframMessage = function(msg) {
		document.getElementById('preview').contentWindow.postMessage(msg, '*');
	}

	var updatePreview = function(editor) {
		var curHash = codeMD5();
		if (editor.contentHash.length == 0) {
			editor.contentHash = curHash;
			editor.edited = false;
		} else {
			if (editor.contentHash != curHash) {
				editor.edited = true;
				$("title").html("*" + $("title").html().replace(/[*]/g, ""));
			} else {
				editor.edited = false;
				$("title").html($("title").html().replace(/[*]/g, ""));
			}
		}
		if (!User.isNotMyCode) {
			try {

				var pre_wrapper = $(".preview");
				pre_wrapper.find("iframe").remove();
				var previewFrame = pre_wrapper.append(
						'<iframe id="preview" frameborder="0"></iframe>').find(
						"iframe")[0];
				var preview = previewFrame.contentDocument
						|| previewFrame.contentWindow.document;
				preview.open();

				var html = getCobinedHtml(instance);
				/*
				 * var params = "{js:'"+escape(instance.editorJs.getValue())+
				 * "',css:'"+escape(instance.editorCss.getValue())+
				 * "',html:'"+escape(instance.editorHtml.getValue())+"',hash:'"+curHash+"'}"
				 * if($("#enableJS:checked").length==0){ }
				 * sendIframMessage(params);
				 */
				var js_enabled = $("#js_switcher:checked").length == 1;

				if (!js_enabled) {
					html = html
							.replace(/<script[^>]*>(.|\n)*?<\/script>/ig, "");
				}
				preview.write(html);
				preview.close();
			} catch (e) {
			}
		} else {
			controls.control_event.removeEditedStatus();
		}

		// 防止引入某些js库导致编辑器失去焦点
		if (typeof runjs.focusEditor != "undefined")
			runjs.focusEditor.focus();
	}

	var getCobinedHtml = function(instance) {
		var html = instance.editorHtml.getValue();
		var temp = "";
		if (html.indexOf("</body>") > -1) {
			var body = [];
			body.push(html.substring(0, html.lastIndexOf("</body>")));
			body.push(html.substring(html.lastIndexOf("</body>")));
			html = body[0];
			temp = body.length == 2 && body[1] ? body[1] : "";
		}
		return html + "<script>" + instance.editorJs.getValue() + "</script>"
				+ "<style>" + instance.editorCss.getValue() + "</style>" + temp;
	}

	var resizeEditor = function() {

		var win_height = $(window).height();

		var win_width = $(window).width();

		var firstSetTop = editorSets.eq(0).offset().top;

		var av_height = win_height - firstSetTop;

		$(".editor_wrapper").css({
			height : av_height - 18
		});

		$(".explorer").css({
			height : win_height - $(".header").height() - 5
		});

		$(".editor textarea").each(function() {
			$(this).css({
				position : 'absolute',
				left : -10000,
				width : 10
			});
		});

		var ver_left = editorSets.eq(0).width() - 3;

		$(".handler_vertical").css({
			left : ver_left
		});

		$(".handler_horizontal").each(
				function(idx) {
					var hor_top = editorSets.eq(idx).find(".editor:eq(0)")
							.height() - 5;
					$(this).css({
						top : hor_top
					});
				});

	};

	var init = function(instance) {

		headerHeight = $(".header").height();
		core = $(".core");
		editorSets = $(".editorSet");
		editors = $(".editor");

		instance.update = updatePreview;

		instance.resize = resizeEditor;

		instance.codeMD5 = codeMD5;

		instance.contentHash = "";

		instance.edited = false;

		$(window).resize(function() {
			resizeEditor(instance);
		});

		$(window).bind('beforeunload', function() {
			if (instance.edited) {
				return "代码尚未保存，确认离开将不会保存当前数据。";
			}
		});

		resizeEditor();

		setTimeout(function() {
			updatePreview(instance);
		}, 300);

		// 初始化Settings
		if (typeof User.settings != 'undefined') {

			// 初始化主题
			var theme = 'night';

			if (typeof User.settings.theme != 'undefined') {
				theme = User.settings.theme;
				$(".theme_list li").removeClass("current");
				if (theme == "default") {
					$(".theme_list li.default").addClass("current");
				} else if (theme == "night") {
					$(".theme_list li.night").addClass("current");
				}
			}
			$(".CodeMirror").ready(function() {
				// 初始化字体
				var fontsize = 12;
				var fontfamily = "Courier New";

				if (typeof User.settings.fontsize != 'undefined') {
					fontsize = User.settings.fontsize;
				} else {
					User.settings.fontsize = fontsize;
				}

				if (typeof User.settings.fontfamily != 'undefined') {
					fontfamily = User.settings.fontfamily;
				} else {
					User.settings.fontfamily = fontfamily;
				}

				$(".CodeMirror").css({
					"font-family" : fontfamily,
					"font-size" : fontsize + "px"
				});

				controls.control_event.refreshEditors();
			});
		}

		var foldFunc_html = CodeMirror
				.newFoldFunction(CodeMirror.tagRangeFinder);
		var foldFunc_js = CodeMirror
				.newFoldFunction(CodeMirror.braceRangeFinder);
		instance.editorHtml = new CodeMirror(document
				.getElementById("code_html").parentElement, {
			mode : "text/html",
			theme : theme,
			value : typeof User.code != "undefined" ? User.code.html : $(
					"#code_html").val(),
			lineNumbers : true,
			lineWrapping : true,
			tabSize : 2,
			onGutterClick : foldFunc_html,
			onChange : function(ins, data) {
				clearTimeout(delay);
				delay = setTimeout(function() {
					updatePreview(instance);
				}, 300);
			},
			onFocus : function() {
				instance.focusEditor = instance.editorHtml;
			},
			extraKeys : {
				"'>'" : function(cm) {
					cm.closeTag(cm, '>');
				},
				"'/'" : function(cm) {
					cm.closeTag(cm, '/');
				},
				"Shift-Ctrl-F" : function() {
					controls.control_event.format()
				},
				"Ctrl-Q" : function(cm) {
					foldFunc_html(cm, cm.getCursor().line);
				}
			},
			onCursorActivity : function(cm) {
				instance.editorHtml.setLineClass(hlLine, null, null);
				hlLine = instance.editorHtml.setLineClass(instance.editorHtml
						.getCursor().line, null, "activeline");
			},
			onBlur : function() {
				instance.editorHtml.setLineClass(hlLine, null, null);
			}
		});

		var hlLine = instance.editorHtml.setLineClass(-1, "activeline");

		instance.editorCss = new CodeMirror(
				document.getElementById("code_css").parentElement, {
					mode : "css",
					theme : theme,
					value : typeof User.code != "undefined" ? User.code.css
							: $("#code_css").val(),
					tabSize : 2,
					lineNumbers : true,
					lineWrapping : true,
					onChange : function() {
						clearTimeout(delay);
						delay = setTimeout(function() {
							updatePreview(instance);
						}, 300);
					},
					onFocus : function() {
						instance.focusEditor = instance.editorCss;
					},
					extraKeys : {
						"Shift-Ctrl-F" : function() {
							controls.control_event.format()
						}
					},
					onCursorActivity : function(cm) {
						instance.editorCss.setLineClass(hlLine1, null, null);
						hlLine1 = instance.editorCss.setLineClass(
								instance.editorCss.getCursor().line, null,
								"activeline");
					},
					onBlur : function() {
						instance.editorCss.setLineClass(hlLine1, null, null);
					}
				});
		var hlLine1 = instance.editorCss.setLineClass(-1, "activeline");
		instance.editorJs = new CodeMirror(
				document.getElementById("code_js").parentElement, {
					mode : "javascript",
					theme : theme,
					value : typeof User.code != "undefined" ? User.code.js : $(
							"#code_js").val(),
					tabSize : 2,
					lineNumbers : true,
					lineWrapping : true,
					onGutterClick : foldFunc_js,
					matchBrackets : true,
					onChange : function(cm) {
						clearTimeout(delay);
						delay = setTimeout(function() {
							updatePreview(instance);
						}, 300);
					},
					onFocus : function() {
						instance.focusEditor = instance.editorJs;
					},
					extraKeys : {
						"Shift-Ctrl-F" : function() {
							controls.control_event.format()
						},
						"Ctrl-Q" : function(cm) {
							foldFunc_js(cm, cm.getCursor().line);
						}
					},
					onCursorActivity : function(cm) {
						instance.editorJs.setLineClass(hlLine2, null, null);
						hlLine2 = instance.editorJs.setLineClass(
								instance.editorJs.getCursor().line, null,
								"activeline");
					},
					onBlur : function() {
						instance.editorJs.setLineClass(hlLine2, null, null);
					}
				});
		var hlLine2 = instance.editorJs.setLineClass(-1, "activeline");
		$(".handler_horizontal").TextAreaResizer({
			vertical : false,
		});

		$(".handler_vertical").TextAreaResizer({
			vertical : true
		});

	}

	var Instance = function(opt) {
		for ( var v in opt) {
			if (opt[v])
				options[v] = opt[v];
		}
		this.options = options;
		init(this);
		this.codes = function() {
			return {
				html : this.editorHtml.getValue(),
				js : this.editorJs.getValue(),
				css : this.editorCss.getValue
			}
		}
	}

	var instance = new Instance(opt)

	return instance;
};

var Controls = function(opt) {

	var options = {
		commons : {
			library : [ 'jquery', 'mootools', 'bootstrap', 'dojo',
					'jquerymobile', 'yui' ]
		}
	};

	var dialog = {
		success : new $.Zebra_Dialog("操作成功", {
			'single' : true,
			'buttons' : false,
			'modal' : false,
			'type' : 'confirmation',
			'auto_close' : 1000
		}),
		success2 : new $.Zebra_Dialog("操作成功", {
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
		}),
		error : new $.Zebra_Dialog("操作失败", {
			'buttons' : false,
			'modal' : false,
			'type' : 'error',
			'auto_close' : 2000
		}),
		jserror : new $.Zebra_Dialog("脚本错误", {
			'title' : '脚本错误',
			'modal' : false,
			'type' : 'error',
			'buttons' : [ {
				caption : '取消'
			}, {
				caption : '反馈',
				enter : true,
				callback : function(d) {
					return dialog.action.feedback(d);
				}
			} ]
		}),
		error2 : new $.Zebra_Dialog("操作失败", {
			'title' : '错误提示',
			'modal' : false,
			'type' : 'error',
			'buttons' : [ {
				caption : '确认',
				callback : function(d) {
					return dialog.action.error_confirm(d);
				}
			} ]
		}),
		create_project : new $.Zebra_Dialog(
				$(".dialog .create_project").html(), {
					'title' : '创建项目',
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
				}),
		login : new $.Zebra_Dialog($("#user_login").html(), {
			'title' : '登录方式',
			'modal' : true,
			'width' : 460,
			'type' : false,
			'buttons' : [ {
				caption : '取消'
			} ]
		}),
		fork : new $.Zebra_Dialog("请选择创建分支方式", {
			'title' : '创建分支',
			'modal' : true,
			'type' : 'question',
			'width' : 400,
			'buttons' : [ {
				caption : '取消'
			}, {
				caption : '创建新项目',
				callback : function(d) {
					return dialog.action.fork(d);
				}
			}, {
				caption : '创建新版本',
				callback : function(d) {
					return dialog.action.new_version(d)
				}
			} ]
		}),
		delete_ : new $.Zebra_Dialog("确认删除该项目？", {
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
		}),
		rename : new $.Zebra_Dialog($(".dialog .rename").html(), {
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
		}),
		help : new $.Zebra_Dialog($(".dialog .f1_help").html(), {
			'title' : '帮助',
			'modal' : true,
			'type' : false,
			'width' : 710,
			'custom_class' : "SettingsBody",
			'single' : true,
			'buttons' : [ {
				caption : '确认',
				enter : true
			} ]
		}),
		share : new $.Zebra_Dialog($(".dialog .share").html(), {
			'title' : '分享',
			'modal' : true,
			'width' : 400,
			'single' : true,
			'buttons' : [ {
				caption : '确认',
				enter : true,
				callback : function(d) {
					d.close(true);
				}
			} ]
		}),
		// 项目设置
		project_setting : new $.Zebra_Dialog($("#projectSetting").html(), {
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
		}),
		// 编辑器设置
		editor_setting : new $.Zebra_Dialog($("#editorSetting").html(), {
			'title' : '编辑器设置',
			'modal' : true,
			'type' : false,
			'width' : 710,
			'custom_class' : "SettingsBody",
			'buttons' : [ {
				caption : '确认',
				enter : true,
				callback : function(d) {
					return dialog.action.doSetting(d);
				}
			} ]
		}),
		version_setting : new $.Zebra_Dialog($("#versionSetting").html(), {
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
		}),
		publish_version : new $.Zebra_Dialog($("#publishVersion").html(), {
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
		}),
		doConfirm : new $.Zebra_Dialog("", {
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
		}),
		doConfirm2 : new $.Zebra_Dialog("", {
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
		}),
		doConfirm3 : new $.Zebra_Dialog("", {
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
		}),
		saveForce : new $.Zebra_Dialog(
				"<p>导致保存失败有可能是因为有多个窗口编辑当前代码，强制保存可以忽视各编辑器差异，是否强制保存？</p>", {
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
				}),
		upload : new $.Zebra_Dialog($("#resourceUpload").html(), {
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
		}),
		action : {}
	};

	var control_event = {
		save : function(callback) {
			if (!runjs.edited && User.login && typeof User.code != 'undefined')
				return;
			cur_opt = "save";
			// 用户为登陆或者还没有创建过项目
			if (!User.login || User.projects.length == 0) {
				// 未登录，先要求登录，登录完成后创建新项目
				if (!User.login && !control_event.login(true))
					return false;
				control_event.create_project(undefined, callback, true);
			} else {
				// 如果当前代码不能存在或者不是用户所有，则创建新项目
				if (typeof User.code == 'undefined'
						|| User.user != User.code.user) {
					control_event.create_project(undefined, callback, true);
					return;
				}
				// 登录并且已存在项目，save即更新当前项目
				var codes = runjs.codes();
				Api.ajax
						.update(
								User.code.id,
								codes.html,
								codes.css,
								codes.js,
								User.code.sign,
								function(msg) {
									return control_event
											.errorHandler(
													msg,
													function(msg) {
														User.code.sign = msg.sign;
														control_event
																.removeEditedStatus();
														// dialog.success.show("保存成功！");
														if (typeof callback != "undefined")
															callback();
													},
													function(msg) {
														dialog.action.doIt = function() {
															Api.ajax
																	.update(
																			User.code.id,
																			codes.html,
																			codes.css,
																			codes.js,
																			User.code.sign,
																			function(
																					msg) {
																				return control_event
																						.errorHandler(
																								msg,
																								function(
																										msg) {
																									User.code.sign = msg.sign;
																									control_event
																											.removeEditedStatus();
																									// dialog.success.show("保存成功！");
																									if (typeof callback != "undefined")
																										callback();
																								});
																			},
																			1);// 强制保存
														};
														dialog.action.cancel = function() {

														};
														if (msg.error == 2)
															dialog.saveForce
																	.show();
													});
								});
			}
		},
		// opt创建项目的可配置参数：如opt.tpl则为创建项目采用的初始模版
		create_project : function(opt, callback, notask) {
			var doIt = function() {
				// 用户未登录，则提醒登陆，并创建项目
				if (!User.login) {
					control_event.save(callback);
					return;
				}

				var template = null;

				dialog.action.create = function(d) {
					var prj_name = $("input[name='project_name']:eq(1)");
					var codes = template == null ? runjs.codes() : template;
					return Api.ajax.add_project(prj_name.val(), codes.html,
							codes.css, codes.js, function(msg) {
								return control_event
										.errorHandler(msg, function(msg) {
											control_event.removeEditedStatus();
											// dialog.success.show("创建成功！");
											if (typeof callback != "undefined")
												callback();
											location.href = "/code/"
													+ msg.ident;
										},
												function(msg) {
													var msg1 = prj_name
															.parent().parent()
															.find(".errorMsg");
													msg1.html(msg.msg);
													prj_name.focus();
													return false;
												}, true);
							});
				}

				dialog.action.cancel = function(d) {
					if (typeof User.code == 'undefined'
							|| User.user != User.code.user) {
						control_event.removeEditedStatus();
						if (typeof callback != "undefined")
							callback();
						if (typeof fakeLogin != "undefined" && fakeLogin)
							location.reload();
					}
					d.close(true);
				}
				// 是否采用模版构建项目
				if (typeof opt != 'undefined') {
					Template.wizards[opt.tpl](function(tpl) {
						template = tpl;
					})
				}
				var msg = $(".create_project").clone().prepend(
						'<p>请输入长度为1~20字符的代码名称</p>');
				dialog.create_project.show(msg.html(), undefined, undefined,
						"创建新代码", false);
			}
			if (typeof notask == "undefined" || !notask)
				control_event.checkEditedStatus(doIt, doIt);
			else
				doIt();
		},
		delete_version : function(project, version, single, sign) {
			var doIt = function() {
				if (typeof single == 'undefined' && typeof id == 'undefined') {
					version = project.vid;
					single = project.single;
					sign = project.sign;
					project = project.pid;
				} else
					sign = User.code.sign;
				if (typeof single == 'undefined' || !single) {
					dialog.action.delete_ = function(d) {
						var cp = $("#captcha_delete_version");
						if (cp.val().length == 0) {
							cp.parent().parent().find(".errorMsg").html(
									"验证码不能为空！");
							cp.focus();
							return false;
						}
						Api.ajax.delete_version(cp.val(), project, version,
								sign, function(msg) {
									return control_event.errorHandler(msg,
											function(msg) {
												// dialog.success.show("删除成功！");
												setTimeout(function() {
													location.href = "/";
												}, 1000);
											}, function(msg) {
												cp.parent().parent().find(
														".errorMsg").html(
														msg.msg);
												cp.focus();
											}, true);
								});
					};
					var prj = control_event.getCurProject(project);
					var v = control_event.getCurVersion(prj, version, true);
					var msg = "确认删除代码： <span style='color:#A00'>" + prj.name
							+ "</span>？";
					msg += '<p>验证码：<img src="/action/project/captcha" class="captcha" style="cursor:pointer;vertical-align:middle;" width="60" height="23"/>'
							+ '<input id="captcha_delete_version" type="text" size="4"/></p>'
							+ '<p><span class="errorMsg"></span></p>';
					dialog.delete_.show(msg, undefined, undefined, undefined,
							false);
					$(".captcha").live(
							"click",
							function() {
								$(this).attr(
										"src",
										"/action/project/captcha?"
												+ (new Date().getTime()));
							});
				} else if (single) {
					dialog.action.delete_ = function(d) {
						var cp = $("#captcha_delete_project");
						if (cp.val().length == 0) {
							cp.parent().parent().find(".errorMsg").html(
									"验证码不能为空！");
							cp.focus();
							return false;
						}
						Api.ajax.delete_project(cp.val(), project,
								function(msg) {
									return control_event.errorHandler(msg,
											function(msg) {
												// dialog.success.show("删除成功！");
												setTimeout(function() {
													location.href = "/";
												}, 1000);
											}, function(msg) {
												cp.parent().parent().find(
														".errorMsg").html(
														msg.msg);
												cp.focus();
											}, true);
								});
					};
					var prj = control_event.getCurProject(project);
					// var msg = "删除 <span
					// style='color:#A00'>"+prj.name+"</span>
					// 项目最后一个版本将会直接删除当前项目，确认删除？";
					var msg = "确认删除代码：<span style='color:#A00'>" + prj.name
							+ "</span>？"
					msg += '<p>验证码：<img src="/action/project/captcha" class="captcha" style="cursor:pointer;vertical-align:middle;" width="60" height="23"/>'
							+ '<input id="captcha_delete_project" type="text" size="4"/></p>'
							+ '<p><span class="errorMsg"></span></p>';
					dialog.delete_.show(msg, undefined, undefined, "删除代码",
							false);
					$(".captcha").live(
							"click",
							function() {
								$(this).attr(
										"src",
										"/action/project/captcha?"
												+ (new Date().getTime()));
							});
				}
			}
			control_event.checkEditedStatus(doIt, doIt);
		},
		rename : function(id) {
			var name = "";
			if (typeof id == "object") {
				name = id.name;
				id = typeof id.id == "undefined" ? undefined : id.id;
			}
			var doIt = function() {
				dialog.action.rename = function(d) {
					var prj_name = $("input[name='newname']:eq(1)");
					if (prj_name.val().length == 0) {
						dialog.error.show("新名称不能为空！");
						return false;
					}
					return typeof id == 'undefined' ? Api.ajax.code_rename(
							User.code.id, prj_name.val(), function(msg) {
								return control_event.errorHandler(msg,
										function(msg) {
											location.href = "/code/"
													+ User.code.ident;
										});
							}) : Api.ajax.project_rename(id, prj_name.val(),
							function(msg) {
								return control_event.errorHandler(msg,
										function(msg) {
											location.href = "/code/"
													+ User.code.ident;
										});
							});
				};
				dialog.rename.show();
				$("input[name='newname']:eq(1)").val(name);
			}
			control_event.checkEditedStatus(doIt, doIt);
		},
		// msg:服务器返回参数，success：成功后执行回调，error：错误后执行回调，auto：是否自动处理错误信息
		errorHandler : function(msg, success, error, auto) {
			try {
				var msg = eval("(" + msg + ")");
				if (msg.error) {
					if (typeof auto == "undefined" || !auto) {
						dialog.error.show(msg.msg);
						if (typeof error != "undefined") {
							setTimeout(function() {
								error(msg);
							}, 2000);
						}
						return false;
					} else {
						if (typeof error != "undefined") {
							return error(msg);
						}
					}
				}
				return success(msg)
			} catch (e) {
				dialog.action.feedback = function(d) {

				};
				if (typeof e.stack != 'undefined') {
					dialog.jserror.show(e.stack.substring(0, 50));
				} else
					dialog.jserror.show(e.message);
				return;
			}
		},
		loginNow : function(type) {
			var url = User.host + "/action/openid/before_login?op=" + type;
			control_event.openwindow(url, 'loginPage', 800, 600);
		},
		login : function(save) {
			if (typeof save != "undefined" && save) {
				var msg = $("#user_login")
						.clone()
						.prepend(
								'<p style="margin-left:20px;">保存代码前请登录，您可以选择以下几种方式登录：</p>');
				dialog.login.show(msg.html(), undefined, undefined, "保存代码");
			} else {
				var msg = $("#user_login")
						.clone()
						.prepend(
								'<p style="margin-left:20px;">您可以选择以下几种方式登录,登录后可以：</p>'
										+ '<p style="margin-left:20px;">&nbsp;&nbsp;&nbsp;1、保存代码</p>'
										+ '<p style="margin-left:20px;">&nbsp;&nbsp;&nbsp;2、发布和分享代码</p>'
										+ '<p style="margin-left:20px;">&nbsp;&nbsp;&nbsp;3、Fork 他人的代码</p>');
				dialog.login.show(msg.html(), undefined, undefined, undefined);
			}
			$(".user_login").click(function() {
				dialog.login.close();
			});
		},
		prelogin : function() {
			if (runjs.edited) {
				cur_opt = "askSave";
				control_event.save();
			} else {
				cur_opt = "login";
				control_event.login();
			}
		},
		help : function() {
			dialog.help.show();
			$(".help_wrapper:eq(1) .setting_list li").click(function() {
				var right = $(".help_wrapper:eq(1) .setting_right");
				var idx = $(this).index();
				var arrow = $(".help_wrapper:eq(1) .list_item_focus");
				$(".item_focus").removeClass("item_focus");
				// 设置选项focus
				$(this).addClass("item_focus");
				// 设置focus小箭头
				arrow.css({
					top : 10 + idx * 41
				});
				// 显示设置主体
				var show = right.find("ul:eq(" + idx + ")");
				var hide = right.find("ul");
				hide.removeClass("setting_on").addClass("setting_off");
				show.removeClass("setting_off").addClass("setting_on");
			});
		},
		openwindow : function(url, name, iWidth, iHeight) {
			var url; // 转向网页的地址;
			var name; // 网页名称，可为空;
			var iWidth; // 弹出窗口的宽度;
			var iHeight; // 弹出窗口的高度;
			var iTop = (window.screen.availHeight - 30 - iHeight) / 2; // 获得窗口的垂直位置;
			var iLeft = (window.screen.availWidth - 10 - iWidth) / 2; // 获得窗口的水平位置;
			window
					.open(
							url,
							name,
							'height='
									+ iHeight
									+ ',,innerHeight='
									+ iHeight
									+ ',width='
									+ iWidth
									+ ',innerWidth='
									+ iWidth
									+ ',top='
									+ iTop
									+ ',left='
									+ iLeft
									+ ',toolbar=no,menubar=no,scrollbars=auto,resizeable=no,location=no,status=no');
		},
		logout : function() {
			var doIt = function() {
				Api.ajax.logout(function(msg) {
					return control_event.errorHandler(msg, function(msg) {
						User.login = false;
						location.href = User.host;
					});
				});
			}
			control_event.checkEditedStatus(doIt, doIt);
		},
		new_version : function() {
			var doIt = function() {
				if (typeof User.code == "undefined"
						|| User.code.user != User.user) {
					return;
				}
				var codes = runjs.codes();
				Api.ajax.new_version(User.code.id, codes.html, codes.css,
						codes.js, function(msg) {
							return control_event.errorHandler(msg,
									function(msg) {
										control_event.removeEditedStatus();
										// dialog.success.show("创建成功！");
										location.href = "/code/" + msg.ident;
									});
						});
			}
			control_event.checkEditedStatus(doIt, doIt);
		},
		fork : function() {
			var doIt = function() {
				dialog.action.fork = function() {
					if (typeof User.code == "undefined") {
						return false;
					}
					dialog.action.cancel = function(d) {
						d.close(true);
					}
					dialog.action.create = function(d) {
						var prj_name = $("input[name='project_name']:eq(1)");
						Api.ajax.fork(User.code.project, User.code.version,
								prj_name.val(), function(msg) {
									return control_event.errorHandler(msg,
											function(msg) {
												// dialog.success.show("创建成功！");
												location.href = "/code/"
														+ msg.ident;
											}, function(msg) {
												var msg1 = prj_name.parent()
														.parent().find(
																".errorMsg");
												msg1.html(msg.msg);
												prj_name.focus();
												return false;
											});
								});
					};
					var msg = $(".create_project")
							.clone()
							.prepend(
									'<p>Fork 即将当前代码复制一份，然后自行维护</p><p>请输入长度为1~20字符的代码名称</p>');
					dialog.create_project.show(msg.html(), undefined,
							undefined, "Fork 当前代码", false);
				};

				// 代码存在，且代码不是当前用户所有，则直接fork
				if ((typeof User.code != 'undefined' && User.user != User.code.user)) {
					return dialog.action.fork();
				}

				dialog.action.fork();
			}
			if (!User.isNotMyCode)
				control_event.checkEditedStatus(doIt, doIt);
			else
				doIt();

		},
		resetEditorValue : function(id, callback) {
			Api.ajax.getCode(id, function(msg) {
				return control_event.errorHandler(msg, function(msg) {

					if (typeof msg.html != 'undefined')
						runjs.editorHtml.setValue(msg.html);
					else
						runjs.editorHtml.setValue('');

					if (typeof msg.css != 'undefined')
						runjs.editorCss.setValue(msg.css);
					else
						runjs.editorCss.setValue('');

					if (typeof msg.js != 'undefined')
						runjs.editorJs.setValue(msg.js);
					else
						runjs.editorJs.setValue('');

					runjs.update();
					if (typeof callback != 'undefined')
						callback(msg);
				});
			});
		},
		format : function() {
			var cur = runjs.focusEditor;
			var gr = control_event.getSelectedRange;
			if (cur != null) {
				if (cur != runjs.editorJs) {
					var range = gr(cur);
					if ((range.from.char == range.to.char)
							&& (range.from.line == range.to.line)) {
						CodeMirror.commands["selectAll"](cur);
						range = gr(cur);
					}
					cur.autoFormatRange(range.from, range.to);
					if (cur == runjs.editorHtml)
						control_event.removeJsTagBlank();
				} else {
					var value = runjs.editorJs.getValue();
					var js_source = value.replace(/^\s+/, '');
					var fjs = js_beautify(js_source, 1, '\t');
					runjs.editorJs.setValue(fjs);
				}
			} else {
				CodeMirror.commands["selectAll"](runjs.editorHtml);
				var range = gr(runjs.editorHtml);
				control_event.removeJsTagBlank();
				runjs.editorHtml.autoFormatRange(range.from, range.to);
				CodeMirror.commands["selectAll"](runjs.editorCss);
				var range = gr(runjs.editorCss);
				runjs.editorCss.autoFormatRange(range.from, range.to);
				var value = runjs.editorJs.getValue();
				var js_source = value.replace(/^\s+/, '');
				var fjs = js_beautify(js_source, 1, '\t');
				runjs.editorJs.setValue(fjs);
			}
		},
		removeJsTagBlank : function() {
			var editor = runjs.editorHtml;
			var line = null, ln = 0;
			var startLine = 0;
			while (line = editor.getLine(ln)) {
				try {
					if (startLine == 0 && line.indexOf("<script") > -1
							&& typeof ($(line).attr("class")) != 'undefined'
							&& $(line).attr("class").indexOf("library") > -1) {
						if (line.indexOf("</script>") == -1)
							startLine = ln;
					} else if (startLine != 0 && line.indexOf("</script>") > -1) {
						var start = editor.getLine(startLine);
						var end = line;
						var blank_start = {
							line : startLine,
							ch : start.length
						};
						var blank_end = {
							line : ln,
							ch : end.indexOf('</')
						};
						editor.replaceRange("", blank_start, blank_end);
						startLine = 0;
						ln--;
					}
				} catch (e) {
				}
				ln++;
			}
		},
		getSelectedRange : function(editor) {
			return {
				from : editor.getCursor(true),
				to : editor.getCursor(false)
			};
		},
		comment : function(isComment) {
			var cur = runjs.focusEditor;
			if (typeof cur != 'undefined') {
				var range = control_event.getSelectedRange(cur);
				if ((range.from.char == range.to.char)
						&& (range.from.line == range.to.line)) {
					var cursor = cur.getCursor();
					range = {
						from : {
							line : cursor.line,
							ch : 0
						},
						to : {
							line : cursor.line,
							char : cursor.ch
						}
					};
				}
				cur.commentRange(typeof isComment == 'undefined' ? true
						: isComment, range.from, range.to);
			}
		},
		updateCurrentUrl : function(url) {
			if (window.history && window.history.pushState) {
				window.history.pushState(null, url, url);
			}
		},
		share_proxy : function(ident, vid, pid) {
			if (User.user != 0) {
				if (User.projects.length == 0) {
					dialog.action.doIt = function(d) {
						control_event.create_project();
					};
					dialog.action.cancel = function(d) {

					};
					dialog.doConfirm.show("<p>当前没有代码可分享，马上创建？</p>");
				} else {
					var curprj = control_event.getCurProject(pid);
					var cv = control_event.getCurVersion(curprj, vid);
					var opt = {
						ident : ident,
						prj : curprj,
						version : cv
					};
					control_event.share(opt);
				}
			} else {
				cur_opt = "login";
				control_event.prelogin();
			}
		},
		share : function(opt) {
			if (User.user != 0) {
				if (typeof User.code != "undefined" && User.code.post) {
					dialog.share.show();
					var detail = User.host + "/detail/" + opt.ident;
					$(".share_code:eq(1)")
							.val(User.host + "/code/" + opt.ident);
					$(".share_show:eq(1)").val(
							User.shost + "/show/" + opt.ident);
					$(".share_detail:eq(1)").val(detail);
					$(".share_iframe:eq(1)")
							.val(
									'<iframe style="width: 100%; height: 300px" src="'
											+ User.shost
											+ '/show/'
											+ opt.ident
											+ '" allowfullscreen="allowfullscreen" frameborder="0"></iframe>');
					$(".share_weibo:eq(1)")
							.attr(
									"href",
									"http://service.weibo.com/share/share.php?appkey=1683392632&url="
											+ detail
											+ "&title=代码分享：‘"
											+ opt.prj.name
											+ "’"
											+ "在线演示，"
											+ opt.version.description
											+ ",%23RunJS%23&source=&sourceUrl=&content=utf-8&pic=");
					$(".share_tencent:eq(1)")
							.attr(
									"href",
									"http://share.v.t.qq.com/index.php?c=share&a=index&url="
											+ detail
											+ "&appkey=8608625f2c2b3e593fa14c86539077b3&site=&title=代码分享：‘"
											+ opt.prj.name + "’" + "在线演示，"
											+ opt.version.description
											+ "，%23RunJS%23#");
					$(".share_tencent:eq(1)").click(function() {
						dialog.share.close();
					});
					$(".share_weibo:eq(1)").click(function() {
						dialog.share.close();
					});
				} else {
					dialog.action.doIt = function(d) {
						control_event.publish({
							vid : opt.version.id,
							pid : opt.prj.id,
							single : true
						});
					};
					dialog.action.cancel = function(d) {

					};
					dialog.doConfirm.show("<p>分享前请先发布当前代码，马上发布？</p>");
				}
			} else {
				cur_opt = "login";
				control_event.prelogin();
			}
		},
		// 编辑器设置
		editor_setting : function() {
			dialog.action.cancel = function() {

			}
			dialog.action.doSetting = function() {

			}
			dialog.editor_setting.show();
			var t = $("input[name='editorTheme']")
			t.removeAttr("checked");

			// 设置主题
			if (typeof User.settings.theme == "undefined") {
				User.settings.theme = "night";
			}
			var theme = User.settings.theme;
			$(".theme_list li").removeClass("current");
			if (theme == "default") {
				$(".theme_list li.default").addClass("current");
				$("body").attr("class", "DefaultTheme");
			} else if (theme == "night") {
				$(".theme_list li.night").addClass("current");
				$("body").attr("class", "NightTheme");
			}

			// 设置字体
			$(".fontsize:eq(1) select").change(
					function() {
						var cur = $(this);
						if (cur.attr("class") == "editor_font") {
							$(".CodeMirror").css({
								"font-family" : cur.val()
							});
							User.settings.fontfamily = cur.val();
							if (User.user != 0)
								Api.ajax.setting("fontfamily", cur.val(),
										function(msg) {
											return control_event.errorHandler(
													msg, function(msg) {
													});
										});
							control_event.refreshEditors();
						} else if (cur.attr("class") == "editor_font_size") {
							$(".CodeMirror").css({
								"font-size" : cur.val() + "px"
							});
							User.settings.fontsize = cur.val();
							if (User.user != 0)
								Api.ajax.setting("fontsize", cur.val(),
										function(msg) {
											return control_event.errorHandler(
													msg, function(msg) {
													});
										});
							control_event.refreshEditors();
						}
					});

			$(".editor_font").val(User.settings.fontfamily);
			$(".editor_font_size").val(User.settings.fontsize);

			$(".editor_setting_wrapper:eq(1) .setting_list li").click(function() {
				var right = $(".editor_setting_wrapper:eq(1) .setting_right");
				var idx = $(this).index();
				var arrow = $(".editor_setting_wrapper:eq(1) .list_item_focus");
				$(".item_focus").removeClass("item_focus");
				// 设置选项focus
				$(this).addClass("item_focus");
				// 设置focus小箭头
				arrow.css({
					top : 10 + idx * 41
				});
				// 显示设置主体
				var show = right.find("ul:eq(" + idx + ")");
				var hide = right.find("ul");
				hide.removeClass("setting_on").addClass("setting_off");
				show.removeClass("setting_off").addClass("setting_on");
			});			
		},
		setTheme : function(theme) {
			if (User.login) {
				Api.ajax.setting("theme", theme, function(msg) {
					return control_event.errorHandler(msg, function() {
						// dialog.success.show("主题("+theme+")设置成功！");
						runjs.editorHtml.setOption("theme", theme);
						runjs.editorCss.setOption("theme", theme);
						runjs.editorJs.setOption("theme", theme);
						User.settings.theme = theme;
					})
				});
			} else {
				runjs.editorHtml.setOption("theme", theme);
				runjs.editorCss.setOption("theme", theme);
				runjs.editorJs.setOption("theme", theme);
				User.settings.theme = theme;
			}
			if (theme == "default") {
				$("body").attr("class", "DefaultTheme");
			} else if (theme == "night") {
				$("body").attr("class", "NightTheme");
			}
			$(".theme_list li").removeClass("current");
			if (theme == "default") {
				$(".theme_list li.default").addClass("current");
				$("body").attr("class", "DefaultTheme");
			} else if (theme == "night") {
				$(".theme_list li.night").addClass("current");
				$("body").attr("class", "NightTheme");
			}
		},
		// 项目设置
		project_setting : function(pid) {
			// 默认设置参数
			var prjname = "";
			var curprj = null;
			dialog.action.cancel2 = function(d) {

			}
			dialog.action.doSetting = function(d) {
				// 重命名项目
				var prj_name = $("input[name='re_project_name']:eq(1)");
				if (prjname != prj_name.val()) {
					if (prj_name.val().length == 0) {
						dialog.error.show("新名称不能为空！");
						return false;
					}
					Api.ajax.project_rename(pid, prj_name.val(), function(msg) {
						return control_event.errorHandler(msg, function(msg) {
							// dialog.success.show("更新成功！");
							curprj.name = prj_name.val();
							$("#prj_" + msg.id).find(".title_name").html(
									prj_name.val());
						});
					});
				}
			}
			dialog.project_setting.show();
			curprj = control_event.getCurProject(pid);
			prjname = curprj.name;
			// 设置默认参数
			if (prjname.length > 0) {
				$("input[name='re_project_name']:eq(1)").val(prjname);
			}

		},
		getCurProject : function(pid) {
			if (User.projects.length > 0) {
				for ( var i = 0; i < User.projects.length; i++) {
					var p = User.projects[i];
					if (p.id == pid) {
						return p;
					}
				}
			}
		},
		getCurVersion : function(prj, vid, version) {
			if (prj.versions.length > 0) {
				for ( var i = 0; i < prj.versions.length; i++) {
					var v = prj.versions[i];
					if (v.id == vid || (version && v.version == vid))
						return v;
				}
			}
		},
		version_setting : function(vid, prjid, single) {
			var curprj = control_event.getCurProject(prjid);
			var cv = control_event.getCurVersion(curprj, vid)
			var name = cv.name.length == 0 ? "版本" + cv.num : cv.name;
			var description = cv.description;
			var version = cv.version;
			var post = cv.post;

			dialog.action.cancel = function() {
			}
			dialog.action.doSetting = function() {
				var name = $(".re_project_name:eq(1)");
				var msg = $(".versionSetting:eq(1)").find(".errorMsg");
				var des = $(".prj_description:eq(1)");
				if (post && (name.val().length == 0 || des.val().length == 0)) {
					msg.html("项目名称和描述内容不能为空！");
					return false;
				}
				if (cv.name != name.val() || cv.description != des.val()) {
					Api.ajax.update_info(cv.id, name.val(), des.val(),
							function(msg) {
								return control_event.errorHandler(msg,
										function(msg) {
											cv.name = name.val();
											cv.description = des.val();
											$(
													".version[ident='"
															+ cv.ident + "']")
													.find(".url").html(
															"[" + cv.version
																	+ "] "
																	+ cv.name);
											// dialog.success.show("更新成功！");
										});
							})
				}
			}
			dialog.version_setting.show();

			var name = $(".re_project_name:eq(1)").val(name);
			var des = $(".prj_description:eq(1)").val(description);

			$('button[name="deleteVersion"]:eq(1)').click(
					function() {
						control_event.delete_version(prjid, version, single,
								runjs.contentHash);
					});
			if (post == 0) {
				$('button[name="updatePublish"]:eq(1)').hide();
				$('button[name="publishVersion"]:eq(1)').click(function() {
					var msg = $(this).parent().find(".errorMsg");
					msg.html("");
					if (name.val().length == 0 || des.val().length == 0) {
						msg.html("项目名称或描述内容不能为空！");
						return;
					}
					Api.ajax.publish(vid, des.val(), function(msg) {
						return control_event.errorHandler(msg, function(msg) {
							dialog.action.success_confirm = function() {
								location.href = "/code/" + msg.ident;
							}
							cv.description = des.val();
							cv.post = 1;
							dialog.success2.show("发布成功！");
						});
					});
				});
			} else {
				$('button[name="publishVersion"]:eq(1)').hide();
			}
		},
		publish : function(opt) {
			var doIt1 = function() {
				var curprj = control_event.getCurProject(opt.pid);
				var cv = control_event.getCurVersion(curprj, opt.vid)
				var name = cv.name.length == 0 ? "版本" + cv.num : cv.name;
				var description = cv.description;
				var version = cv.version;
				var post = cv.post;
				if (post == 0) {
					dialog.action.publish = function() {
						var des = $(".project_description:eq(1)");
						var msg = $(".publishVersion:eq(1)").find(".errorMsg");
						if (des.val().length == 0) {
							msg.html("项目描述内容不能为空！");
							des.focus();
							return false;
						}
						Api.ajax
								.publish(
										opt.vid,
										des.val(),
										function(msg) {
											return control_event
													.errorHandler(
															msg,
															function(msg) {
																$(
																		".btn.publish")
																		.remove();
																dialog.action.success_confirm = function() {
																	location.href = "/code/"
																			+ msg.ident;
																}
																cv.description = des
																		.val();
																cv.post = 1;
																if (typeof User.code != "undefined"
																		&& User.code.id == cv.id) {
																	User.code.post = true;
																}
																dialog.action.cancel = function(
																		d) {
																	d
																			.close(true);
																}
																dialog.action.doIt = function(
																		d) {
																	location.href = "/detail/"
																			+ msg.ident;
																}
																dialog.doConfirm
																		.show("发布成功,查看发布详情？");
															});
										});
					};
					dialog.publish_version.show();
					$(".publishVersion:eq(1)")
							.prepend(
									'<p>发布即将当前代码设置为所有人可见的状态，发布过后，该代码将拥有单独的详情页面，能够被分享、评论以及被 Fork </p>')
					var des = $(".project_description:eq(1)").val(description);
				} else {
					dialog.action.publish = function() {
						var des = $(".project_description:eq(1)");
						var msg = $(".publishVersion:eq(1)").find(".errorMsg");
						if (des.val().length == 0) {
							msg.html("项目描述内容不能为空！");
							return false;
						}
						if (cv.description != des.val()) {
							Api.ajax.update_info(cv.id, cv.name, des.val(),
									function(msg) {
										return control_event.errorHandler(msg,
												function(msg) {
													cv.description = des.val();
													dialog.success
															.show("更新成功！");
												});
									})
						}
					};
					dialog.publish_version.show($("#publishVersion").html(), [
							{
								caption : '取消'
							}, {
								caption : '更新',
								enter : true,
								callback : function(d) {
									return dialog.action.publish(d);
								}
							} ], undefined, "更新发布信息");
					var des = $(".project_description:eq(1)").val(description);
				}
			}
			control_event.checkEditedStatus(doIt1, doIt1);
		},
		// 清除编辑状态（未保存状态）
		removeEditedStatus : function() {
			runjs.edited = false;
			runjs.contentHash = runjs.codeMD5();
			$("title").html($("title").html().replace(/[*]/g, ""));

		},
		// 在某些新创建操作前作编辑状态判断，判断当前是否保存
		checkEditedStatus : function(doIt, cancelIt) {
			if (runjs.edited) {
				dialog.action.cancel = function(d) {
					if (typeof cancelIt != "undefined") {
						control_event.removeEditedStatus();
						cancelIt();
					}
				}
				dialog.action.doIt = function(d) {
					control_event.save(doIt);
				}
				dialog.doConfirm3.show("请保存后再继续操作，是否保存当前修改？");
			} else {
				if (typeof doIt != "undefined")
					doIt();
			}
		},
		resetView : function(html, js, css, preview, cur) {
			var view = controls.view;
			view.html_view = html;
			view.js_view = js;
			view.css_view = css;
			view.pre_view = preview;
			control_event.resetViewCheckbox(view);
			control_event.switchView(view);
			/*
			 * var c = $(cur); var event_bk = c.attr("onclick");
			 * c.attr("onclick","controls.control_event.showAllView(this)");
			 * c.attr("event_backup",event_bk);
			 * c.attr("src","/img/arrow-in.png");
			 */
		},
		showAllView : function(cur) {
			var view = controls.view;
			view.html_view = true;
			view.js_view = true;
			view.css_view = true;
			view.pre_view = true;
			control_event.resetViewCheckbox(view);
			control_event.switchView(view);
			/*
			 * var c = $(cur); var event_bk = c.attr("event_backup");
			 * c.attr("onclick",event_bk); c.removeAttr("event_backup");
			 * c.attr("src","/img/arrow-out.png")
			 */
		},
		resetViewCheckbox : function(view) {
			if (view.html_view) {
				$("#html_view").attr("checked", "checked");
			} else {
				$("#html_view").removeAttr("checked");
			}
			if (view.js_view) {
				$("#js_view").attr("checked", "checked");
			} else {
				$("#js_view").removeAttr("checked");
			}
			if (view.css_view) {
				$("#css_view").attr("checked", "checked");
			} else {
				$("#css_view").removeAttr("checked");
			}
			if (view.pre_view) {
				$("#pre_view").attr("checked", "checked");
			} else {
				$("#pre_view").removeAttr("checked");
			}
		},
		switchView : function(view) {
			var left = true;
			var right = true;
			if (view.html_view) {
				view.left.css({
					width : "50%"
				});
				view.right.css({
					width : "50%"
				});
				view.ver_ctrl.css({
					left : view.left.width() - 5
				});
				if (view.js_view) {
					view.html.css({
						height : "50%"
					});
					view.js.css({
						height : "50%"
					});
				} else {
					view.html.css({
						height : "100%"
					});
					view.js.css({
						height : 0
					});
				}
				view.hor_left.css({
					top : parseInt(view.html.height()) - 5
				});
			} else {
				if (view.js_view) {
					view.left.css({
						width : "50%"
					});
					view.right.css({
						width : "50%"
					});
					view.ver_ctrl.css({
						left : view.left.width() - 5
					});
					view.html.css({
						height : 0
					});
					view.js.css({
						height : "100%"
					});
					view.hor_left.css({
						top : -5
					});
				} else {
					// html和js都不显示
					left = false;
					view.left.css({
						width : 0
					});
					view.right.css({
						width : "100%"
					});
					view.ver_ctrl.css({
						left : -5
					});
				}
			}

			if (view.css_view) {
				if (left) {
					view.left.css({
						width : "50%"
					});
					view.right.css({
						width : "50%"
					});
					view.ver_ctrl.css({
						left : view.left.width() - 5
					});
				}
				if (view.pre_view) {
					view.css.css({
						height : "50%"
					});
					view.preview.css({
						height : "50%"
					});
				} else {
					view.css.css({
						height : "100%"
					});
					view.preview.css({
						height : 0
					});
				}
				view.hor_right.css({
					top : view.css.height() - 5
				});
			} else {
				if (view.pre_view) {
					// css不显示preview显示
					if (left) {
						view.left.css({
							width : "50%"
						});
						view.right.css({
							width : "50%"
						});
						view.ver_ctrl.css({
							left : view.left.width() - 5
						});
					}
					view.css.css({
						height : 0
					});
					view.preview.css({
						height : "100%"
					});
					view.hor_right.css({
						top : -5
					});
				} else {
					// css和preview都不显示
					right = false;
					view.right.css({
						width : 0
					});
					view.left.css({
						width : "100%"
					});
					view.ver_ctrl.css({
						left : view.left.width() - 5
					});
				}
			}
			var count = 0;
			var idx = 0;
			$(".editor").each(function(i, e) {
				e = $(e);
				if (e.height() != 0 && e.width() != 0) {
					count++;
					idx = i;
				} else {
					var c = e.find(".quick_tools img");
					var event_bk = c.attr("event_backup");
					if (typeof event_bk != 'undefined') {
						c.attr("onclick", event_bk);
						c.removeAttr("event_backup");
						c.attr("src", "/img/arrow-out.png")
					}
				}
			});
			if (count == 1) {
				var e = $(".editor:eq(" + idx + ")");
				var tool = e.find(".quick_tools img");
				var event_bk = tool.attr("onclick");
				tool
						.attr("onclick",
								"controls.control_event.showAllView(this)");
				tool.attr("event_backup", event_bk);
				tool.attr("src", "/img/arrow-in.png");
			} else if (count > 1) {
				$(".editor").each(function(i, e) {
					e = $(e);
					if (e.height() != 0 && e.width() != 0) {
						var c = e.find(".quick_tools img");
						var event_bk = c.attr("event_backup");
						if (typeof event_bk != 'undefined') {
							c.attr("onclick", event_bk);
							c.removeAttr("event_backup");
							c.attr("src", "/img/arrow-out.png")
						}
					}
				});
			}
			control_event.refreshEditors();
		},
		refreshEditors : function() {
			runjs.editorHtml.refresh()
			runjs.editorCss.refresh()
			runjs.editorJs.refresh()
		},
		add_advice : function() {
			var advice = $(".advice:eq(1)");
			var errorMsg = advice.find(".errorMsg");
			var email = advice.find("#advice_email");
			var content = advice.find("#advice_content");
			var captcha = advice.find("#captcha_advice");
			if ($.trim(email.val()).length == 0) {
				errorMsg.html("Email 不能为空");
				email.focus();
				return false;
			} else if (email.val().match(
					/^\w+([-+.']\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/) == null) {
				errorMsg.html("Email 格式错误");
				email.focus();
				return false;
			}
			if ($.trim(content.val()).length == 0
					|| $.trim(content.val()).length > 1000) {
				errorMsg.html("反馈内容长度为1~1000字符");
				content.focus();
				return false;
			}
			if ($.trim(captcha.val()).length == 0) {
				errorMsg.html("验证码不能为空");
				captcha.focus();
				return false;
			}
			var ident = (typeof User.code == "undefined" || typeof User.code.ident == "undefined") ? ""
					: User.code.ident;
			Api.ajax
					.add_advice(
							captcha.val(),
							ident,
							email.val(),
							content.val(),
							function(msg) {
								return control_event
										.errorHandler(
												msg,
												function(msg) {
													advice
															.html('<div style="text-align:center;color:#40AA53;font-size:20px;font-weight:bold;top:150px;position:relative;">感谢您的反馈和意见！</div>');
												}, function(msg) {
													errorMsg.html(msg.msg);
												}, true);
							});

		},
		upload : function() {
			var doIt = function() {
				dialog.action.upload = function() {
					var method = $(".resourceUpload:eq(1)").find(
							"[name='method']:checked");
					if (method.val() == "file") {
						$("form[name='upload_form']:eq(1)").ajaxForm(
								{
									success : function(msg) {
										return control_event.errorHandler(msg,
												function(msg) {
													// dialog.success.show("上传成功!");
													setTimeout(function() {
														location.reload();
													}, 1000);
												}, function(msg) {
													$(".resourceUpload:eq(1)")
															.find(".errorMsg")
															.html(msg.msg);
												}, true);
									}
								}).submit();
					} else if (method.val() == "url") {
						var url = method.parent().find("[name='url']");
						if (url.val().length == 0) {
							url.parent().parent().find(".errorMsg").html(
									"URL不能为空！");
							url.focus();
						}
						Api.ajax.add_url_file(url.val(), function(msg) {
							return control_event.errorHandler(msg,
									function(msg) {
										location.reload();
									}, function(msg) {
										url.parent().parent().find(".errorMsg")
												.html(msg.msg);
										url.focus();
									}, true);
						});
					}
				};
				dialog.upload.show(undefined, undefined, undefined, undefined,
						false);
				$(".resourceUpload:eq(1)").find("input[type='radio']").click(
						function() {
							var method = $(".resourceUpload:eq(1)").find(
									"[name='method']:checked");
							$(".resourceUpload:eq(1)").find("input:odd").attr(
									"disabled", "disabled");
							method.parent().find("input")
									.removeAttr("disabled");
						});
			}
			control_event.checkEditedStatus(doIt, doIt);
		},
		delete_file : function(id, sel) {
			dialog.action.doIt = function() {
				var cp = $("#captcha_delete_file");
				if (cp.val().length == 0) {
					cp.parent().parent().find(".errorMsg").html("验证码不能为空！");
					cp.focus();
					return false;
				}
				Api.ajax.delete_file(cp.val(), id, function(msg) {
					return control_event.errorHandler(msg, function(msg) {
						dialog.success.show("删除成功!");
						$("#file_" + id).remove();
						User.resources[sel]--;
						$("#" + sel).html(User.resources[sel]);
					}, function(msg) {
						cp.parent().parent().find(".errorMsg").html(msg.msg);
						cp.focus();
					}, true);
				});
			}
			dialog.action.cancel = function(d) {
				d.close(true);
			}
			var html = '<p>确认删除该资源文件？</p>'
					+ '<p>验证码：<img src="/action/project/captcha" class="captcha" style="cursor:pointer;vertical-align:middle;" width="60" height="23"/>'
					+ '<input id="captcha_delete_file" type="text" size="4"/></p>'
					+ '<p><span class="errorMsg"></span></p>';
			dialog.doConfirm2
					.show(html, undefined, undefined, undefined, false);
			$(".captcha").live(
					"click",
					function() {
						$(this).attr(
								"src",
								"/action/project/captcha?"
										+ (new Date().getTime()));
					});
		},
		delete_project : function(opt) {
			var project = opt.pid;
			dialog.action.delete_ = function(d) {
				var cp = $("#captcha_delete_prj");
				if (cp.val().length == 0) {
					cp.parent().parent().find(".errorMsg").html("验证码不能为空！");
					cp.focus();
					return false;
				}
				Api.ajax.delete_project(cp.val(), project, function(msg) {
					return control_event.errorHandler(msg, function(msg) {
						dialog.action.success_confirm = function() {
							location.href = "/";
						}
						location.href = "/";
						// dialog.success2.show("删除成功！");
					}, function(msg) {
						cp.parent().parent().find(".errorMsg").html(msg.msg);
						cp.focus();
					}, true);
				});
			};
			var title = $("#prj_" + project).find(".title").html();
			// var msg = '<p>删除当前项目，会将项目下的所有版本都一并删除，也会将所有发布过的版本的评论删除。</p>';
			var msg = '<p>确认删除该代码： <span style="color:red;font-weight:bold;">"'
					+ title + '"</span> ？</p>';
			msg += '<p>验证码：<img src="/action/project/captcha" class="captcha" style="cursor:pointer;vertical-align:middle;" width="60" height="23"/>'
					+ '<input id="captcha_delete_prj" type="text" size="4"/></p>'
					+ '<p><span class="errorMsg"></span></p>';
			dialog.delete_.show(msg, undefined, undefined, undefined, false);
			$(".captcha").live(
					"click",
					function() {
						$(this).attr(
								"src",
								"/action/project/captcha?"
										+ (new Date().getTime()));
					});

		},
		makeMenu : function(setting) {
			$(".setting_menu").remove();
			if (typeof setting == "undefined") {
				return;
			}
			var menu = $('<div class="setting_menu"></div>').addClass(
					setting.clazz);
			$
					.each(
							setting.layout,
							function(i, layout, agr3, arg4) {
								var ul = $("<ul></ul>");
								$
										.each(
												layout,
												function(j, item) {
													var li = $("<li></li>")
															.html(
																	typeof item.warn == 'undefined' ? item.title
																			: '<em class="red">'
																					+ item.title
																					+ '</em>');
													var showhk = typeof setting.showHotKey != 'undefined'
															&& setting.showHotKey;
													if (showhk) {
														var hotkey = $(
																"<span></span>")
																.html(
																		item.hotkey);
														li.append(hotkey);
													}
													// 绑定事件
													if (typeof item.event == "function") {
														li
																.click(function() {
																	$(
																			".setting_menu")
																			.remove();
																	item
																			.event(item.data);
																});
														// 注册快捷键
														if (showhk
																&& typeof item.hotkey != "undefined") {
															var key = item.hotkey
																	.split("+")[1];
															controls.altHotKey.events[key] = item.event;
															controls.altHotKey.data[key] = item.data;
														}
													} else if (typeof item.event == "string") {
														var a = $(
																"<a href='"
																		+ item.event
																		+ "' target='_blank'>"
																		+ item.title
																		+ "</>")
																.click(
																		function() {
																			$(
																					".setting_menu")
																					.remove();
																		});
														li.html(a);
													}
													if (typeof item.show == 'undefined'
															|| item.show)
														ul.append(li);
												});
								menu.append(ul);
							});
			menu.css({
				top : setting.top
			});
			$("body").append(menu);
			menu.unbind("mouseleave");
			menu.bind("mouseenter", function() {
				menu.bind("mouseleave", function() {
					menu.remove();
				});
				menu.unbind("mouseenter");
			})
			return menu;
		},
		showProjectMenu : function(pid) {
			var clazz = "project_menu_" + pid;
			var curprj = control_event.getCurProject(pid);
			var cur = $("#prj_" + pid + " .rename");
			if ($(".setting_menu").length != 0) {
				var menu = $(".setting_menu").remove();
				if (menu.attr("class").indexOf(clazz) > 0)
					return;
			}
			var setting = {
				layout : [ [ {
					title : '重命名',
					hotkey : 'Alt+R',
					event : control_event.rename,
					data : {
						name : curprj.name,
						id : pid
					}
				}, ],

				[ {
					title : '删除',
					event : control_event.delete_project,
					warn : true,
					data : {
						pid : pid
					}
				}, ] ],
				top : cur.offset().top + 10,
				showHotKey : true,
				clazz : clazz
			};
			control_event.makeMenu(setting);
		},
		showVersionMenu : function(ident, vid, pid, single) {
			var curprj = control_event.getCurProject(pid);
			var cv = control_event.getCurVersion(curprj, vid);
			var clazz = "version_menu_" + pid + "_" + vid;
			if ($(".setting_menu").length != 0) {
				var menu = $(".setting_menu").remove();
				if (menu.attr("class").indexOf(clazz) > 0)
					return;
			}
			var cur = $("#prj_" + pid + " .rename");
			var setting = {
				layout : [],
				top : cur.offset().top + 10,
				showHotKey : true,
				clazz : clazz
			};
			var pl = [];
			if (cv.post == 1) {
				pl = [ {
					title : "查看发布详情",
					event : User.host + "/detail/" + ident,
					show : cv.post == 1
				} ];
			}
			pl.push({
				title : "全屏预览",
				event : User.shost + "/show/" + ident,

			});
			setting.layout.push(pl);
			var ml1 = [ {
				title : '下载',
				event : User.host + "/action/api/get_zip?id=" + cv.id
			}, {
				title : '更新发布信息',
				event : control_event.publish,
				data : {
					vid : vid,
					pid : pid,
					single : single
				},
				show : cv.post == 1
			}, {
				title : '重命名',
				hotkey : 'Alt+R',
				event : control_event.rename,
				data : {
					name : curprj.name,
					id : pid
				}
			} ];
			var ml2 = [ {
				title : '删除',
				event : control_event.delete_project,
				warn : true,
				data : {
					pid : pid
				}
			} ];
			setting.layout.push(ml1);
			setting.layout.push(ml2);
			control_event.makeMenu(setting);
		}
	};

	var init = function(instance) {

		// if(typeof prj_info != "undefined")
		// control_event.load_projects(prj_info);

		instance.dialog = dialog;

		instance.control_event = control_event;

		instance.insertScript = insertScriptIntoHead;

		instance.altHotKey = {
			events : [],
			data : []
		};

		if (typeof User.code != 'undefined')
			control_event.updateCurrentUrl("/code/" + User.code.ident);
		else {
			User.ident = undefined;
			control_event.updateCurrentUrl("/");
		}

		var html = $(".html");
		var js = $(".js");
		var css = $(".css");
		var preview = $(".preview");
		var hor_left = html.parent().find(".handler_horizontal");
		var hor_right = css.parent().find(".handler_horizontal");
		var ver_ctrl = $(".handler_vertical");
		var left = $(".editorSet.left");
		var right = $(".editorSet.right");

		instance.view = {
			html_view : true,
			css_view : true,
			js_view : true,
			pre_view : true,
			ver : {
				left : {
					top : hor_left.css('top'),
					height_top : '50%',
					height_bottom : '50%'
				},
				right : {
					top : 0,
					height_top : 0,
					height_bottom : 0
				}
			},
			hor : {
				left : 0,
				width_left : 0,
				width_right : 0
			},
			html : html,
			js : js,
			hor_left : hor_left,
			css : css,
			preview : preview,
			hor_right : hor_right,
			ver_ctrl : ver_ctrl,
			left : left,
			right : right
		};

		instance.share = $(".share");

		instance.toolbar = $(".toolBar");

		instance.scriptsCache = {
			url : [],
			style : [],
			lib : []
		};

		var keys = CodeMirror.keyNames;

		// 页面快捷键处理
		$(document).keydown(
				function(event) {
					if (event.ctrlKey) {
						switch (keys[event.keyCode]) {
						case "S":// 保存
							setTimeout(function() {
								control_event.save();
							}, 200);
							event.preventDefault();
							event.returnvalue = false;
							cur_opt = 'save';
							return false;
							break;
						case "/":// 注释
							setTimeout(function() {
								control_event.comment();
							}, 200);
							event.preventDefault();
							event.returnvalue = false;
							cur_opt = 'save';
							return false;
							break;
						case "1":
							setTimeout(function() {
								var view = instance.view;
								view.html_view = true;
								view.js_view = false;
								view.css_view = false;
								view.pre_view = false;
								control_event.resetViewCheckbox(view)
								control_event.switchView(view);
							}, 200);
							event.preventDefault();
							event.returnvalue = false;
							return false;
							break;
						case "2":
							setTimeout(function() {
								var view = instance.view;
								view.html_view = false;
								view.js_view = true;
								view.css_view = false;
								view.pre_view = false;
								control_event.resetViewCheckbox(view)
								control_event.switchView(view);
							}, 200);
							event.preventDefault();
							event.returnvalue = false;
							return false;
							break;
						case "3":
							setTimeout(function() {
								var view = instance.view;
								view.html_view = false;
								view.js_view = false;
								view.css_view = true;
								view.pre_view = false;
								control_event.resetViewCheckbox(view)
								control_event.switchView(view);
							}, 200);
							event.preventDefault();
							event.returnvalue = false;
							return false;
							break;
						case "4":
							setTimeout(function() {
								var view = instance.view;
								view.html_view = false;
								view.js_view = false;
								view.css_view = false;
								view.pre_view = true;
								control_event.resetViewCheckbox(view)
								control_event.switchView(view);
							}, 200);
							event.preventDefault();
							event.returnvalue = false;
							return false;
							break;
						case "5":
							setTimeout(function() {
								var view = instance.view;
								view.html_view = true;
								view.js_view = true;
								view.css_view = true;
								view.pre_view = true;
								control_event.resetViewCheckbox(view)
								control_event.switchView(view);
							}, 200);
							event.preventDefault();
							event.returnvalue = false;
							return false;
							break;
						}
					} else if (event.altKey) {
						var hk = instance.altHotKey;
						var k = keys[event.keyCode];
						switch (k) {
						case "/":
							var cur_editor = runjs.focusEditor;
							if (cur_editor != null)
								CodeMirror.simpleHint(cur_editor,
										CodeMirror.javascriptHint);
							break;
						default:
							if (typeof hk.events[k] != "undefined") {
								hk.events[k](hk.data[k]);
								instance.altHotKey = {
									events : [],
									data : []
								};
								$(".setting_menu").remove();
							}
							break;
						}
					}
					switch (keys[event.keyCode]) {
					case "F1":
						event.preventDefault();
						event.returnvalue = false;
						setTimeout(function() {
							control_event.help();
						}, 200);
						return false;
						break;
					}
				});
		// 根据.btn的id绑定control_event中的方法
		$(".btn").live("click", function(event) {
			var action = $(this).attr("id");
			if (typeof action != 'undefined' && action.length > 0) {
				cur_opt = action;
				event.preventDefault();
				event.returnvalue = false;
				try {
					control_event[action]();
				} catch (e) {
					dialog.action.feedback = function(d) {

					};
					if (typeof e.stack != 'undefined') {
						dialog.jserror.show(e.stack.substring(0, 50));
					} else
						dialog.jserror.show(e.message);
					return;
				}
			}
		});

		// 创建项目
		$("#create_project").click(function(event) {
			event.preventDefault();
			// 使用默认模版创建项目
			control_event.create_project({
				tpl : 'default_'
			});
		});

		$("#add_resource").click(function() {
			control_event.upload();
		});

		// 左侧Explorer下拉效果
		$(".resources").find(".title .title_name").click(function() {
			$(this).parent().parent().toggleClass('dropped');
		});

		// 根据常用库，创建toolItem
		var common_libs = options.commons.library;

		// 加载常用库节点
		instance.toolbar.html("");
		for ( var idx = 0; idx < common_libs.length; idx++) {
			var cur_lib = common_libs[idx];
			if (Resouce[cur_lib]) {
				instance.toolbar.append('<div class="toolItem library" id="'
						+ cur_lib + '"></div>');
			}
		}
	    if(typeof User.code !="undefined" && User.code.post==1)
	    	instance.toolbar.append('<div class="toolItem view" style="float:right;"><a href="/detail/'+User.code.ident+'" target="_blank">查看发布详情&raquo;</a></div>');
		// 添加其他库节点
		var other_lib_dom = $('<div class="toolItem library" id="other"><div class="select"><div class="title">其他库<div class="arrow_bottom_blue"></div></div><ul></ul></div></div>');
		for ( var idx in Resouce) {
			var cur_lib = Resouce[idx];
			if (common_libs.indexOf(idx) == -1) {
				// 为其他库添加Javascipt（ul li节点
				var html = '';
				for ( var idx1 in cur_lib.scripts) {
					var script = cur_lib.scripts[idx1];
					html += '<li class="' + idx + '" title="' + script.text
							+ '">' + script.text + '</li>';
				}
				other_lib_dom.find("ul").append(html);
			}
		}
		instance.toolbar.append(other_lib_dom);

		// 填充常用JavaScript库至toolBar
		$(".toolBar .library").each(
				function() {
					var cur = $(this);
					var lib_name = cur.attr("id");
					var lib = Resouce[lib_name];
					if (!lib)
						return;
					cur.html("");
					var html = '<div class="select"><div class="title">'
							+ lib.text
							+ '<div class="arrow_bottom_blue"></div></div>';
					html += '<ul>'
					for ( var idx in lib.scripts) {
						var script = lib.scripts[idx];
						html += '<li title="' + script.text + '">'
								+ script.text + '</li>';
					}
					html += '</ul></div>';
					cur.append(html);
				}).find('li').live("click", function() {
			var cur = $(this);
			var lib_name = cur.parents(":eq(2)").attr("id");
			var other = lib_name;
			if (lib_name == 'other') {
				lib_name = cur.attr("class");
			}
			var lib = Resouce[lib_name];
			var script = lib.scripts[cur.index()];
			if (other == 'other') {
				var ot = cur.parent().find("." + lib_name);
				var cur_title = cur.attr("title");
				for ( var idx in ot) {
					if (ot.eq(idx).attr("title") == cur_title) {
						script = lib.scripts[idx];
						break;
					}
				}
			}
			var requires = lib.requires;
			var style = lib.style;
			if (typeof script.style != "undefined") {
				style = parseArray(style, script.style, undefined);
			}
			control_event.removeJsTagBlank();
			insertScriptIntoHead(script.url, requires, style, lib_name);
		});

		var js_switcher = $('<div class="toolItem view"><input type="checkbox" checked id="js_switcher"/><label for="js_switcher">启用JavaScript</label></div>');

		instance.toolbar.append(js_switcher);

		js_switcher.change(function() {
			runjs.update(runjs);
		});

		$(".select").mouseover(function() {
			$(this).find("ul").show();
		}).mouseout(function() {
			$(this).find("ul").hide();
		});

		$(".select li").click(function() {
			$(this).parent().hide();
		});

		/*
		 * //select显示 $(".select").find(".title").click(function() { var cur_ul =
		 * $(this).parent().find("ul").toggle(); })
		 * 
		 * $(".toolItem li").click(function() { $(this).parent().hide(); });
		 */

		// 切换视图
		$(".menuItem .view input").change(function() {
			var v = $(this);
			var id = v.attr('id');
			// 至少要打开一个视图
			if ($(".menuItem .view input:checked").length == 0)
				v.attr("checked", "checked");
			else {
				var view = instance.view;
				view[id] = !view[id];
				control_event.switchView(view);
			}
		});

		// 选择编辑器主题
		/*
		 * $("#editor_theme li.aTheme").click(function(){ var theme =
		 * $(this).html(); if(User.login){
		 * Api.ajax.setting("theme",theme,function(msg){ return
		 * control_event.errorHandler(msg,function(){
		 * dialog.success.show("主题("+theme+")设置成功！");
		 * runjs.editorHtml.setOption("theme",theme);
		 * runjs.editorCss.setOption("theme",theme);
		 * runjs.editorJs.setOption("theme",theme);
		 * $("#editor_theme").hide().parent().find(".title").html("主题("+theme+")<div
		 * class=\"arrow_bottom\"></div>"); }) }); }else{
		 * runjs.editorHtml.setOption("theme",theme);
		 * runjs.editorCss.setOption("theme",theme);
		 * runjs.editorJs.setOption("theme",theme);
		 * $("#editor_theme").hide().parent().find(".title").html("主题("+theme+")<div
		 * class=\"arrow_bottom\"></div>"); } });
		 */

		$(".editor").mouseover(function() {
			$(this).find(".quick_tools").show();
		}).mouseout(function() {
			$(this).find(".quick_tools").hide();
		});

		// 图片资源预览
		$(".image_resource .rurl").mouseover(
				function(event) {
					$("#image_preview").remove();
					var cur = $(this);
					var url = $(this).attr("href");
					img = $(
							"<img id='image_preview' src='" + url
									+ "' width='100'/>").css({
						left : cur.offset().left + 180,
						top : cur.offset().top,
						position : 'absolute',
						'z-index' : 999999
					});
					$("body").append(img);
				}).mouseout(function() {
			$("#image_preview").remove();
		});

		// 将css/js资源引入到html的头部
		$(".js_resource .rurl").click(function(event) {
			var editor = runjs.editorHtml;
			var path = $(this).attr("href");
			instance.insertScript(path, undefined, undefined, 'resources')
			event.preventDefault();
			event.returnvalue = false;
		});
		$(".css_resource .rurl").click(function(event) {
			var editor = runjs.editorHtml;
			var path = $(this).attr("href");
			instance.insertScript(undefined, undefined, path, 'resources')
			event.preventDefault();
			event.returnvalue = false;
		});

		// 向当前视图中插入img链接
		$(".image_resource .rurl,.other_resource .rurl").click(function(event) {
			var editor = runjs.focusEditor;
			if (typeof editor != "undefined") {
				var path = $(this).attr("href");
				var range = control_event.getSelectedRange(editor)
				editor.replaceRange(path, range.from, range.to);
			}
			event.preventDefault();
			event.returnvalue = false;
		});

		// 鼠标滞留显示设置图标
		$(".project .title").mouseover(function() {
			$(this).find(".rename").show();
		}).mouseout(function() {
			$(this).find(".rename").hide();
		});

		// 鼠标滞留显示设置图标
		$(".resources .version").mouseover(function() {
			$(this).find(".rdelete").show();
		}).mouseout(function() {
			$(this).find(".rdelete").hide();
		});

		// 鼠标滞留显示设置图标
		$(".projects .version").mouseover(function() {
			$(this).find(".nocurrent").show();
		}).mouseout(function() {
			$(this).find(".nocurrent").hide();
		});

		// 点击编辑器隐藏设置菜单
		$(".CodeMirror").click(function() {
			$(".setting_menu").remove();
		});

		// 隐藏Explorer
		$(".fold_control").toggle(
				function() {
					var cur = $(this);
					$(".explorer").css({
						width : 0,
						opacity : 0
					});
					$(".core_margin").removeClass("core_margin").addClass(
							"core_margin1");
					cur.css({
						left : 2
					});
					cur.removeClass("on").addClass("off");
					runjs.resize();
				},
				function() {
					var cur = $(this);
					$(".explorer").css({
						width : 210,
						opacity : 1
					});
					$(".core_margin1").removeClass("core_margin1").addClass(
							"core_margin");
					cur.css({
						left : 215
					});
					cur.removeClass("off").addClass("on");
					runjs.resize();
				});

	}

	var insertScriptIntoHead = function(url, requires, style, lib_name) {
		// 检查引库时HTML视图是否打开
		var view = controls.view;
		if (!view.html_view) {
			view.html_view = true;
			control_event.resetViewCheckbox(view);
			control_event.switchView(view);
		}

		// 检索代码中引入的库，重置scriptsCache
		resetScriptsCache();

		// 先从scriptsCache中过滤掉重复脚本以及将相同库替换成新引入的
		var newCache = insertScriptsCache(url, requires, style, lib_name);

		lib_name = lib_name + " library";

		var editor = runjs.editorHtml;
		var ln = 0, line = "", loc = null, indent = "";
		var rm_lines = [];
		// 移除lib
		while (line = editor.getLine(ln)) {
			try {
				if ((line.indexOf("</script>") > -1 || line.indexOf("<link") > -1)
						&& typeof ($(line).attr("class")) != 'undefined'
						&& $(line).attr("class").indexOf("library") > -1) {
					rm_lines[rm_lines.length] = ln;
				}
			} catch (e) {
			}
			ln++;
		}
		if (rm_lines.length == 1)
			editor.removeLine(rm_lines[0]);
		else if (rm_lines.length > 1)
			editor.replaceRange("", {
				line : rm_lines[0],
				ch : 0
			}, {
				line : rm_lines[rm_lines.length - 1] + 1,
				ch : 0
			})

		ln = 0, line = "", loc = null, indent = "";
		// 若发现有</head>标签，则在之前一行插入所有资源
		while (ln <= editor.lineCount()) {
			line = editor.getLine(ln)
			if (typeof line != "undefined" && line.indexOf("</head>") > -1) {

				var index = line.lastIndexOf("\t");
				for ( var i = 0; i <= index + 1; i++)
					indent += '\t';

				loc = {
					ch : 0,
					line : ln
				};
				break;
			} else {
				ln++;
			}
		}
		// 无head标签
		if (loc == null) {
			loc = {
				ch : 0,
				line : 0
			};
			indent = "";
		}

		var scripts = '';
		var styles = '';
		var cache = newCache;

		for ( var idx = 0; idx < cache.url.length; idx++)
			scripts += indent + '<script class="' + cache.url[idx].lib
					+ ' library" src="' + cache.url[idx].url
					+ '" type="text/javascript"></script>\n';

		var sl = cache.style.length;
		for ( var idx = 0; idx < cache.style.length; idx++)
			styles += indent + '<link class="' + cache.style[idx].lib
					+ ' library" rel="stylesheet" type="text/css" href="'
					+ cache.style[idx].url + '">\n';
		if (styles.length != 0)
			editor.replaceRange(styles, loc);
		loc.line += sl;
		if (scripts.length != 0)
			editor.replaceRange(scripts, loc);
	}

	var parseArray = function(url1, url2, withlib) {
		var url = [];

		if (typeof url1 == "string") {
			if (typeof withlib == "undefined") {
				url[url.length] = url1;
			} else {
				url[url.length] = {
					url : url1,
					lib : withlib
				};
			}
		} else if (typeof url1 == "object") {
			if (typeof url1.length == "undefined")
				url[url.length] = url1;
			else
				for ( var idx = 0; idx < url1.length; idx++) {
					if (typeof withlib == "undefined") {
						url[url.length] = url1[idx];
					} else {
						url[url.length] = {
							url : url1[idx],
							lib : withlib
						};
					}
				}
		}

		if (typeof url2 == "string") {
			if (typeof withlib == "undefined") {
				url[url.length] = url2;
			} else {
				url[url.length] = {
					url : url2,
					lib : withlib
				};
			}
		} else if (typeof url2 == "object") {
			if (typeof url2.length == "undefined")
				url[url.length] = url2;
			else
				for ( var idx = 0; idx < url2.length; idx++) {
					if (typeof withlib == "undefined") {
						url[url.length] = url2[idx];
					} else {
						url[url.length] = {
							url : url2[idx],
							lib : withlib
						};
					}
				}
		}

		return url;
	}

	var urlExists = function(url, urlarr) {
		for ( var idx = 0; idx < urlarr.length; idx++) {
			if (typeof url == "string") {
				if (typeof urlarr[idx] == "string" && url == urlarr[idx]) {
					return true;
				} else if (urlarr[idx].url == url) {
					return true;
				}
			} else {
				if (typeof urlarr[idx] == "string" && url.url == urlarr[idx]) {
					return true;
				} else if (urlarr[idx].url == url.url) {
					return true;
				}
			}
		}
		return false;
	}
	// 检查是否引入外部库
	var checkExternalScripts = function(js_arr) {
		if (typeof js_arr != "object" || typeof js_arr.length == "undefined")
			return false;
		for ( var i = 0; i < js_arr.length; i++) {
			if (js_arr[i].indexOf(User.shost) > -1)
				return true;
		}
		return false;
	}

	// 脚本去重
	var insertScriptsCache = function(url, requires, style, lib) {

		var newCache = {
			url : [],
			style : [],
			remove_lib : null
		};

		var jscache = instance.scriptsCache.url;
		var csscache = instance.scriptsCache.style;
		var libcache = instance.scriptsCache.lib;
		var js = parseArray(requires, url, lib);
		var css = parseArray(style, undefined, lib);

		if (libcache.indexOf(lib) == -1) {
			libcache[libcache.length] = lib;
		} else {
			newCache.remove_lib = lib;
			var tempjs = [], tempcss = [];
			// 若先引入的库与当前引入的库不同,并且当前url未曾引入过,则将其加入临时库中
			for ( var idx = 0; idx < jscache.length; idx++) {
				if ((jscache[idx].lib != lib || lib == "others")
						&& !urlExists(jscache[idx], js)) {
					tempjs = parseArray(jscache[idx], tempjs);
				}
			}
			for ( var idx = 0; idx < csscache.length; idx++) {
				if ((csscache[idx].lib != lib || lib == "others")
						&& !urlExists(csscache[idx], css)) {
					tempcss = parseArray(csscache[idx], tempcss);
				}
			}
			jscache = tempjs;
			csscache = tempcss;
		}

		newCache.url = parseArray(jscache, js);

		newCache.style = parseArray(csscache, css);

		return newCache;
	}

	var resetScriptsCache = function() {
		var line, ln = 0;
		var editor = runjs.editorHtml;
		instance.scriptsCache = {
			url : [],
			style : [],
			lib : []
		};
		var jscache = instance.scriptsCache.url;
		var csscache = instance.scriptsCache.style;
		var libcache = instance.scriptsCache.lib;
		while (line = editor.getLine(ln)) {
			try {
				if (line.indexOf("</script>") > -1
						&& typeof ($(line).attr("class")) != 'undefined'
						&& $(line).attr("class").indexOf("library")) {
					var url = $(line).attr("src");
					if (jscache.length > 0 && jscache.indexOf(url) > -1) {
						ln++;
						continue;
					}
					var lib_name = $(line).removeClass("library").attr("class");
					if (Resouce[lib_name] && libcache.indexOf(lib_name) == -1)
						libcache[libcache.length] = lib_name;
					jscache[jscache.length] = {
						url : url,
						lib : lib_name
					};
					;
				}
				if (line.indexOf("<link") > -1
						&& $(line).attr("class").indexOf("library")) {
					var css = $(line).attr("href");
					if (csscache.length > 0 && csscache.indexOf(css) > -1) {
						ln++;
						continue;
					}
					var lib_name = $(line).removeClass("library").attr("class");
					if (Resouce[lib_name] && libcache.indexOf(lib_name) == -1)
						libcache[libcache.length] = lib_name;
					csscache[csscache.length] = {
						url : css,
						lib : lib_name
					};
				}
			} catch (e) {
			}
			ln++;
		}
	}

	var Instance = function(opt) {
		for ( var v in opt) {
			if (opt[v])
				options[v] = opt[v];
		}
		this.options = options;
		init(this);
	}

	var instance = new Instance(opt)

	return instance;
};

/*---模版向导---*/
var Template = {
	html : '',
	css : '',
	js : '',
	wizards : {
		default_ : function(callback) {
			Template.html = '<!DOCTYPE html>\n<html>\n\t<head>\n\t\t<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">\n\t\t<title>RunJS<\/title>\n\t<\/head>\n\t<body>\n\t\t<button onclick=\"javascript:say_hello();\">Hello RunJS!<\/button>\n\t<\/body>\n<\/html>';
			Template.css = 'button{\n\tborder:1px solid #ccc;\n\tcursor:pointer;\n  display:block;\n  margin:auto;\n  position:relative;\n  top:100px;\n}';
			Template.js = 'function say_hello(){\n\t alert("Hello RunJS!");\n}';
			if (typeof callback != 'undefined')
				callback(Template);
		}
	}
};

$(function() {
	runjs = RunJS();
	controls = Controls({
		commons : {
			library:['jquery','jquerymobile','mootools','dojo','prototype','yui']
		}
	});
	if (typeof User.action != "undefined") {
		cur_opt = User.action;
		var a = controls.control_event[User.action];
		if (typeof a != "undefined") {
			a();
		}
	}
});

/*---IE Extend---*/
if (!Array.prototype.indexOf) {
	Array.prototype.indexOf = function(elt /* , from */) {
		var len = this.length >>> 0;

		var from = Number(arguments[1]) || 0;
		from = (from < 0) ? Math.ceil(from) : Math.floor(from);
		if (from < 0)
			from += len;

		for (; from < len; from++) {
			if (from in this && this[from] === elt)
				return from;
		}
		return -1;
	};
}