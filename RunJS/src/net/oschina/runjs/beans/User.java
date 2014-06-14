package net.oschina.runjs.beans;

import java.sql.Timestamp;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.oschina.common.db.QueryHelper;
import net.oschina.common.servlet.IUser;
import net.oschina.common.servlet.RequestContext;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 用户
 * 
 * @author jack
 * 
 */
public class User extends Pojo implements IUser {

	// 帐号来源
	public static final transient String FROM_OSCHINA = "oschina";
	public static final transient String FROM_WEIBO = "weibo";
	public static final transient String FROM_GITHUB = "github";
	public static final transient String FROM_QQ = "qq";
	public static final transient String FROM_GOOGLE = "google";
	public static final transient String FROM_HOTMAIL = "hotmail";
	public static final transient String FROM_YAHOO = "yahoo";

	// 在线状态
	public final static transient byte ONLINE = 0x01;
	public final static transient byte OFFLINE = 0x00;
	public static final User INSTANCE = new User();

	// 用户帐户，同一个来源没有相同的account，用以表示不同来源的用户的唯一性
	private String account;

	private String name;
	private String email;
	private String ident;
	private Timestamp create_time;
	private int status;
	private int email_validated;
	private int online;
	// 帐号来源
	private String type;
	private byte role = User.ROLE_GENERAL;
	private String portrait;

	private String space_url;
	private String blog;

	/**
	 * 判断是否为后台用户
	 * 
	 * @return
	 */
	public boolean IsSuperior() {
		return role >= ROLE_EDITOR;
	}

	/**
	 * 判断用户是否有权进行某项操作
	 * 
	 * @param uid
	 * @return
	 */
	public boolean IsCurrentUser(String verify_code) {
		return verify_code.equalsIgnoreCase(GetVCode());
	}

	/**
	 * 用户提交action同时需要提交的验证身份的验证码 先计算sha256，再计算md5，最后取前20位
	 * 
	 * @return
	 */
	public String GetVCode() {
		// Calendar c = Calendar.getInstance();
		return DigestUtils.md5Hex(
				DigestUtils.sha256Hex(this.getId() + "#" + this.ident))
				.substring(0, 19);
	}

	/**
	 * 判断该用户是否有对某项目的管理权 项目所有者或者管理员
	 * 
	 * @param project
	 * @return
	 */
	public boolean IsAdmin(Project project) {
		if (null == project)
			return role >= ROLE_ADMIN;
		return (getId() == project.getUser()) || (role >= ROLE_ADMIN);
	}
	/**
	 * 判断该用户是否有对某插件的管理权F
	 * 
	 * @param project
	 * @return
	 */
	public boolean IsAdmin(Plugin plugin) {
		if (null == plugin)
			return role >= ROLE_ADMIN;
		return (getId() == plugin.getUser()) || (role >= ROLE_ADMIN);
	}
	/**
	 * 判断该用户是否有对某代码的管理权 代码所有者或者管理员
	 * 
	 * @param project
	 * @return
	 */
	public boolean IsAdmin(Code code) {
		if (null == code)
			return role >= ROLE_ADMIN;
		return (getId() == code.getUser()) || (role >= ROLE_ADMIN);
	}

	/**
	 * 根据Api验证身份代码
	 * 
	 * @param api
	 * @return
	 */
	public boolean validateUser(String verify_code, String api) {
		String old = String.valueOf(Math.abs(email.hashCode() + api.hashCode()
				+ ident.hashCode()));
		return StringUtils.equals(old, verify_code);
	}

	/**
	 * 判断该用户是否有对某评论的管理权 评论者，管理员，代码所有者
	 * 
	 * @param project
	 * @return
	 */
	public boolean IsAdmin(Comment c) {
		if (null == c)
			return role >= ROLE_ADMIN;
		Code code = Code.INSTANCE.Get(c.getCode());
		if (null == code)
			return role >= ROLE_ADMIN;
		return (getId() == c.getUser()) || (role >= ROLE_ADMIN)
				|| (getId() == code.getUser());
	}

	/**
	 * 判断该用户是否管理员
	 * 
	 * @param project
	 * @return
	 */
	public boolean IsAdmin() {
		return role >= ROLE_ADMIN;
	}

	/**
	 * 判断是否在线
	 * 
	 * @return
	 */
	public boolean IsOnline() {
		return online == ONLINE;
	}

	@SuppressWarnings("unchecked")
	public User getUserByName(String name) {
		String sql = "SELECT id FROM " + this.TableName() + " WHERE name= ? ";
		List<Long> ids = QueryHelper.query(long.class, sql, name);
		List<User> users = User.INSTANCE.LoadList(ids);
		if (null != users && 0 != users.size())
			return users.get(0);
		return null;
	}

	/**
	 * 通过email查询用户
	 * 
	 * @param email
	 * @return
	 */
	public User getUserByEmail(String email) {
		String sql = "SELECT * FROM " + this.TableName() + " WHERE email = ?";
		return QueryHelper.read(User.class, sql, email);
	}

	/**
	 * 查询用户
	 * 
	 * @param op
	 * @param account
	 * @return
	 */
	public User getUserByAccount(String op, String account) {
		String sql = "SELECT * FROM " + this.TableName()
				+ " WHERE type = ? AND account = ?";
		return QueryHelper.read(User.class, sql, op, account);
	}

	/**
	 * 返回当前登录用户的资料
	 * 
	 * @param req
	 * @return
	 */
	public static User GetLoginUser(HttpServletRequest req) {
		User loginUser = (User) req.getAttribute(G_USER);
		if (loginUser == null) {
			IUser cookie_user = RequestContext.get().getUserFromCookie();
			if (cookie_user == null)
				return null;
			User user = User.INSTANCE.Get(cookie_user.getId());
			if (user != null) {
				req.setAttribute(G_USER, user);
				return user;
			}
		}
		return loginUser;
	}

	/**
	 * 判断ident是否存在
	 * 
	 * @param id_ent
	 * @return
	 */
	public boolean isIdentExist(String id_ent) {
		String sql = "SELECT COUNT(id) FROM " + this.TableName()
				+ " WHERE ident = ?";
		if (0 != QueryHelper.stat(sql, id_ent))
			return true;
		return false;
	}

	/**
	 * 用户活跃度
	 * 
	 * @return
	 */
	public int ActiveDegree() {
		// TODO
		return 0;
	}

	/**
	 * 重写Save，在Save之前生成小写字符加数字的ident。
	 */
	@Override
	public long Save() {
		this.ident = RandomStringUtils.randomAlphanumeric(8).toLowerCase();
		while (isIdentExist(this.ident))
			this.ident = RandomStringUtils.randomAlphanumeric(8).toLowerCase();
		this.setSpace_url("/profile/" + this.ident);
		return super.Save();
	}

	@Override
	public byte getRole() {
		return this.role;
	}

	@Override
	public boolean IsBlocked() {
		return false;
	}

	@Override
	protected String TableName() {
		return "osc_users";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getIdent() {
		return ident;
	}

	public void setIdent(String ident) {
		this.ident = ident;
	}

	public Timestamp getCreate_time() {
		return create_time;
	}

	public void setCreate_time(Timestamp create_time) {
		this.create_time = create_time;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getEmail_validated() {
		return email_validated;
	}

	public void setEmail_validated(int email_validated) {
		this.email_validated = email_validated;
	}

	public int getOnline() {
		return online;
	}

	public void setOnline(int online) {
		this.online = online;
	}

	public void setRole(byte role) {
		this.role = role;
	}

	public String getPortrait() {
		return portrait;
	}

	public void setPortrait(String portrait) {
		this.portrait = portrait;
	}

	public static User getUserById(long id) {
		return User.INSTANCE.Get(id);
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSpace_url() {
		return space_url;
	}

	public void setSpace_url(String space_url) {
		this.space_url = space_url;
	}

	public String getBlog() {
		return blog;
	}

	public void setBlog(String blog) {
		this.blog = blog;
	}
}