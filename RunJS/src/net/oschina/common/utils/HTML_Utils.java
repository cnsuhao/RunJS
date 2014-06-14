package net.oschina.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

/**
 * 用于格式化HTML的工具类
 * @author Winter Lau
 */
public class HTML_Utils {

	public static void main(String[] args) {
		String html = "你好，这里是视频<embed src=\"http://\" type=\"application/x-shockwave-flash\" width=\"550\" height=\"400\" quality=\"high\" />";
		System.out.println(HTML_Utils.filterUserInputContent(html));
		//System.out.println("<p>Hello</p>".replaceAll("<(.+?)>", "&lt;$1&gt;"));
	}
	
	private final static Pattern url_pattern = Pattern.compile("http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&amp;=]*)?");
	/**
	 * 自动为文本中的url生成链接
	 * 下载地址：http://www.oschina.net/p/tomcat
	 * @param txt
	 * @param only_oschina
	 * @return
	 */
	public static String autoMakeLink(String txt, boolean only_oschina) {
		StringBuilder html = new StringBuilder();
		int lastIdx = 0;
		Matcher matchr = url_pattern.matcher(txt);
		while (matchr.find()) {
			String str = matchr.group();			
			html.append(txt.substring(lastIdx, matchr.start()));
			if(!only_oschina || StringUtils.containsIgnoreCase(str, "oschina.net") || StringUtils.containsIgnoreCase(str, "osctools.net"))
				html.append("<a href='"+str+"' rel='nofollow' target='_blank'>"+str+"</a>");
			else
				html.append(str);
			lastIdx = matchr.end();
		}
		html.append(txt.substring(lastIdx));
		return html.toString();
	}

	public final static Whitelist user_content_filter = Whitelist.basicWithImages();
	static {
		user_content_filter.addTags("embed","object","param","span","pre","div","h1","h2","h3","h4","h5","table","tbody","tr","th","td");
		user_content_filter.addAttributes("span", "style");
		user_content_filter.addAttributes("pre", "class");
		user_content_filter.addAttributes("div", "class");
		user_content_filter.addAttributes("a", "target");
		user_content_filter.addAttributes("table", "border","cellpadding","cellspacing");
		user_content_filter.addAttributes("object", "width", "height","classid","codebase","data","type");	
		user_content_filter.addAttributes("param", "name", "value");
		user_content_filter.addAttributes("embed", "src","quality","width","height","allowFullScreen","allowScriptAccess","flashvars","name","type","pluginspage");
	}
	
	/**
	 * 对用户输入内容进行过滤
	 * @param html
	 * @return
	 */
	public static String filterUserInputContent(String html) {
		if(StringUtils.isBlank(html)) return "";
		html = Jsoup.clean(html, "http://www.oschina.net", user_content_filter);
		return filterEmbedSrc(html);
	}
	
	public static String filterEmbedSrc(String html) {
		Document doc = Jsoup.parse(html);
		Elements elems = doc.select("embed");
		for(int i=0;i<elems.size();i++){
			Element elem = elems.get(i);
			try{
			String src = elem.attr("src");
			if(!StringUtils.startsWith(src, "http://"))
				elem.remove();
			int w = NumberUtils.toInt(elem.attr("width"),1);
			int h = NumberUtils.toInt(elem.attr("height"),1);
			if(w == 0 || h == 0)
				elem.remove();
			}catch(Exception e){}
		}
		return doc.body().html();
	}

	public static String preview(String html, int max_count){
		if(html == null || html.length()<= max_count * 1.1)
			return html;
		int len = 0;
		StringBuffer prvContent = new StringBuffer();
		Document doc = new Cleaner(user_content_filter).clean(Jsoup.parse(html));
		Element e = doc.body();
		for(Element child : e.getAllElements()){
			String text = child.html().trim();
			len += text.length();
			if(len >= max_count){
				child.html(text.substring(0, text.length() - len + max_count) + "...");
				prvContent.append(child.outerHtml());
				break;
			}
			else
				prvContent.append(child.outerHtml());
		}
		String res = StringUtils.remove(prvContent.toString(), "<body>");
		res = StringUtils.remove(res, "</body>");
		return res;
	}
	
}
