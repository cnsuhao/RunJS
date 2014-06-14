package net.oschina.runjs.beans;

import java.sql.Timestamp;
import java.util.List;

import net.oschina.common.db.QueryHelper;

public class Plugin extends Pojo {
	public static Plugin INSTANCE = new Plugin();
	public static final String PLUGIN_CODE = "plugin#code";
	public static final String SYS_PLUGIN_LIST = "sysplugin";
	public static final String PLUGIN_LIST = "all_plugins";
	// 系统插件
	public static final int SYS_PLUGIN = 127;
	// 普通插件
	public static final int PLUGIN = 1;

	public static final int UNCHECK = 0;
	// 驳回
	public static final int REJECT = 1;
	public static final int CHECKED = 2;

	private int status;
	private int type;
	private long code;
	private long user;
	private Timestamp create_time;
	private Timestamp update_time;

	@Override
	protected String TableName() {
		return "osc_plugins";
	}

	/**
	 * 启用或者关闭插件
	 * 
	 * @param user
	 * @param turnup
	 */
	public void turn(User user, boolean turnup) {
		turn(user, turnup, true);
	}

	/**
	 * 启用或者关闭插件
	 * 
	 * @param user
	 * @param turnup
	 */
	@SuppressWarnings("unchecked")
	public void turn(User user, boolean turnup, boolean all) {
		if (user == null)
			return;
		if (user.IsAdmin() && all) {
			List<Plugin> sys_plugins = this.GetSysPlugins();
			if (null != sys_plugins)
				for (Plugin p : sys_plugins) {
					p.UpdateField("status", turnup ? Plugin.CHECKED
							: Plugin.UNCHECK);
					Plugin.evictCache(this.CacheRegion(),
							PLUGIN_CODE + p.getCode());
				}
		}
		String sql = "SELECT id FROM " + this.TableName() + " WHERE user = ?";
		List<Plugin> user_plugins = this.LoadList(QueryHelper.query(long.class,
				sql, user.getId()));
		if (null != user_plugins)
			for (Plugin p : user_plugins) {
				p.UpdateField("status", turnup ? Plugin.CHECKED
						: Plugin.UNCHECK);
				Plugin.evictCache(this.CacheRegion(), PLUGIN_CODE + p.getCode());
			}
		EvictListCache();
	}

	/**
	 * 列出所有已审核的系统插件
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Plugin> GetSysPlugins() {
		String sql = "SELECT id FROM " + this.TableName()
				+ " WHERE `type` = ? AND `status` = ? ORDER BY create_time";
		List<Long> ids = QueryHelper.query_cache(long.class,
				this.CacheRegion(), SYS_PLUGIN_LIST, sql, Plugin.SYS_PLUGIN,
				Plugin.CHECKED);
		return this.LoadList(ids);
	}

	/**
	 * 列出所有满足条件的插件
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Plugin> GetPlugins(int type, int status) {
		String sql = "SELECT id FROM " + this.TableName()
				+ " WHERE `type` = ? AND `status` = ? ORDER BY create_time";
		List<Long> ids = QueryHelper.query(long.class, sql, type, status);
		return this.LoadList(ids);
	}

	@SuppressWarnings("unchecked")
	public List<Plugin> GetEditPlugins(User user) {
		if (user == null || user.getId() <= 0)
			return null;
		String sql = "SELECT id FROM " + this.TableName()
				+ " WHERE `user`=? ORDER BY create_time";
		List<Long> ids = QueryHelper.query(long.class, sql, user.getId());
		List<Plugin> plugins = this.LoadList(ids);
		List<Plugin> sysPlugins = GetSysPlugins();
		if (sysPlugins != null && plugins != null) {
			sysPlugins.addAll(plugins);
		}
		return sysPlugins;
	}

	@SuppressWarnings("unchecked")
	public List<Plugin> GetUserPlugins(User user, int type, int status) {
		if (user == null || user.getId() <= 0)
			return null;
		String sql = "SELECT id FROM "
				+ this.TableName()
				+ " WHERE `type` = ? AND `status` = ? AND `user` = ? ORDER BY create_time";
		List<Long> ids = QueryHelper.query(long.class, sql, type, status,
				user.getId());
		return this.LoadList(ids);
	}

	@Override
	public boolean Delete() {
		EvictListCache();
		return super.Delete();
	}

	public void EvictListCache() {
		Plugin.evictCache(this.CacheRegion(), Plugin.PLUGIN_LIST);
		Plugin.evictCache(this.CacheRegion(), Plugin.SYS_PLUGIN_LIST);
	}

	@Override
	public long Save() {
		EvictListCache();
		return super.Save();
	}

	/**
	 * 列出所有插件
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Plugin> GetAllPlugins() {
		String sql = "SELECT id FROM " + this.TableName()
				+ " ORDER BY create_time";
		List<Long> ids = QueryHelper.query_cache(long.class,
				this.CacheRegion(), PLUGIN_LIST, sql);
		return this.LoadList(ids);
	}

	/**
	 * 判断代码是否为插件F
	 * 
	 * @param cid
	 * @return
	 */
	public static boolean IsPlugin(long cid) {
		return null != Plugin.INSTANCE.GetPluginByCode(cid);
	}

	/**
	 * 通过代码查询插件
	 * 
	 * @param cid
	 * @return
	 */
	public Plugin GetPluginByCode(long cid) {
		if (cid <= 0)
			return null;
		String sql = "SELECT * FROM " + this.TableName() + " WHERE code = ?";
		return QueryHelper.read_cache(Plugin.class, this.CacheRegion(),
				PLUGIN_CODE + cid, sql, cid);
	}

	public boolean IsChecked() {
		return status == CHECKED;
	}

	public boolean IsSysPlugin() {
		return type == SYS_PLUGIN;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public long getCode() {
		return code;
	}

	public void setCode(long code) {
		this.code = code;
	}

	public long getUser() {
		return user;
	}

	public void setUser(long user) {
		this.user = user;
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
}