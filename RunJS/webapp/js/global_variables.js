/**
 * 全局变量定义，定义了 全局状态信息、用户账户相关信息、用户设置信息
 * @class Global_Variables
 */
Global_Variables = (function(){
	
	/**
	 * {{#crossLink "RunJS"}}{{/crossLink}} 实例
	 * @property runjs
	 * @type {RunJS}
	 */
	var runjs = new RunJS({
		editor:editor,
		explorer:explorer,
		menu:menu,
		flow:flow,
		dialog:dialog,
		plugins:plugins,
		cur_opt:"login",
		mode:'code'
	});
	
	/**
	 * <a href="..\classes\Editor.html" class="crosslink">Editor</a> 实例，以 runjs.editor 访问该实例
	 * @property runjs.editor
	 * @type {Editor}
	 */
	var runjs.editor = new Editor({
		target:$(".editor_wrapper")
	});
	
	/**
	 * <a href="..\classes\Explorer.html" class="crosslink">Explorer</a> 实例，以 runjs.explorer 访问该实例
	 * @property runjs.explorer
	 * @type {Explorer}
	 */
	var runjs.explorer = new Explorer({
		target:$(".explorer"),
		fold:$(".fold_control"),
		core:$(".core"),
		show:true
	});
	
	/**
	 * <a href="..\classes\Menu.html" class="crosslink">Menu</a> 实例，以 runjs.menu 访问该实例
	 * @property runjs.menu
	 * @type {Menu}
	 */
	var runjs.menu = new Menu({
		target:$(".headerMenu"),
	});
	
	/**
	 * <a href="..\classes\Dialog.html" class="crosslink">Dialog</a> 实例
	 * @property dialog
	 */
	var dialog = new Dialog();
	
	/**
	 * <a href="..\classes\Flow.html" class="crosslink">Flow</a> 实例
	 * @property flow
	 */
	var flow = new Flow();
	
	/**
	 * 全局状态信息,通过 runjs.initStatus(ident) 可更新当前代码状态信息
	 * @property g_status
	 * @type {JSON}
	 */
	var g_status = {
		/**
		 * 是否已登录
		 * @property g_status.login
		 * @type {Boolean}
		 */
		login:true,
		/**
		 * 当前代码是否为我的代码
		 * @property g_status.isMyCode
		 * @type {Boolean}
		 */
		isMyCode:true,
		/**
		 * 当前代码是否为他人的代码
		 * @property g_status.isOtherCode
		 * @type {Boolean}
		 */
		isOtherCode:false,
		/**
		 * 当前用户是否为存在代码
		 * @property g_status.hasNoCode
		 * @type {Boolean}
		 */
		hasNoCode:false,
		/**
		 * 当前代码是否为示例代码
		 * @property g_status.isDemo
		 * @type {Boolean}
		 */
		isDemo:false,
		/**
		 * 当前代码是否已发布
		 * @property g_status.posted
		 * @type {Boolean}
		 */
		posted:false,
		/**
		 * 代码签名，每次保存过后签名将改变，防止两个编辑器同时编辑同一份代码导致代码丢失
		 * @property g_status.sign
		 * @type {String}
		 */
		sign:'xxxxx',
		/**
		 * 代码ID
		 * @property g_status.codeid
		 * @type {Nubmer}
		 */
		codeid:0,
		/**
		 * 代码名称
		 * @property g_status.codename
		 * @type {String}
		 */
		codename:'',
		/**
		 * 代码唯一表示，<span style="color:#A00">[注]</span>该属性被频繁使用
		 * @property g_status.ident
		 * @type {String}
		 */
		ident:'',
		/**
		 * host路径
		 * @property g_status.host
		 * @type {String}
		 */
		host:'http://runjs.cn',
		/**
		 * 沙盒路径
		 * @property g_status.shost
		 * @type {String}
		 */
		shost:'http://sandbox.runjs.cn/',
		/**
		 * 开发模式，'code'为一般模式，'plugin'为插件开发模式
		 * @property g_status.mode
		 * @type {String}
		 */
		mode:'code',
		/**
		 * HTML代码类型
		 * @property g_status.htmlType
		 * @type {Number}
		 */
		htmlType:1,
		/**
		 * CSS代码类型，1为CSS，2为LESS
		 * @property g_status.cssType
		 * @type {Number}
		 */
		cssType:1,
		/**
		 * JavaScript代码类型，1为JavaScript，2为CoffeeScript
		 * @property g_status.jsType
		 * @type {Number}
		 */
		jsType:1
	};
	
	/**
	 * <a href="..\classes\Utils.html" class="crosslink">Utils</a> 实例
	 * @property g_utils
	 */
	var g_utils = new Utils();
	
	/**
	 * 编辑器所在路径
	 * @property editor_path
	 * @type {String}
	 */
	var editor_path = "/code";
	
	/**
	 * 用户相关属性
	 * @property User
	 * @type {JSON}
	 */
	var User = {
			/**
			 * 用户ID
			 * @property User.user
			 * @type {Number}
			 */
			user:0,
			/**
			 * 用户安全标识，与服务器端进行ajax交互式所需参数
			 * @property User.v_code
			 * @type {String}
			 */
			v_code:'',
			/**
			 * 是否为管理员
			 * @property User.admin
			 * @type {Boolean}
			 */
			admin:false
	};
	
	/**
	 * 用户设置信息
	 * @property Setting
	 * @type {JSON}
	 */
	var Setting = {
			/**
			 * 编辑器主题，"night"|"default"
			 * @property Setting.theme
			 * @type {String}
			 */
			theme:"night",
			/**
			 * 字体大小
			 * @property Setting.fontsize
			 * @type {String}
			 */
			fontsize:14,
			/**
			 * 字体类型
			 * @property Setting.fontfamily
			 * @type {String}
			 */
			fontfamily:"consola",
	};
})();