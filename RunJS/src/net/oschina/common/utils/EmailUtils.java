package net.oschina.common.utils;

import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import net.oschina.runjs.action.AdviceAction;

public class EmailUtils {
	public static boolean send(String subject, String content) {
		return EmailUtils.send("smtp.163.com", "jack230230", "jack330",
				subject, content, "jack230230@163.com",
				"wangzhenwei@makingware.com");
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