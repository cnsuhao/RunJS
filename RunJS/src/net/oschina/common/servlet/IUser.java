package net.oschina.common.servlet;

/**
 * 会员资料接口
 * 
 * @author Winter Lau
 * @date 2010-2-3 下午03:20:57
 */
public interface IUser {

	public final static String G_USER = "g_user";

	public final static byte ROLE_GENERAL = 1;
	public final static byte ROLE_EDITOR = 101;
	public final static byte ROLE_ADMIN = 127;

	/**
	 * 用户编号
	 * 
	 * @return
	 */
	public long getId();
	
	/**
	 * 用户随机码
	 * @return
	 */
	public String getIdent();

	/**
	 * 返回用户角色
	 * 
	 * @return
	 */
	public byte getRole();

	/**
	 * 判断用户是否被阻止使用
	 * 
	 * @return
	 */
	public boolean IsBlocked();
}
