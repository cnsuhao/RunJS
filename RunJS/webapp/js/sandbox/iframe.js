var RUNJS_HOST = "http://sandbox.runjs.cn";
var JS = {};
var onmessage = function(e) {
	var data = eval("(" + e.data + ")");
	var html = unescape(data.html) + '<style>' + unescape(data.css)
			+ '</style>' + '<script>' + unescape(data.js) + '</script>';
	JS.myInnerHTML(document.getElementsByTagName("body")[0], html)
};

if (typeof window.addEventListener != 'undefined') {
	window.addEventListener('message', onmessage, false);
} else if (typeof window.attachEvent != 'undefined') {
	window.attachEvent('onmessage', onmessage);
}

JS.myInnerHTML = function(elem, html) {
	/* 生成一个动态 */
	var dynDiv = document.createElement('div');
	/* 把内容都添加到此div中 , 因为如果第一个节点为script时ie会忽略此节点，所以就要上面加一个新节点 */
	dynDiv.innerHTML = '<span style="display:none;">for ie</span>' + html;
	/* 取出动态div中的script节点 */
	var scripts = dynDiv.getElementsByTagName('script');
	/* 取出head节点，再新生成的节点添加到head中 */
	var head = document.getElementsByTagName('head')[0];
	/* 把script中的脚本或要引入的外部 脚本 */
	for ( var i = 0; i < scripts.length; i++) {
		var jsCode = '';
		/* 如果为外部脚本，就再去加载数据 */
		var src = scripts[i].src;
		if (src) {
			JS.ajax({
				url : src,
				type : 'get',
				success : function(respon) {
					jsCode = respon.responseText;
					evalJs(jsCode);
				}
			});
			/* 如果只是内部脚本，就取出程序代码 */
		} else {
			jsCode = scripts[i].innerText || scripts[i].textContent
					|| scripts[i].text || '';
			evalJs(jsCode);
		}
	}
	function evalJs(jsCode) {
		/* 新建一个script节点 */
		var scpt = document.createElement('script');
		scpt.type = 'text/javascript';
		scpt.text = jsCode;
		head.insertBefore(scpt, head.firstChild);
		head.removeChild(scpt);
	}
	/* 删除内容中的script节点 */
	for ( var i = 0; i < scripts.length; i++) {
		if (scripts[0].parentNode) {
			scripts[0].parentNode.removeChild(scripts[0]);
			i--;
		}
	}
	elem.innerHTML = dynDiv.innerHTML;
}

JS.getXmlHttp = function() {
	var xmlhttp;
	if (window.XMLHttpRequest) {
		// code for IE7+, Firefox, Chrome, Opera, Safari
		xmlhttp = new XMLHttpRequest();
	} else {
		// code for IE6, IE5
		xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
	}
	return xmlhttp;
}

JS.ajax = function(data) {
	if (typeof data == "object") {
		var xmlhttp = JS.getXmlHttp();
		xmlhttp.onreadystatechange = function() {
			if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
				data.success(xmlhttp);
			}
		}
		xmlhttp.open(data.type, data.url, false);
		xmlhttp.send();
	}
};