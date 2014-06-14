package net.oschina.common.servlet;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import net.oschina.common.utils.CryptUtils;
import net.oschina.common.utils.Multimedia;
import net.oschina.common.utils.RequestUtils;
import net.oschina.common.utils.ResourceUtils;
import net.oschina.runjs.beans.User;
import nl.bitwalker.useragentutils.UserAgent;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * 请求上下文
 * 
 * @author Winter Lau
 * @date 2010-1-13 下午04:18:00
 */
public class RequestContext {

	private final static Log log = LogFactory.getLog(RequestContext.class);

	private final static int MAX_FILE_SIZE = 10 * 1024 * 1024;
	private final static String UTF_8 = "UTF-8";

	private final static ThreadLocal<RequestContext> contexts = new ThreadLocal<RequestContext>();
	private final static boolean isResin;
	private final static String upload_tmp_path;
	private final static String TEMP_UPLOAD_PATH_ATTR_NAME = "$OSCHINA_TEMP_UPLOAD_PATH$";

	private static String webroot = null;

	private ServletContext context;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private Map<String, Cookie> cookies;

	private final static Converter dt_converter = new Converter() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
		SimpleDateFormat sdf_time = new SimpleDateFormat("yyyy-M-d H:m");

		@SuppressWarnings("rawtypes")
		public Object convert(Class type, Object value) {
			if (value == null)
				return null;
			if (value.getClass().equals(type))
				return value;
			Date d = null;
			try {
				d = sdf_time.parse(value.toString());
			} catch (ParseException e) {
				try {
					d = sdf.parse(value.toString());
				} catch (ParseException e1) {
					return null;
				}
			}
			if (type.equals(java.util.Date.class))
				return d;
			if (type.equals(java.sql.Date.class))
				return new java.sql.Date(d.getTime());
			if (type.equals(java.sql.Timestamp.class))
				return new java.sql.Timestamp(d.getTime());
			return null;
		}
	};

	static {
		webroot = getWebrootPath();
		isResin = _CheckResinVersion();
		// 上传的临时目录
		upload_tmp_path = webroot + "WEB-INF" + File.separator + "tmp"
				+ File.separator;
		try {
			FileUtils.forceMkdir(new File(upload_tmp_path));
		} catch (IOException excp) {
		}

		// BeanUtils对时间转换的初始化设置
		ConvertUtils.register(dt_converter, java.sql.Date.class);
		ConvertUtils.register(dt_converter, java.sql.Timestamp.class);
		ConvertUtils.register(dt_converter, java.util.Date.class);
	}

	private final static String getWebrootPath() {
		String root = RequestContext.class.getResource("/").getFile();
		try {
			if (root.endsWith(".svn/"))
				root = new File(root).getParentFile().getParentFile()
						.getParentFile().getCanonicalPath();
			else
				root = new File(root).getParentFile().getParentFile()
						.getCanonicalPath();
			root += File.separator;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return root;
	}

	/**
	 * 初始化请求上下文
	 * 
	 * @param ctx
	 * @param req
	 * @param res
	 */
	public static RequestContext begin(ServletContext ctx,
			HttpServletRequest req, HttpServletResponse res) {
		RequestContext rc = new RequestContext();
		rc.context = ctx;
		rc.request = _AutoUploadRequest(_AutoEncodingRequest(req));
		rc.response = res;
		rc.response.setCharacterEncoding(UTF_8);
		rc.cookies = new HashMap<String, Cookie>();
		Cookie[] cookies = req.getCookies();
		if (cookies != null)
			for (Cookie ck : cookies) {
				rc.cookies.put(ck.getName(), ck);
			}
		contexts.set(rc);
		rc.user();// 读取当前登录用户
		return rc;
	}

	/**
	 * 返回Web应用的路径
	 * 
	 * @return
	 */
	public static String root() {
		return webroot;
	}

	public static String getContextPath() {
		RequestContext ctx = RequestContext.get();
		return (ctx != null) ? ctx.contextPath() : "";
	}

	/**
	 * 获取当前请求的上下文
	 * 
	 * @return
	 */
	public static RequestContext get() {
		return contexts.get();
	}

	public void end() {
		String tmpPath = (String) request
				.getAttribute(TEMP_UPLOAD_PATH_ATTR_NAME);
		if (tmpPath != null) {
			try {
				FileUtils.deleteDirectory(new File(tmpPath));
			} catch (IOException e) {
				log.fatal("Failed to cleanup upload directory: " + tmpPath, e);
			}
		}
		this.context = null;
		this.request = null;
		this.response = null;
		this.cookies = null;
		contexts.remove();
	}

	public Locale locale() {
		return request.getLocale();
	}

	/**
	 * 自动编码处理
	 * 
	 * @param req
	 * @return
	 */
	private static HttpServletRequest _AutoEncodingRequest(
			HttpServletRequest req) {
		if (req instanceof RequestProxy)
			return req;
		HttpServletRequest auto_encoding_req = req;
		if ("POST".equalsIgnoreCase(req.getMethod())) {
			try {
				auto_encoding_req.setCharacterEncoding(UTF_8);
			} catch (UnsupportedEncodingException e) {
			}
		} else if (!isResin)
			auto_encoding_req = new RequestProxy(req, UTF_8);

		return auto_encoding_req;
	}

	/**
	 * 自动文件上传请求的封装
	 * 
	 * @param req
	 * @return
	 */
	private static HttpServletRequest _AutoUploadRequest(HttpServletRequest req) {
		if (_IsMultipart(req)) {
			String path = upload_tmp_path
					+ RandomStringUtils.randomAlphanumeric(10);
			File dir = new File(path);
			if (!dir.exists() && !dir.isDirectory())
				dir.mkdirs();
			try {
				req.setAttribute(TEMP_UPLOAD_PATH_ATTR_NAME, path);
				return new MultipartRequest(req, dir.getCanonicalPath(),
						MAX_FILE_SIZE, UTF_8);
			} catch (NullPointerException e) {
			} catch (IOException e) {
				log.fatal("Failed to save upload files into temp directory: "
						+ path, e);
			}
		}
		return req;
	}

	public long id() {
		return param("id", 0L);
	}

	public String ip() {
		String ip = RequestUtils.getRemoteAddr(request);
		if (ip == null)
			ip = "127.0.0.1";
		return ip;
	}

	public Enumeration<String> params() {
		return request.getParameterNames();
	}

	public String param(String name, String... def_value) {
		String v = request.getParameter(name);
		return (v != null) ? v : ((def_value.length > 0) ? def_value[0] : null);
	}

	public long param(String name, long def_value) {
		return NumberUtils.toLong(param(name), def_value);
	}

	public int param(String name, int def_value) {
		return NumberUtils.toInt(param(name), def_value);
	}

	public byte param(String name, byte def_value) {
		return (byte) NumberUtils.toInt(param(name), def_value);
	}

	public String[] params(String name) {
		return request.getParameterValues(name);
	}

	public long[] lparams(String name) {
		String[] values = params(name);
		if (values == null)
			return null;
		List<Long> lvs = new ArrayList<Long>();
		for (String v : values) {
			long lv = NumberUtils.toLong(v, Long.MIN_VALUE);
			if (lv != Long.MIN_VALUE && !lvs.contains(lvs))
				lvs.add(lv);
		}
		long[] llvs = new long[lvs.size()];
		for (int i = 0; i < lvs.size(); i++)
			llvs[i] = lvs.get(i);
		return llvs;
	}

	public String uri() {
		return request.getRequestURI();
	}

	public String contextPath() {
		return request.getContextPath();
	}

	public void redirect(String uri) throws IOException {
		response.sendRedirect(uri);
	}

	public void forward(String uri) throws ServletException, IOException {
		RequestDispatcher rd = context.getRequestDispatcher(uri);
		rd.forward(request, response);
	}

	public void include(String uri) throws ServletException, IOException {
		RequestDispatcher rd = context.getRequestDispatcher(uri);
		rd.include(request, response);
	}

	public boolean isUpload() {
		return (request instanceof MultipartRequest);
	}

	public File file(String fieldName) {
		if (request instanceof MultipartRequest)
			return ((MultipartRequest) request).getFile(fieldName);
		return null;
	}

	public List<File> files() {
		if (request instanceof MultipartRequest) {
			Enumeration<String> names = ((MultipartRequest) request)
					.getFileNames();
			List<File> files = new ArrayList<File>();
			while (names.hasMoreElements()) {
				String name = names.nextElement();
				File file = ((MultipartRequest) request).getFile(name);
				if (null != file)
					files.add(file);
			}
			return files;
		}
		return null;
	}

	public File image(String fieldname) {
		File imgFile = file(fieldname);
		return (imgFile != null && Multimedia.isImageFile(imgFile.getName())) ? imgFile
				: null;
	}

	public boolean isRobot() {
		return RequestUtils.isRobot(request);
	}

	public ActionException fromResource(String bundle, String key,
			Object... args) {
		String res = ResourceUtils.getStringForLocale(request.getLocale(),
				bundle, key, args);
		return new ActionException(res);
	}

	public ActionException error(String key, Object... args) {
		return fromResource("error", key, args);
	}

	/**
	 * 输出信息到浏览器
	 * 
	 * @param msg
	 * @throws IOException
	 */
	public void print(Object msg) throws IOException {
		if (!UTF_8.equalsIgnoreCase(response.getCharacterEncoding()))
			response.setCharacterEncoding(UTF_8);
		response.getWriter().print(msg);
	}

	public void output_json(String[] key, Object[] value) throws IOException {
		JsonObject jo = new JsonObject();
		for (int i = 0; i < key.length; i++) {
			if (value[i] instanceof Number)
				jo.addProperty(key[i], (Number) value[i]);
			else if (value[i] instanceof Boolean)
				jo.addProperty(key[i], (Boolean) value[i]);
			else
				jo.addProperty(key[i], (String) value[i]);
		}
		print(new Gson().toJson(jo));
	}

	public void output_json(String key, Object value) throws IOException {
		output_json(new String[] { key }, new Object[] { value });
	}

	public void json_msg(String msgkey, Object... args) throws IOException {
		output_json(new String[] { "error", "msg" }, new Object[] { 0,
				ResourceUtils.getString("error", msgkey, args) });
	}

	public void error(int code, String... msg) throws IOException {
		if (msg.length > 0)
			response.sendError(code, msg[0]);
		else
			response.sendError(code);
	}

	public void forbidden() throws IOException {
		error(HttpServletResponse.SC_FORBIDDEN);
	}

	public void not_found() throws IOException {
		error(HttpServletResponse.SC_NOT_FOUND);
	}

	public ServletContext context() {
		return context;
	}

	public HttpSession session() {
		return request.getSession(false);
	}

	public HttpSession session(boolean create) {
		return request.getSession(true);
	}

	public Object sessionAttr(String attr) {
		HttpSession ssn = session();
		return (ssn != null) ? ssn.getAttribute(attr) : null;
	}

	public HttpServletRequest request() {
		return request;
	}

	public HttpServletResponse response() {
		return response;
	}

	public Cookie cookie(String name) {
		return cookies.get(name);
	}

	public void cookie(String name, String value, int max_age,
			boolean all_sub_domain) {
		RequestUtils.setCookie(request, response, name, value, max_age,
				all_sub_domain);
	}

	public void deleteCookie(String name, boolean all_domain) {
		RequestUtils.deleteCookie(request, response, name, all_domain);
	}

	public String header(String name) {
		return request.getHeader(name);
	}

	public void header(String name, String value) {
		response.setHeader(name, value);
	}

	public void header(String name, int value) {
		response.setIntHeader(name, value);
	}

	public void header(String name, long value) {
		response.setDateHeader(name, value);
	}

	public String user_agent() {
		return header("user-agent");
	}

	public int user_agent_code() {
		String ua = user_agent();
		return (ua != null) ? Math.abs(ua.hashCode()) : 0;
	}

	/**
	 * 设置public缓存，设置了此类型缓存要求此页面对任何人访问都是同样数据
	 * 
	 * @param minutes
	 *            分钟
	 * @return
	 */
	public void setPublicCache(int minutes) {
		if (!"POST".equalsIgnoreCase(request.getMethod())) {
			int seconds = minutes * 60;
			header("Cache-Control", "max-age=" + seconds);
			Calendar cal = Calendar.getInstance(request.getLocale());
			cal.add(Calendar.MINUTE, minutes);
			header("Expires", cal.getTimeInMillis());
		}
	}

	/**
	 * 设置私有缓存
	 * 
	 * @param minutes
	 * @return
	 */
	public void setPrivateCache(int minutes) {
		if (!"POST".equalsIgnoreCase(request.getMethod())) {
			header("Cache-Control", "private");
			Calendar cal = Calendar.getInstance(request.getLocale());
			cal.add(Calendar.MINUTE, minutes);
			header("Expires", cal.getTimeInMillis());
		}
	}

	/**
	 * 关闭缓存
	 */
	public void closeCache() {
		header("Pragma", "must-revalidate, no-cache, private");
		header("Cache-Control", "no-cache");
		header("Expires", "Sun, 1 Jan 2000 01:00:00 GMT");
	}

	/**
	 * 将HTTP请求参数映射到bean对象中
	 * 
	 * @param req
	 * @param beanClass
	 * @return
	 * @throws Exception
	 */
	public <T> T form(Class<T> beanClass) {
		try {
			T bean = beanClass.newInstance();
			BeanUtils.populate(bean, request.getParameterMap());
			return bean;
		} catch (Exception e) {
			throw new ActionException(e.getMessage());
		}
	}

	/**
	 * 返回当前登录的用户资料
	 * 
	 * @return
	 */
	public IUser user() {
		return User.GetLoginUser(this.request());
	}

	/**
	 * 保存登录信息
	 * 
	 * @param req
	 * @param res
	 * @param user
	 * @param save
	 */
	public void saveUserInCookie(IUser user, boolean save) {
		String new_value = _GenLoginKey(user, ip(), header("user-agent"));
		int max_age = save ? MAX_AGE : -1;
		deleteCookie(COOKIE_LOGIN, true);
		cookie(COOKIE_LOGIN, new_value, max_age, true);
	}

	public void deleteUserInCookie() {
		deleteCookie(COOKIE_LOGIN, true);
	}

	/**
	 * 3.0 以上版本的 Resin 无需对URL参数进行转码
	 * 
	 * @return
	 */
	private final static boolean _CheckResinVersion() {
		try {
			Class<?> verClass = Class.forName("com.caucho.Version");
			String ver = (String) verClass.getDeclaredField("VERSION").get(
					verClass);
			String mainVer = ver.substring(0, ver.lastIndexOf('.'));
			/*
			 * float fVer = Float.parseFloat(mainVer);
			 * System.out.println("----------------> " + fVer);
			 */
			return Float.parseFloat(mainVer) > 3.0;
		} catch (Throwable t) {
		}
		return false;
	}

	/**
	 * 自动解码
	 * 
	 * @author liudong
	 */
	private static class RequestProxy extends HttpServletRequestWrapper {
		private String uri_encoding;

		RequestProxy(HttpServletRequest request, String encoding) {
			super(request);
			this.uri_encoding = encoding;
		}

		/**
		 * 重载getParameter
		 */
		public String getParameter(String paramName) {
			String value = super.getParameter(paramName);
			return _DecodeParamValue(value);
		}

		/**
		 * 重载getParameterMap
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Map getParameterMap() {
			Map params = super.getParameterMap();
			HashMap<String, Object> new_params = new HashMap<String, Object>();
			Iterator<String> iter = params.keySet().iterator();
			while (iter.hasNext()) {
				String key = (String) iter.next();
				Object oValue = params.get(key);
				if (oValue.getClass().isArray()) {
					String[] values = (String[]) params.get(key);
					String[] new_values = new String[values.length];
					for (int i = 0; i < values.length; i++)
						new_values[i] = _DecodeParamValue(values[i]);

					new_params.put(key, new_values);
				} else {
					String value = (String) params.get(key);
					String new_value = _DecodeParamValue(value);
					if (new_value != null)
						new_params.put(key, new_value);
				}
			}
			return new_params;
		}

		/**
		 * 重载getParameterValues
		 */
		public String[] getParameterValues(String arg0) {
			String[] values = super.getParameterValues(arg0);
			for (int i = 0; values != null && i < values.length; i++)
				values[i] = _DecodeParamValue(values[i]);
			return values;
		}

		/**
		 * 参数转码
		 * 
		 * @param value
		 * @return
		 */
		private String _DecodeParamValue(String value) {
			if (StringUtils.isBlank(value) || StringUtils.isBlank(uri_encoding)
					|| StringUtils.isNumeric(value))
				return value;
			try {
				return new String(value.getBytes("8859_1"), uri_encoding);
			} catch (Exception e) {
			}
			return value;
		}

	}

	private static boolean _IsMultipart(HttpServletRequest req) {
		return ((req.getContentType() != null) && (req.getContentType()
				.toLowerCase().startsWith("multipart")));
	}

	/**
	 * 加密
	 * 
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public static String encrypt(String value) {
		return encrypt(value, E_KEY);
	}

	/**
	 * 加密
	 * 
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public static String encrypt(String value, byte[] key) {
		byte[] data = CryptUtils.encrypt(value.getBytes(), key);
		try {
			return URLEncoder.encode(new String(Base64.encodeBase64(data)),
					UTF_8);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 解密
	 * 
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public static String decrypt(String value) {
		return decrypt(value, E_KEY);
	}

	/**
	 * 解密
	 * 
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public static String decrypt(String value, byte[] key) {
		try {
			value = URLDecoder.decode(value, UTF_8);
			if (StringUtils.isBlank(value))
				return null;
			byte[] data = Base64.decodeBase64(value.getBytes());
			return new String(CryptUtils.decrypt(data, key));
		} catch (Exception excp) {
			return null;
		}
	}

	private static String _CleanUserAgent(String ua) {
		if (StringUtils.isBlank(ua))
			return "";
		int idx = StringUtils.indexOf(ua, "staticlogin");
		return (idx > 0) ? StringUtils.substring(ua, 0, idx) : ua;
	}

	/**
	 * 从cookie中读取保存的用户信息
	 * 
	 * @param req
	 * @return
	 */
	public IUser getUserFromCookie() {
		try {
			Cookie cookie = cookie(COOKIE_LOGIN);
			if (cookie != null && StringUtils.isNotBlank(cookie.getValue())) {
				return userFromUUID(cookie.getValue());
			}
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * 生成用户登录标识字符串
	 * 
	 * @param user
	 * @param ip
	 * @param user_agent
	 * @return
	 */
	public static String _GenLoginKey(IUser user, String ip, String user_agent) {
		user_agent = _CleanUserAgent(user_agent);
		StringBuilder sb = new StringBuilder();
		sb.append(user.getId());
		sb.append('|');
		sb.append(ip);
		sb.append('|');
		sb.append(GetUASign(user_agent));
		sb.append('|');
		sb.append(System.currentTimeMillis());
		return encrypt(sb.toString());
	}

	/**
	 * cookie中UA标识， 包括操作系统名称、浏览器名称，浏览器版本号
	 * 
	 * @param user_agent
	 * @return
	 */
	public static String GetUASign(String user_agent) {
		UserAgent ua = UserAgent.parseUserAgentString(user_agent);
		if (ua == null)
			return "";
		StringBuilder sb = new StringBuilder();
		sb.append(ua.getOperatingSystem().name());
		sb.append("#");
		sb.append(ua.getBrowser().getName());
		sb.append("#");
		sb.append(ua.getBrowserVersion().getVersion());
		return sb.toString();
	}

	/**
	 * 从cookie中读取保存的用户信息 FIXME: 此方法可能导致严重的安全隐患
	 * 
	 * @param req
	 * @return
	 */
	public IUser userFromUUID(String uuid) {
		if (StringUtils.isBlank(uuid))
			return null;
		String ck = decrypt(uuid);
		final String[] items = StringUtils.split(ck, '|');
		if (items != null && items.length == 4) {
			// String ua = _CleanUserAgent(header("user-agent"));
			// int ua_code = ua.hashCode();
			// int old_ua_code = Integer.parseInt(items[3]);
			// if(ua_code == old_ua_code){

			// 验证UA，当操作系统和浏览器版本都相同的时候，此法验证会失败
			String req_ua_sign = GetUASign(header("user-agent"));
			if (!req_ua_sign.equals(items[2]))
				return null;
			return new IUser() {
				public boolean IsBlocked() {
					return false;
				}

				public long getId() {
					return NumberUtils.toLong(items[0], -1L);
				}

				public byte getRole() {
					return IUser.ROLE_GENERAL;
				}
			};
			// }
		}
		return null;
	}

	public final static String COOKIE_LOGIN = "oscid";
	private final static int MAX_AGE = 86400 * 365;
	private final static byte[] E_KEY = new byte[] { 'R', 'U', 'N', 'J', 'S',
			'.', 'C', 'N' };
}
