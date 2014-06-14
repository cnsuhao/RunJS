$(document).ready(function() {
	/*
	 * $(window).bind('scroll', onScroll);
	 * 
	 * $(".loading").ajaxStart(function() { $(this).show();
	 * }).ajaxStop(function() { $(this).hide(); });
	 */
	if (typeof isIE6 != "undefined") {
		$(".logo").attr({
			"class" : "ie6_logo"
		})
	}
	$(".booth_show").live("mouseenter", function() {
		$(this).find(".booth_description").css("display", "block");
	});

	$(".booth_show").live("mouseleave", function() {
		$(".booth_description").css("display", "none");
	});

	$(".squareMenu li").live("click", function() {
		$(this).addClass("focus");
		$(this).siblings().removeClass("focus");
	});

	$(".select").mouseover(function() {
		$(this).find("ul").show();
	}).mouseout(function() {
		$(this).find("ul").hide();
	});

	$(".select li").click(function() {
		$(this).parent().hide();
	});
});
var RURL = location.href;
function login(type) {
	var url = "http://runjs.cn/action/openid/before_login?op=" + type;
	openwindow(url, 'loginPage', 800, 600);
}

function openwindow(url, name, iWidth, iHeight) {
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
}
function logout(uid) {
	$.post("/action/ajax/logout", 'uid=' + uid, function(msg) {
		location.reload();
	});
}
/*
 * var next_page = 2; var fetch_url = "/fetch_code"; var loading = false;
 * function onScroll() { // Check if we're within 100 pixels of the bottom edge
 * of the broser window. var closeToBottom = ($(window).scrollTop() +
 * $(window).height() > $( document).height() - 100); if (closeToBottom &&
 * !loading) { // Get the first then items from the grid, clone them, and add
 * them to // the bottom of the grid. var items = $('#tiles li'); loading =
 * true; if (next_page <= 10) { $.post(fetch_url, "p=" + next_page,
 * function(html) { if (loading) { $('#code_list').append(html); next_page =
 * next_page + 1; loading = false; } $(window).resize(); }); } else {
 * $(".loading").html("没有更多了").show(); } } }
 */