package net.oschina.runjs.action;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import net.oschina.common.cache.CacheManager;
import net.oschina.common.servlet.Annotation.JSONOutputEnabled;
import net.oschina.common.servlet.Annotation.PostMethod;
import net.oschina.common.servlet.Annotation.UserRoleRequired;
import net.oschina.common.servlet.RequestContext;
import net.oschina.runjs.beans.User;
import net.oschina.runjs.beans.UserFile;
import net.oschina.runjs.toolbox.ProjectTool;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * ajax测试
 * 
 * @author jack
 * 
 */
public class AjaxAction {
	public static final String JS_CACHE_REGION = "js_region";

	/**
	 * 编译 Less
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	public void less_compile(RequestContext ctx) throws IOException {
		String less = IOUtils.toString(ctx.request().getReader());
		ctx.print(ProjectTool.less_compile(less));
	}

	/**
	 * 返回用户指定的上传的json或者xml
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@UserRoleRequired
	@JSONOutputEnabled
	public void echo(RequestContext ctx) throws IOException {
		User user = (User) ctx.user();
		String name = ctx.param("name", "");
		if (StringUtils.isBlank(name))
			throw ctx.error("file_not_found");
		String ext = FilenameUtils.getExtension(name);
		List<UserFile> list = null;
		if (ext.equalsIgnoreCase("json")) {
			list = UserFile.INSTANCE.getUserFileList(user.getId(),
					UserFile.F_JSON);
		} else if (ext.equalsIgnoreCase("xml")) {
			list = UserFile.INSTANCE.getUserFileList(user.getId(),
					UserFile.F_XML);
		}
		if (null == list)
			throw ctx.error("file_not_found");
		for (UserFile uf : list)
			if (name.equalsIgnoreCase(uf.getName())) {
				ctx.response().setContentType("text/plain");
				ctx.print(FileUtils.readFileToString(
						new File(RequestContext.root() + uf.getPath()), "UTF-8"));
				return;
			}
		throw ctx.error("file_not_found");
	}

	/**
	 * 获取js FIXME 这只是临时的解决办法，必须找出其他的引入js的方法，不能让服务器代为处理
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@UserRoleRequired(role = User.ROLE_ADMIN)
	public void get_js(RequestContext ctx) throws IOException {
		// 如果是重复请求，返回304
		if (StringUtils.isNotBlank(ctx.header("If-Modified-Since"))) {
			ctx.response().sendError(HttpServletResponse.SC_NOT_MODIFIED);
			return;
		} else {
			String url = ctx.param("url", "");
			String md5 = DigestUtils.md5Hex(url);
			String js = (String) CacheManager.get(JS_CACHE_REGION, md5);
			ctx.header("Content-Type", "application/x-javascript;charset=UTF-8");
			ctx.header("Last-Modified",
					new SimpleDateFormat().format(new Date()));
			if (null == js) {
				HttpClient hc = new DefaultHttpClient();
				try {
					HttpGet httpget = new HttpGet(url);
					HttpResponse response = hc.execute(httpget);
					int r_code = response.getStatusLine().getStatusCode();
					if (r_code == 200) {
						HttpEntity entity = response.getEntity();
						js = IOUtils.toString(entity.getContent(), "UTF-8");
						CacheManager.set(JS_CACHE_REGION, md5, js);
						ctx.print(js);
						return;
					} else {
						ctx.response().sendError(r_code);
						return;
					}
				} catch (Exception e) {
					ctx.response().sendError(HttpServletResponse.SC_NOT_FOUND);
					return;
				}
			} else {
				ctx.print(js);
			}
		}
	}

	/*
	 * 登录
	 * 
	 * @param ctx
	 * 
	 * @throws IOException
	 
	@JSONOutputEnabled
	public void login(RequestContext ctx) throws IOException {
		String username = ctx.param("username", "");
		User user = User.INSTANCE.getUserByName(username);
		if (null != user) {
			user.UpdateField("online", 1);

			String new_value = RequestContext._GenLoginKey(user, ctx.ip(),
					ctx.header("user-agent"));

			ctx.header("Set-Cookie", RequestContext.COOKIE_LOGIN + "="
					+ new_value + "; path=/;HTTPOnly");
			ctx.redirect("/");
		} else {
			throw ctx.error("user_login_failed");
		}
	}
*/
	@JSONOutputEnabled
	@PostMethod
	public void logout(RequestContext ctx) throws IOException {
		User user = (User) ctx.user();
		if (null != user)
			user.UpdateField("online", User.OFFLINE);
		ctx.deleteCookie(RequestContext.COOKIE_LOGIN, false);
		ctx.output_json("msg", "ok");
	}
}