package net.oschina.common.utils;

/**
 * Velocity模板上用的调试工具
 * @author Winter Lau
 * @date 2011-3-7 上午01:01:21
 */
public class DebugTool {

	public String out(Object obj){
		System.out.println(obj);
		return "";
	}
	
	public void sleep(long millis) throws InterruptedException {
		Thread.sleep(millis);
	}
}
