package net.oschina.common.db;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.oschina.common.cache.CacheManager;
import net.oschina.common.utils.Inflector;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.dbutils.DbUtils;

/**
 * 数据库对象的基类
 * @author Winter Lau
 * @date 2010-1-22 上午11:33:56
 */
public class POJO implements Serializable {

	protected final static transient char OBJ_COUNT_CACHE_KEY = '#';
	private long ___key_id;
	
	public long getId() { return ___key_id; }
	public void setId(long id) { this.___key_id = id; }

	private String __this_table_name;
	
	public static void evictCache(String cache, Serializable key) {
		CacheManager.evict(cache, key);
	}
	
	public static void setCache(String cache, Serializable key, Serializable value) {
		CacheManager.set(cache, key, value);
	}
	
	public static Object getCache(String cache, Serializable key) {
		return CacheManager.get(cache, key);
	}
	
	/**
	 * 分页列出所有对象
	 * @param page
	 * @param size
	 * @return
	 */
	public List<? extends POJO> List(int page, int size) {
		String sql = "SELECT * FROM " + TableName() + " ORDER BY id DESC";
		return QueryHelper.query_slice(getClass(), sql, page, size);
	}
	
	public List<? extends POJO> Filter(String filter, int page, int size) {
		String sql = "SELECT * FROM " + TableName() + " WHERE " + filter + " ORDER BY id DESC";
		return QueryHelper.query_slice(getClass(), sql, page, size);
	}

	/**
	 * 统计此对象的总记录数
	 * @return
	 */
	public int TotalCount(String filter) {
		return (int)QueryHelper.stat("SELECT COUNT(*) FROM " + TableName() + " WHERE " + filter);
	}

	/**
	 * 返回默认的对象对应的表名
	 * @return
	 */
	protected String TableName() {
		if(__this_table_name == null)
			__this_table_name = "osc_" + Inflector.getInstance().tableize(getClass());
		return __this_table_name;
	}

	/**
	 * 返回对象对应的缓存区域名
	 * @return
	 */
	public String CacheRegion() { return this.getClass().getSimpleName(); }
	
	/**
	 * 是否根据ID缓存对象，此方法对Get(long id)有效
	 * @return
	 */
	protected boolean IsObjectCachedByID() { return false; }
	
	/**
	 * 插入对象到数据库表中
	 * @return
	 */
	public long Save() {
		if(getId() > 0)
			_InsertObject(this);
		else
			setId(_InsertObject(this));
		if(this.IsObjectCachedByID())
			CacheManager.evict(CacheRegion(), OBJ_COUNT_CACHE_KEY);
		return getId();
	}

	/**
	 * 根据id主键删除对象
	 * @return
	 */
	public boolean Delete() {
		boolean dr = Evict(QueryHelper.update("DELETE FROM " + TableName() + " WHERE id=?", getId()) == 1);
		if(dr)
			CacheManager.evict(CacheRegion(), OBJ_COUNT_CACHE_KEY);		
		return dr;
	}

	/**
	 * 根据条件决定是否清除对象缓存
	 * @param er
	 * @return
	 */
	public boolean Evict(boolean er) {
		if(er && IsObjectCachedByID())
			CacheManager.evict(CacheRegion(), getId());
		return er;
	}
	
	/**
	 * 清除指定主键的对象缓存
	 * @param obj_id
	 */
	protected void Evict(long obj_id) {
		CacheManager.evict(CacheRegion(), obj_id);
	}

	/**
	 * 根据主键读取对象详细资料，根据预设方法自动判别是否需要缓存
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends POJO> T Get(long id) {
		if(id <= 0) return null;
		String sql = "SELECT * FROM " + TableName() + " WHERE id=?";
		boolean cached = IsObjectCachedByID();
		return (T)QueryHelper.read_cache(getClass(), cached?CacheRegion():null, id, sql, id);
	}
	
	public List<? extends POJO> BatchGet(List<Long> ids) {
		if(ids==null || ids.size()==0)
			return null;
		StringBuilder sql = new StringBuilder("SELECT * FROM " + TableName() + " WHERE id IN (");
		for(int i=1;i<=ids.size();i++) {
			sql.append('?');
			if(i < ids.size())
				sql.append(',');
		}
		sql.append(')');
		List<? extends POJO> beans = QueryHelper.query(getClass(), sql.toString(), ids.toArray(new Object[ids.size()]));
		if(IsObjectCachedByID()){
			for(Object bean : beans){
				CacheManager.set(CacheRegion(), ((POJO)bean).getId(), (Serializable)bean);
			}
		}
		return beans;
	}
	
	/**
	 * 统计此对象的总记录数
	 * @return
	 */
	public int TotalCount() {
		if(this.IsObjectCachedByID()){
			return (int)QueryHelper.stat_cache(CacheRegion(),OBJ_COUNT_CACHE_KEY, 
					"SELECT COUNT(*) FROM " + TableName());			
		}
		return (int)QueryHelper.stat("SELECT COUNT(*) FROM " + TableName());
	}

	/**
	 * 批量加载项目
	 * @param pids
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List LoadList(List p_pids) {
		if(p_pids==null)
			return null;
		
		if(p_pids.size()==0){
            return new ArrayList();
        }
		
		final List<Long> pids = new ArrayList<Long>(p_pids.size());
		for(Number obj : (List<Number>)p_pids){
			pids.add(obj.longValue());
		}
		String cache = this.CacheRegion();
		List<POJO> prjs = new ArrayList<POJO>(pids.size()){{for(int i=0;i<pids.size();i++)add(null);}};
		List<Long> no_cache_ids = new ArrayList<Long>();
		for(int i=0;i<pids.size();i++) {
			long pid = pids.get(i);
			POJO obj = (POJO)CacheManager.get(cache, pid);
			
			if(obj != null)
				prjs.set(i, obj);
			else{
				no_cache_ids.add(pid);
			}
		}
		
		if(no_cache_ids.size()>0){
			List<? extends POJO> no_cache_prjs = BatchGet(no_cache_ids);
			if(no_cache_prjs != null)
			for(POJO obj : no_cache_prjs){
				prjs.set(pids.indexOf(obj.getId()), obj);
			}			
		}
		
		no_cache_ids = null;
		
		return prjs;
	}
	
	/**
	 * 插入对象
	 * @param obj
	 * @return 返回插入对象的主键
	 */
	private long _InsertObject(POJO obj) {		
		Map<String, Object> pojo_bean = obj.ListInsertableFields();		
		String[] fields = pojo_bean.keySet().toArray(new String[pojo_bean.size()]);
		StringBuilder sql = new StringBuilder("INSERT INTO ") ;
		sql.append(obj.TableName());
		sql.append('(');
		for(int i=0;i<fields.length;i++){
			if(i > 0) sql.append(',');
			sql.append(fields[i]);
		}
		sql.append(") VALUES(");
		for(int i=0;i<fields.length;i++){
			if(i > 0) sql.append(',');
			sql.append('?');
		}
		sql.append(')');
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = QueryHelper.getConnection().prepareStatement(sql.toString(), PreparedStatement.RETURN_GENERATED_KEYS);		
			for(int i=0;i<fields.length;i++){
				ps.setObject(i+1, pojo_bean.get(fields[i]));
			}
			ps.executeUpdate();			
			if(getId() > 0)
				return getId();
			
			rs = ps.getGeneratedKeys();
			return rs.next()?rs.getLong(1):-1;
		}catch(SQLException e){
			throw new DBException(e);
		}finally{
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
			sql = null;
			fields = null;
			pojo_bean = null;
		}
	}
	
	/**
	 * 列出要插入到数据库的域集合，子类可以覆盖此方法
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Map<String, Object> ListInsertableFields() {
		try {
			Map<String, Object> props = BeanUtils.describe(this);
			if(getId() <= 0)
				props.remove("id");
			props.remove("class");
			return props ;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Exception when Fetching fields of " + this);
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		//不同的子类尽管ID是相同也是不相等的
		if(!getClass().equals(obj.getClass()))
			return false;
		POJO wb = (POJO) obj;
		return wb.getId() == getId();
	}
	
}
