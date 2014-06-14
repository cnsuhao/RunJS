package net.oschina.runjs.action;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.oschina.common.servlet.Annotation.JSONOutputEnabled;
import net.oschina.common.servlet.Annotation.UserRoleRequired;
import net.oschina.common.servlet.RequestContext;
import net.oschina.common.utils.FormatTool;
import net.oschina.runjs.beans.User;
import net.oschina.runjs.beans.UserFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;

public class EchoAction {
	public Gson gson = new Gson();

	/**
	 * echo JSON数据
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	public void json(RequestContext ctx) throws IOException {
		ctx.header("Content-Type", "text/plain");
		// ctx.print(gson.toJson(ctx.request().getParameterMap()));
		Map<String, String[]> map = ctx.request().getParameterMap();
		StringBuilder sb = new StringBuilder("{");
		Iterator<String> i = map.keySet().iterator();
		while (i.hasNext()) {
			String key = i.next();
			String[] values = map.get(key);
			sb.append("\"").append(StringEscapeUtils.escapeJavaScript(key))
					.append("\":");
			// 如果没有值
			if (values == null || 0 == values.length) {
				sb.append("\"\"");

			}
			// 如果只有一个值
			else if (values.length == 1) {
				sb.append("\"")
						.append(StringEscapeUtils.escapeJavaScript(values[0]))
						.append("\"");
			}
			// 如果有多个值
			else {
				sb.append("[");
				for (int j = 0; j < values.length; j++) {
					if (j != values.length - 1)
						sb.append("\"")
								.append(StringEscapeUtils
										.escapeJavaScript(values[j]))
								.append("\",");
					else
						sb.append("\"")
								.append(StringEscapeUtils
										.escapeJavaScript(values[j]))
								.append("\"");
				}
				sb.append("]");
			}
			if (i.hasNext())
				sb.append(",");
		}
		sb.append("}");
		ctx.print(sb.toString());
	}

	/**
	 * echo XML数据
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	public void xml(RequestContext ctx) throws IOException {
		Map<String, String[]> map = ctx.request().getParameterMap();
		StringBuilder sb = new StringBuilder(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
		sb.append("<params>\r\n");
		Iterator<String> i = map.keySet().iterator();
		while (i.hasNext()) {
			String key = i.next();
			sb.append("\t<param>\r\n");
			sb.append("\t\t<name>");
			sb.append(FormatTool.html(key));
			sb.append("</name>\r\n");
			String[] values = map.get(key);
			for (String value : values) {
				sb.append("\t\t<value>");
				sb.append(FormatTool.html(value));
				sb.append("</value>\r\n");
			}
			sb.append("\t</param>\r\n");
		}
		sb.append("</params>");
		ctx.header("Content-Type", "text/xml");
		ctx.print(sb.toString());
	}

	/**
	 * echo 文本数据
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	public void text(RequestContext ctx) throws IOException {
		Map<String, String[]> map = ctx.request().getParameterMap();
		StringBuilder sb = new StringBuilder("");
		Iterator<String> i = map.keySet().iterator();
		while (i.hasNext()) {
			String key = i.next();
			String[] values = map.get(key);
			if (values != null) {
				for (String v : values) {
					sb.append(FormatTool.html(key));
					sb.append("=");
					sb.append(v);
					sb.append("\r\n");
				}
			}
		}
		ctx.header("Content-Type", "text/plain");
		ctx.print(sb.toString());
	}

	/**
	 * 返回用户指定的上传的json或者xml
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@UserRoleRequired
	@JSONOutputEnabled
	public void file(RequestContext ctx) throws IOException {
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
}