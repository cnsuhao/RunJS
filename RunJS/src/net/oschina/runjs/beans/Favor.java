package net.oschina.runjs.beans;

import java.sql.Timestamp;
import java.util.List;

import net.oschina.common.db.QueryHelper;

/**
 * 用户收藏
 * 
 * @author jack
 * 
 */
public class Favor extends Pojo {

	public static final String FAVOR_LIST = "favors#user#";
	public static final String FAVOR_USER_CODE = "favor#uc#";
	public static final String FAVOR_COUNT = "favor_Count#code#";
	public static final int NEWER = 1;
	public static final int NOTNEW = 0;
	public static Favor INSTANCE = new Favor();
	private long code;
	private long user;
	private Timestamp create_time;
	private String code_ident;
	private int status;

	/**
	 * 用户收藏列表
	 * 
	 * @param user
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Favor> getFavorListByUser(long user) {
		String sql = "SELECT id FROM " + this.TableName() + " WHERE user = ?";
		return this.LoadList(QueryHelper.query_cache(Favor.class,
				this.CacheRegion(), FAVOR_LIST + user, sql, user));
	}

	/**
	 * 通过用户和代码查询收藏
	 * 
	 * @param uid
	 * @param cid
	 * @return
	 */
	public Favor getFavorByCodeAndUser(long uid, long cid) {
		String sql = "SELECT id FROM " + this.TableName()
				+ " WHERE user = ? AND code = ? ";
		return QueryHelper.read_cache(Favor.class, this.CacheRegion(),
				FAVOR_USER_CODE + uid + "#" + cid, sql, uid, cid);
	}



	/**
	 * 判断收藏是否有更新
	 * 
	 * @return
	 */
	public boolean IsNew() {
		return NEWER == this.getStatus();
	}

	/**
	 * 通知所有指定代码的收藏者有更新
	 * 
	 * @param id
	 */
	public void notifyAllFavors(long code_id) {
		List<Favor> f_l = this.getFavorListByUser(code_id);
		if (null != f_l && !f_l.isEmpty())
			for (Favor f : f_l) {
				f.UpdateField("status", NEWER);
			}
	}
	/**
	 * 清除缓存
	 * @param user
	 * @param code_id
	 * @param favor
	 */
	public void evictCache(User user, long code_id) {
		Favor.evictCache(this.CacheRegion(),
				Favor.FAVOR_LIST + user.getId());
		Favor.evictCache(this.CacheRegion(),
				Favor.FAVOR_USER_CODE + user.getId() + "#" + code_id);
		Favor.evictCache(this.CacheRegion(), Favor.FAVOR_COUNT + code_id);
	}
	
	@Override
	protected String TableName() {
		return "osc_favors";
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

	public String getCode_ident() {
		return code_ident;
	}

	public void setCode_ident(String code_ident) {
		this.code_ident = code_ident;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

}