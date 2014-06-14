$(function(){
	var editorHtml = new CodeMirror(document.getElementById("code_html"));
	var editors = {
		html:null,
		js:null,
		css:null,
		preview:null
	}
	runjs = new RunJS({
		editors:editors
	});
	runjs.init();
});