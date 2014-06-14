package net.oschina.runjs.toolbox;

import java.util.List;

import net.oschina.runjs.beans.Dynamic;
import net.oschina.runjs.beans.Msg;

public class MsgTool {
	public static List<Dynamic> getDynamicsByUser(long user, int page, int count) {
		return Dynamic.INSTANCE.getDynamicList(user, page, count);
	}

	public static List<Dynamic> getAlldynamics(int page, int count) {
		return Dynamic.INSTANCE.GetAllDynamicList(page, count);
	}

	public static List<Msg> getMsgByType(long user, int type) {
		return Msg.INSTANCE.listMsgByType(user, type);
	}

	public static int getUnreadCount(long user) {
		return Msg.INSTANCE.GetUnreadCount(user);
	}
}