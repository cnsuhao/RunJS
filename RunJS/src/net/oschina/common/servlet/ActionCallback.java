package net.oschina.common.servlet;

import java.lang.reflect.Method;

/**
 * Action回调接口
 * @author tsl
 * @date 2012-7-10 下午2:02:40
 */
public interface ActionCallback {

	/**
	 * 调用 Action 方法之前
	 * @param method
	 * @param ctx
	 */
	public void beforeCall(Method method, RequestContext ctx) ;
	
	/**
	 * 调用Action方法之后
	 * @param method
	 * @param ctx
	 */
	public void afterCall(Method method, RequestContext ctx) ;
	
}
