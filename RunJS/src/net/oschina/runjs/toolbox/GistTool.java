package net.oschina.runjs.toolbox;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import net.oschina.common.servlet.RequestContext;
import net.oschina.common.servlet.VelocityHelper;
import net.oschina.runjs.beans.Code;
import net.oschina.runjs.beans.User;

import org.apache.commons.io.FileUtils;

public class GistTool {
	public static final transient String GIST_VM = "/WEB-INF/templates/common/_gist.vm";
	public static final transient String GIST_DIR = "/WEB-INF/user_gist/";

	// 获取gist的js代码
	public static String getGist(String ident, String type) {
		Code code = Code.INSTANCE.getCodeByIdent(ident);
		if (code == null) {
			return null;
		}
		long time1 = code.getUpdate_time().getTime();
		File gist_file = getGistFile(code, ident, type);
		if (gist_file != null && time1 <= gist_file.lastModified()) {
			try {
				return FileUtils.readFileToString(gist_file, "UTF-8");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("project", new ProjectTool());
		map.put("ident", ident);
		map.put("code", code);
		map.put("type", type);
		map.put("theme", "theme");
		try {
			String result = VelocityHelper.execute(GIST_VM, map);
			saveGistToFile(result, getGistPath(code, ident, type));
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	// 查找文件
	public static File getGistFile(Code code, String ident, String type) {
		File gist_file = new File(getGistPath(code, ident, type));
		if (!gist_file.exists() || !gist_file.isFile())
			return null;
		return gist_file;
	}

	// 保存到文件
	public static boolean saveGistToFile(String gist, String path) {
		File file = new File(path);
		try {
			FileUtils.writeStringToFile(file, gist, "UTF-8");
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	// 生成路径
	public static String getGistPath(Code code, String ident, String type) {
		if (code == null)
			return null;
		User user = User.INSTANCE.Get(code.getUser());
		if (user == null)
			return null;
		StringBuilder path = new StringBuilder(RequestContext.root());
		path.append(GIST_DIR);
		path.append(String.valueOf(code.getUser() % 500));
		path.append("/");
		path.append(user.getIdent());
		path.append("/");
		path.append(ident);
		path.append("_");
		path.append(type);
		path.append(".gist");
		return path.toString();
	}
}