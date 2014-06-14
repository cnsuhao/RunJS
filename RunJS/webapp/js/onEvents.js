/**
 *系统事件，个模块在某特定条件下自动触发的事件，为了更便于查看，将 {{#crossLink "Plugins"}}{{/crossLink}} 中的 onEvents 抽离出来单独说明
 *@class onEvents
 */
onEvents = (function () {
	
	/**
	 *当编辑器视图初始化或重置后时调用
	 *@method onEditorViewInit
	 *@param {Editor} editor 编辑器模块实例
	 */
	onEvents.prototype.onEditorViewInit = function(editor) {
		
	};

	/**
	 *当顶部菜单视图初始化或重置后调用
	 *@method onMenuViewInit
	 *@param {Menu} menu 菜单模块实例
	 */
	onEvents.prototype.onMenuViewInit = function(menu) {
		// body...
	};

	/**
	 *当左边的资源管理器初始化或重置后调用
	 *@method onExplorerViewInit
	 *@param {Explorer} explorer 资源管理器模块实例
	 */
	onEvents.prototype.onExplorerViewInit = function(explorer) {
		// body...
	};

	/**
	 *上下文菜单加载后调用
	 *@method onContextMenuLoad
	 *@param {Object} menu 上下文菜单jQuery包装对象
	 */
	onEvents.prototype.onContextMenuLoad = function(menu) {
		// body...
	};

	/**
	 *上下文菜单消失后调用
	 *@method onContextMenuRemove
	 *@param {Object} menu 上下文菜单jQuery包装对象
	 */
	onEvents.prototype.onContextMenuRemove = function(menu) {
		// body...
	};

	/**
	 *上下文菜单显示前调用
	 *@method beforeContextMenuLoad
	 *@param {Object} menu 上下文菜单jQuery包装对象
	 */
	onEvents.prototype.beforeContextMenuLoad = function(menu) {
		// body...
	};

	/**
	 *当对话框弹出后调用
	 *@method onDialogLoad
	 *@param {Object} dialog Zebra Dialog 窗口实例
	 */
	onEvents.prototype.onDialogLoad = function(dialog) {
		// body...
	};

	/**
	 *当引入脚本时调用
	 *@method onScriptImport
	 *@param {String} script 如果当前是从toolbar区域引入的则返回脚本的唯一标识，否则则返回引入的脚本链接地址
	 */
	onEvents.prototype.onScriptImport = function(script) {
		// body...
	};

	/**
	 *当HTML编辑器内容有改变时调用
	 *@method onHtmlEditorChange
	 *@param {CodeMirror} cm CodeMirror 实例，当前的HTML编辑器
	 */
	onEvents.prototype.onHtmlEditorChange = function(cm) {
		// body...
	};

	/**
	 *当JavaScript编辑器视图内容变化时调用
	 *@method onJsEditorChange
	 *@param {CodeMirror} cm CodeMirror 实例，当前的JavaScript编辑器
	 */
	onEvents.prototype.onJsEditorChange = function(cm) {
		// body...
	};
	
	/**
	 *当CSS编辑器内容变化时调用
	 *@method onCssEditorChange
	 *@param {CodeMirror} cm CodeMirror 实例，当前的CSS编辑器
	 */
	onEvents.prototype.onCssEditorChange = function(cm) {
		// body...
	};

	/**
	 *当Html编辑器中光标变动时调用
	 *@method onHtmlCursorActivity
	 *@param {CodeMirror} cm CodeMirror 实例，当前的HTML编辑器
	 */
	onEvents.prototype.onHtmlCursorActivity = function(cm) {
		// body...
	};

	/**
	 *当Js编辑器中光标变动时调用
	 *@method onJsCursorActivity
	 *@param {CodeMirror} cm CodeMirror 实例，当前的JavaScript编辑器
	 */
	onEvents.prototype.onJsCursorActivity = function(cm) {
		// body...
	};

	/**
	 *当Css编辑器中光标变动时调用
	 *@method onCssCursorActivity
	 *@param {CodeMirror} cm CodeMirror 实例，当前的CSS编辑器
	 */
	onEvents.prototype.onCssCursorActivity = function(cm) {
		// body...
	};
}();