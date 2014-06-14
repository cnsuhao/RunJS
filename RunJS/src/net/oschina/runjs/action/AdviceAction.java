package net.oschina.runjs.action;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import net.oschina.common.servlet.Annotation.JSONOutputEnabled;
import net.oschina.common.servlet.RequestContext;
import net.oschina.common.utils.FormatTool;
import net.oschina.common.utils.ImageCaptchaService;
import net.oschina.runjs.beans.User;

import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;

public class AdviceAction {
	private Gson gson = new Gson();

	@JSONOutputEnabled
	public void add_advice(RequestContext ctx) throws IOException {
		
		if (!ImageCaptchaService.validate(ctx.request())) { throw
			ctx.error("captcha_error"); }
		String content = ctx.param("content", "");
		String email = ctx.param("email", "");
		String ident = ctx.param("ident", "");

		Advice advice = new Advice();
		if (StringUtils.isBlank(content) || content.length() > 1000) {
			throw ctx.error("advice_content_error");
		}
		if (!FormatTool.is_email(email))
			throw ctx.error("advice_email_error");
		advice.setContent(content);
		advice.setCreate_time(new Date());
		advice.setUa(ctx.user_agent());
		advice.setEmail(email);
		User user = (User) ctx.user();
		advice.setUser(user);
		advice.setIdent(ident);
		if (AdviceAction.send(advice))
			ctx.print(gson.toJson(advice));
		else
			throw ctx.error("operation_failed");
	}

	public static boolean send(Advice advice) {
		return AdviceAction.send("smtp.163.com", "jack230230", "jack330",
				"RunJS received an advice!", advice.toString(),
				"jack230230@163.com", "wangzhenwei@makingware.com");
	}

	/**
	 * 发送邮件
	 * 
	 * @param smtp
	 *            SMTP服务器
	 * @param user
	 *            用户名
	 * @param password
	 *            密码
	 * @param subject
	 *            标题
	 * @param content
	 *            邮件内容
	 * @param from
	 *            发件人邮箱
	 * @param to
	 *            收件人邮箱
	 */
	public static boolean send(String smtp, final String user,
			final String password, String subject, String content, String from,
			String to) {
		try {
			Properties props = new Properties();
			props.put("mail.smtp.host", smtp);
			props.put("mail.smtp.auth", "true");
			Session ssn = Session.getInstance(props, new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(user, password);
				}

			});
			MimeMessage message = new MimeMessage(ssn);// 由邮件会话新建一个消息对象
			InternetAddress fromAddress = new InternetAddress(from);// 发件人的邮件地址
			message.setFrom(fromAddress);// 设置发件人
			InternetAddress toAddress = new InternetAddress(to);// 收件人的邮件地址
			message.addRecipient(Message.RecipientType.TO, toAddress);// 设置收件人
			message.setSubject(subject);// 设置标题
			message.setText(content);// 设置内容
			message.setSentDate(new Date());// 设置发信时间

			Transport transport = ssn.getTransport("smtp");
			transport.connect(smtp, user, password);
			transport.sendMessage(message,
					message.getRecipients(Message.RecipientType.TO));
			// transport.send(message);
			transport.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// 测试发邮件
	public static void main(String[] args) {
		AdviceAction.send("smtp.163.com", "jack230230", "jack330",
				"test 163 mail", "can you receive this email?",
				"jack230230@163.com", "916978237@qq.com");
	}
}

class Advice {
	private String content;
	private Date create_time;
	private String ua;
	private String email;
	private String ident;
	private User user;

	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (user != null) {
			sb.append("用户名：").append(user.getName()).append("\r\n");
			sb.append("用户ID：").append(user.getId()).append("\r\n");
			sb.append("用户来源： ").append(user.getType()).append("\r\n");
		} else {
			sb.append("未登录用户（IP）： ").append(RequestContext.get().ip())
					.append("\r\n");
		}
		sb.append("建议时间： ").append(new SimpleDateFormat().format(create_time))
				.append("\r\n");
		sb.append("用户邮箱：").append(email).append("\r\n");
		sb.append("代码地址：").append("http://runjs.cn/detail/").append(ident)
				.append("\r\n");
		sb.append("建议内容： ").append(content).append("\r\n");
		sb.append("UA： ").append(ua);
		return sb.toString();
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getCreate_time() {
		return create_time;
	}

	public void setCreate_time(Date create_time) {
		this.create_time = create_time;
	}

	public String getUa() {
		return ua;
	}

	public void setUa(String ua) {
		this.ua = ua;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getIdent() {
		return ident;
	}

	public void setIdent(String ident) {
		this.ident = ident;
	}
}
