package net.oschina.common.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;


import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 格式化工具
 * @author liudong
 */
public class FormatTool {

	public int i(Object obj, int def) {
		if(obj != null && obj instanceof Number)
			return ((Number)obj).intValue();
		return def;
	}
	
	public static List<Integer> splite_strings_to_int(String str, String c){
		if(StringUtils.isBlank(str))
			return null;
		Integer[] nums = (Integer[])ConvertUtils.convert(StringUtils.split(str, c), Integer.class);
		return Arrays.asList(nums);
	}
	
	public static long to_long(Object str){
		if(str instanceof Number) return ((Number)str).longValue();
		return NumberUtils.toLong((String)str,-1L);
	}
		
	public static String price(int price) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		return nf.format(price/100.0);
	}
	
	/**
	 * 替换掉html中的img，并返回替换后的html与替换掉的img数目
	 * @param html
	 * @return
	 */
	public static String[] filterImages(String html){
		String[] str=new String[]{"",""};
		if(StringUtils.isBlank(html)) return str;
		Document doc = Jsoup.parse(html);
		Elements imgs=doc.getElementsByTag("img");
		if(imgs.size()==0){
			str[0]=html;
			return str;
		}
		int count=imgs.size();
		for (Element i : imgs) {
			String src = i.attr("src");
			if(src.indexOf("oschina.net/img/") > 0 || src.indexOf("oschina.net/js/")>0)
				continue;
			if(src.startsWith("/img/") || src.startsWith("/js/"))
				continue;
			try{
				i.tagName("div");
				i.attr("id","div_img_"+count);
				i.addClass("div_imgs");
				i.attr("onclick","loadImgs("+(count++)+")");
				i.html("点击加载图片");
				if(i.parent()!=null && i.parent().tagName().equals("a")){
					i.attr("href",i.parent().attr("href"));				
					i.parent().replaceWith(i);
				}
			}catch(Exception e){}
		}
		str[0]=doc.html();
		str[1]=imgs.size()+"";
		return str;
	}
	
	/**
	 * 删除html中多余的空白
	 * @param html
	 * @return
	 */
	public static String trim_html(String html){
		return StringUtils.replace(StringUtils.replace(html,"\r\n",""),"\t","");
	}

	/**
	 * 格式化HTML文本
	 * @param content
	 * @return
	 */
	public static String text(String content) {
        if(content==null) return "";        
		String html = StringUtils.replace(content, "<", "&lt;");
		return StringUtils.replace(html, ">", "&gt;");
	}

	/**
	 * 格式化HTML文本
	 * @param content
	 * @return
	 */
	public static String html(String content) {
        if(content==null) return "";        
		String html = content;
        html = StringUtils.replace(html, "'", "&apos;");
		html = StringUtils.replace(html, "\"", "&quot;");
		html = StringUtils.replace(html, "\t", "&nbsp;&nbsp;");// 替换跳格
		//html = StringUtils.replace(html, " ", "&nbsp;");// 替换空格
		html = StringUtils.replace(html, "<", "&lt;");
		html = StringUtils.replace(html, ">", "&gt;");
		return html;
	}

	/**
	 * 格式化HTML文本
	 * @param content
	 * @return
	 */
	public static String rhtml(String content) {
		if(StringUtils.isBlank(content))
			return content;
		String html = content;
		/*
		html = StringUtils.replace(html, "&", "&amp;");
		html = StringUtils.replace(html, "'", "&apos;");
		html = StringUtils.replace(html, "\"", "&quot;");
		*/
		html = StringUtils.replace(html, "&", "&amp;");
		html = StringUtils.replace(html, "<", "&lt;");
		html = StringUtils.replace(html, ">", "&gt;");
		return html;
	}
	
	public static String wml(String content){
		return html(content);
	}
	
	public static String escape(Object text) {
		return StringEscapeUtils.escapeHtml((String)text);
	}
	
	/**
	 * 格式化评论输出
	 * @param cmt
	 * @return
	 */
	public static String comment(String text) {
		//内容清洗
		text = HTML_Utils.autoMakeLink(FormatTool.html(text), true);
		text = StringUtils.replace(text, "\r\n", "<br/>");
		text = StringUtils.replace(text, "\r", "<br/>");
		text = StringUtils.replace(text, "\n", "<br/>");
		return text;
	}

	/**
	 * 自动为url生成链接
	 * @param text
	 * @param only_oschina
	 * @return
	 */
	public static String auto_url(String text, boolean only_oschina) {
		return HTML_Utils.autoMakeLink(text, only_oschina);
	}
	
	/**
	 * 字符串智能截断
	 * @param str
	 * @param maxWidth
	 * @return
	 */
	public static String abbreviate(String str, int maxWidth){
		return _Abbr(str, maxWidth);
		//if(str==null) return null;
		//return StringUtils.abbreviate(str,maxWidth);
	}

	public static String abbr(String str, int maxWidth){
		return StringUtils.abbreviate(str, maxWidth);
	}
	public static String abbreviate_plaintext(String str, int maxWidth){
		return StringUtils.trim(abbreviate(plain_text(str), maxWidth));
	}
	
	public static String preview(String content, int count) {
		return HTML_Utils.preview(content, count);
	}

	/**
	 * 从一段HTML中萃取纯文本
	 * @param html
	 * @return
	 */
	public static String plain_text(String html){
		return StringUtils.getPlainText(html);
	}

	private static String _Abbr(String str, int count) {
		if(str==null) return null;
		if(str.length() <= count) return str;
		StringBuilder buf = new StringBuilder();
		int len = str.length();
		int wc = 0;
		int ncount = 2 * count - 3;
		for(int i=0;i<len;){
			if(wc >= ncount) break;
			char ch = str.charAt(i++);
			buf.append(ch);
			wc += 2;
			if(wc >= ncount) break;
			if(CharUtils.isAscii(ch)){
				wc -= 1;
				//read next char
				if(i >= len) break;
				char nch = str.charAt(i++);
				buf.append(nch);
				if(!CharUtils.isAscii(nch))
					wc += 2;
				else
					wc += 1;
			}
		}
		buf.append("...");
		return buf.toString();
	}
	
	public static boolean is_empty(String str) {
		return str == null || str.trim().length() == 0;
	}

	public static boolean not_empty(String str) {
		return !is_empty(str);
	}

	public static boolean is_number(String str){
		return StringUtils.isNotBlank(str) && StringUtils.isNumeric(str);
	}
	
	private final static Pattern emailer = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
	private final static Pattern link = Pattern.compile("[a-zA-z]+://[^\\s]*");
	
	/**
	 * 判断是不是一个合法的电子邮件地址
	 * @param email
	 * @return
	 */
	public static boolean is_email(String email){
		if(StringUtils.isBlank(email)) return false;
		email = email.toLowerCase();
		if(email.endsWith("nepwk.com")) return false;
		if(email.endsWith(".con")) return false;
		if(email.endsWith(".cm")) return false;
		if(email.endsWith("@gmial.com")) return false;
		if(email.endsWith("@gamil.com")) return false;
		if(email.endsWith("@gmai.com")) return false;
	    return emailer.matcher(email).matches();
	}
	
	public static boolean is_link(String slink){
		if(StringUtils.isBlank(slink)) return false;
		return link.matcher(slink).matches();
	}
	
	public static void main(String[] args) {
		String url = "http://www.oschina.net/question/ask?amsdf=sdf<";
		System.out.println(is_oschina_link(url));
	}
	
	private static char[] ok_chars = new char[]{'/','=','?','&',' ',':','.'}; 
	public static boolean is_oschina_link(String lnk) {
		for(int i=0;i<lnk.length();i++){
			char ch = lnk.charAt(i);
			if(Character.isLetterOrDigit(ch) || ArrayUtils.contains(ok_chars, ch))
				continue;
			return false;
		}
		return true;
	}
	
	public static String replace(String text, String repl, String with){
		return StringUtils.replace(text, repl, with);
	}
	
	/**
	 * 按给定的模式格式化数字
	 * 如：$format.number($0.2345,'##.##%')返回23.45%
	 * @param number
	 * @param pattern @see DecimalFormat.applyPattern()
	 * @return
	 */
	public static String number(double number, String pattern) {
		DecimalFormat df = (DecimalFormat)DecimalFormat.getInstance();
		df.applyPattern(pattern);
		
		return df.format(number);
	}
	
	public static long toLong(String str) {
		return Long.parseLong(str);
	}
	
}
