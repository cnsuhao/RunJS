package net.oschina.common.utils;

import java.io.*;
import java.util.*;

import javax.servlet.http.HttpServletRequest;


import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;

/**
 * 邮件发送工具类
 * @author liudong
 */
public class SmtpHelper {

	private final static Log log = LogFactory.getLog(SmtpHelper.class);
	private final static Properties smtp_props = new Properties();
	private final static Properties backup_smtp_props = new Properties();
	private final static Properties other_props = new Properties();
	private final static String error_css = "<style>\r\nbody{font-family:Courier New,Tahoma}\r\nh1{color:#00c;font-family:Tahoma;font-size:14pt}\r\nh2{color:#00c;font-family:Tahoma;font-size:12pt}\r\ntable {background:#dddddd}\r\ntable tr{background:#ffffff}\r\ntable tr td{padding-left:10px;padding-right:10px;font-size:10pt}\r\ntable tr th{background:#ffffcc;text-align:left;padding-left:5px;padding-right:5px;}\r\n.r{text-align:right}\r\n</style>\r\n";
	
	static {
		Properties oschinaProperties = new Properties();
		try{
			oschinaProperties.load(SmtpHelper.class.getResourceAsStream("smtp.properties"));
		}catch(IOException e){
			throw new RuntimeException("Unabled to load smtp.properties",e);
		}
		for(Object key : oschinaProperties.keySet()) {
			String skey = (String)key;
			if(skey.startsWith("smtp.")){
				String name = skey.substring(5);
				smtp_props.put(name, oschinaProperties.getProperty(skey));
			}
			else if(skey.startsWith("smtp_backup.")){
				String name = skey.substring(12);
				backup_smtp_props.put(name, oschinaProperties.getProperty(skey));
			}
			else{
				other_props.put(skey, oschinaProperties.getProperty(skey));
			}
		}
		System.getProperties().put("mail.smtp.quitwait", false);
	}
	
	/**
	 * 发送邮件
	 * @param email
	 * @param title
	 * @param content
	 */
	public static void send(String email, String title, String content) {
		try {
			HtmlEmail body = (HtmlEmail)_NewMailInstance(Arrays.asList(email), true);
			body.setSubject(title);
			body.setHtmlMsg(content);
			body.send();
		} catch (EmailException e) {
			throw new RuntimeException("Unabled to send mail", e);
		}
	}
	public static void sendText(String email, String title, String content) {
		try {
			SimpleEmail body = (SimpleEmail)_NewMailInstance(Arrays.asList(email), false);
			body.setSubject(title);			
			body.setMsg(content);
			body.send();
		} catch (EmailException e) {
			throw new RuntimeException("Unabled to send mail", e);
		}
	}
	
	/**
	 * 报告错误信息
	 * @param req
	 * @param excp
	 */
	public static void reportError(HttpServletRequest req, Throwable excp){
		boolean is_localhost = (req!=null)?"127.0.0.1".equals(RequestUtils.getRemoteAddr(req)):false;
		Throwable t = excp;
		if(t == null) t = _GetException(req);
		if(t == null) return ;

		log.error("System Exception", t);
		if(!is_localhost)
		//发送电子邮件通知
		try {
			String email = other_props.getProperty("error_reporter");
			String title = ResourceUtils.getString("ui", "error_500", t.getClass().getSimpleName());
			String content = getErrorHtml(req, t);
			_SendHtmlMail(Arrays.asList(StringUtils.split(email,",")), title, content);
		} catch (Exception e) {
			log.error("Failed to send error report.", e);
		}
	}

	/**
	 * 格式化错误信息
	 * @param req
	 * @param t 错误信息
	 * @param site 出错的个人空间
	 * @return
	 * <h2>Request Headers</h2>
	 */
	@SuppressWarnings("rawtypes")
	public static String getErrorHtml(HttpServletRequest req, Throwable t) {
		StringBuilder html = new StringBuilder(error_css);
		if(req != null){
			html.append("<h2>Request Headers</h2><table>");	
			html.append("<tr><th>Request URL</th><td>");
			html.append(req.getRequestURL().toString());
			if(req.getQueryString()!=null){
				html.append('?');
				html.append(req.getQueryString());						
			}
			html.append("</td></tr>");
			html.append("<tr><th>Remote Addr</th><td>");
			html.append(RequestUtils.getRemoteAddr(req));
			html.append("</td></tr>");
			html.append("<tr><th>Request Method</th><td>");
			html.append(req.getMethod());
			html.append("</td></tr>");
			html.append("<tr><th>CharacterEncoding</th><td>");
			html.append(req.getCharacterEncoding());
			html.append("</td></tr>");
			html.append("<tr><th>Request Locale</th><td>");
			html.append(req.getLocale());
			html.append("</td></tr>");
			html.append("<tr><th>Content Type</th><td>");
			html.append(req.getContentType());
			html.append("</td></tr>");
			Enumeration headers = req.getHeaderNames();
			while(headers.hasMoreElements()){
				String key = (String)headers.nextElement();
				html.append("<tr><th>");
				html.append(key);
				html.append("</th><td>");
				html.append(req.getHeader(key));
				html.append("</td></tr>");
			}		
			html.append("</table><h2>Request Parameters</h2><table>");		
			Enumeration params = req.getParameterNames();
			while(params.hasMoreElements()){
				String key = (String)params.nextElement();
				html.append("<tr><th>");
				html.append(key);
				html.append("</th><td>");
				html.append(req.getParameter(key));
				html.append("</td></tr>");
			}
			html.append("</table>");
		}
		html.append("<h2>");
		html.append(t.getClass().getName());
		html.append('(');
		html.append(DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss"));
		html.append(")</h2><pre>");
		try {
			html.append(_Exception(t));
		} catch (IOException ex) {}
		html.append("</pre>");

		html.append("<h2>System Properties</h2><table>");		
		Set props = System.getProperties().keySet();
		for(Object prop : props){
			html.append("<tr><th>");
			html.append(prop);
			html.append("</th><td>");
			html.append(System.getProperty((String)prop));
			html.append("</td></tr>");
		}
		html.append("</table>");
		return html.toString();
	}

	/**
	 * 将当前上下文发生的异常转为字符串
	 * @return
	 * @throws IOException
	 */
	private static Throwable _GetException(HttpServletRequest req) {
		if(req == null) return null;
		Throwable t = (Throwable)req.getAttribute("javax.servlet.jsp.jspException");
		if(t==null){
			//Tomcat的错误处理方式
			t = (Throwable)req.getAttribute("javax.servlet.error.exception");
		}
		return t;
	}

	/**
	 * 将异常信息转化成字符串
	 * @param t
	 * @return
	 * @throws IOException 
	 */
	private static String _Exception(Throwable t) throws IOException{
		if(t == null)
			return null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try{
			t.printStackTrace(new PrintStream(baos));
		}finally{
			baos.close();
		}
		return baos.toString();
	}
	
	private static void _SendHtmlMail(List<String> emails, String title, String html) throws EmailException {
		HtmlEmail mail = (HtmlEmail)_NewMailInstance(emails, true);
		try{
			mail.setSubject(title);
			mail.setHtmlMsg(html);		
			mail.send();
		}catch(Exception e){
			log.fatal("Unabled to send mail via " + mail.getHostName(), e);
			mail = _NewMailBackupInstance(emails);
			mail.setSubject(title);
			mail.setHtmlMsg(html);		
			mail.send();
		}
	}
	
	/**
	 * 初始化邮件
	 * @param emails 所有接收者
	 * @param html
	 * @return
	 * @throws EmailException
	 */
	private final static Email _NewMailInstance(List<String> emails, boolean html) throws EmailException{
		Email body = html?new HtmlEmail():new SimpleEmail();
		body.setCharset("GB2312");
		body.setHostName(smtp_props.getProperty("hostname"));
		body.setSmtpPort(NumberUtils.toInt(smtp_props.getProperty("port"), 25));
		body.setSSL("true".equalsIgnoreCase(smtp_props.getProperty("ssl")));
		body.setAuthentication(smtp_props.getProperty("username"), smtp_props.getProperty("password"));
		String[] senders = StringUtils.split(smtp_props.getProperty("sender"),':');
		body.setFrom(senders[1], senders[0]);
		for(String m : emails){
			if(FormatTool.is_email(m))
				body.addTo(m);	
		}
		return body;
	}

	/**
	 * 初始化邮件
	 * @param emails 所有接收者
	 * @return
	 * @throws EmailException
	 */
	private final static HtmlEmail _NewMailBackupInstance(List<String> emails) throws EmailException{
		HtmlEmail body = new HtmlEmail();
		body.setCharset("GB2312");
		body.setHostName(backup_smtp_props.getProperty("hostname"));
		body.setSmtpPort(NumberUtils.toInt(backup_smtp_props.getProperty("port"), 25));
		body.setSSL("true".equalsIgnoreCase(backup_smtp_props.getProperty("ssl")));
		body.setAuthentication(backup_smtp_props.getProperty("username"), backup_smtp_props.getProperty("password"));
		String[] senders = StringUtils.split(backup_smtp_props.getProperty("sender"),':');
		body.setFrom(senders[1], senders[0]);
		for(String m : emails)
			body.addTo(m);	
		return body;
	}

}
