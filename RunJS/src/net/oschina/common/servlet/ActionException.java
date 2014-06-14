package net.oschina.common.servlet;

/**
 * Action执行异常异常
 * @author liudong
 */
public class ActionException extends RuntimeException {

	private String key;
	
	public ActionException(String message){
		super(message);
	}
	
	public ActionException(String key, String message){
		super(message);
		this.key = key;
	}
	
	public String getKey(){
		return this.key;
	}

}
