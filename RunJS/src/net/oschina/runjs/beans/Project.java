package net.oschina.runjs.beans;

import java.sql.Timestamp;
import java.util.List;

import net.oschina.common.db.QueryHelper;

/**
 * 项目
 * 
 * @author jack
 * 
 */
public class Project extends Pojo {
	public static final String USER_PRO_LIST = "#prolist#uid#";
	public static final Project INSTANCE = new Project();
	public static final int INIT_VERSION = 1;
	public static final int STATUS_POST = 1;
	public static final String FORK_PROJECTS = "forkpros#cid#";

	private long id;
	private long user;
	private String name;
	private int version;
	private Timestamp create_time;
	private Timestamp update_time;
	private int status;

	/**
	 * 根据用户id加载项目列表
	 * 
	 * @param uid
	 *            用户id
	 */
	@SuppressWarnings("unchecked")
	public List<Project> listProjectByUserId(long uid) {
		String sql = "SELECT id FROM " + INSTANCE.TableName()
				+ " WHERE user = ? ORDER BY create_time DESC";
		return INSTANCE.LoadList(QueryHelper.query_cache(long.class,
				CacheRegion(), USER_PRO_LIST + uid, sql, uid));
	}

	/**
	 * 列出所有已发布的项目
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Project> listAllPostProject() {
		String sql = "SELECT id FROM " + INSTANCE.TableName()
				+ " WHERE status = ? ORDER BY create_time DESC";
		return INSTANCE.LoadList(QueryHelper.query(long.class, sql,
				Project.STATUS_POST));
	}

	/**
	 * 获取从指定代码fork的代码列表（每个项目只列出最大的版本号的code）
	 * 
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Project> getAllForkProject(long cid) {
		if (cid <= 0)
			return null;
		String sql = "SELECT DISTINCT project FROM "
				+ Code.INSTANCE.TableName()
				+ " WHERE fork = ? ORDER BY create_time DESC";
		return this.LoadList(QueryHelper.query_cache(long.class,
				this.CacheRegion(), FORK_PROJECTS + cid, sql, cid));
	}

	/**
	 * 判断指定用户是否存在指定的项目。
	 * 
	 * @param name
	 * @param user
	 * @return
	 */
	public static boolean isProjectExist(String name, long user) {
		String sql = "SELECT COUNT(id) FROM " + INSTANCE.TableName()
				+ " WHERE user = ? AND name = ?";
		int count = (int) QueryHelper.stat(sql, user, name);
		return 0 == count ? false : true;
	}

	@Override
	protected String TableName() {
		return "osc_projects";
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

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public Timestamp getCreate_time() {
		return create_time;
	}

	public void setCreate_time(Timestamp create_time) {
		this.create_time = create_time;
	}

	public Timestamp getUpdate_time() {
		return update_time;
	}

	public void setUpdate_time(Timestamp update_time) {
		this.update_time = update_time;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

}
