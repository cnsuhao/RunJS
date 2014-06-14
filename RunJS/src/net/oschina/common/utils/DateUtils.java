package net.oschina.common.utils;

import java.util.Calendar;

/**
 * 时间工具类
 * @author Winter Lau
 * @date 2011-2-21 下午04:08:37
 */
public class DateUtils extends org.apache.commons.lang.time.DateUtils {

	public static java.sql.Date makeDate(int y, int m, int d) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, y);
		cal.set(Calendar.MONTH, m - 1);
		cal.set(Calendar.DATE, d);
		return new java.sql.Date(cal.getTimeInMillis());
	}
	
	public static boolean isDatesBefore(java.util.Date d, int date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		cal.add(Calendar.DATE, date);
		return cal.before(Calendar.getInstance());
	}
	
}
