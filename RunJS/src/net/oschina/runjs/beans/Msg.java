package net.oschina.runjs.beans;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import net.oschina.common.db.QueryHelper;

/**
 * 留言、通知
 * 
 * @author jack
 * 
 */
public class Msg extends Pojo {
	public static final Msg INSTANCE = new Msg();
	// 系统发送通知
	public transient static final int SYSTEM = 0;
	// 未读
	public transient static final int UNREAD = 0;
	// 已读
	public transient static final int READ = 1;

	// 评论类型
	public transient static final int TYPE_COMMENT = 0;
	// Fork类型
	public transient static final int TYPE_FORK = 1;
	// 顶类型
	public transient static final int TYPE_UP = 2;
	// 留言类型
	public transient static final int TYPE_MSG = 3;
	// 通知类型
	public transient static final int TYPE_NOTIFY = 4;
	// 没有引用
	public transient static final int NULL_REFER = 0;
	private long sender;
	private long receiver;
	private int status;
	// 通知、留言类型
	private int type;
	private Timestamp create_time;
	private String content;
	private long refer;

	@SuppressWarnings("unchecked")
	public List<Msg> listUnreadMsg(long user) {
		String sql = "SELECT id FROM " + this.TableName()
				+ " WHERE status = ? AND receiver = ?";
		List<Long> ids = QueryHelper.query(long.class, sql, UNREAD, user);
		return this.LoadList(ids);
	}

	/**
	 * 列出不同类型的通知
	 * 
	 * @param user
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Msg> listMsgByType(long user, int type) {
		String sql = "SELECT id FROM " + this.TableName()
				+ " WHERE receiver = ? AND type = ? AND status = ? ORDER BY id DESC";
		List<Long> ids = QueryHelper.query(long.class, sql, user, type, UNREAD);
		return this.LoadList(ids);
	}

	/**
	 * 未读消息数量
	 * 
	 * @param user
	 * @return
	 */
	public int GetUnreadCount(long user) {
		String sql = "SELECT COUNT(id) FROM " + this.TableName()
				+ " WHERE status = ? AND receiver = ?";
		return (int) QueryHelper.stat(sql, UNREAD, user);
	}

	@SuppressWarnings("unchecked")
	public List<Msg> listAllMsg(long user) {
		String sql = "SELECT * FROM " + this.TableName()
				+ " WHERE receiver = ?";
		List<Long> ids = QueryHelper.query(long.class, sql, user);
		return this.LoadList(ids);
	}

	/**
	 * 添加一条通知，留言
	 * 
	 * @param sender
	 * @param reveicer
	 * @param content
	 * @return
	 */
	public boolean addMsg(long sender, long reveicer, int type, long refer,
			String content) {
		Msg msg = new Msg();
		msg.setContent(content);
		msg.setCreate_time(new Timestamp(new Date().getTime()));
		msg.setReceiver(reveicer);
		msg.setSender(sender);
		msg.setStatus(UNREAD);
		msg.setType(type);
		msg.setRefer(refer);
		if (0 != msg.Save())
			return true;
		return false;
	}

	public long getSender() {
		return sender;
	}

	public void setSender(long sender) {
		this.sender = sender;
	}

	public long getReceiver() {
		return receiver;
	}

	public void setReceiver(long receiver) {
		this.receiver = receiver;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Timestamp getCreate_time() {
		return create_time;
	}

	public void setCreate_time(Timestamp create_time) {
		this.create_time = create_time;
	}

	@Override
	protected String TableName() {
		return "osc_msgs";
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public long getRefer() {
		return refer;
	}

	public void setRefer(long refer) {
		this.refer = refer;
	}
}