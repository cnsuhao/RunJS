package net.oschina.runjs.action;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import net.oschina.common.servlet.Annotation.JSONOutputEnabled;
import net.oschina.common.servlet.Annotation.PostMethod;
import net.oschina.common.servlet.Annotation.UserRoleRequired;
import net.oschina.common.servlet.RequestContext;
import net.oschina.common.utils.FormatTool;
import net.oschina.common.utils.ImageCaptchaService;
import net.oschina.common.utils.ResourceUtils;
import net.oschina.runjs.beans.Code;
import net.oschina.runjs.beans.Comment;
import net.oschina.runjs.beans.Dynamic;
import net.oschina.runjs.beans.Favor;
import net.oschina.runjs.beans.Msg;
import net.oschina.runjs.beans.Plugin;
import net.oschina.runjs.beans.Project;
import net.oschina.runjs.beans.SquareCode;
import net.oschina.runjs.beans.User;
import net.oschina.runjs.beans.Vote;

import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;

/**
 * 项目管理，包括新建、删除、更新、fork等操作
 * 
 * @author jack
 * 
 */
public class ProjectAction {

	private Gson gson = new Gson();

	/**
	 * 新增一个项目
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

		String pro_name = ctx.param("pro_name", "").trim();
		String html = ctx.param("html", "");
		String css = ctx.param("css", "");
		String js = ctx.param("js", "");
		if (pro_name.length() < 1 || pro_name.length() > 20)
			throw ctx.error("pro_name_invalid");
		// 判断该用户是否已经存在该项目,项目名不能重名。
		if (Project.isProjectExist(pro_name, user.getId()))
			throw ctx.error("pro_exist");
		Project p = new Project();
		Timestamp time = new Timestamp(new Date().getTime());
		p.setName(pro_name);
		p.setUser(user.getId());
		p.setVersion(Project.INIT_VERSION);
		p.setCreate_time(time);
		p.setUpdate_time(time);
		// 保存代码
		Code code = new Code();
		code.setUser(user.getId());
		code.setProject(p.Save());
		code.setName(p.getName());
		code.setCss(css);
		code.setHtml(html);
		code.setJs(js);
		code.setNum(Project.INIT_VERSION);
		code.setCreate_time(time);
		code.setUpdate_time(time);
		code.setCode_type(Code.DEFAULT_TYPE);
		if (0 < code.Save()) {
			ctx.print(gson.toJson(code));
			// 清除用户项目列表的缓存
			Project.evictCache(p.CacheRegion(),
					Project.USER_PRO_LIST + user.getId());
		} else {
			p.Delete();
			throw ctx.error("operation_failed");
		}
	}

	/**
	 * 删除项目，将会删除该项目的所有代码以及代码的所有评论
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@PostMethod
	@UserRoleRequired
	@JSONOutputEnabled
	public void delete_project(RequestContext ctx) throws IOException {
		if (!ImageCaptchaService.validate(ctx.request())) {
			throw ctx.error("captcha_error");
		}

		long id = ctx.id();
		User user = (User) ctx.user();

		String verify_code = ctx.param("v_code", "");
		if (!user.IsCurrentUser(verify_code))
			throw ctx.error("operation_forbidden");

		// 权限检查
		Project project = Project.INSTANCE.Get(id);
		if (null == project)
			throw ctx.error("project_not_exist");
		if (null == user || (!user.IsAdmin(project)))
			throw ctx.error("operation_forbidden");
		// 循环删除代码
		List<Code> code_list = Code.INSTANCE.getAllCodeByProject(id);
		if (null != code_list)
			for (Code code : code_list) {
				// 循环删除评论
				List<Comment> comment_list = Comment.INSTANCE
						.getAllCommentByCode(code.getId());
				if (null != comment_list) {
					for (Comment comment : comment_list) {
						comment.Delete();
					}
				}
				if (code.Delete()) {
					SquareCode sc = SquareCode.INSTANCE
							.GetSquareCodeByCode(code.getId());
					if (sc != null)
						sc.Delete();
					Plugin plugin = Plugin.INSTANCE.GetPluginByCode(code
							.getId());
					if (plugin != null)
						plugin.Delete();
				}
			}

		if (project.Delete()) {
			// 清除用户项目列表的缓存
			Project.evictCache(project.CacheRegion(), Project.USER_PRO_LIST
					+ user.getId());
			ctx.print(gson.toJson(project));
			ImageCaptchaService.clear(ctx.request());
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
	public void delete_version(RequestContext ctx) throws IOException {

		if (!ImageCaptchaService.validate(ctx.request())) {
			throw ctx.error("captcha_error");
		}

		User user = (User) ctx.user();

		String verify_code = ctx.param("v_code", "");
		if (!user.IsCurrentUser(verify_code))
			throw ctx.error("operation_forbidden");

		long pro_id = ctx.param("pro_id", 0l);
		int version = ctx.param("ver", 0);
		String sign = ctx.param("sign", "");
		// 是否强制保存 0为不强制删除，1为强制删除
		int force = ctx.param("force", 0);

		Project pro = Project.INSTANCE.Get(pro_id);
		if (null == pro)
			throw ctx.error("project_not_exist");
		// 权限检查
		if (null == user || (!user.IsAdmin(pro)))
			throw ctx.error("operation_forbidden");

		Code code = Code.INSTANCE.getCodeByVersion(pro.getId(), version);
		if (null == code)
			throw ctx.error("code_not_exist");

		// 验证签名
		if (0 == force && !code.verifySign(sign))
			throw ctx.error("code_is_old");

		// 循环删除评论
		List<Comment> comment_list = Comment.INSTANCE.getAllCommentByCode(code
				.getId());
		if (null != comment_list) {
			for (Comment comment : comment_list) {
				comment.Delete();
			}
		}
		if (code.Delete()) {
			ctx.print(gson.toJson(code));
			ImageCaptchaService.clear(ctx.request());
		} else
			throw ctx.error("operation_failed");
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
	 * 项目重命名
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
		String pro_name = ctx.param("name", "").trim();
		long pro_id = ctx.param("pro_id", 0l);
		Timestamp time = new Timestamp(new Date().getTime());
		if (StringUtils.isBlank(pro_name) || pro_name.length() > 30)
			throw ctx.error("pro_name_invalid");

		Project pro = Project.INSTANCE.Get(pro_id);
		if (null == pro)
			throw ctx.error("project_not_exist");
		// 权限检查
		if (null == user || (!user.IsAdmin(pro)))
			throw ctx.error("operation_forbidden");

		// 判断是否已经存在这样的项目名
		if (!StringUtils.equalsIgnoreCase(pro_name, pro.getName())
				&& Project.isProjectExist(pro_name, user.getId()))
			throw ctx.error("pro_exist");

		if (pro.UpdateField("name", pro_name)) {
			pro.UpdateField("update_time", time);
			ctx.print(gson.toJson(pro));
		} else
			throw ctx.error("operation_failed");
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
	public void rename_code(RequestContext ctx) throws IOException {
		User user = (User) ctx.user();
		String verify_code = ctx.param("v_code", "");
		if (!user.IsCurrentUser(verify_code))
			throw ctx.error("operation_forbidden");
		String code_name = ctx.param("name", "").trim();
		if (StringUtils.isBlank(code_name) || code_name.length() > 20)
			throw ctx.error("code_name_invalid");

		long code_id = ctx.param("code_id", 0l);
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
		boolean su = false;

		// 如果名称有改变
		if (!code_name.equals(code.getName())) {
			su = code.UpdateField("name", code_name);
			code.setName(code_name);
			// 清除ident的缓存
			Code.evictCache(code.CacheRegion(),
					Code.CODE_IDENT + code.getIdent());
		}
		// 如果描述有改变
		if (!description.equals(code.getDescription())) {
			su = code.UpdateField("description", description);
			Timestamp time = new Timestamp(new Date().getTime());
			code.UpdateField("update_time", time);
			code.setDescription(description);
			if (code.IsPosted())
				Favor.INSTANCE.notifyAllFavors(id);
			// 清除ident的缓存
			Code.evictCache(code.CacheRegion(),
					Code.CODE_IDENT + code.getIdent());
		}
		// 如果操作成功
		if (su)
			ctx.print(gson.toJson(code));
		else
			throw ctx.error("operation_failed");
	}

	/**
	 * 存为新版本
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@PostMethod
	@UserRoleRequired
	@JSONOutputEnabled
	public void new_version(RequestContext ctx) throws IOException {
		User user = (User) ctx.user();
		String verify_code = ctx.param("v_code", "");
		if (!user.IsCurrentUser(verify_code))
			throw ctx.error("operation_forbidden");
		long id = ctx.id();
		String css = ctx.param("css");
		String js = ctx.param("js");
		String html = ctx.param("html");

		Code old_code = Code.INSTANCE.Get(id);
		if (null == old_code)
			throw ctx.error("code_not_exist");
		Project pro = Project.INSTANCE.Get(old_code.getProject());
		if (null == pro)
			throw ctx.error("project_not_exist");
		// 权限检查
		if (null == user || (!user.IsAdmin(old_code)))
			throw ctx.error("operation_forbidden");

		Timestamp time = new Timestamp(new Date().getTime());

		// 验证上次创建新版本时间
		if (Code.MIN_NEW_VER_TIME >= (time.getTime() - old_code
				.getAllCodeByProject(pro.getId()).get(0).getCreate_time()
				.getTime()))
			throw ctx.error("new_version_too_fast");

		// 保存代码
		Code code = new Code();
		code.setUser(user.getId());
		code.setProject(pro.getId());
		// 判断是否有提交css、html、js，如果有则更新，否则保留原来的版本。
		if (null != css)
			code.setCss(css);
		else
			code.setCss(old_code.getCss());
		if (null != html)
			code.setHtml(html);
		else
			code.setHtml(old_code.getHtml());
		if (null != js)
			code.setJs(js);
		else
			code.setJs(old_code.getJs());
		// 版本号加1
		code.setNum(pro.getVersion() + 1);
		code.setCreate_time(time);
		code.setUpdate_time(time);
		// 将其fork字段设置过来
		code.setFork(old_code.getFork());
		if (code.getFork() != 0) {
			Code fork_code = Code.INSTANCE.Get(code.getFork());

			Project fork_pro = Project.INSTANCE.Get(fork_code.getProject());
			// Fork后默认为发布状态
			code.setStatus(Code.STATUS_POST);
			code.setPost_time(time);
			code.setDescription(ResourceUtils.getString("description",
					"fork_code_v", fork_pro.getName()));
		}

		if (0 != code.Save()) {
			pro.UpdateField("version", pro.getVersion() + 1);
			pro.UpdateField("update_time", time);
			// 清除项目版本列表缓存
			Code.evictCache(code.CacheRegion(),
					Code.PRO_CODE_LIST + pro.getId());
			// 清除fork列表缓存
			Code.evictCache(code.CacheRegion(), Code.FORK_LIST + code.getFork());

			ctx.print(gson.toJson(code));
			return;
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
		long fork_pro_id = ctx.param("pro_id", 0l);
		int fork_version = ctx.param("ver", 0);
		String pro_name = ctx.param("pro_name", "").trim();
		// 判断项目名是否合法
		if (StringUtils.isBlank(pro_name))
			throw ctx.error("pro_name_invalid");
		// 判断该用户是否已经存在该项目
		if (Project.isProjectExist(pro_name, user.getId()))
			throw ctx.error("pro_exist");
		Code fork_code = Code.INSTANCE.getCodeByVersion(fork_pro_id,
				fork_version);
		if (null == fork_code)
			throw ctx.error("code_not_exist");
		Project fork_pro = Project.INSTANCE.Get(fork_code.getProject());
		Project p = new Project();
		p.setName(pro_name);
		p.setUser(user.getId());
		p.setVersion(Project.INIT_VERSION);
		Timestamp time = new Timestamp(new Date().getTime());
		p.setCreate_time(time);
		p.setUpdate_time(time);

		// 克隆代码
		Code code = new Code();
		code.setFork(fork_code.getId());
		code.setUser(user.getId());
		if (0 != p.Save()) {
			// 清除用户项目列表的缓存
			Project.evictCache(p.CacheRegion(),
					Project.USER_PRO_LIST + user.getId());

			// 清除fork列表缓存
			Code.evictCache(code.CacheRegion(), Code.FORK_LIST + code.getFork());

			// Fork别人的代码默认是发布状态
			// 自己的代码默认不发布，用户可以自行选择
			if (code.getUser() != fork_code.getUser()) {
				code.setStatus(Code.STATUS_POST);
				code.setPost_time(time);
				code.setDescription(ResourceUtils.getString("description",
						"fork_code", fork_pro.getName()));
			}

			code.setProject(p.getId());
			code.setCss(fork_code.getCss());
			code.setHtml(fork_code.getHtml());
			code.setJs(fork_code.getJs());
			code.setNum(Project.INIT_VERSION);
			code.setCreate_time(time);
			code.setUpdate_time(time);
			code.setCode_type(fork_code.getCode_type());
			if (0 != code.Save()) {
				// 添加动态
				Dynamic.INSTANCE.add_fork_dy(code);
				if (fork_code.getUser() != code.getUser()) {
					// 添加通知
					String notify = ResourceUtils.getString("description",
							"fork_notify", user.getName(),
							fork_code.getIdent(), fork_code.getName(),
							code.getIdent());
					Msg.INSTANCE.addMsg(code.getUser(), fork_code.getUser(),
							Msg.TYPE_FORK, fork_code.getId(), notify);
				}
				// 清除被fork的代码的缓存列表
				Project.evictCache(p.CacheRegion(), Project.FORK_PROJECTS
						+ code.getFork());
				Code.evictCache(code.CacheRegion(),
						Code.FORK_COUNT + fork_code.getId());
				ctx.print(gson.toJson(code));
				return;
			} else
				throw ctx.error("operation_failed");
		} else
			throw ctx.error("operation_failed");
	}

	/**
	 * 添加评论
	 * 
	 * @param ctx
	 */
	@PostMethod
	@UserRoleRequired
	@JSONOutputEnabled
	public void add_comment(RequestContext ctx) throws IOException {
		long id = ctx.id();
		User user = (User) ctx.user();
		String verify_code = ctx.param("v_code", "");
		if (!user.IsCurrentUser(verify_code))
			throw ctx.error("operation_forbidden");
		String content = ctx.param("content", "").trim();

		if (StringUtils.isBlank(content) || 200 < content.length())
			throw ctx.error("comment_length", 200);
		Code code = Code.INSTANCE.Get(id);
		if (null == code)
			throw ctx.error("code_not_exist");

		// 如果代码未发布，提示操作失败
		if (!code.IsPosted())
			throw ctx.error("not_publish");

		Comment comment = new Comment();
		comment.setUser(user.getId());
		comment.setCode(id);
		comment.setContent(FormatTool.text(content));
		comment.setCreate_time(new Timestamp(new Date().getTime()));
		if (0 < comment.Save()) {
			// 添加一条动态
			Dynamic.INSTANCE.add_comment_dy(comment);
			if (comment.getUser() != code.getUser()) {
				// TODO 添加通知
				String notify = ResourceUtils.getString("description",
						"comment_notify", user.getName(), code.getIdent(),
						code.getName(), code.getIdent());
				Msg.INSTANCE.addMsg(comment.getUser(), code.getUser(),
						Msg.TYPE_COMMENT, code.getId(), notify);
			}
			// 清除代码评论缓存列表
			Comment.evictCache(comment.CacheRegion(), Comment.COMMENT_LIST
					+ comment.getCode());
			ctx.print(gson.toJson(comment));
			return;
		} else
			throw ctx.error("operation_failed");
	}

	/**
	 * 删除评论
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@PostMethod
	@UserRoleRequired
	@JSONOutputEnabled
	public void delete_comment(RequestContext ctx) throws IOException {
		long id = ctx.id();
		User user = (User) ctx.user();
		String verify_code = ctx.param("v_code", "");
		if (!user.IsCurrentUser(verify_code))
			throw ctx.error("operation_forbidden");
		// 检查权限
		Comment co = Comment.INSTANCE.Get(id);
		if (null == co)
			throw ctx.error("comment_not_exist");
		// 权限检查
		if (null == user || (!user.IsAdmin(co)))
			throw ctx.error("operation_forbidden");
		if (co.Delete()) {
			// 清除代码评论缓存列表
			Comment.evictCache(co.CacheRegion(),
					Comment.COMMENT_LIST + co.getCode());
			ctx.print(gson.toJson(co));
			return;
		} else
			throw ctx.error("operation_failed");
	}

	/**
	 * 投票，type为投票类型，1顶，-1踩，以后可以有分值
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@PostMethod
	@UserRoleRequired
	@JSONOutputEnabled
	public void vote(RequestContext ctx) throws IOException {
		User user = (User) ctx.user();
		String verify_code = ctx.param("v_code", "");
		if (!user.IsCurrentUser(verify_code))
			throw ctx.error("operation_forbidden");
		long id = ctx.id();
		int value = ctx.param("type", Vote.VOTE_LOVE);
		if (Vote.INSTANCE.isVoteExist(user.getId(), id))
			throw ctx.error("cannot_repeat_vote");
		Code code = Code.INSTANCE.Get(id);
		if (null == code)
			throw ctx.error("code_not_exist");
		else if (code.getUser() == user.getId())
			throw ctx.error("can_not_vote_self");
		else {
			Vote vote = new Vote();
			vote.setCreate_time(new Timestamp(new Date().getTime()));
			vote.setCode(id);
			vote.setUser(user.getId());
			vote.setValue(value);
			if (0 < vote.Save()) {
				if (vote.getValue() > 0) {
					Dynamic.INSTANCE.add_up_dy(user, code);

					// TODO 添加通知
					String notify = ResourceUtils.getString("description",
							"vote_notify", user.getName(), code.getIdent(),
							code.getName(), code.getIdent());
					Msg.INSTANCE.addMsg(vote.getUser(), code.getUser(),
							Msg.TYPE_UP, code.getId(), notify);

				}
				Vote.evictCache(vote.CacheRegion(), Vote.VOTE_COUNT + id);
				ctx.print(gson.toJson(vote));
			} else
				throw ctx.error("operation_failed");
		}
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
	 * 用户收藏
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@PostMethod
	@UserRoleRequired
	@JSONOutputEnabled
	public void favor(RequestContext ctx) throws IOException {
		User user = (User) ctx.user();
		String verify_code = ctx.param("v_code", "");
		if (!user.IsCurrentUser(verify_code))
			throw ctx.error("operation_forbidden");
		long code_id = ctx.id();
		Code code = Code.INSTANCE.Get(code_id);
		if (null == code)
			throw ctx.error("code_not_exist");
		if (code.IsFavor(user.getId()))
			throw ctx.error("favor_exist");
		Favor favor = new Favor();
		favor.setCode(code_id);
		favor.setUser(user.getId());
		favor.setStatus(Favor.NOTNEW);
		favor.setCode_ident(code.getIdent());
		favor.setCreate_time(new Timestamp(new Date().getTime()));
		if (0 != favor.Save()) {
			// 清除缓存
			favor.evictCache(user, code_id);
			ctx.print(gson.toJson(favor));
		} else
			throw ctx.error("operation_failed");
	}

	/**
	 * 取消收藏
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@PostMethod
	@UserRoleRequired
	@JSONOutputEnabled
	public void un_favor(RequestContext ctx) throws IOException {
		User user = (User) ctx.user();
		String verify_code = ctx.param("v_code", "");
		if (!user.IsCurrentUser(verify_code))
			throw ctx.error("operation_forbidden");
		long code_id = ctx.id();
		Code code = Code.INSTANCE.Get(code_id);
		if (!code.IsFavor(user.getId()))
			throw ctx.error("favor_not_exist");
		Favor favor = Favor.INSTANCE.getFavorByCodeAndUser(user.getId(),
				code_id);
		if (null == favor)
			throw ctx.error("favor_not_exist");
		if (favor.Delete()) {
			// 清除缓存
			favor.evictCache(user, code_id);
			ctx.print(gson.toJson(favor));
		} else
			throw ctx.error("operation_failed");
	}

	/**
	 * 验证码
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	public void captcha(RequestContext ctx) throws IOException {
		ImageCaptchaService.get(ctx);
	}
}
