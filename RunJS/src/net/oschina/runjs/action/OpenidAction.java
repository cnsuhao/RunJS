package net.oschina.runjs.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

import net.oschina.common.cache.CacheManager;
import net.oschina.common.servlet.Annotation.JSONOutputEnabled;
import net.oschina.common.servlet.RequestContext;
import net.oschina.common.utils.FormatTool;
import net.oschina.common.utils.HttpConnManager;
import net.oschina.common.utils.ResourceUtils;
import net.oschina.runjs.beans.Dynamic;
import net.oschina.runjs.beans.User;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.brickred.socialauth.AuthProvider;
import org.brickred.socialauth.Profile;
import org.brickred.socialauth.SocialAuthConfig;
import org.brickred.socialauth.SocialAuthManager;
import org.brickred.socialauth.util.OAuthConfig;
import org.brickred.socialauth.util.SocialAuthUtil;

/**
 * 用于OpenID登录
 * 
 * @author Winter Lau
 * @date 2011-1-3 下午10:42:34
 */
public class OpenidAction {

	private final static String AFTER_BIND_URL = "http://runjs.cn/action/openid/after_login";
	private final static String SUCCESS_URL = "/login";

	private final static String SOCIAL_AUTH_CACHE = "1h";
	private final static String SOCIAL_AUTH_KEY = "socialauth_id";
	private final static SocialAuthConfig config;

	static {
		config = SocialAuthConfig.getDefault();

		try {
			config.load();
			// 加载扩展的登录provider
			for (String provider_name : Arrays.asList("oschina", "weibo",
					"github", "google", "qq")) {
				String className = config.getApplicationProperties()
						.getProperty(provider_name);
				String consumer_key = config.getApplicationProperties()
						.getProperty(provider_name + ".consumer_key");
				String consumer_secret = config.getApplicationProperties()
						.getProperty(provider_name + ".consumer_secret");
				String custom_permissions = config.getApplicationProperties()
						.getProperty(provider_name + ".custom_permissions");
				OAuthConfig c = new OAuthConfig(consumer_key, consumer_secret);
				if (custom_permissions != null)
					c.setCustomPermissions(custom_permissions);
				c.setProviderImplClass(Class.forName(className));
				config.addProviderConfig(provider_name, c);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 绑定帐号认证返回处理
	 * 
	 * @param ctx
	 * @throws ServletException
	 * @throws Exception
	 */
	@JSONOutputEnabled
	public void after_login(RequestContext ctx) throws IOException,
			ServletException {
		// check user
		User user = (User) ctx.user();
		if (null != user) {
			ctx.redirect(ctx.contextPath() + "/");
			return;
		}
		// get the auth provider manager from session
		SocialAuthManager manager = _getSocialAuthManager(ctx);
		if (manager == null) {
			System.err.println(ctx.ip()
					+ " ---------------> SocialAuthManager is Null "
					+ ctx.user_agent());
			ctx.redirect(ctx.contextPath() + "/");
			return;
		}
		Profile p = null;
		AuthProvider provider = null;
		try {
			// call connect method of manager which returns the provider object.
			// Pass request parameter map while calling connect method.
			provider = manager.connect(SocialAuthUtil
					.getRequestParametersMap(ctx.request()));
			// get profile
			p = provider.getUserProfile();
		} catch (Exception e) {
			e.printStackTrace();
			ctx.print("Connection error! Check if you can connect to  the OAuth provider");
			return;
			// throw ctx.error("connect_error");
		}
		if (p == null) {
			throw ctx.error("profile_error");
		}

		String email = p.getEmail();

		if (StringUtils.isBlank(email))
			email = p.getValidatedId();
		if (email == null) {
			throw ctx.error("no_email");
		}

		String name = p.getFullName();
		if (StringUtils.isBlank(name))
			name = p.getDisplayName();
		if (StringUtils.isBlank(name))
			name = getName(email, p.getFirstName(), p.getLastName());
		if (StringUtils.isBlank(name))
			name = ResourceUtils.getString("description", "default_name",
					p.getProviderId());
		String avatar = p.getProfileImageURL();

		// profile的location存储的是oschina的个人空间地址，weibo是微博地址，github是github个人主页，google是google
		// plus地址。
		String blog = null;
		if (p.getProviderId().equals("oschina")
				|| p.getProviderId().equals("weibo")
				|| p.getProviderId().equals("github")
				|| p.getProviderId().equals("google"))
			blog = p.getLocation();

		// 查询是否为新用户
		user = User.INSTANCE.getUserByAccount(p.getProviderId(), email);

		if (user == null) {
			// 新用户，将其信息插入数据库（email单一）
			User new_u = new User();
			new_u.setType(p.getProviderId());
			// 如果account是email则将email填进去
			if (FormatTool.is_email(email))
				new_u.setEmail(email);
			// account是唯一标识（同一个来源的时候）
			new_u.setAccount(email);
			new_u.setName(name);
			new_u.setBlog(blog);
			new_u.setCreate_time(new Timestamp(new Date().getTime()));
			new_u.setOnline(User.ONLINE);
			new_u.setRole(User.ROLE_GENERAL);
			long uid = new_u.Save();
			/*
			 * if (p.getProviderId().equals(User.FROM_WEIBO)) try {
			 * provider.updateStatus(ResourceUtils.getString( "description",
			 * "weibo_login_hello")); } catch (Exception e) {
			 * e.printStackTrace(); } else if
			 * (p.getProviderId().equals(User.FROM_QQ)) try {
			 * provider.updateStatus(ResourceUtils.getString( "description",
			 * "qq_login_hello")); } catch (Exception e) { e.printStackTrace();
			 * } else if (p.getProviderId().equals(User.FROM_OSCHINA)) try {
			 * provider.updateStatus(ResourceUtils.getString( "description",
			 * "osc_tweet")); } catch (Exception e1) { e1.printStackTrace(); }
			 */
			if (0 < uid) {
				Dynamic.INSTANCE.add_join_dy(new_u);
				// 保存头像
				if (StringUtils.isNotBlank(avatar))
					new_u.UpdateField("portrait", savePortrait(uid, avatar));
				setLoginCookie(ctx, new_u);
				ctx.redirect(ctx.contextPath() + SUCCESS_URL);
				return;
			}
		} else {
			// 已有用户
			user.UpdateField("online", User.ONLINE);
			setLoginCookie(ctx, user);
			ctx.redirect(ctx.contextPath() + SUCCESS_URL);
			return;
		}
	}

	/**
	 * 设置登录cookie
	 * 
	 * @param ctx
	 * @param new_u
	 * @throws IOException
	 */
	public void setLoginCookie(RequestContext ctx, User new_u)
			throws IOException {
		String new_value = RequestContext._GenLoginKey(new_u, ctx.ip(),ctx.header("user-agent"), new_u.getIdent());
		// 此处要加上HTTPOnly以防js可以取到cookie。cookie的有效期为session。
		Cookie oscid = new Cookie(RequestContext.COOKIE_LOGIN, new_value);
		oscid.setHttpOnly(true);
		oscid.setPath("/");
		// 有效期为1年
		oscid.setMaxAge(86400 * 365);
		ctx.response().addCookie(oscid);

		// ctx.header("Set-Cookie", RequestContext.COOKIE_LOGIN + "=" +
		// new_value
		// + "; path=/;HTTPOnly");
	}

	/**
	 * 获取姓名
	 * 
	 * @param email
	 *            邮箱地址
	 * @param fn
	 *            名
	 * @param ln
	 *            姓
	 * @return
	 */
	private String getName(String email, String fn, String ln) {
		String name = "";
		if (StringUtils.isNotBlank(fn) && StringUtils.isNotBlank(ln)) {
			if (StringUtils.equals(fn, ln))
				name = fn;
			else {
				if (fn.length() < ln.length())
					name = fn + ln;
				else
					name = ln + fn;
			}
		} else {
			if (StringUtils.isNotBlank(ln))
				name = ln;
			if (StringUtils.isNotBlank(fn))
				name += fn;
		}
		if (StringUtils.isBlank(name) && FormatTool.is_email(email))
			name = email.substring(0, email.indexOf('@'));

		return name;
	}

	/**
	 * 准备跳转到认证网站
	 * 
	 * @param ctx
	 * @param op
	 * @param url
	 * @throws Exception
	 */
	private void _saveManagerAndGo(RequestContext ctx, String op, String url)
			throws Exception {
		SocialAuthManager manager = new SocialAuthManager();
		manager.setSocialAuthConfig(config);
		// get Provider URL to which you should redirect for authentication.
		String auth_url = manager.getAuthenticationUrl(op, url);
		// Store in session
		String socialauth_id = RandomStringUtils.randomAlphanumeric(20);
		ctx.deleteCookie(SOCIAL_AUTH_KEY, false);
		ctx.cookie(SOCIAL_AUTH_KEY, socialauth_id, -1, false);
		CacheManager.set(SOCIAL_AUTH_CACHE, socialauth_id, manager);
		// ctx.session(true).setAttribute(AUTH_MANAGER_SESSION_KEY, manager);
		// Redirect to the URL
		ctx.response().setContentType("text/html");
		String html = "<p>Redirecting...</p><script type='text/javascript'>location.href='"
				+ auth_url + "';</script>";
		ctx.print(html);
	}

	private SocialAuthManager _getSocialAuthManager(RequestContext ctx) {
		Cookie ck = ctx.cookie(SOCIAL_AUTH_KEY);
		String socialauth_id = (ck != null) ? ck.getValue() : null;
		try {
			return (socialauth_id != null) ? (SocialAuthManager) CacheManager
					.get(SOCIAL_AUTH_CACHE, socialauth_id) : null;
		} finally {
			ctx.deleteCookie(SOCIAL_AUTH_KEY, false);
		}
	}

	/**
	 * 
	 * 跳到provider的登录界面
	 * 
	 * @param ctx
	 * @throws Exception
	 */
	public void before_login(RequestContext ctx) throws Exception {
		String op = ctx.param("op", "oschina");
		if (op.equals(User.FROM_GITHUB) || op.equals(User.FROM_OSCHINA)
				|| op.equals(User.FROM_WEIBO) || op.equals(User.FROM_GOOGLE)
				|| op.equals(User.FROM_HOTMAIL) || op.equals(User.FROM_YAHOO)
				|| op.equals(User.FROM_QQ))
			_saveManagerAndGo(ctx, op, AFTER_BIND_URL);
		else
			ctx.redirect("/");
	}

	/**
	 * 由用户id和图片地址保存用户头像
	 * 
	 * @param uid
	 * @param img_url
	 * @throws IOException
	 */
	public String savePortrait(long uid, String img_url) {
		String portrait_path = "/uploads/user_space/";
		String user_dir = String.valueOf(uid % 500) + "/" + String.valueOf(uid)
				+ "/";
		// 最终的的存储路径是这样的：webapp/uploads/resource_files/123/623/xxx.js

		File fileDest = new File(RequestContext.root() + portrait_path
				+ user_dir + uid + "_100.jpg");

		HttpClient hc = HttpConnManager.getHttpClient();
		try {
			HttpGet httpget = new HttpGet(img_url);
			HttpResponse response = hc.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				if (!fileDest.exists()) {
					fileDest.getParentFile().mkdirs();
					fileDest.createNewFile();
				}
				InputStream in = entity.getContent();
				OutputStream os = new FileOutputStream(fileDest);
				int count = IOUtils.copy(in, os);
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(os);
				if (0 != count)
					return portrait_path + user_dir + uid + "_100.jpg";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
}
