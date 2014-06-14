package net.oschina.runjs.action;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import net.oschina.common.servlet.Annotation.JSONOutputEnabled;
import net.oschina.common.servlet.Annotation.PostMethod;
import net.oschina.common.servlet.Annotation.UserRoleRequired;
import net.oschina.common.servlet.IUser;
import net.oschina.common.servlet.RequestContext;
import net.oschina.runjs.beans.Code;
import net.oschina.runjs.beans.Plugin;
import net.oschina.runjs.beans.PluginCode;
import net.oschina.runjs.beans.Project;
import net.oschina.runjs.beans.User;
import net.oschina.runjs.beans.UserFile;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;

public class PluginAction {
	private Gson gson = new Gson();

	/**
	 * 添加到市场
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@UserRoleRequired(role = IUser.ROLE_ADMIN)
	@PostMethod
	@JSONOutputEnabled
	public void add_to_market(RequestContext ctx) throws IOException {
		long id = ctx.id();
		Code code = Code.INSTANCE.Get(id);
		if (code == null)
			throw ctx.error("code_not_exist");
		PluginCode sc = new PluginCode();
		sc.setCodeid(id);
		sc.setCreate_time(new Timestamp(new Date().getTime()));
		sc.setCss(code.getCss());
		sc.setHtml(code.getHtml());
		sc.setJs(code.getJs());
		sc.setUpdate_time(code.getUpdate_time());
		if (0 < sc.Save()) {
			ctx.print(gson.toJson(sc));
			PluginCode.evictCache(PluginCode.INSTANCE.CacheRegion(),
					PluginCode.NEW_LIST);
		} else
			throw ctx.error("operation_failed");
	}

	/**
	 * 更新到市场
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@UserRoleRequired(role = IUser.ROLE_ADMIN)
	@PostMethod
	@JSONOutputEnabled
	public void update_to_market(RequestContext ctx) throws IOException {
		long id = ctx.id();
		Code code = Code.INSTANCE.Get(id);
		if (code == null)
			throw ctx.error("code_not_exist");
		PluginCode sc = PluginCode.INSTANCE.GetPluginCodeByCode(id);
		if (sc == null)
			throw ctx.error("code_not_in_square");
		sc.setCss(code.getCss());
		sc.setHtml(code.getHtml());
		sc.setJs(code.getJs());
		Timestamp time = new Timestamp(new Date().getTime());
		sc.setUpdate_time(time);
		sc.setCss(code.getCss());
		sc.setHtml(code.getHtml());
		sc.setJs(code.getJs());
		sc.setUpdate_time(code.getUpdate_time());
		if (sc.Update())
			ctx.print(gson.toJson(sc));
		else
			throw ctx.error("operation_failed");
	}

	/**
	 * 从市场删除
	 */
	@UserRoleRequired(role = IUser.ROLE_ADMIN)
	@PostMethod
	@JSONOutputEnabled
	public void delete_from_market(RequestContext ctx) throws IOException {
		long id = ctx.id();
		PluginCode sc = PluginCode.INSTANCE.Get(id);
		if (sc == null)
			throw ctx.error("square_code_null");
		if (sc.Delete()) {
			PluginCode.evictCache(PluginCode.INSTANCE.CacheRegion(),
					PluginCode.NEW_LIST);
			ctx.print(gson.toJson(sc));
		} else
			throw ctx.error("operation_failed");
	}

	/**
	 * 将代码设为插件
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@UserRoleRequired
	@PostMethod
	@JSONOutputEnabled
	public void set_code_plugin(RequestContext ctx) throws IOException {
		User user = (User) ctx.user();
		int sys = ctx.param("sys", 0);
		long id = ctx.id();
		Code code = Code.INSTANCE.Get(id);
		if (null == code)
			throw ctx.error("code_not_exist");
		Plugin plugin = Plugin.INSTANCE.GetPluginByCode(id);
		Timestamp time = new Timestamp(new Date().getTime());
		if (plugin == null) {
			plugin = new Plugin();
			plugin.setCode(id);
			plugin.setUser(code.getUser());
			plugin.setStatus(Plugin.UNCHECK);
			plugin.setType((user.IsAdmin() && sys == 1) ? Plugin.SYS_PLUGIN
					: Plugin.PLUGIN);
			plugin.setCreate_time(time);
			plugin.setUpdate_time(time);
			if (0 < plugin.Save()) {
				ctx.print(gson.toJson(code));
			} else
				throw ctx.error("operation_failed");
		} else
			throw ctx.error("plugin_exist");
	}

	@UserRoleRequired(role = IUser.ROLE_ADMIN)
	@PostMethod
	@JSONOutputEnabled
	public void create_sys_plugin(RequestContext ctx) throws IOException {
		create_plugin(ctx, Plugin.SYS_PLUGIN);
	}

	@UserRoleRequired
	@PostMethod
	@JSONOutputEnabled
	public void create_normal_plugin(RequestContext ctx) throws IOException {
		create_plugin(ctx, Plugin.PLUGIN);
	}

	/**
	 * 添加系统插件
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	@UserRoleRequired(role = IUser.ROLE_ADMIN)
	@PostMethod
	@JSONOutputEnabled
	public void add_sys_plugin(RequestContext ctx) throws IOException {
		User user = (User) ctx.user();
		File file = ctx.file("file");
		if (null == file)
			throw ctx.error("file_not_found");
		// 判断是否超出大小限制
		if (file.length() > UserFile.MAX_SIZE)
			throw ctx.error("user_file_max_size", UserFile.MAX_SIZE
					/ (1024 * 1024));
		String pro_name = ctx.param("name", "");
		if (pro_name.length() < 1 || pro_name.length() > 20)
			throw ctx.error("pro_name_invalid");
		// 判断该用户是否已经存在该项目,项目名不能重名。
		if (Project.isProjectExist(pro_name, user.getId()))
			throw ctx.error("pro_exist");
		String html = "";
		String js = "";
		String css = "";
		try {
			ZipFile zip = new ZipFile(file);
			Enumeration e = zip.entries();
			while (e.hasMoreElements()) {
				ZipEntry ze = (ZipEntry) e.nextElement();
				if (!ze.isDirectory()) {
					String name = ze.getName();
					if (FilenameUtils.isExtension(name, "html"))
						html = IOUtils
								.toString(zip.getInputStream(ze), "utf-8");
					else if (FilenameUtils.isExtension(name, "js"))
						js = IOUtils.toString(zip.getInputStream(ze), "utf-8");
					else if (FilenameUtils.isExtension(name, "css"))
						css = IOUtils.toString(zip.getInputStream(ze), "utf-8");
				}
			}
			zip.close();
			if (StringUtils.isBlank(html) && StringUtils.isBlank(js)
					&& StringUtils.isBlank(css))
				throw ctx.error("file_code_null");
			Code code = new Code();
			code.setName(pro_name);
			code.setCss(css);
			code.setJs(js);
			code.setHtml(html);
			code.setUser(user.getId());
			code.setNum(Project.INIT_VERSION);
			Timestamp time = new Timestamp(new Date().getTime());
			code.setCreate_time(time);
			code.setUpdate_time(time);
			Project p = new Project();
			p.setName(pro_name);
			p.setUser(user.getId());
			p.setVersion(Project.INIT_VERSION);
			p.setCreate_time(time);
			p.setUpdate_time(time);
			long pid = p.Save();
			if (0 < pid) {
				// 清除用户项目列表的缓存
				Project.evictCache(p.CacheRegion(), Project.USER_PRO_LIST
						+ user.getId());
				code.setProject(pid);
				long cid = code.Save();
				if (0 < cid) {
					Plugin plugin = new Plugin();
					plugin.setCode(cid);
					plugin.setUser(code.getUser());
					plugin.setStatus(Plugin.UNCHECK);
					plugin.setType(Plugin.SYS_PLUGIN);
					plugin.setCreate_time(time);
					plugin.setUpdate_time(time);
					if (0 < plugin.Save()) {
						ctx.print(gson.toJson(code));
					} else {
						code.Delete();
						p.Delete();
						throw ctx.error("operation_failed");
					}
				} else {
					p.Delete();
					throw ctx.error("operation_failed");
				}
			} else
				throw ctx.error("operation_failed");
		} catch (ZipException e) {
			e.printStackTrace();
			throw ctx.error("zip_not_valid");
		}
	}

	@UserRoleRequired
	@PostMethod
	@JSONOutputEnabled
	public void check(RequestContext ctx) throws IOException {
		User user = (User) ctx.user();
		long id = ctx.id();
		Plugin plugin = Plugin.INSTANCE.Get(id);
		if (plugin == null) {
			throw ctx.error("plugin_not_exist");
		}
		if (!user.IsAdmin(plugin))
			throw ctx.error("operation_forbidden");
		plugin.setStatus(Plugin.CHECKED);
		plugin.UpdateField("status", Plugin.CHECKED);
		plugin.EvictListCache();
		Plugin.evictCache(plugin.CacheRegion(),
				Plugin.PLUGIN_CODE + plugin.getCode());
		ctx.print(gson.toJson(plugin));
	}

	@UserRoleRequired
	@PostMethod
	@JSONOutputEnabled
	public void uncheck(RequestContext ctx) throws IOException {
		User user = (User) ctx.user();
		long id = ctx.id();
		Plugin plugin = Plugin.INSTANCE.Get(id);
		if (plugin == null) {
			throw ctx.error("plugin_not_exist");
		}
		if (!user.IsAdmin(plugin))
			throw ctx.error("operation_forbidden");
		plugin.setStatus(Plugin.UNCHECK);
		plugin.UpdateField("status", Plugin.UNCHECK);
		plugin.EvictListCache();
		Plugin.evictCache(plugin.CacheRegion(),
				Plugin.PLUGIN_CODE + plugin.getCode());
		ctx.print(gson.toJson(plugin));
	}

	/**
	 * 创建不同类型的插件
	 * 
	 * @param ctx
	 * @param type
	 * @throws IOException
	 */
	private void create_plugin(RequestContext ctx, int type) throws IOException {
		User user = (User) ctx.user();
		String pro_name = ctx.param("name", "");
		if (pro_name.length() < 1 || pro_name.length() > 20)
			throw ctx.error("pro_name_invalid");
		// 判断该用户是否已经存在该项目,项目名不能重名。
		if (Project.isProjectExist(pro_name, user.getId()))
			throw ctx.error("pro_exist");
		String html = ctx.param("html", "");
		String js = ctx.param("js", "");
		String css = ctx.param("css", "");
		Timestamp time = new Timestamp(new Date().getTime());

		Code code = new Code();
		code.setName(pro_name);
		code.setCss(css);
		code.setJs(js);
		code.setHtml(html);
		code.setUser(user.getId());
		code.setNum(Project.INIT_VERSION);
		code.setCreate_time(time);
		code.setUpdate_time(time);
		code.setCode_type(Code.DEFAULT_TYPE);

		Project p = new Project();
		p.setName(pro_name);
		p.setUser(user.getId());
		p.setVersion(Project.INIT_VERSION);
		p.setCreate_time(time);
		p.setUpdate_time(time);
		long pid = p.Save();

		if (0 < pid) {
			// 清除用户项目列表的缓存
			Project.evictCache(p.CacheRegion(),
					Project.USER_PRO_LIST + user.getId());
			code.setProject(pid);
			long cid = code.Save();
			if (0 < cid) {
				Plugin plugin = new Plugin();
				plugin.setCode(cid);
				plugin.setUser(code.getUser());
				plugin.setStatus(Plugin.UNCHECK);
				plugin.setType(type);
				plugin.setCreate_time(time);
				plugin.setUpdate_time(time);
				if (0 < plugin.Save()) {
					ctx.print(gson.toJson(code));
				} else {
					code.Delete();
					p.Delete();
					throw ctx.error("operation_failed");
				}
			} else {
				p.Delete();
				throw ctx.error("operation_failed");
			}
		} else
			throw ctx.error("operation_failed");
	}
	/*
	 * public void install(RequestContext ctx) throws IOException { User user =
	 * (User) ctx.user(); long id = ctx.id(); Code fork_code =
	 * Code.INSTANCE.Get(id); if (null == fork_code) throw
	 * ctx.error("code_not_exist"); if (fork_code.IsPosted()) throw
	 * ctx.error("code_not_publish"); Timestamp time = new Timestamp(new
	 * Date().getTime()); // 克隆代码 Code code = new Code();
	 * code.setName(fork_code.getName()+"_"+fork_code.getIdent());
	 * code.setFork(fork_code.getId()); code.setUser(user.getId()); //
	 * 清除fork列表缓存 Code.evictCache(code.CacheRegion(), Code.FORK_LIST +
	 * code.getFork()); code.setProject(0); code.setCss(fork_code.getCss());
	 * code.setHtml(fork_code.getHtml()); code.setJs(fork_code.getJs());
	 * code.setNum(1); code.setCreate_time(time); code.setUpdate_time(time);
	 * code.setCode_type(fork_code.getCode_type()); code.setId(code.Save()); if
	 * (0 != code.getId()) { // 添加动态 Dynamic.INSTANCE.add_fork_dy(code); if
	 * (fork_code.getUser() != code.getUser()) { // 添加通知 String notify =
	 * ResourceUtils.getString("description", "fork_notify", user.getName(),
	 * fork_code.getIdent(), fork_code.getName(), code.getIdent());
	 * Msg.INSTANCE.addMsg(code.getUser(), fork_code.getUser(), Msg.NULL_REFER,
	 * Msg.TYPE_FORK, notify); } Code.evictCache(code.CacheRegion(),
	 * Code.FORK_COUNT + fork_code.getId()); } else throw
	 * ctx.error("operation_failed"); Plugin plugin =
	 * Plugin.INSTANCE.GetPluginByCode(id); plugin = new Plugin();
	 * plugin.setCode(code.getId()); plugin.setUser(user.getId());
	 * plugin.setStatus(Plugin.UNCHECK); plugin.setType(Plugin.PLUGIN);
	 * plugin.setCreate_time(time); plugin.setUpdate_time(time); if (0 <
	 * plugin.Save()) { ctx.print(gson.toJson(code)); } else throw
	 * ctx.error("operation_failed"); }
	 */
}