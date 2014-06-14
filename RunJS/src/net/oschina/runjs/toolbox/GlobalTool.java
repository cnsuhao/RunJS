package net.oschina.runjs.toolbox;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.oschina.common.servlet.RequestContext;
import net.oschina.common.utils.SmtpHelper;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Global toolbox
 * 
 * @author liudong
 */
public class GlobalTool {

	public String out(Object obj) {
		System.out.println(obj);
		return "";
	}

	public List<File> list_root() {
		return Arrays.asList(File.listRoots());
	}

	public List<File> list_files(String path) {
		File[] files = new File(RequestContext.get().context()
				.getRealPath(path)).listFiles();
		List<File> lFiles = Arrays.asList(files);
		Collections.sort(lFiles, new Comparator<File>() {
			@Override
			public int compare(File f1, File f2) {
				if (f1.isDirectory() && f2.isDirectory())
					return f2.getName().compareTo(f1.getName());
				else
					return (int) (f2.lastModified() - f1.lastModified());
			}
		});
		return lFiles;
	}

	public long file_size(File file) {
		if (file == null)
			return -1L;
		return file.isDirectory() ? FileUtils.sizeOfDirectory(file) : file
				.length();
	}

	public static List<Thread> list_threads() {
		int tc = Thread.activeCount();
		Thread[] ts = new Thread[tc];
		Thread.enumerate(ts);
		List<Thread> threads = Arrays.asList(ts);
		Collections.sort(threads, new Comparator<Thread>() {
			@Override
			public int compare(Thread t1, Thread t2) {
				return t1.getName().toLowerCase()
						.compareTo(t2.getName().toLowerCase());
			}
		});
		return threads;
	}

	/**
	 * TODO:对请求数据进行采样，并自动屏蔽抓取者
	 * 
	 * @param req
	 * @return
	 */
	public boolean auto_block_spider(HttpServletRequest req) {
		return false;
	}

	/**
	 * 设置public缓存，设置了此类型缓存要求此页面对任何人访问都是同样数据
	 * 
	 * @param minutes
	 *            分钟
	 * @return
	 */
	public String set_public_cache(HttpServletRequest request,
			HttpServletResponse response, int minutes) {
		if (!"POST".equalsIgnoreCase(request.getMethod())) {
			int seconds = minutes * 60;
			response.setHeader("Cache-Control", "max-age=" + seconds);
			Calendar cal = Calendar.getInstance(request.getLocale());
			cal.add(Calendar.MINUTE, minutes);
			response.setDateHeader("Expires", cal.getTimeInMillis());
		}
		return "";
	}

	/**
	 * 设置私有缓存
	 * 
	 * @param minutes
	 * @return
	 */
	public String set_private_cache(HttpServletRequest request,
			HttpServletResponse response, int minutes) {
		if (!"POST".equalsIgnoreCase(request.getMethod())) {
			response.setHeader("Cache-Control", "private");
			Calendar cal = Calendar.getInstance(request.getLocale());
			cal.add(Calendar.MINUTE, minutes);
			response.setDateHeader("Expires", cal.getTimeInMillis());
		}
		return "";
	}

	/**
	 * 关闭缓存
	 * 
	 * @param minutes
	 * @return
	 */
	public String close_cache(HttpServletRequest request,
			HttpServletResponse response) {
		response.setHeader("Cache-Control",
				"must-revalidate, no-cache, private");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Expires", "Sun, 1 Jan 2000 01:00:00 GMT");
		return "";
	}

	// public Properties sys_props(){ return System.getProperties(); }

	public Object random_elem(List<?> objs) {
		if (objs == null || objs.size() == 0)
			return null;
		return objs.get(random_seed.nextInt(objs.size()));
	}

	private final static Random random_seed = new Random(
			System.currentTimeMillis());

	public String random(int size) {
		return RandomStringUtils.randomAlphanumeric(size);
	}

	public int random(int min, int max) {
		return min + random_seed.nextInt(max);
	}

	public <T> List<T> random(List<T> objs) {
		Collections.shuffle(objs);
		return objs;
	}

	/**
	 * 取出list中的一段
	 * 
	 * @param objs
	 * @param page
	 * @param size
	 * @return
	 */
	public static <T> List<T> sub_list(List<T> objs, int page, int size) {
		if (objs == null || objs.size() == 0)
			return objs;
		if (page <= 1 && size > objs.size())
			return objs;
		int from = (page - 1) * size;
		if (from >= objs.size())
			from = objs.size() - 1;
		if (from < 0)
			from = 0;
		int to = from + size;
		if (to < 0 || to >= objs.size())
			to = objs.size();
		return objs.subList(from, to);
	}

	public static Long[] split_as_long(String str, String separatorChars) {
		if (StringUtils.isBlank(str))
			return null;
		String[] values = StringUtils.split(str, separatorChars);
		if (values == null)
			return null;
		return (Long[]) ConvertUtils.convert(values, Long.class);
	}

	/**
	 * 列表倒序
	 * 
	 * @param lst
	 * @return
	 */
	public static List<?> reverse(List<?> lst) {
		if (lst != null)
			Collections.reverse(lst);
		return lst;
	}

	public static <T extends Comparable<T>> List<T> sort(List<T> lst) {
		if (lst != null)
			Collections.sort(lst);
		return lst;
	}

	public Locale locale(String language) {
		return new Locale(language);
	}

	public Locale locale(String language, String country) {
		return new Locale(language, country);
	}

	/**
	 * 报告错误信息
	 * 
	 * @param t
	 */
	public static void report_error(HttpServletRequest req) {
		SmtpHelper.reportError(req, null);
	}

	/**
	 * 根据记录数和每页现实文章数确定页数
	 * 
	 * @param recordCount
	 * @param perPage
	 * @return
	 */
	public static int page_count(int recordCount, int perPage) {
		int pc = (int) Math.ceil(recordCount / (double) perPage);
		return (pc == 0) ? 1 : pc;
	}

	public static int record_index(int rc, int p, int ps, int vc) {
		return rc - ((p - 1) * ps) - vc + 1;
	}

}
