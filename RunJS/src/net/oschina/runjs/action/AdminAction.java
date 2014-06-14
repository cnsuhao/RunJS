package net.oschina.runjs.action;

import java.io.IOException;
import java.util.List;

import net.oschina.common.servlet.Annotation.JSONOutputEnabled;
import net.oschina.common.servlet.Annotation.PostMethod;
import net.oschina.common.servlet.Annotation.UserRoleRequired;
import net.oschina.common.servlet.IUser;
import net.oschina.common.servlet.RequestContext;
import net.oschina.runjs.beans.Code;
import net.oschina.runjs.beans.Comment;
import net.oschina.runjs.beans.Project;

import com.google.gson.Gson;

/**
 * 项目管理，包括新建、删除、更新、fork等操作
 * 
 * @author jack
 * 
 */
public class AdminAction {
	private static Gson gson = new Gson();

	/**
	 * 通过id删除代码（只有管理员）
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@PostMethod
	@UserRoleRequired(role = IUser.ROLE_ADMIN)
	@JSONOutputEnabled
	public void delete_code(RequestContext ctx) throws IOException {
		long id = ctx.id();
		Code code = Code.INSTANCE.Get(id);
		if (null == code)
			throw ctx.error("code_not_exist");
		List<Comment> comment_list = Comment.INSTANCE.getAllCommentByCode(code
				.getId());
		if (null != comment_list) {
			// 循环删除评论
			for (Comment comment : comment_list) {
				comment.Delete();
			}
		}
		if (code.Delete()) {
			ctx.print(gson.toJson(code));
		} else
			throw ctx.error("operation_failed");
	}

	/**
	 * 删除项目，将会删除该项目的所有代码以及代码的所有评论
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@PostMethod
	@UserRoleRequired(role = IUser.ROLE_ADMIN)
	@JSONOutputEnabled
	public void delete_project(RequestContext ctx) throws IOException {
		long id = ctx.id();
		Project project = Project.INSTANCE.Get(id);
		if (null == project)
			throw ctx.error("project_not_exist");
		List<Code> code_list = Code.INSTANCE.getAllCodeByProject(id);
		if (null != code_list)
			// 循环删除代码
			for (Code code : code_list) {
				List<Comment> comment_list = Comment.INSTANCE
						.getAllCommentByCode(code.getId());
				if (null != comment_list) {
					// 循环删除评论
					for (Comment comment : comment_list) {
						comment.Delete();
					}
				}
				code.Delete();
			}

		if (project.Delete()) {
			// 清除用户项目列表的缓存
			Project.evictCache(project.CacheRegion(), Project.USER_PRO_LIST
					+ project.getUser());
			ctx.print(gson.toJson(project));
		} else
			throw ctx.error("operation_failed");
	}

}
