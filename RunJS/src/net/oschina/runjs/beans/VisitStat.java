package net.oschina.runjs.beans;

import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

import net.oschina.common.db.QueryHelper;

import org.apache.commons.lang.time.DateFormatUtils;

/**
 * 对象访问统计
 * 
 * @author liudong
 */
public class VisitStat extends Pojo {

	public final static transient byte TYPE_CODE = 0x0A; // 代码访问统计
	public final static transient byte TYPE_SQUARE = 0x0B; // 广场访问统计

	public static void main(String[] args) {
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
				"yyyy-MM-dd");
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("CTT"));
		System.out.println("TODAY:" + sdf.format(cal.getTime()));
		cal.add(Calendar.DATE, -1);
		System.out.println("YESTERDAY:" + sdf.format(cal.getTime()));
		cal.add(Calendar.DATE, 1);
		int d_of_w = cal.get(Calendar.DAY_OF_WEEK);
		System.out.println(d_of_w);
		if (d_of_w == 1)
			cal.add(Calendar.DATE, -6);
		else
			cal.add(Calendar.DATE, 2 - d_of_w);
		System.out.println("WEEK:" + sdf.format(cal.getTime()));
		cal = Calendar.getInstance();
		cal.set(Calendar.DATE, 1);
		System.out.println("MONTH:" + sdf.format(cal.getTime()));
	}

	private static void _BatchWrite(byte type, Map<Long, Integer> datas) {
		int i = 0;
		int stat_date = _GetStatDate();
		Object[][] args = new Object[datas.size()][5];
		for (long id : datas.keySet()) {
			int count = datas.get(id);
			args[i][0] = stat_date;
			args[i][1] = type;
			args[i][2] = id;
			args[i][3] = count;
			args[i][4] = count;
			i++;
		}
		QueryHelper.batch(IU_SQL, args);
	}

	public static void VisitCode(Map<Long, Integer> datas) {
		Code.INSTANCE.UpdateViewCount(datas, false);
		_BatchWrite(TYPE_CODE, datas);
	}

	public static void VisitSquare(Map<Long, Integer> datas) {
		_BatchWrite(TYPE_SQUARE, datas);
	}

	private final static transient String IU_SQL = "INSERT INTO osc_visit_stats(stat_date,type,id,view_count) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE view_count=view_count+?";

	private int stat_date;
	private byte type;
	private long id;
	private int view_count;

	public VisitStat() {
	}

	public VisitStat(byte type, long id, int view_count) {
		this.id = id;
		this.type = type;
		this.view_count = view_count;
		this.stat_date = _GetStatDate();
	}

	private static int _GetStatDate() {
		return Integer.parseInt(DateFormatUtils.format(
				System.currentTimeMillis(), "yyyyMMdd"));
	}

	public int getStat_date() {
		return stat_date;
	}

	public void setStat_date(int stat_date) {
		this.stat_date = stat_date;
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getView_count() {
		return view_count;
	}

	public void setView_count(int view_count) {
		this.view_count = view_count;
	}

}
