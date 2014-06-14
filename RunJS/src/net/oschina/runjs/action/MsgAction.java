package net.oschina.runjs.action;

import java.io.IOException;
import java.util.List;

import net.oschina.common.servlet.Annotation.JSONOutputEnabled;
import net.oschina.common.servlet.Annotation.PostMethod;
import net.oschina.common.servlet.Annotation.UserRoleRequired;
import net.oschina.common.servlet.RequestContext;
import net.oschina.common.utils.FormatTool;
import net.oschina.runjs.beans.Msg;
import net.oschina.runjs.beans.User;

import org.apache.commons.lang.StringUtils;

public class MsgAction {

	/**
	 * 发送留言
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@UserRoleRequired
	@PostMethod
	@JSONOutputEnabled
	public void sendMsg(RequestContext ctx) throws IOException {
		User user = (User) ctx.user();
		long receiver_id = ctx.param("receiver", 0l);
		User receiver = User.INSTANCE.Get(receiver_id);
		if (receiver == null)
			throw ctx.error("user_not_exist");
		String content = FormatTool.text(user.getName() + " : "
				+ ctx.param("content", "").trim());
		if (StringUtils.isBlank(content) || content.length() >= 200)
			throw ctx.error("content_error");
		if (Msg.INSTANCE.addMsg(user.getId(), receiver_id, Msg.TYPE_MSG,
				Msg.NULL_REFER, content))
			ctx.output_json(new String[] { "success", "content" },
					new String[] { "1", content });
	}

	/**
	 * 阅读消息
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@UserRoleRequired
	@PostMethod
	@JSONOutputEnabled
	public void readMsg(RequestContext ctx) throws IOException {
		long[] ids = ctx.lparams("id");
		User user = (User) ctx.user();
		int count = 0;
		if (ids != null) {
			for (long id : ids) {
				Msg msg = Msg.INSTANCE.Get(id);
				if (msg == null)
					throw ctx.error("msg_not_exist");
				if (msg.getReceiver() != user.getId())
					throw ctx.error("operation_forbidden");
				if (msg.getStatus() != Msg.READ) {
					if (msg.UpdateField("status", Msg.READ)) {
						count++;
					}
				}
			}
			if (count == ids.length)
				ctx.output_json("success", 1);
		} else
			throw ctx.error("id_not_exist");
	}

	/**
	 * 批量阅读通知
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@UserRoleRequired
	@PostMethod
	@JSONOutputEnabled
	public void readAllMsg(RequestContext ctx) throws IOException {
		User user = (User) ctx.user();
		String verify_code = ctx.param("v_code", "");
		if (!user.IsCurrentUser(verify_code))
			throw ctx.error("operation_forbidden");
		int type = ctx.param("type", -1);
		List<Msg> msgs = null;
		if (type == 0) {
			msgs = Msg.INSTANCE.listUnreadMsg(user.getId());
		} else if (type == 1 || type == 2 || type == 3) {
			msgs = Msg.INSTANCE.listMsgByType(user.getId(), type - 1);
		}
		int count = 0;
		if (msgs != null && msgs.size() > 0) {
			for (Msg m : msgs) {
				if (m.UpdateField("status", Msg.READ))
					count++;
			}
			if (count == msgs.size())
				ctx.output_json("success", 1);
			else
				throw ctx.error("operation_failed");
		} else
			ctx.output_json("success", 1);
	}
}