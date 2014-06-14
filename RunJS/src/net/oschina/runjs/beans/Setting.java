package net.oschina.runjs.beans;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.oschina.common.db.QueryHelper;

/**
 * 用户设置项
 * 
 * @author jack
 * 
 */
public class Setting extends Pojo {
	public static final String USER_SET_LIST = "#user#set#";

	// 设置项key的白名单，防止用户恶意的插入很多值
	public static final String SETTING_LIST = "theme,fontsize,fontfamily,library,plugins";

	public static Setting INSTANCE = new Setting();
	private long user;
	private String name;
	private String value;
	private Timestamp create_time;

	/**
	 * 获取所有设置项
	 * 
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Setting> getAllSettingsByUser(long id) {
		String sql = "SELECT id FROM " + TableName() + " WHERE user = ?";
		return this.LoadList(QueryHelper.query_cache(long.class,
				this.CacheRegion(), USER_SET_LIST + id, sql, id));
	}

	/**
	 * 通过uid和key拿value
	 * 
	 * @param uid
	 * @param key
	 * @return
	 */
	public static String GetValue(long uid, String key) {
		List<Setting> s_l = Setting.INSTANCE.getAllSettingsByUser(uid);
		if (s_l == null)
			return null;
		Map<String, String> s_m = new HashMap<String, String>();
		for (Setting setting : s_l) {
			s_m.put(setting.getName(), setting.getValue());
		}
		return s_m.get(key);
	}

	/**
	 * 根据用户和设置项类型加载设置项
	 * 
	 * @param type
	 * @param id
	 * @return
	 */
	public Setting getSettingByType(String name, long id) {
		String sql = "SELECT * FROM " + TableName()
				+ " WHERE user = ? AND name = ?";
		return QueryHelper.read(Setting.class, sql, id, name);
	}

	public long getUser() {
		return user;
	}

	public void setUser(long user) {
		this.user = user;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Timestamp getCreate_time() {
		return create_time;
	}

	public void setCreate_time(Timestamp create_time) {
		this.create_time = create_time;
	}

	@Override
	protected String TableName() {
		return "osc_settings";
	}
}
