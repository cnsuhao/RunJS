package net.oschina.runjs.action;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import net.oschina.common.servlet.Annotation.JSONOutputEnabled;
import net.oschina.common.servlet.Annotation.PostMethod;
import net.oschina.common.servlet.Annotation.UserRoleRequired;
import net.oschina.common.servlet.RequestContext;
import net.oschina.common.utils.ImageCaptchaService;
import net.oschina.common.utils.ResourceUtils;
import net.oschina.runjs.beans.Code;
import net.oschina.runjs.beans.Comment;
import net.oschina.runjs.beans.Dynamic;
import net.oschina.runjs.beans.Favor;
import net.oschina.runjs.beans.Msg;
import net.oschina.runjs.beans.Plugin;
import net.oschina.runjs.beans.SquareCode;
import net.oschina.runjs.beans.User;

import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;

/**
 * JS CSS压缩，代码处理Action
 * 
 * @author wangzhenwei
 */
public class CodeAction {
	public Gson gson = new Gson();

	/**
	 * 新建代码
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@PostMethod
	@UserRoleRequired
	@JSONOutputEnabled
	public void add(RequestContext ctx) throws IOException {
		User user = (User) ctx.user();
		String verify_code = ctx.param("v_code", "");
		if (!user.IsCurrentUser(verify_code))
			throw ctx.error("operation_forbidden");

		String code_name = ctx.param("code_name", "").trim();
		String html = ctx.param("html", "");
		String css = ctx.param("css", "");
		String js = ctx.param("js", "");
		if (code_name.length() < 1 || code_name.length() > 20)
			throw ctx.error("pro_name_invalid");
		// 判断该用户是否已经存在该项目,项目名不能重名。
		if (Code.IsCodeExist(code_name, user.getId()))
			throw ctx.error("pro_exist");
		Timestamp time = new Timestamp(new Date().getTime());
		// 保存代码
		Code code = new Code();
		code.setUser(user.getId());
		code.setProject(0);
		code.setName(code_name);
		code.setCss(css);
		code.setHtml(html);
		code.setJs(js);
		code.setNum(1);
		code.setCreate_time(time);
		code.setUpdate_time(time);
		code.setCode_type(Code.DEFAULT_TYPE);
		if (0 < code.Save()) {
			ctx.print(gson.toJson(code));
		} else {
			throw ctx.error("operation_failed");
		}
	}

	/**
	 * 设置代码类型
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@PostMethod
	@UserRoleRequired
	@JSONOutputEnabled
	public void set_code_type(RequestContext ctx) throws IOException {
		User user = (User) ctx.user();
		long id = ctx.id();
		Code code = Code.INSTANCE.Get(id);
		if (null == code) {
			throw ctx.error("code_not_exist");
		}
		if (!user.IsAdmin(code))
			throw ctx.error("operation_forbidden");
		String str = ctx.param("code", "");
		int type = ctx.param("type", 1);
		if (type > 9)
			throw ctx.error("type_error");
		if ("html".equalsIgnoreCase(str)) {
			code.SetHTMLType(type);
			ctx.output_json("success", 1);
		} else if ("js".equalsIgnoreCase(str)) {
			code.SetJSType(type);
			ctx.output_json("success", 1);
		} else if ("css".equalsIgnoreCase(str)) {
			code.SetCSSType(type);
			ctx.output_json("success", 1);
		} else
			throw ctx.error("code_type_error");
	}

	/**
	 * 代码重命名
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@PostMethod
	@UserRoleRequired
	@JSONOutputEnabled
	public void rename(RequestContext ctx) throws IOException {
		User user = (User) ctx.user();
		String verify_code = ctx.param("v_code", "");
		if (!user.IsCurrentUser(verify_code))
			throw ctx.error("operation_forbidden");
		String code_name = ctx.param("name", "").trim();
		if (StringUtils.isBlank(code_name) || code_name.length() > 20)
			throw ctx.error("code_name_invalid");
		long code_id = ctx.id();
		Timestamp time = new Timestamp(new Date().getTime());
		Code code = Code.INSTANCE.Get(code_id);
		if (null == code)
			throw ctx.error("code_not_exist");
		if (null == user || (!user.IsAdmin(code)))
			throw ctx.error("operation_forbidden");
		if (code.UpdateField("name", code_name)) {
			code.UpdateField("update_time", time);
			Code.evictCache(code.CacheRegion(),
					Code.CODE_IDENT + code.getIdent());
			code.setName(code_name);
			ctx.print(gson.toJson(code));
		} else
			throw ctx.error("operation_failed");
	}

	/**
	 * 删除指定的代码
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@PostMethod
	@UserRoleRequired
	@JSONOutputEnabled
	public void delete(RequestContext ctx) throws IOException {
		if (!ImageCaptchaService.validate(ctx.request()))
			throw ctx.error("captcha_error");
		User user = (User) ctx.user();
		String verify_code = ctx.param("v_code", "");
		if (!user.IsCurrentUser(verify_code))
			throw ctx.error("operation_forbidden");

		long id = ctx.param("id", 0l);
		String sign = ctx.param("sign", "");
		// 是否强制删除 0为不强制删除，1为强制删除
		int force = ctx.param("force", 0);
		// 是否放到回收站，默认为1放到回收站
		int recycle = ctx.param("recycle", 1);

		Code code = Code.INSTANCE.Get(id);
		if (null == code)
			throw ctx.error("code_not_exist");
		// 权限检查
		if (null == user || (!user.IsAdmin(code)))
			throw ctx.error("operation_forbidden");
		// 验证签名
		if (0 == force && !code.verifySign(sign))
			throw ctx.error("code_is_old");

		SquareCode sc = SquareCode.INSTANCE.GetSquareCodeByCode(code.getId());
		if (sc != null)
			sc.Delete();
		Plugin plugin = Plugin.INSTANCE.GetPluginByCode(code.getId());
		if (plugin != null)
			plugin.Delete();

		if (recycle == 1) {
			code.UpdateField("status", Code.STATUS_RECYCLE);
			code.clearCache();
			ctx.print(gson.toJson(code));
			ImageCaptchaService.clear(ctx.request());
		} else {
			// 删除代码
			// 循环删除评论
			List<Comment> comment_list = Comment.INSTANCE
					.getAllCommentByCode(code.getId());
			if (null != comment_list) {
				for (Comment comment : comment_list) {
					comment.Delete();
				}
			}
			if (code.Delete()) {
				if (plugin != null)
					plugin.Delete();
				ctx.print(gson.toJson(code));
				ImageCaptchaService.clear(ctx.request());
			} else
				throw ctx.error("operation_failed");
		}
	}

	/**
	 * 更新最新的版本的代码
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@PostMethod
	@UserRoleRequired
	@JSONOutputEnabled
	public void update(RequestContext ctx) throws IOException {
		User user = (User) ctx.user();
		String verify_code = ctx.param("v_code", "");
		if (!user.IsCurrentUser(verify_code))
			throw ctx.error("operation_forbidden");
		Timestamp time = new Timestamp(new Date().getTime());
		String sign = ctx.param("sign", "");
		// 是否强制保存 0为不强制保存，1为强制保存
		int force = ctx.param("force", 0);
		long id = ctx.id();
		String css = ctx.param("css");
		String js = ctx.param("js");
		String html = ctx.param("html");

		Code code = Code.INSTANCE.Get(id);
		if (code == null)
			throw ctx.error("code_not_exist");

		// 验证上次更新时间,如果是2秒以内，拒绝掉
		if (Code.MIN_UPDATE_TIME >= (time.getTime() - code.getUpdate_time()
				.getTime()))
			throw ctx.error("update_too_fast");
		// 验证签名
		// 如果签名不对范围错误码为2的错误提示
		if (0 == force && !code.verifySign(sign)) {
			String[] keys = { "error", "msg" };
			Object[] values = { 2,
					ResourceUtils.getString("error", "code_is_old") };
			ctx.output_json(keys, values);
			return;
		}
		// 如果css、js、html不为null则更新,否则保留原来的版本
		if (null != css)
			code.setCss(css);
		if (null != html)
			code.setHtml(html);
		if (null != js)
			code.setJs(js);
		// 更新时间
		code.setUpdate_time(time);
		if (code.update()) {
			ctx.print(gson.toJson(code));
		} else
			throw ctx.error("operation_failed");
	}

	/**
	 * 修改代码信息
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@PostMethod
	@UserRoleRequired
	@JSONOutputEnabled
	public void update_info(RequestContext ctx) throws IOException {
		User user = (User) ctx.user();
		String verify_code = ctx.param("v_code", "");
		if (!user.IsCurrentUser(verify_code))
			throw ctx.error("operation_forbidden");

		long id = ctx.param("id", 0l);

		Code code = Code.INSTANCE.Get(id);
		if (null == code)
			throw ctx.error("code_not_exist");
		// 权限检查
		if (null == user || (!user.IsAdmin(code)))
			throw ctx.error("operation_forbidden");
		String code_name = ctx.param("name", "").trim();
		String description = ctx.param("description", "").trim();
		if (StringUtils.isBlank(code_name) || code_name.length() > 30)
			throw ctx.error("code_name_invalid");
		if (StringUtils.isBlank(description) || description.length() > 300)
			throw ctx.error("description_error");
		// 如果名称有改变
		if (!code_name.equals(code.getName())) {
			code.UpdateField("name", code_name);
			code.setName(code_name);
			// 清除ident的缓存
			Code.evictCache(code.CacheRegion(),
					Code.CODE_IDENT + code.getIdent());
		}
		// 如果描述有改变
		if (!description.equals(code.getDescription())) {
			code.UpdateField("description", description);
			Timestamp time = new Timestamp(new Date().getTime());
			code.setDescription(description);
			if (code.UpdateField("update_time", time)) {
				ctx.print(gson.toJson(code));
				if (code.IsPosted())
					Favor.INSTANCE.notifyAllFavors(id);
				// 清除ident的缓存
				Code.evictCache(code.CacheRegion(),
						Code.CODE_IDENT + code.getIdent());
			}
		} else
			throw ctx.error("operation_failed");
	}

	/**
	 * 发布代码
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@PostMethod
	@UserRoleRequired
	@JSONOutputEnabled
	public void post(RequestContext ctx) throws IOException {
		User user = (User) ctx.user();
		String verify_code = ctx.param("v_code", "");
		if (!user.IsCurrentUser(verify_code))
			throw ctx.error("operation_forbidden");
		long id = ctx.id();
		Code code = Code.INSTANCE.Get(id);
		if (null == code)
			throw ctx.error("code_not_exist");
		// 权限检查
		if (null == user || (!user.IsAdmin(code)))
			throw ctx.error("operation_forbidden");
		String description = ctx.param("description", "").trim();
		if (StringUtils.isBlank(description) || description.length() > 300)
			throw ctx.error("description_error");
		if (code.post(description)) {
			ctx.print(gson.toJson(code));
		} else
			throw ctx.error("operation_failed");
	}

	/**
	 * 从某一指定项目的指定版本fork
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@PostMethod
	@UserRoleRequired
	@JSONOutputEnabled
	public void fork(RequestContext ctx) throws IOException {
		User user = (User) ctx.user();
		String verify_code = ctx.param("v_code", "");
		if (!user.IsCurrentUser(verify_code))
			throw ctx.error("operation_forbidden");
		long id = ctx.id();
		String code_name = ctx.param("code_name", "").trim();
		// 判断项目名是否合法
		if (StringUtils.isBlank(code_name))
			throw ctx.error("pro_name_invalid");
		// 判断该用户是否已经存在该项目
		if (Code.IsCodeExist(code_name, user.getId()))
			throw ctx.error("pro_exist");
		Code fork_code = Code.INSTANCE.Get(id);
		if (null == fork_code)
			throw ctx.error("code_not_exist");
		Timestamp time = new Timestamp(new Date().getTime());
		// 克隆代码
		Code code = new Code();
		code.setFork(fork_code.getId());
		code.setUser(user.getId());
		// 清除fork列表缓存
		Code.evictCache(code.CacheRegion(), Code.FORK_LIST + code.getFork());

		// Fork别人的代码默认是发布状态
		// 自己的代码默认不发布，用户可以自行选择
		if (code.getUser() != fork_code.getUser()) {
			code.setStatus(Code.STATUS_POST);
			code.setPost_time(time);
			code.setDescription(ResourceUtils.getString("description",
					"fork_code", fork_code.getName()));
		}
		code.setProject(0);
		code.setCss(fork_code.getCss());
		code.setHtml(fork_code.getHtml());
		code.setJs(fork_code.getJs());
		code.setNum(1);
		code.setCreate_time(time);
		code.setUpdate_time(time);
		code.setCode_type(fork_code.getCode_type());
		if (0 != code.Save()) {
			// 添加动态
			Dynamic.INSTANCE.add_fork_dy(code);
			if (fork_code.getUser() != code.getUser()) {
				// 添加通知
				String notify = ResourceUtils.getString("description",
						"fork_notify", user.getName(), fork_code.getIdent(),
						fork_code.getName(), code.getIdent());
				Msg.INSTANCE.addMsg(code.getUser(), fork_code.getUser(),
						Msg.NULL_REFER, Msg.TYPE_FORK, notify);
			}
			Code.evictCache(code.CacheRegion(),
					Code.FORK_COUNT + fork_code.getId());
			ctx.print(gson.toJson(code));
			return;
		} else
			throw ctx.error("operation_failed");
	}

	/**
	 * 清空回收站
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@PostMethod
	@UserRoleRequired
	@JSONOutputEnabled
	public void wipe_recycle(RequestContext ctx) throws IOException {
		if (!ImageCaptchaService.validate(ctx.request()))
			throw ctx.error("captcha_error");
		User user = (User) ctx.user();
		String verify_code = ctx.param("v_code", "");
		if (!user.IsCurrentUser(verify_code))
			throw ctx.error("operation_forbidden");
		List<Code> codes = Code.INSTANCE.listRecycleCode(user.getId());
		if (null == codes)
			throw ctx.error("no_recycle_code");
		// 删除代码
		for (Code code : codes) {
			// 循环删除评论
			List<Comment> comment_list = Comment.INSTANCE
					.getAllCommentByCode(code.getId());
			if (null != comment_list) {
				for (Comment comment : comment_list) {
					comment.Delete();
				}
			}
			code.Delete();
			Code.evictCache(code.CacheRegion(),
					Code.RECYCLE_CODE_LIST + code.getUser());
		}
		ctx.output_json("success", 1);
		ImageCaptchaService.clear(ctx.request());
	}
}
