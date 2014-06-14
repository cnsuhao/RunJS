package net.oschina.runjs.beans;

import net.oschina.common.db.POJO;
import net.oschina.common.db.QueryHelper;

/**
 * 持久化对象的基类
 * 
 * @author liudong
 */
public abstract class Pojo extends POJO {

	protected final static transient int MODIFY_TIMEOUT_1 = 3600; // 一个小时
	protected final static transient int MODIFY_TIMEOUT_2 = 604800;// 一周

	public final static transient String CACHE_1_HOUR = "1h";
	public final static transient String CACHE_1_DATE = "1d";
	public final static transient String CACHE_10_SENDS = "10s";

	/**
	 * 更新某个字段值
	 * 
	 * @param field
	 * @param value
	 * @return
	 */
	public boolean UpdateField(String field, Object value) {
		String sql = "UPDATE " + TableName() + " SET " + field
				+ " = ? WHERE id=?";
		return Evict(QueryHelper.update(sql, value, getId()) == 1);
	}

	@Override
	protected boolean IsObjectCachedByID() {
		return true;
	}
}
