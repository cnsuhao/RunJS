$(function() {
	if (typeof isIE6 != "undefined") {
		$(".logo").attr({
			"class" : "ie6_logo"
		})
	}
	$(window).bind("resize", function() {
		resize();
	})
	$(".comment").click(function() {
		$(".fork").attr("class", "nofocus fork");
		$(this).attr("class", "focus comment");
		$(".detail_comment").show();
		$(".detail_fork").hide();
	});
	$(".fork").click(function() {
		$(".comment").attr("class", "nofocus comment");
		$(this).attr("class", "focus fork");
		$(".detail_comment").hide();
		$(".detail_fork").show();
	});
	$("#logout").click(function() {
		logout();
	});
	$(".prelogin").click(function() {
		new $.Zebra_Dialog($("#user_login").html(), {
			'title' : '登录方式',
			'modal' : true,
			'width' : 460,
			'type' : false,
			'buttons' : [ {
				caption : '取消'
			} ]
		}).show();
		/*
		 * var url; //转向网页的地址; var name; //网页名称，可为空; var iWidth; //弹出窗口的宽度; var
		 * iHeight; //弹出窗口的高度; var iTop =
		 * (window.screen.availHeight-30-iHeight)/2; //获得窗口的垂直位置; var iLeft =
		 * (window.screen.availWidth-10-iWidth)/2; //获得窗口的水平位置;
		 * window.open(url,name,'height='+iHeight+',,innerHeight='+iHeight+',width='+iWidth+',innerWidth='+iWidth+',top='+iTop+',left='+iLeft+',toolbar=no,menubar=no,scrollbars=auto,resizeable=no,location=no,status=no');
		 */
	});
	resize();
	$("button.detail_comment_button").click(function() {
		addComment();
	});
	$("textarea[name='content']").keydown(function(event) {
		if (event.ctrlKey && event.keyCode == 13) {
			addComment();
		}
	});
	$("#view_scale").change(function() {
		var sc = $(this).val();
		var w = '100%';
		var h = '100%';
		var t = 'scale(1)';
		if (sc == '0.5') {
			w = '200%';
			h = '200%';
			t = 'scale(0.5)';
		} else if (sc == '2') {
			t = 'scale(2)';
		}
		$("#code_detail").css({
			'width' : w,
			'height' : h,
			'-webkit-transform' : t,
			'transform' : t
		});
	});
	$("#show_gist").mouseenter(function(){
		show_gist();
	});
});

var resize = function() {
	var width = $(window).width();
	var cw = $(".comment").width();
	var w = width;
	if (w < 1000)
		w = 1000;
	w = w - 300 - 60;
	$(".detail_mainContent_left,.detail_comment_input textarea")
			.css("width", w);
	$(".detail_wrapper").css("width", width < 1000 ? 960 : width - 40);
	$(".fork").css("width", w - cw - 43);
}
function logout() {
	Api.ajax.logout(function(msg) {
		location.href = RURL;
	});
}
function login(op) {
	var url = "http://runjs.cn/action/openid/before_login?op=" + op; // 转向网页的地址;
	var name = "用" + op + "登录RunJS"; // 网页名称，可为空;
	var iWidth = 800; // 弹出窗口的宽度;
	var iHeight = 600; // 弹出窗口的高度;
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
}
function addComment() {
	var ctn = $("textarea[name='content']");
	var content = ctn.val();
	if (content.length == 0 || content == "觉得怎么样？赶紧说几句"){
		alert("请输入评论内容");
		return;
	}
	Api.ajax
			.add_comment(
					Code.id,
					content,
					function(msg) {
						var msg = eval("(" + msg + ")");
						if (msg.error) {
							alert(msg.msg);
							return false;
						}
						var comment = $('<li id="comment_'
								+ msg.id
								+ '">'
								+ '<img src="'
								+ User.portrait
								+ '" width="48" height="48">'
								+ '<p><a href="'
								+ (typeof User.space == "undefined" ? 'javascript:void(0);'
										: User.space)
								+ '">'
								+ User.name
								+ '</a><span class="time">1分钟前</span><span class="delete"><a href="javascript:removeComment('
								+ msg.id + ');">删除</a></span></p>' + '<p>'
								+ msg.content + '</p>' + '</li>');
						comment.hide();
						$(".detail_comment_list").prepend(comment);
						comment.show(500);
						$(".zeroComment").remove();
						ctn.val("");
					});
}
function removeComment(id) {
	if (confirm("确认删除此条评论？")) {
		Api.ajax
				.delete_comment(
						id,
						function(msg) {
							var msg = eval("(" + msg + ")");
							if (msg.error) {
								alert(msg.msg);
								return false;
							}
							$("#comment_" + id)
									.hide(
											500,
											function() {
												$(this).remove();
												if ($(".detail_comment_list li").length == 0) {
													$(".detail_comment_list")
															.prepend(
																	'<li class="zeroComment" style="text-align:center;">还没有人评论哦，赶紧抢个沙发吧~~</li>');
												}
											});
						});
	}
}

function viewAll(t) {
	$(".detail_otherProject li").show();
	$("#view_all_li").remove();
}
function add_to_square(id) {
	Api.ajax.add_to_square(id, function(msg) {
		var msg = eval("(" + msg + ")");
		if (msg.error) {
			alert(msg.msg);
			return false;
		} else {
			alert("添加成功");
			location.reload();
		}
	});
}
function update_to_square(id) {
	Api.ajax.update_to_square(id, function(msg) {
		var msg = eval("(" + msg + ")");
		if (msg.error) {
			alert(msg.msg);
			return false;
		} else {
			alert("更新成功");
			location.reload();
		}
	});
}

function delete_from_square(id) {
	$.post("/action/square/delete", {
		"id" : id
	}, function(msg) {
		var msg = eval("(" + msg + ")");
		if (msg.error) {
			alert(msg.msg);
			return false;
		} else {
			alert("去除成功");
			location.reload();
		}
	});
}
function love(id) {
	Api.ajax.vote(id, 1, function(m) {
		m = eval("(" + m + ")");
		if (m.error) {
			alert(m.msg);
		} else {
			var ar = $(".detail_interactArea_like a");
			ar.css({
				'background-image' : 'url(../img/liked.gif)'
			});
			ar.attr("href", "javascript:void(0);");
			love_count++;
			ar.html("已喜欢<span>（" + love_count + "）</span>");
		}
	});
}
function show_gist(){
	$("#gist_span").show();
	$("#gist_input").focus().select();
}