package net.oschina.common.utils;

import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.velocity.tools.generic.DateTool;

/**
 * 扩展日期时间工具
 * 
 * @author liudong
 */
public class DateTimeTool extends DateTool {

	private final static SimpleDateFormat FMT_DATE = new SimpleDateFormat(
			"yyyy-MM-dd");

	public Date get_date(HttpServletRequest req, String fn, int factor) {
		String fv = req.getParameter(fn);
		Date d;
		try {
			d = FMT_DATE.parse(fv);
		} catch (Exception e) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, factor);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			d = cal.getTime();
		}
		return d;
	}

	/**
	 * 计算两个整型日期之间的天数
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static Integer daysBetweenDate(Date startDate, Date endDate) {
		if (startDate == null || endDate == null) {
			return null;
		}
		Long interval = endDate.getTime() - startDate.getTime();
		interval = interval / (24 * 60 * 60 * 1000);
		return interval.intValue();
	}

	public static boolean is_today(Date d1) {
		return (d1 != null) ? DateUtils.isSameDay(d1, new Date()) : null;
	}

	public static boolean within_a_day(Date d1) {
		if (d1 == null)
			return false;
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		return d1.after(cal.getTime());
	}

	public static boolean in_a_week(Date d1) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -7);
		return cal.getTimeInMillis() < d1.getTime();
	}

	public static boolean in_a_hour(Date dl) {
		return (System.currentTimeMillis() - dl.getTime()) < 3600000;
	}

	public static boolean in_hours(int hours, Date d1) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, -hours);
		return (d1 != null) ? cal.getTime().before(d1) : false;
	}

	/**
	 * 以友好的方式显示时间
	 * 
	 * @FIXME 此方法怎么那么变态，必须改，越写越乱
	 * @param req
	 * @param time
	 * @return
	 */
	public static String friendly_time(HttpServletRequest req, Date time) {
		Locale loc = (req != null) ? req.getLocale() : Locale.getDefault();
		if (time == null)
			return ResourceUtils.getString("ui", "unknown", loc);
		Calendar cal = Calendar.getInstance();
		if (DateUtils.isSameDay(time, cal.getTime())) {
			int ct = (int) ((cal.getTimeInMillis() - time.getTime()) / 1000);
			if (ct < 3600)
				return ResourceUtils.getStringForLocale(loc, "ui",
						"minutes_before", Math.max(ct / 60, 1));
			return ResourceUtils.getStringForLocale(loc, "ui", "hours_before",
					ct / 3600);
		}
		long lt = time.getTime() / 86400000;
		long ct = cal.getTimeInMillis() / 86400000;
		int days = (int) (ct - lt);

		if (days == 0) {
			int hour = (int) ((cal.getTimeInMillis() - time.getTime()) / 3600000);
			if (hour == 0)
				return ResourceUtils
						.getStringForLocale(
								loc,
								"ui",
								"minutes_before",
								Math.max(
										(cal.getTimeInMillis() - time.getTime()) / 60000,
										1));
			return ResourceUtils.getStringForLocale(loc, "ui", "hours_before",
					hour);
		}
		if (days >= 1 && days < 30) {
			return ResourceUtils.getStringForLocale(loc, "ui",
					(days > 1) ? "days_before" : "yesterday", days,
					(days <= 1) ? DateFormatUtils.format(time, "(H:mm)") : "");
		}
		if (days >= 30 && days < 365) {
			days /= 30;
			return ResourceUtils.getStringForLocale(loc, "ui", "months_before",
					days);
		}
		days /= 365;
		return ResourceUtils
				.getStringForLocale(loc, "ui", "years_before", days);
	}

	/**
	 * 获取相对今日偏移d天的日期
	 * 
	 * @param d
	 * @return
	 */
	public static String date_offset(int d) {
		Calendar date = Calendar.getInstance();
		date.add(Calendar.DATE, d);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(date.getTime());
	}

}
