package net.oschina.runjs.beans;

import java.sql.Timestamp;
import java.util.List;

import net.oschina.common.db.QueryHelper;

/**
 * 投票
 * 
 * @author jack
 * 
 */
public class Vote extends Pojo {
	public static final transient String VOTE_COUNT = "#votecount#cid#";
	public static final int VOTE_LOVE = 1;// 代表顶一下+1
	public static final int VOTE_HATE = -1;// 代表踩一下-1
	public static Vote INSTANCE = new Vote();
	private long user;
	private long code;
	private int value;
	private Timestamp create_time;

	/**
	 * 验证某用户是否已经投过票了
	 * 
	 * @param user_id
	 * @param code_id
	 * @return
	 */
	public boolean isVoteExist(long user_id, long code_id) {
		String sql = "SELECT COUNT(id) FROM " + this.TableName()
				+ " WHERE user = ? AND code = ?";
		if (1 <= QueryHelper.stat(sql, user_id, code_id))
			return true;
		return false;
	}

	/**
	 * 查询某代码的投票数
	 * 
	 * @param code_id
	 * @return
	 */
	public int getVoteCountById(long code_id) {
		String sql = "SELECT COUNT(id) FROM " + this.TableName()
				+ " WHERE code = ?";
		return (int) QueryHelper.stat_cache(this.CacheRegion(), VOTE_COUNT
				+ code_id, sql, code_id);
	}

	/**
	 * 统计某代码的投票得分。
	 * 
	 * @param code_id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public int getVoteScoreById(long code_id) {
		int score = 0;
		String sql = "SELECT id FROM " + this.TableName() + " WHERE code = ?";
		List<Vote> v_l = this.LoadList(QueryHelper.query(long.class, sql,
				code_id));
		if (null != v_l) {
			for (Vote v : v_l)
				score += v.getValue();
			return score;
		} else
			return 0;
	}

	public long getUser() {
		return user;
	}

	public void setUser(long user) {
		this.user = user;
	}

	public long getCode() {
		return code;
	}

	public void setCode(long code) {
		this.code = code;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	@Override
	protected String TableName() {
		return "osc_votes";
	}

	public Timestamp getCreate_time() {
		return create_time;
	}

	public void setCreate_time(Timestamp create_time) {
		this.create_time = create_time;
	}
}