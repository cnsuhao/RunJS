var Api = {
	ajax:{
		// 添加项目 旧！
		add_project : function(pro_name, html, css, js,callback) {
			$.post("/action/project/add", {
				"v_code":User.v_code,
				"pro_name" : pro_name,
				"html" : html,
				"css" : css,
				"js" : js
			}, function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		// 添加代码 新！
		add_code : function(code_name, html, css, js,callback) {
			$.post("/action/code/add", {
				"v_code":User.v_code,
				"code_name" : code_name,
				"html" : html,
				"css" : css,
				"js" : js
			}, function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		// 保存当前代码 旧！
		update : function(id, html, css, js,sign,callback,force) {
			$.post("/action/project/update", {
				"v_code":User.v_code,
				"id" : id,
				"css" : css,
				"js" : js,
				"html" : html,
				"sign":sign,
				"force":force
			}, function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		// 保存当前代码 新！
		update_code : function(id, html, css, js,sign,callback,force) {
			$.post("/action/code/update", {
				"v_code":User.v_code,
				"id" : id,
				"css" : css,
				"js" : js,
				"html" : html,
				"sign":sign,
				"force":force
			}, function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		// 存为新版本
		new_version : function(id, html, css, js,callback) {
			$.post("/action/project/new_version", {
				"v_code":User.v_code,
				"id" : id,
				"css" : css,
				"js" : js,
				"html" : html
			}, function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		// fork代码 旧！
		fork : function(pro_id, ver, pro_name,callback) {
			$.post("/action/project/fork", {
				"v_code":User.v_code,
				"pro_id" : pro_id,
				"ver" : ver,
				"pro_name" : pro_name
			}, function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		// fork代码 新！
		fork_code : function(id, code_name,callback) {
			$.post("/action/code/fork", {
				"v_code":User.v_code,
				"id" : id,
				"code_name" : code_name
			}, function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		// 删除指定版本的代码
		delete_version : function(captcha,pro_id, ver,sign,callback,force) {
			$.post("/action/project/delete_version", {
				"captcha_":captcha,
				"v_code":User.v_code,
				"pro_id" : pro_id,
				"ver" : ver,
				"sign":sign,
				"force":force
			}, function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},

		/**
		 * 删除代码，新!
		 * 
		 */
		delete_code:function(captcha,id,callback){
			$.post("/action/code/delete", {
				"captcha_":captcha,
				"v_code":User.v_code,
				"id" : id
			}, function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},

		// 删除项目
		delete_project : function(captcha,pro_id,callback) {
			$.post("/action/project/delete_project", {
				"captcha_":captcha,
				"v_code":User.v_code,
				"id" : pro_id
			}, function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		// 添加评论
		add_comment　: function(code_id, content,callback) {
			$.post("/action/project/add_comment", {
				"v_code":User.v_code,
				"id" : code_id,
				"content" : content
			}, function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		// 删除评论
		delete_comment : function(comment_id,callback) {
			$.post("/action/project/delete_comment", {
				"v_code":User.v_code,
				"id" : comment_id
			}, function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		// 投票，顶踩等
		vote : function (code_id, type,callback) {
			$.post("/action/project/vote", {
				"v_code":User.v_code,
				"id" : code_id,
				"type" : type
			}, function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},

		login : function (name,callback) {
			$.post("/action/ajax/login", {
				"username" : name
			}, function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		// 登出
		logout : function (callback) {
			$.post("/action/ajax/logout",'uid='+User.user, function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		// 通过id获取代码
		getCode : function(id,callback) {
			$.post("/action/api/getCode", {
				"id" : id
			}, function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		// 项目重命名
		project_rename : function(id,name,callback){
			$.post("/action/project/rename", {
				"v_code":User.v_code,
				"pro_id" : id,
				"name":name
			}, function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		// 代码重命名 旧！
		code_rename : function(id,name,callback){
			$.post("/action/project/rename_code", {
				"v_code":User.v_code,
				"code_id" : id,
				"name":name
			}, function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		// 代码重命名 新！
		code_rename : function(id,name,callback){
			$.post("/action/code/rename", {
				"v_code":User.v_code,
				"id" : id,
				"name":name
			}, function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		// 用户设置项，主题等
		setting : function(name,value,callback){
			$.post("/action/api/setting", {
				"v_code":User.v_code,
				"name" : name,
				"value":value
			}, function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		/**
		 * 发布代码 旧！
		 */
		publish : function(id,description,callback){
			$.post("/action/project/post", {
				"v_code":User.v_code,
				"id" : id,
				"description":description
			}, function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		/**
		 * 发布代码 新！
		 */
		publish_code : function(id,description,callback){
			$.post("/action/code/post", {
				"v_code":User.v_code,
				"id" : id,
				"description":description
			}, function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		// 更新代码信息 旧！
		update_info : function(id,name,description,callback){
			$.post("/action/project/update_info", {
				"v_code":User.v_code,
				"id" : id,
				"name" : name,
				"description":description
			}, function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		// 更新代码信息 新！
		update_code_info : function(id,name,description,callback){
			$.post("/action/code/update_info", {
				"v_code":User.v_code,
				"id" : id,
				"name" : name,
				"description":description
			}, function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		
		// 收藏
		favor : function(code_id,callback){
			$.post("/action/project/favor", {
				"v_code":User.v_code,
				"id" : code_id
			}, function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		// 取消收藏
		un_favor : function(favor_id,callback){
			$.post("/action/project/un_favor", {
				"v_code":User.v_code,
				"id" : favor_id
			}, function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		/**
		 * 添加url文件
		 */
		add_url_file : function(url,callback){
			$.post("/action/file/add_url_file",{
				"v_code":User.v_code,
				"url":url
			},function(msg){
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		/**
		 * 删除文件
		 */
		delete_file : function(captcha,id,callback){
			$.post("/action/file/delete_file", {
				"captcha_":captcha,
				"v_code":User.v_code,
				"id" : id
			}, function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		/**
		 * 添加建议
		 */
		add_advice : function(captcha,ident,email,content,callback){
			$.post("/action/advice/add_advice",{
				"captcha_":captcha,
				"ident":ident,
				"email":email,
				"content":content
			}, function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		/**
		 * 发送消息
		 */
		send_msg:function(receiver,content,callback){
			$.post("/action/msg/sendMsg",{
				"receiver":receiver,
				"content":content
			}, function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		/**
		 * 阅读消息
		 */
		read_msg:function(id,callback){
			var params = '?';
			$.each(id,function(i,cur){
				params+="id="+cur;
				if(i<id.length-1){
					params+="&";
				}
			});
			if(params=='?'){
				params+="id="+id;
			}
			$.post("/action/msg/readMsg"+params,params, function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		/**
		 * 阅读所有的未读消息
		 */
		read_all_msg:function(type,callback){
			$.post("/action/msg/readAllMsg",{
				"v_code":User.v_code,
				"type":type,
			}, function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		/**
		 * 将插件加到市场
		 */
		add_to_market:function(id,callback){
			$.post("/action/plugin/add_to_market",{
				"id":id
			},function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		/**
		 * 将代码更新到插件市场，id为code的id.
		 */
		update_to_market:function(id,callback){
			$.post("/action/plugin/update_to_market",{
				"id":id
			},function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		/**
		 * 从插件市场移出
		 */
		delete_from_market:function(id,callback){
			$.post("/action/plugin/delete_from_market",{
				"id":id
			},function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		/**
		 * 将代码设置为插件
		 */
		set_plugin:function(id,sys,callback){
			$.post("/action/plugin/set_code_plugin",{
				"id":id,
				"sys":sys
			},function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		/**
		 * 审核插件
		 */
		check_plugin:function(id,callback){
			$.post("/action/plugin/check",{
				"id":id
			},function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		/**
		 * 取消审核插件
		 */
		uncheck_plugin:function(id,callback){
			$.post("/action/plugin/uncheck",{
				"id":id
			},function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		/**
		 * 加到广场
		 */
		add_to_square:function(id,callback){
			$.post("/action/square/add",{
				"id":id
			},function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		/**
		 * 更新到广场
		 */
		update_to_square:function(id,callback){
			$.post("/action/square/update",{
				"id":id
			},function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		/**
		 * 添加分类
		 */
		add_catalog:function(name,callback){
			$.post("/action/catalog/add",{
				"name":name
			},function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		/**
		 * 分类重命名
		 */
		rename_catalog:function(name,id,callback){
			$.post("/action/catalog/rename",{
				"name":name,
				"id":id
			},function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		/**
		 * 删除分类，该分类必须没有代码
		 */
		delete_catalog:function(id,callback){
			$.post("/action/catalog/delete",{
				"id":id
			},function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		/**
		 * 移动到分类
		 */
		move_to_catalog:function(code_id,catalog_id,callback){
			$.post("/action/catalog/move_to",{
				"id":code_id,
				"catalog":catalog_id
			},function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		/**
		 * 设置代码类型
		 */
		set_code_type:function(id,code_type,type,callback){
			$.post("/action/code/set_code_type",{
				"id":id,
				"code":code_type,
				"type":type
			},function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		},
		less_compile:function(less,callback){
			$.post("/action/ajax/less_compile",less,function(msg) {
				if(typeof callback != 'undefined')return callback(msg);
			});
		}
	}
};


