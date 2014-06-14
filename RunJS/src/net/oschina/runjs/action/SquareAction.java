package net.oschina.runjs.action;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

import net.oschina.common.servlet.Annotation.JSONOutputEnabled;
import net.oschina.common.servlet.Annotation.PostMethod;
import net.oschina.common.servlet.Annotation.UserRoleRequired;
import net.oschina.common.servlet.IUser;
import net.oschina.common.servlet.RequestContext;
import net.oschina.runjs.beans.Code;
import net.oschina.runjs.beans.SquareCode;

import com.google.gson.Gson;

public class SquareAction {
	private Gson gson = new Gson();

	/**
	 * 添加到广场
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@UserRoleRequired(role = IUser.ROLE_ADMIN)
	@PostMethod
	@JSONOutputEnabled
	public void add(RequestContext ctx) throws IOException {
		long id = ctx.id();
		Code code = Code.INSTANCE.Get(id);
		if (code == null)
			throw ctx.error("code_not_exist");
		SquareCode sc = new SquareCode();
		sc.setCodeid(id);
		sc.setCreate_time(new Timestamp(new Date().getTime()));
		sc.setCss(code.getCss());
		sc.setHtml(code.getHtml());
		sc.setJs(code.getJs());
		sc.setCode_type(code.getCode_type());
		sc.setUpdate_time(code.getUpdate_time());
		if (0 < sc.Save()) {
			ctx.print(gson.toJson(sc));
			SquareCode.evictCache(SquareCode.INSTANCE.CacheRegion(),
					SquareCode.NEW_LIST);
		} else
			throw ctx.error("operation_failed");
	}

	/**
	 * 更新到广场
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@UserRoleRequired(role = IUser.ROLE_ADMIN)
	@PostMethod
	@JSONOutputEnabled
	public void update(RequestContext ctx) throws IOException {
		long id = ctx.id();
		Code code = Code.INSTANCE.Get(id);
		if (code == null)
			throw ctx.error("code_not_exist");
		SquareCode sc = SquareCode.INSTANCE.GetSquareCodeByCode(id);
		if (sc == null)
			throw ctx.error("code_not_in_square");
		sc.setCss(code.getCss());
		sc.setHtml(code.getHtml());
		sc.setJs(code.getJs());
		Timestamp time = new Timestamp(new Date().getTime());
		sc.setUpdate_time(time);
		sc.setCss(code.getCss());
		sc.setHtml(code.getHtml());
		sc.setJs(code.getJs());
		sc.setCode_type(code.getCode_type());
		sc.setUpdate_time(code.getUpdate_time());
		if (sc.Update())
			ctx.print(gson.toJson(sc));
		else
			throw ctx.error("operation_failed");
	}

	/**
	 * 从广场删除
	 */
	@UserRoleRequired(role = IUser.ROLE_ADMIN)
	@PostMethod
	@JSONOutputEnabled
	public void delete(RequestContext ctx) throws IOException {
		long id = ctx.id();
		SquareCode sc = SquareCode.INSTANCE.Get(id);
		if (sc == null)
			throw ctx.error("square_code_null");
		if (sc.Delete()) {
			SquareCode.evictCache(SquareCode.INSTANCE.CacheRegion(),
					SquareCode.NEW_LIST);
			ctx.print(gson.toJson(sc));
		} else
			throw ctx.error("operation_failed");
	}
}