package net.oschina.runjs.action;

import java.io.IOException;

import javax.servlet.ServletContext;

import net.oschina.common.servlet.RequestContext;
import net.oschina.runjs.beans.VisitStat;
import net.oschina.runjs.service.VisitStatService;

import org.apache.commons.lang.StringUtils;

/**
 * 访问统计
 * 
 * @author liudong
 */
public class VisitAction {

	private final static int max_age = 7200;

	/**
	 * 启动统计数据写入定时器
	 * 
	 * @param ctx
	 */
	public void init(ServletContext ctx) {
		VisitStatService.start();
	}

	/**
	 * 释放Action
	 */
	public void destroy() {
		VisitStatService.destroy();
	}

	/**
	 * 代码访问统计
	 * 
	 * @param req
	 * @param res
	 */
	public void code() {
		long id = _prepare(true);
		if (id > 0)
			VisitStatService.record(VisitStat.TYPE_CODE, id);
	}
	/**
	 * 广场访问统计
	 * @param cache
	 * @return
	 */
	public void square(){
		long id=_prepare(true);
		if(id>0)
			VisitStatService.record(VisitStat.TYPE_SQUARE, id);
	}
	
	private long _prepare(boolean cache) {
		RequestContext ctx = RequestContext.get();
		if (cache)
			ctx.header("Cache-Control", "private,max-age=" + max_age);
		else
			ctx.header("Cache-Control", "no-cache");
		String user_agent = ctx.header("user-agent");
		String referer = ctx.header("referer");
		if (ctx.isRobot()) {
			try {
				ctx.not_found();
			} catch (IOException e) {
			}
			return -1;
		}
		if (StringUtils.contains(user_agent, "Avant Browser")
				&& StringUtils.isBlank(referer))
			return -2;
		if (StringUtils.isNotBlank(referer)
				&& referer.indexOf("runjs.cn") < 0)
			return -3;
		/*
		 * if (!BlockIP.CanView(ctx.ip())) { try { ctx.forbidden(); } catch
		 * (IOException e) { } return -4; }
		 */
		return ctx.id();
	}

}
