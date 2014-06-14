package net.oschina.runjs.beans;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import net.oschina.common.db.QueryHelper;

/**
 * 动态
 * 
 * @author jack
 * 
 */
public class Dynamic extends Pojo {
	public static final Dynamic INSTANCE = new Dynamic();
	// 加入RunJS
	public transient static final int JOIN_RUNJS = 0;
	// 发布代码
	public transient static final int POST_CODE = 1;
	// 评论代码
	public transient static final int COMMENT_CODE = 2;
	// Fork代码
	public transient static final int FORK_CODE = 3;
	// 顶代码
	public transient static final int UP_CODE = 4;

	public transient static final String CACHE_USER_DY = "user#dy#";
	public transient static final String CACHE_ALL_DY = "all#dy#";
	private long user;
	/**
	 * refer指向动态类型的id 加入RunJS user id 发布代码：code id 评论代码：comment id
	 * Fork代码：fork后的code id 顶代码：code id
	 */
	private long refer;
	private int type;
	private Timestamp create_time;

	/**
	 * 分页获取动态列表
	 * 
	 * @param uid
	 * @param page
	 * @param count
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Dynamic> getDynamicList(long uid, int page, int count) {
		String sql = "SELECT id FROM " + this.TableName()
				+ " WHERE user=? ORDER BY create_time DESC";
		// query_slice_cache有一个100大小的伪分页
		List<Long> ids = QueryHelper.query_slice_cache(long.class,
				this.CacheRegion(), "CACHE_USER_DY" + uid, 100, sql, page,
				count, uid);
		return this.LoadList(ids);
	}

	/**
	 * 查询所有非加入RunJS的动态列表 示例sql ：
	 * SELECT d.* FROM (SELECT * FROM osc_dynamics WHERE
	 * `type` IN (1,2,3,4) ORDER BY id DESC LIMIT 1,50) d, osc_users u WHERE
	 * d.user = u.id GROUP BY d.user ORDER BY d.create_time DESC LIMIT 1,15;
	 * 
	 * @param page
	 * @param count
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Dynamic> GetAllDynamicList(int page, int count) {
		String sql = "SELECT d.id FROM (SELECT * FROM "
				+ this.TableName()
				+ " WHERE `type` IN (?,?,?,?) ORDER BY id DESC LIMIT 1,500) d, "
				+ User.INSTANCE.TableName()
				+ " u WHERE d.user = u.id GROUP BY d.user ORDER BY d.create_time DESC";
		List<Long> ids = QueryHelper.query_slice_cache(long.class, "3min",
				CACHE_ALL_DY + page + "#" + count, 100, sql, page, count,
				POST_CODE, COMMENT_CODE, FORK_CODE, UP_CODE);
		return this.LoadList(ids);
	}

	/**
	 * 重写Save，保存之前清除缓存
	 */
	@Override
	public long Save() {
		Dynamic.evictCache(this.CacheRegion(), Dynamic.CACHE_USER_DY
				+ this.user);
		return super.Save();
	}

	/**
	 * 添加加入RunJS动态
	 * 
	 * @param user
	 * @return
	 */
	public boolean add_join_dy(User user) {
		Dynamic d = new Dynamic();
		d.setCreate_time(new Timestamp(new Date().getTime()));
		d.setRefer(user.getId());
		d.setType(Dynamic.JOIN_RUNJS);
		d.setUser(user.getId());
		if (0 != d.Save())
			return true;
		else
			return false;
	}

	/**
	 * 添加发布代码动态
	 * 
	 * @param code
	 * @return
	 */
	public boolean add_post_dy(Code code) {
		Dynamic d = new Dynamic();
		d.setCreate_time(new Timestamp(new Date().getTime()));
		d.setRefer(code.getId());
		d.setType(Dynamic.POST_CODE);
		d.setUser(code.getUser());
		if (0 != d.Save())
			return true;
		else
			return false;
	}

	/**
	 * 添加评论动态
	 * 
	 * @param comment
	 * @return
	 */
	public boolean add_comment_dy(Comment comment) {
		Dynamic d = new Dynamic();
		d.setCreate_time(new Timestamp(new Date().getTime()));
		d.setRefer(comment.getId());
		d.setType(Dynamic.COMMENT_CODE);
		d.setUser(comment.getUser());
		if (0 != d.Save())
			return true;
		else
			return false;
	}

	/**
	 * 添加fork动态
	 * 
	 * @param code
	 * @return
	 */
	public boolean add_fork_dy(Code code) {
		Dynamic d = new Dynamic();
		d.setCreate_time(new Timestamp(new Date().getTime()));
		d.setRefer(code.getId());
		d.setType(Dynamic.FORK_CODE);
		d.setUser(code.getUser());
		if (0 != d.Save())
			return true;
		else
			return false;
	}

	/**
	 * 添加顶动态
	 * 
	 * @param user
	 * @param code
	 * @return
	 */
	public boolean add_up_dy(User user, Code code) {
		Dynamic d = new Dynamic();
		d.setCreate_time(new Timestamp(new Date().getTime()));
		d.setRefer(code.getId());
		d.setType(Dynamic.UP_CODE);
		d.setUser(user.getId());
		if (0 != d.Save())
			return true;
		else
			return false;
	}

	public long getUser() {
		return user;
	}

	public void setUser(long user) {
		this.user = user;
	}

	public long getRefer() {
		return refer;
	}

	public void setRefer(long refer) {
		this.refer = refer;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Timestamp getCreate_time() {
		return create_time;
	}

	public void setCreate_time(Timestamp create_time) {
		this.create_time = create_time;
	}

	@Override
	protected String TableName() {
		return "osc_dynamics";
	}
}