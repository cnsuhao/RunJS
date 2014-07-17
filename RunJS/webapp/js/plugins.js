/**
 * 插件辅助类，用来创建、管理插件
 * @class Plugins
 */
Plugins = (function() {

    var _plugins_ = {}, instance;

    /**
     * 定义所有的系统事件
     * @property onEvents
     * @type {Array}
     */
    var onEvents = Plugins.prototype.onEvents = [ "onEditorViewInit", "onExplorerViewInit", "onMenuViewInit", "onContextMenuLoad", "onContextMenuRemove", "beforeContextMenuLoad", "onDialogLoad", "onScriptImport", "onHtmlEditorChange", "onJsEditorChange", "onCssEditorChange","onHtmlCursorActivity","onJsCursorActivity","onCssCursorActivity" ];

    /**
     * 定义所有的系统事件的描述信息
     * @property onEventsDescription
     * @type {Array}
     */
    Plugins.prototype.onEventsDescription = [ "当编辑器视图初始化或重置后时调用", "当左边的资源管理器初始化或重置后调用", "当顶部菜单视图初始化或重置后调用", "上下文菜单加载后调用", "上下文菜单消失后调用", "上下文菜单显示前调用", "当对话框弹出后调用", "当引入脚本时调用", "当HTML编辑器内容有改变时调用", "当JavaScript编辑器视图内容变化时调用", "当CSS编辑器内容变化时调用","当Html编辑器中光标变动时调用","当Js编辑器中光标变动时调用","当Css编辑器中光标变动时调用" ];

    var events_stack = {};

    /**
     * @class Plugins
     * @constructor
     */
    function Plugins() {
        instance = this;
        initEventsStack();
    }

    /**
     * 初始化事件栈
     * @method initEventsStack
     * @private
     */
    var initEventsStack = function() {
        $.each(onEvents, function(i, event) {
            events_stack[event] = [];
        });
    };

    /**
     * 引入JavaScript库，该方法将向 head 中添加一个以 link 为 src 的 script 标签
     * @method importJavaScript
     * @param {String} link 引入js库链接地址
     */
    Plugins.prototype.importJavaScript = function(link) {

        var head = document.getElementsByTagName("head").item(0);

        var script = document.createElement("script");

        script.language = "javascript";

        script.type = "text/javascript";

        script.src = g_utils.getHttpLink(link);

        head.appendChild(script);
    }

    /**
     * 引入 Css 文件，该方法将向 head 中引入一个以 link 为 href 的 link 标签
     * @method importCss
     * @param {String} link 引入css链接地址
     */
    Plugins.prototype.importCss = function(link) {

        var head = document.getElementsByTagName("head").item(0);

        var css = document.createElement("link");

        css.rel = "stylesheet";

        css.type = "text/css";

        css.href = g_utils.getHttpLink(link);

        head.appendChild(css);
    }

    /**
     * 构建新的插件
     * @method newPlugin
     * @param {String} indent 插件唯一标识（即代码的 url 唯一标识）
     * @param {Object} plugin 插件类的引用
     * @param {Object} opt　插件的默认配置项
     */
    Plugins.prototype.newPlugin = function(ident, plugin, opt) {
        if (isNotEmpty(ident) && typeOf(ident, "string") && isFunc(plugin)) {
            _plugins_[ident] = {
                plugin : plugin,
                opt : opt
            };
            return _plugins_[ident];
        }
        return false;
    };

    /**
     * 初始化所有插件，并调用插件的 init 方法
     * @method init
     */
    Plugins.prototype.init = function() {
        initEventsStack();
        $.each(_plugins_, function(ident, plugin) {
            try {
                var cur = _plugins_[ident] = $.extend(new Plugin(ident), new _plugins_[ident].plugin(_plugins_[ident].opt));
                cur.init();
                Console.log("Plugin[" + ident + "] inited!");
                g_utils.binder.call(cur);
            } catch (e) {
                $.error(e);
            }
        });
    }

    /**
     * 根据唯一标识获取某插件实例
     * @method getPlugin
     * @return {String} plugin 插件实例
     */
    Plugins.prototype.getPlugin = function(ident) {
        if (isNotEmpty(ident)) {
            return _plugins_[ident];
        }
    }

    /**
     * 判断是为有效系统事件，即是否为<a href="#attr_onEvents">onEvent</a>中成员
     * @private
     * @param {String} name 事件名称
     * @method isValidOnEvent
     * @return {Boolean}
     */
    var isValidOnEvent = function(name) {
        return onEvents.indexOf(name) > -1;
    };

    /**
     * 判断不是有效系统事件，即不为 <a href="#attr_onEvents">onEvents</a> 中成员
     * @private
     * @param {String} name 事件名称
     * @method isInvalidOnEvent
     * @return {Boolean}
     */
    var isInvalidOnEvent = function(name) {
        return !isValidOnEvent(name);
    };

    /**
     * 获取插件注册的某名称事件栈
     * @method getEvents
     * @params {String} name 注册事件的名称，为 <a href="#attr_onEvents">onEvents</a> 中成员
     * @return {Array} 事件数组
     */
    Plugins.prototype.getEvents = function(name) {
        if (isValidOnEvent(name)) {
            return events_stack[name];
        }
    };

    /**
     * 为某插件添加事件
     * @method addEvent
     * @param {Object} plugin 插件实例
     * @param {String} name 注册事件名称
     * @param {Function} event 回调函数
     */
    Plugins.prototype.addEvent = function(plugin, name, event) {
        if (isNotEmpty(plugin) && isValidOnEvent(name) && isFunc(event)) {
            events_stack[name].push(event);
            events_stack[name] = $.unique($.unique(events_stack[name]));
        }
    };

    /**
     * 调用某事件的所有事件栈方法
     * @method fireEvent
     * @param {String} event：调用的事件名称
     * @param {Object} data：为调用的事件传递的参数
     * @return {undefined} 如果当前事件为无效方法，则返回 undefined
     */
    Plugins.prototype.fireEvent = function(event, data) {
        var cur = this;
        if (isInvalidOnEvent(event))
            return;
        $.each(_plugins_, function(ident, plugin) {
            var e = plugin[event];
            if (isFunc(e)) {
                e.call(cur, data);
                Console.log("Plugin[" + ident + "] event:" + event + " called!");
            }
            var events = instance.getEvents(event);
            $.each(events, function(i, evt) {
                evt.call(cur, data);
            });
        });
        return true;
    };

    return Plugins;
})();

/**
 * Plugins 实例[全局]
 * @attribute plugins
 */
plugins = new Plugins();

/**
 * 插件类
 * @class Plugin
 */
Plugin = (function() {
    var PT = Plugin.prototype;
    var instance;
    function Plugin(id) {
        PT.ident = id;
        instance = this;
    }

    /**
     * 默认初始方法
     * @method init
     */
    PT.init = function() {
        Console.log("Default Init Called Plugin[" + PT.ident + "]!");
    }

    /**
     * 为当前插件添加事件
     * @method addEvent
     * @param {String} name 注册事件名称
     * @param {Function} event 回调函数
     */
    PT.addEvent = function(name, event) {
        plugins.addEvent(instance, name, event);
    };

    return Plugin;
})();
