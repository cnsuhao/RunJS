Toolbar = (function() {

	var PT = Toolbar.prototype;

	PT.defaultParams = {}

	PT.Events = {};

	function Toolbar() {
		this.arg = arguments;
		this.clazz = className(this);
		g_utils.initParams.call(this, this.defaultParams);
		this.initView.call(this);
		return this;
	}

	PT.initView = function(show) {
		g_utils.binder.call(this);
	}

	return Toolbar;

})();