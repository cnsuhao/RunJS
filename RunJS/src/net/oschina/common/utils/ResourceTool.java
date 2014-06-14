package net.oschina.common.utils;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;


import org.apache.velocity.VelocityContext;
import org.apache.velocity.tools.view.context.ViewContext;

/**
 * 用于国际化资源的Velocity工具类
 * @author liudong
 */
public class ResourceTool {

	private HttpServletRequest request;
	private VelocityContext velocity;

	/*
	 * Initialize toolbox
	 * @see org.apache.velocity.tools.view.tools.ViewTool#init(java.lang.Object)
	 */
	public void init(Object arg0) {
		//scope: request or session
		if(arg0 instanceof ViewContext){
			ViewContext viewContext = (ViewContext) arg0;
			request = viewContext.getRequest();
			velocity = (VelocityContext)viewContext.getVelocityContext();
		}
	}

	public String this_vm(){
		return velocity.getCurrentTemplateName();
	}

	/**
	 * 读取资源文件 xxxx.properties 中对应键为key的字符串值
	 * @param bundle
	 * @param key
	 * @param def_value
	 * @return
	 */
	public String get(String bundle, String key, String def_value){
		String v = ResourceUtils.getStringForLocale(_getLocale(), bundle, key);
		return (v == null)?def_value:v;			
	}
	
	/**
	 * 读取资源文件 error.properties 中对应键为key的字符串值
	 * @param key
	 * @return
	 */
	public String msg(String key){
		return ResourceUtils.getStringForLocale(_getLocale(), "message", key);
	}
	
	public String msg(String key, List<Object> args){
		return ResourceUtils.getStringForLocale(_getLocale(), "message", key, args.toArray(new Object[args.size()]));
	}

	private Locale _getLocale(){
		return (request != null)?request.getLocale():null;
	}
	
}