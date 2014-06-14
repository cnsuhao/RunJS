package net.oschina.runjs.beans;

import java.sql.Timestamp;
import java.util.List;

import net.oschina.common.db.QueryHelper;

public class Catalog extends Pojo {
	public static Catalog INSTANCE = new Catalog();

	private long id;
	private long user;
	private String name;
	private long parent;
	private Timestamp create_time;
	public static final transient String CATALOG_LIST = "catalog#user#";

	public Catalog GetCatalogByName(long user, String name) {
		String sql = "SELECT * FROM " + this.TableName()
				+ " WHERE user = ? AND name = ?";
		return QueryHelper.read(Catalog.class, sql, user, name);
	}

	/**
	 * 判断是否存在某个名字的分类
	 * 
	 * @param user
	 * @param name
	 * @return
	 */
	public boolean IsCatalogExist(long user, String name) {
		return null != this.GetCatalogByName(user, name);
	}

	/**
	 * 列出用户的所有分类
	 * 
	 * @param uid
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Catalog> listCatalogByUser(long uid) {
		if (uid < 0)
			return null;
		else {
			String sql = "SELECT id FROM " + this.TableName()
					+ " WHERE user = ? ORDER BY create_time DESC";
			List<Long> ids = QueryHelper.query_cache(long.class,
					this.CacheRegion(), CATALOG_LIST + uid, sql, uid);
			return this.LoadList(ids);
		}
	}

	@Override
	public long Save() {
		// 清除分类列表缓存
		Catalog.evictCache(this.CacheRegion(), Catalog.CATALOG_LIST + this.user);
		return super.Save();
	}
	@Override
	public boolean Delete() {
		Catalog.evictCache(this.CacheRegion(), Catalog.CATALOG_LIST + this.user);
		return super.Delete();
	}
	@Override
	protected String TableName() {
		return "osc_catalogs";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getParent() {
		return parent;
	}

	public void setParent(long parent) {
		this.parent = parent;
	}

	public Timestamp getCreate_time() {
		return create_time;
	}

	public void setCreate_time(Timestamp create_time) {
		this.create_time = create_time;
	}

	public long getUser() {
		return user;
	}

	public void setUser(long user) {
		this.user = user;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

}