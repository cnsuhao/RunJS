package net.oschina.runjs.beans;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import net.oschina.common.cache.CacheManager;
import net.oschina.common.db.QueryHelper;

public class SquareCode extends Pojo {
	public static SquareCode INSTANCE = new SquareCode();
	public static final String NEW_LIST = "squarelist";
	public static final String HOT_LIST = "hotlist";
	public static final int HAVE_UPDATE = 1;
	public static final int NEWEST = 0;

	public static final transient String SQUARE_CODE_COUNT = "square#code#count";
	public static final transient String SQUARE_HOT_CODE_COUNT = "square#hot#code#count";
	
	private int code_type;
	private int status;
	private long codeid;
	private String html;
	private String js;
	private String css;
	private Timestamp create_time;
	private Timestamp update_time;

	public int GetHTMLType() {
		return (code_type - code_type % 100) / 100;
	}

	public int GetJSType() {
		return (code_type % 100 - GetCSSType()) / 10;
	}

	public int GetCSSType() {
		return code_type % 10;
	}

	/**
	 * 广场代码数量
	 * 
	 * @return
	 */
	public int GetSquareCodeCount() {
		String sql = "SELECT COUNT(id) FROM " + this.TableName();
		return (int) QueryHelper.stat_cache("10min", SQUARE_CODE_COUNT, sql);
	}

	@SuppressWarnings("unchecked")
	public List<SquareCode> listSquarecode(int page, int count) {
		String sql = "SELECT id FROM " + this.TableName()
				+ " ORDER BY create_time DESC";
		List<Long> ids = QueryHelper.query_slice_cache(long.class,
				this.CacheRegion(), NEW_LIST, 500, sql, page, count);
		return this.LoadList(ids);
	}

	@Override
	public boolean Delete() {
		SquareCode.evictCache(this.CacheRegion(), NEW_LIST);
		return super.Delete();
	}

	public int HotSquareCount() {
		String sql = "SELECT COUNT(id) FROM " + this.TableName()
				+ " WHERE create_time > ?";
		Calendar cur = Calendar.getInstance();
		cur.add(Calendar.DATE, -60);
		return (int) QueryHelper.stat_cache("10min", SQUARE_HOT_CODE_COUNT,
				sql, cur.getTime());
	}

	/**
	 * 分页列出广场上热门的代码,每小时更新一次，按热门程度排序
	 * 
	 * @param page
	 * @param count
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<SquareCode> listHotSquarecode(int page, int count) {
		String sql = "SELECT id FROM " + this.TableName()
				+ " WHERE create_time > ?";
		List<Integer> ids = (List<Integer>) CacheManager.get("1h", HOT_LIST);
		if (ids == null) {
			Calendar cur = Calendar.getInstance();
			cur.add(Calendar.DATE, -60);
			ids = QueryHelper.query(Integer.class, sql, cur.getTime());
			Collections.sort(ids, new Comparator<Integer>() {
				@Override
				public int compare(Integer o1, Integer o2) {
					SquareCode sc1 = SquareCode.INSTANCE.Get(o1);
					SquareCode sc2 = SquareCode.INSTANCE.Get(o2);
					if (sc1 == null)
						return 1;
					else if (sc2 == null)
						return -1;
					Code code1 = sc1.GetCodeBySquare();
					Code code2 = sc2.GetCodeBySquare();
					if (code1 == null)
						return 1;
					else if (code2 == null)
						return -1;
					return code2.GetHotValue(code2) - code1.GetHotValue(code1);
				}
			});
			CacheManager.set("1h", HOT_LIST, (Serializable) ids);
		}
		if (ids == null || ids.size() == 0)
			return null;
		int from = (page - 1) * count;
		if (from < 0)
			return null;
		if (count > 500)// 超出缓存的范围
			return this.LoadList(ids);
		int end = Math.min(from + count, ids.size());
		if (from >= end)
			return null;
		return this.LoadList(ids.subList(from, end));
	}

	/**
	 * 标记此代码有更新
	 * 
	 * @return
	 */
	public boolean HaveAnUpdate() {
		return this.UpdateField("status", HAVE_UPDATE);
	}

	/**
	 * 是否是最新的
	 * 
	 * @return
	 */
	public boolean IsNewest() {
		return this.status == NEWEST;
	}

	public Code GetCodeBySquare() {
		return Code.INSTANCE.Get(this.codeid);
	}

	public SquareCode GetSquareCodeByIdent(String ident) {
		Code code = Code.INSTANCE.getCodeByIdent(ident);
		if (code == null)
			return null;
		return GetSquareCodeByCode(code.getId());
	}

	public SquareCode GetSquareCodeByCode(long cid) {
		String sql = "SELECT * FROM " + this.TableName() + " WHERE codeid = ?";
		return QueryHelper.read(SquareCode.class, sql, cid);
	}

	public boolean Update() {
		String sql = "UPDATE "
				+ this.TableName()
				+ " SET html = ? , js = ? ,css = ? , update_time = ? , status = ? WHERE id = ?";
		if (1 == QueryHelper.update(sql, html, js, css, new Timestamp(
				new Date().getTime()), NEWEST, this.getId())) {
			this.Evict(true);
			return true;
		}
		return false;
	}

	@Override
	protected String TableName() {
		return "osc_square_codes";
	}

	public long getCodeid() {
		return codeid;
	}

	public void setCodeid(long codeid) {
		this.codeid = codeid;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public String getJs() {
		return js;
	}

	public void setJs(String js) {
		this.js = js;
	}

	public String getCss() {
		return css;
	}

	public void setCss(String css) {
		this.css = css;
	}

	public Timestamp getCreate_time() {
		return create_time;
	}

	public void setCreate_time(Timestamp create_time) {
		this.create_time = create_time;
	}

	public Timestamp getUpdate_time() {
		return update_time;
	}

	public void setUpdate_time(Timestamp update_time) {
		this.update_time = update_time;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getCode_type() {
		return code_type;
	}

	public void setCode_type(int code_type) {
		this.code_type = code_type;
	}

}