package net.oschina.runjs.beans;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import net.oschina.common.db.QueryHelper;

/**
 * 代码评论
 * 
 * @author jack
 * 
 */
public class Comment extends Pojo {

	public static final String COMMENT_LIST = "comments#cid#";

	public static Comment INSTANCE = new Comment();
	private long code;
	private long user;
	private String content;
	private Timestamp create_time;
	private long id;

	/**
	 * 根据代码id获取所有的评论
	 * 
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Comment> getAllCommentByCode(long id) {
		if (id <= 0)
			return null;
		String sql = "SELECT id FROM " + this.TableName()
				+ " WHERE code = ? ORDER BY create_time DESC";
		return this.LoadList(QueryHelper.query_cache(long.class,
				this.CacheRegion(), COMMENT_LIST + id, sql, id));
	}
	
	@SuppressWarnings("unchecked")
	public List<Comment> getCommentByCode(long id, int page, int count) {
		if (id <= 0)
			return null;
		String sql = "SELECT id FROM " + this.TableName()
				+ " WHERE code = ? ORDER BY create_time DESC";
		return this.LoadList(QueryHelper.query_slice_cache(long.class,
				this.CacheRegion(), COMMENT_LIST + id, 100, sql, page, count,
				id));
	}

	/**
	 * 获取某项目的所有评论
	 * 
	 * @param pro_id
	 * @return
	 */
	public List<Comment> getAllCommentByPro(long pro_id) {
		if (pro_id <= 0)
			return null;
		List<Code> c_l = Code.INSTANCE.getAllCodeByProject(pro_id);
		if (null == c_l || c_l.size() == 0)
			return null;
		else {
			List<Comment> comments = new ArrayList<Comment>();
			for (Code code : c_l)
				comments.addAll(this.getAllCommentByCode(code.getId()));
			return comments;
		}
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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Timestamp getCreate_time() {
		return create_time;
	}

	public void setCreate_time(Timestamp create_time) {
		this.create_time = create_time;
	}

	@Override
	protected String TableName() {
		return "osc_comments";
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
}
