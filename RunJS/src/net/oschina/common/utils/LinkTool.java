package net.oschina.common.utils;

import java.io.*;
import java.net.URLEncoder;
import java.util.Properties;

import net.oschina.common.servlet.RequestContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.tools.struts.StrutsLinkTool;

/**
 * 链接工具
 * @author Winter Lau
 */
public class LinkTool extends StrutsLinkTool {

	private final static Log log = LogFactory.getLog(LinkTool.class);

	private final static String HOSTS = RequestContext.root() + java.io.File.separator + "WEB-INF" + java.io.File.separator + "conf" + java.io.File.separator + "hosts.properties";
	private final static Properties hosts = new Properties();
	private static long lastCheck = -1L;
	private static long lastModified = -1L;
	private final static long CHECK_INTERVAL = 120000;	
	
	/**
	 * 获取在 /WEB-INF/conf/hosts.properties 中定义的各种类型文件对应的域名
	 * @param key
	 * @return
	 */
	static String getHostOf(String key) {
		if (StringUtils.isNotBlank(key)) {
			long ct = System.currentTimeMillis();
			if (hosts.size() == 0 || ((ct - lastCheck) > CHECK_INTERVAL)) {
				java.io.File f_hosts = new java.io.File(HOSTS);
				if (hosts.size() == 0 || f_hosts.lastModified() > lastModified) {
					if (f_hosts.lastModified() > lastModified) {
						synchronized (LinkTool.class) {
							InputStream inStream = null;
							try {
								hosts.clear();
								inStream = new FileInputStream(f_hosts);
								hosts.load(inStream);
								log.info("Reload " + f_hosts.getPath() + " finished.");
							} catch (IOException e) {
								log.error("Failed to load " + f_hosts.getPath(), e);
								return "";
							} finally {
								IOUtils.closeQuietly(inStream);
							}
							lastModified = f_hosts.lastModified();
						}
					}
				}
				lastCheck = ct;
			}
			String prefixs = hosts.getProperty(key);
			if (StringUtils.isNotBlank(prefixs)){
				//多个主机头自动随机均衡负载
				String[] ps = StringUtils.split(prefixs, ',');
				if(ps.length == 1) return ps[0];
				return ps[RandomUtils.nextInt(ps.length)];
			}
		}
		return "";
	}

	public void set_cookie(String name, String value, int expire) {
		RequestUtils.deleteCookie(request, response, name, true);
		RequestUtils.setCookie(request, response, name, value, expire);
	}
	
	public String get_cookie(String name) {
		return RequestUtils.getCookieValue(request, name);
	}
	
	public boolean is_robot(){ return RequestUtils.isRobot(request); }
	
	public String relocate(String url) throws IOException {
		request.setAttribute("close_comment", true);
		response.setStatus(301);
		response.setHeader("Location", url);
		response.setHeader("Connection", "close");
		return "";
	}

	public String redirect(String url) throws IOException {
		request.setAttribute("close_comment", true);
		response.sendRedirect(url);
		return "";
	}

	public static String root() {
		return root("/");
	}
	
	public static String root(String uri) {
		StringBuilder root = new StringBuilder(RequestContext.getContextPath());
		if (uri.length() > 0 && uri.charAt(0) != '/')
			root.append('/');
		root.append(uri);
		return root.toString();
	}
	
	public static String home() {
		return home("/");
	}
	
	public static String home(String uri) {
		if(StringUtils.isBlank(uri))
			return home();
		StringBuilder root = new StringBuilder(getHostOf("home"));
		root.append(RequestContext.getContextPath());
		if (uri.length() > 0 && uri.charAt(0) != '/')
			root.append('/');
		root.append(uri);
		return root.toString();
	}

	public static String my() {
		return my("/");
	}
	
	public static String my(String uri) {
		StringBuilder root = new StringBuilder(getHostOf("my"));
		root.append(RequestContext.getContextPath());
		if (uri.length() > 0 && uri.charAt(0) != '/')
			root.append('/');
		root.append(uri);
		return root.toString();
	}
	
	public static String action(String action_uri){
		StringBuilder action = new StringBuilder(RequestContext.getContextPath());
		action.append("/action");
		if (action_uri.length() > 0 && action_uri.charAt(0) != '/')
			action.append('/');
		action.append(action_uri);
		return action.toString();
	}
	
	/**
	 * URL编码
	 * 
	 * @param url
	 * @return
	 */
	public static String encode_url(String url) {
		return encode_url(url, "utf-8");
	}

	/**
	 * URL编码
	 * 
	 * @param url
	 * @param charset
	 * @return
	 */
	public static String encode_url(String url, String charset) {
		if (StringUtils.isEmpty(url)) return "";
		try{
			return URLEncoder.encode(url, charset);
		}catch(UnsupportedEncodingException e){}
		return null;
	}

	/**
	 * 获取浏览器提交的整形参数
	 * 
	 * @param param
	 * @param defaultValue
	 * @return
	 */
	public int param(String param, int defaultValue) {
		return RequestUtils.getParam(request, param, defaultValue);
	}

	/**
	 * 获取浏览器提交的长整形参数
	 * 
	 * @param param
	 * @param defaultValue
	 * @return
	 */
	public long lparam(String param, long defaultValue) {
		return RequestUtils.getParam(request, param, defaultValue);
	}

	public long[] lparams(String param) {
		return RequestUtils.getParamValues(request, param);
	}

	/**
	 * 获取浏览器提交的字符串参数
	 * 
	 * @param param
	 * @param defaultValue
	 * @return
	 */
	public String param(String param, String defaultValue) {
		return RequestUtils.getParam(request, param, defaultValue);
	}
	
	public String param(String param){
		return request.getParameter(param);
	}

	/**
	 * 根据img名称返回其URL
	 * 
	 * @param name
	 * @return
	 */
	public static String img(String name) {
		return _static(name, "img", "/img");
	}

	/**
	 * 根据js名称返回其URL
	 * 
	 * @param name
	 * @return
	 */
	public static String js(String name) {
		return _static(name, "js", "/js");
	}

	/**
	 * 根据css名称返回其URL
	 * 
	 * @param name
	 * @return
	 */
	public static String css(String name) {
		return _static(name, "css", "/css");
	}
	
	public static String upload(String name) {
		return _static(name, "upload", "/uploads");
	}

	private static String _static(String file, String type, String base) {
		if(file == null) return null;
		StringBuilder js = new StringBuilder(getHostOf(type));
		js.append(RequestContext.getContextPath());
		js.append(base);
		if (!file.startsWith("/") && !js.toString().endsWith("/"))
			js.append('/');
		js.append(file);
		return js.toString();
	}

}
