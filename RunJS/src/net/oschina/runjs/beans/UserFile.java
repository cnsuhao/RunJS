package net.oschina.runjs.beans;

import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import net.oschina.common.db.QueryHelper;

/**
 * 用户资源文件
 * 
 * @author jack
 * 
 */
public class UserFile extends Pojo {

	public static final byte F_PIC = 1;
	public static final byte F_CSS = 2;
	public static final byte F_JS = 3;
	public static final byte F_JSON = 4;
	public static final byte F_XML = 5;
	public static final byte F_OTHER = 100;

	public static final String RESOURCE_PATH = "/uploads/rs/";
	// 图片格式字符串
	public static final String PIC_STR = "jpg,gif,bmp,jpeg,tiff,svg,png,ico";

	public static final String CACHE_FILES = "userfile#uid#";
	// 大小限制
	public static final long MAX_SIZE = 1024 * 1024;
	// 数量限制
	public static final int MAX_COUNT = 50;
	public static UserFile INSTANCE = new UserFile();
	private long user;
	private String name;
	private String path;
	private Timestamp create_time;
	private String hash;
	private String ident;
	// 文件类型
	private byte type;

	public static boolean IsUserFileExist(String name, long user) {
		String sql = "SELECT COUNT(id) FROM " + INSTANCE.TableName()
				+ " WHERE user = ? AND name = ?";
		int count = (int) QueryHelper.stat(sql, user, name);
		return 0 == count ? false : true;
	}

	@SuppressWarnings("unchecked")
	public UserFile getUserFileByName(long user, String name) {
		String sql = "SELECT id FROM " + INSTANCE.TableName()
				+ " WHERE user = ? AND name = ?";
		List<UserFile> c_l = INSTANCE.LoadList(QueryHelper.query(long.class,
				sql, user, name));
		if (null != c_l && c_l.size() >= 0)
			return c_l.get(0);
		return null;
	}

	/**
	 * 统计一个用户上传了几个文件
	 * 
	 * @param user
	 * @return
	 */
	public int getUserFileCount(long user) {
		String sql = "SELECT COUNT(*) FROM " + this.TableName()
				+ " WHERE user = ?";
		return (int) QueryHelper.stat(sql, user);
	}

	/**
	 * 用户文件列表
	 * 
	 * @param user
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UserFile> getUserFileList(long user, byte type) {
		String sql = "SELECT id FROM " + this.TableName() + " WHERE user = ?";
		if (0 != type) {
			sql += " AND type = ?";
			return this.LoadList(QueryHelper.query_cache(long.class,
					this.CacheRegion(), CACHE_FILES + user + "#" + type, sql,
					user, type));
		} else
			return this.LoadList(QueryHelper.query_cache(long.class,
					this.CacheRegion(), CACHE_FILES + user, sql, user));
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
	 * 生成用户资源文件存储路径
	 * 
	 * @param u
	 * @param f_name
	 * @return
	 */
	public static String generatePath(User u, UserFile user_file) {
		// 用户id对500取余，将用户分别划分在500个文件夹下。
		StringBuilder path = new StringBuilder();
		path.append(RESOURCE_PATH);
		path.append(String.valueOf(u.getId() % 500));
		path.append("/");
		path.append(u.getIdent());
		path.append("/");
		String f_name = user_file.getName();
		// 是否有中文或者其他控制字符，如果有就是用ident加扩展名作为文件名。
		if (!StringUtils.isAsciiPrintable(f_name)) {
			f_name = user_file.getIdent() + "."
					+ FilenameUtils.getExtension(f_name);
		}
		path.append(f_name);
		// 最终的的存储路径是这样的：/uploads/rs/123/xxxxxxxx/aaaaa.js
		return path.toString();
	}

	public long getUser() {
		return user;
	}

	public void setUser(long user) {
		this.user = user;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Timestamp getCreate_time() {
		return create_time;
	}

	public void setCreate_time(Timestamp create_time) {
		this.create_time = create_time;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getIdent() {
		return ident;
	}

	public void setIdent(String ident) {
		this.ident = ident;
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	@Override
	protected String TableName() {
		return "osc_files";
	}
}
