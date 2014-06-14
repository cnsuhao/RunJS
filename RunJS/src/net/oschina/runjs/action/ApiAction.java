package net.oschina.runjs.action;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.oschina.common.servlet.Annotation.JSONOutputEnabled;
import net.oschina.common.servlet.Annotation.PostMethod;
import net.oschina.common.servlet.Annotation.UserRoleRequired;
import net.oschina.common.servlet.RequestContext;
import net.oschina.runjs.beans.Code;
import net.oschina.runjs.beans.Project;
import net.oschina.runjs.beans.Setting;
import net.oschina.runjs.beans.User;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;

/**
 * API处理
 * 
 * @author jack
 * 
 */
public class ApiAction {

	private Gson gson = new Gson();

	/**
	 * 通过code的id获取code信息。 管理员、代码所有者才可以拿到
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@PostMethod
	@UserRoleRequired
	@JSONOutputEnabled
	public void getCode(RequestContext ctx) throws IOException {
		User user = (User) ctx.user();
		long id = ctx.id();
		Code code = Code.INSTANCE.Get(id);
		if (null == code)
			throw ctx.error("code_not_exist");
		if (!user.IsAdmin(code))
			throw ctx.error("operation_forbidden");
		else
			ctx.print(gson.toJson(code));
	}

	/**
	 * 打包下载
	 */
	@JSONOutputEnabled
	@UserRoleRequired
	public void get_zip(RequestContext ctx) throws IOException {
		User user = (User) ctx.user();
		long id = ctx.id();
		Code code = Code.INSTANCE.Get(id);
		if (null == code)
			throw ctx.error("code_not_exist");

		if (!user.IsAdmin(code) && !code.IsPosted())
			throw ctx.error("operation_forbidden");

		else {
			// 将相对路径替换成绝对路径
			String new_html = StringUtils.replace(code.getHtml(),
					"src=\"/js/sandbox",
					"src=\"http://sandbox.runjs.cn/js/sandbox");
			new_html = StringUtils.replace(new_html, "href=\"/js/sandbox",
					"href=\"http://sandbox.runjs.cn/js/sandbox");
			// 临时文件
			File html = new File(code.getIdent() + "/my.html");
			File css = new File(code.getIdent() + "/my.css");
			File js = new File(code.getIdent() + "/my.js");
			// 如果有CSS
			if (StringUtils.isNotBlank(code.getCss())) {
				FileUtils.writeStringToFile(css, code.getCss(), "UTF-8");
				// 如果有head
				if (new_html.contains("</head>")) {
					new_html = StringUtils
							.replaceOnce(new_html, "</head>",
									"<link rel=\"stylesheet\" type=\"text/css\" href=\"my.css\">\r\n</head>");
				} else
					new_html = new_html
							+ "\r\n<link rel=\"stylesheet\" type=\"text/css\" href=\"my.css\">";
			}
			if (StringUtils.isNotBlank(code.getJs())) {
				FileUtils.writeStringToFile(js, code.getJs(), "UTF-8");
				// 如果有head
				if (new_html.contains("</head>")) {
					new_html = StringUtils
							.replaceOnce(new_html, "</head>",
									"<script type=\"text/javascript\" src=\"my.js\"></script>\r\n</head>");
				} else
					new_html = new_html
							+ "\r\n<script type=\"text/javascript\" src=\"my.js\"></script>";
			}
			FileUtils.writeStringToFile(html, new_html, "UTF-8");

			ctx.response().setContentType("application/octet-stream");
			String ua = ctx.user_agent();
			String file_name = code.getName() + "-" + code.getIdent() + ".zip";
			if (ua != null && ua.contains("Firefox"))
				ctx.header("Content-Disposition",
						"attachment; filename*=\"utf8''" + file_name + "\"");
			else
				ctx.header("Content-Disposition", "attachment; filename="
						+ file_name);
			// 输出zip包
			this.zipDIR(code.getIdent(), ctx.response().getOutputStream());

			// 删除临时文件
			html.delete();
			css.delete();
			js.delete();
			new File(code.getIdent()).delete();
		}
	}

	/**
	 * 压缩文件夹
	 * 
	 * @param sourceDIR
	 *            文件夹名称（包含路径）
	 * @param os
	 *            生成zip文件名
	 */
	private void zipDIR(String sourceDIR, OutputStream os) throws IOException {
		ZipOutputStream out = new ZipOutputStream(os);
		int BUFFER_SIZE = 1024;
		byte buff[] = new byte[BUFFER_SIZE];
		File dir = new File(sourceDIR);
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException(sourceDIR
					+ " is not a directory!");
		}
		File files[] = dir.listFiles();

		for (int i = 0; i < files.length; i++) {
			FileInputStream fi = new FileInputStream(files[i]);
			BufferedInputStream origin = new BufferedInputStream(fi);
			ZipEntry entry = new ZipEntry(files[i].getName());
			out.putNextEntry(entry);
			int count;
			while ((count = origin.read(buff)) != -1) {
				out.write(buff, 0, count);
			}
			origin.close();
		}
		out.close();
	}

	/**
	 * 获取一个项目的所有版本的代码
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@PostMethod
	@UserRoleRequired
	@JSONOutputEnabled
	public void getVersions(RequestContext ctx) throws IOException {
		User user = (User) ctx.user();
		long id = ctx.id();
		Project pro = Project.INSTANCE.Get(id);
		if (null == pro)
			throw ctx.error("project_not_exist");
		if (!user.IsAdmin(pro))
			throw ctx.error("operation_forbidden");
		else {
			List<Code> c_l = Code.INSTANCE.getAllCodeByProject(pro.getId());
			ctx.print(gson.toJson(c_l));
		}
	}

	/**
	 * 设置用户设置项
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@UserRoleRequired
	@JSONOutputEnabled
	@PostMethod
	public void setting(RequestContext ctx) throws IOException {
		User user = (User) ctx.user();
		String verify_code = ctx.param("v_code", "");
		if (!user.IsCurrentUser(verify_code))
			throw ctx.error("operation_forbidden");
		String name = ctx.param("name", "");
		String value = ctx.param("value", "");

		if (StringUtils.isBlank(name) || StringUtils.isBlank(value))
			throw ctx.error("param_null");
		// 防止用户随意输入key，添加设置项
		if (!Arrays.asList(Setting.SETTING_LIST.split(",")).contains(name))
			throw ctx.error("invalid_setting_key");

		Setting old_s = Setting.INSTANCE.getSettingByType(name, user.getId());
		if (null == old_s) {
			Setting s = new Setting();
			s.setUser(user.getId());
			s.setName(name);
			s.setCreate_time(new Timestamp(new Date().getTime()));
			s.setValue(value);
			if (0 < s.Save()) {
				// 清除缓存
				Setting.evictCache(s.CacheRegion(), Setting.USER_SET_LIST
						+ user.getId());
				ctx.print(gson.toJson(s));
			} else
				throw ctx.error("operation_failed");
		} else {
			if (old_s.UpdateField("value", value)) {
				// 清除缓存
				Setting.evictCache(old_s.CacheRegion(), Setting.USER_SET_LIST
						+ user.getId());
				ctx.print(gson.toJson(old_s));
			} else
				throw ctx.error("operation_failed");
		}
	}
}
