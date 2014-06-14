package net.oschina.runjs.beans;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.oschina.common.cache.CacheManager;
import net.oschina.common.db.QueryHelper;
import net.oschina.common.utils.DateTimeTool;
import net.oschina.runjs.toolbox.SquareTool;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 代码版本
 * 
 * @author jack
 * 
 */
public class Code extends Pojo {
	public static final transient String PRO_CODE_LIST = "#codelist#proid#";
	public static final transient String CODE_IDENT = "#code#ident#";
	public static final transient String FORK_LIST = "#forklist#codeid#";
	public static final transient String POST_LIST = "#postlist#uid#";
	public static final transient String NEW_POST_LIST = "#postlist#page#";
	public static final transient String HOT_POST_LIST = "#hotlist#";
	public static final transient String FORK_COUNT = "#forkcount#cid#";
	public static final transient String CATALOG_CODE_LIST = "#catalog_code_list#";
	public static final transient String RECYCLE_CODE_LIST = "recycle_code#";
	/**
	 * 111为默认代码类型 ， 223为markdown、coffee、scss
	 */
	public static final transient int TYPE_HTML = 1;
	public static final transient int TYPE_MARKDOWN = 2;
	public static final transient int TYPE_JAVASCRIPT = 1;
	public static final transient int TYPE_COFFEESCRIPT = 2;
	public static final transient int TYPE_CSS = 1;
	public static final transient int TYPE_LESS = 2;
	public static final transient int TYPE_SCSS = 3;
	public static final transient int DEFAULT_TYPE = 111;

	// public static List<Code> post_code = new ArrayList<Code>();

	public static final transient int DEFAULT_CATALOG = 0;

	public static final transient Code INSTANCE = new Code();
	// 保存操作最小间隔
	public static final transient int MIN_UPDATE_TIME = 2 * 1000;
	// 创建新版本操作最小间隔
	public static final transient int MIN_NEW_VER_TIME = 5 * 1000;

	// 代码已发布
	public static final transient int STATUS_POST = 1;
	// 回收站里的代码
	public static final transient int STATUS_RECYCLE = -1;

	private long user;
	private long project;
	private long fork;
	private int num;
	private String html;
	private String css;
	private String js;
	private int status;
	private String ident;
	private Timestamp create_time;
	private String name;
	private Timestamp update_time;
	private String description;
	private Timestamp post_time;
	// 代码签名，用于保存代码的时候识别是否为基于最新代码
	private String sign;
	private int view_count;
	private long catalog;
	private int code_type;

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
	 * 设置HTML代码类型，MARKDWON、HTML等
	 * 
	 * @param t
	 */
	public void SetHTMLType(int t) {
		this.setCode_type(t * 100 + GetJSType() * 10 + GetCSSType());
		this.UpdateField("code_type", this.getCode_type());
	}

	/**
	 * 设置JS代码类型，js、Coffee等
	 * 
	 * @param t
	 */
	public void SetJSType(int t) {
		this.setCode_type(GetHTMLType() * 100 + t * 10 + GetCSSType());
		this.UpdateField("code_type", this.getCode_type());
	}

	/**
	 * 设置CSS代码类型，less、css、scss等
	 * 
	 * @param t
	 */
	public void SetCSSType(int t) {
		this.setCode_type(GetHTMLType() * 100 + GetJSType() * 10 + t);
		this.UpdateField("code_type", this.getCode_type());
	}

	/**
	 * 将view_count加1，当某职位被阅读一次的时候执行
	 * 
	 * @param pid
	 */
	public static void view(long pid) {
		String sql = "UPDATE " + Code.INSTANCE.TableName()
				+ " SET view_count = view_count + 1 WHERE id = ?";
		QueryHelper.update(sql, pid);
	}

	/**
	 * 判断指定用户是否存在指定的代码。
	 * 
	 * @param name
	 * @param user
	 * @return
	 */
	public static boolean IsCodeExist(String name, long user) {
		String sql = "SELECT COUNT(id) FROM " + INSTANCE.TableName()
				+ " WHERE user = ? AND name = ?";
		int count = (int) QueryHelper.stat(sql, user, name);
		return 0 != count;
	}

	/**
	 * 发布代码
	 * 
	 * @param description
	 * @return
	 */
	public boolean post(String description) {
		Timestamp time = new Timestamp(new Date().getTime());
		String sql = "UPDATE "
				+ this.TableName()
				+ " SET description = ? , post_time = ? ,status = ? WHERE id = ?";
		if (1 == QueryHelper.update(sql, description, time, Code.STATUS_POST,
				this.getId())) {
			// 清除缓存
			Code.evictCache(this.CacheRegion(), CODE_IDENT + this.getIdent());
			Code.evictCache(this.CacheRegion(), POST_LIST + this.getUser());
			this.Evict(true);
			Dynamic.INSTANCE.add_post_dy(this);
			/*
			 * post_code.add(this); if (post_code.size() > 10) { StringBuilder
			 * sb = new StringBuilder(); for (Code code : post_code) {
			 * sb.append("Post Code : ").append(code.getName())
			 * .append(" : ").append("http://runjs.cn/detail")
			 * .append(code.getIdent()).append("\r\n"); } post_code.clear();
			 * EmailUtils.send("POST CODES", sb.toString()); }
			 */
			return true;
		}
		return false;
	}

	/**
	 * 查询某分类下所有代码
	 * 
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Code> listCodeByCatalog(long uid, long id) {
		if (id < 0)
			return null;
		else {
			String sql = "SELECT id FROM "
					+ this.TableName()
					+ " WHERE catalog = ? AND user = ? AND status != ? ORDER BY update_time DESC";
			List<Long> ids = QueryHelper.query_cache(long.class,
					this.CacheRegion(), CATALOG_CODE_LIST + uid + "#" + id,
					sql, id, uid, STATUS_RECYCLE);
			return this.LoadList(ids);
		}
	}

	/**
	 * 列出回收站代码
	 * 
	 * @param uid
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Code> listRecycleCode(long uid) {
		if (uid < 0)
			return null;
		String sql = "SELECT id FROM osc_codes WHERE user = ? AND status = ? ORDER BY id DESC";
		List<Long> ids = QueryHelper.query_cache(long.class,
				this.CacheRegion(), RECYCLE_CODE_LIST + uid, sql, uid,
				STATUS_RECYCLE);
		return this.LoadList(ids);
	}

	@Override
	public boolean UpdateField(String field, Object value) {
		boolean a = super.UpdateField(field, value);
		if (a) {
			Code.evictCache(this.CacheRegion(),
					Code.CODE_IDENT + this.getIdent());
		}
		return a;
	}

	/**
	 * 查询所有已经发布的代码。ids为排除的id数组
	 * 
	 * @param uid
	 * @param ids
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Code> getAllPosted(long uid, long... ids) {
		StringBuilder sql = new StringBuilder("SELECT id FROM ");
		sql.append(this.TableName());
		sql.append(" WHERE user = ?");
		sql.append(" AND status = ?");
		List<Code> all_posted = this.LoadList(QueryHelper.query_cache(
				long.class, this.CacheRegion(), Code.POST_LIST + uid,
				sql.toString(), uid, Code.STATUS_POST));
		if (null == ids || ids.length == 0)
			return all_posted;
		List<Code> res = new ArrayList<Code>();
		if (all_posted != null && !all_posted.isEmpty()) {
			for (Code c : all_posted) {
				boolean a = true;
				for (long ex : ids) {
					if (c.getId() == ex) {
						a = false;
						break;
					}
				}
				if (a)
					res.add(c);
			}
		}
		return res;
	}

	/**
	 * 根据项目id、版本号获取代码
	 * 
	 * @param uid
	 * @return
	 */
	public Code getCodeByVersion(long pro_id, int ver) {
		String sql = "SELECT * FROM " + this.TableName()
				+ " WHERE project = ? AND num = ?";
		return QueryHelper.read(Code.class, sql, pro_id, ver);
	}

	/**
	 * 根据ident获取Code
	 * 
	 * @param ident
	 * @return
	 */
	public Code getCodeByIdent(String id_ent) {
		if (StringUtils.isBlank(id_ent))
			return null;
		String sql = "SELECT * FROM " + this.TableName() + " WHERE ident = ?";
		return QueryHelper.read_cache(Code.class, this.CacheRegion(),
				CODE_IDENT + id_ent, sql, id_ent);
	}

	/**
	 * FIXME 获取所有从某项目fork的项目数量 ，此方法效率低下，待改进。
	 * 
	 * @param pro_id
	 * @return
	 */
	public int GetForkCountByProject(long pro_id) {
		int count = 0;
		for (int i = Project.INIT_VERSION; i <= ((Project) Project.INSTANCE
				.Get(pro_id)).getVersion(); i++)
			count += this.getForkCountById(this.getCodeByVersion(pro_id, i)
					.getId());
		return count;
	}

	/**
	 * 获取某版本的fork数
	 * 
	 * @param id
	 * @return
	 */
	public int getForkCountById(long cid) {
		if (cid <= 0)
			return 0;
		String sql = "SELECT COUNT(id) FROM " + this.TableName()
				+ " WHERE fork = ? AND num = 1";
		return (int) QueryHelper.stat_cache(this.CacheRegion(), FORK_COUNT
				+ cid, sql, cid);
	}

	/**
	 * 代码评论数
	 * 
	 * @return
	 */
	public int GetCommentCount() {
		List<Comment> c_l = Comment.INSTANCE.getAllCommentByCode(this.getId());
		return c_l == null ? 0 : c_l.size();
	}

	/**
	 * 查询某代码的收藏数
	 * 
	 * @param cid
	 * @return
	 */
	public int GetFavorCount() {
		String sql = "SELECT COUNT(id) FROM " + Favor.INSTANCE.TableName()
				+ " WHERE code = ?";
		return (int) QueryHelper.stat_cache(this.CacheRegion(),
				Favor.FAVOR_COUNT + this.getId(), sql, this.getId());
	}

	/**
	 * 查询某个项目的所有版本的代码
	 * 
	 * @param project
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Code> getAllCodeByProject(long pid) {
		String sql = "SELECT id FROM " + this.TableName()
				+ " WHERE project = ? ORDER BY id DESC";
		return this.LoadList(QueryHelper.query_cache(long.class,
				this.CacheRegion(), PRO_CODE_LIST + pid, sql, pid));
	}

	/**
	 * 查询某个时间段内所有更新的代码
	 * 
	 * @param d1
	 * @param d2
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Code> getUpdateCode(Date d1, Date d2) {
		String sql = "SELECT id FROM "
				+ this.TableName()
				+ " WHERE update_time >= ? AND update_time <= ? ORDER BY update_time DESC";
		return this.LoadList(QueryHelper.query(long.class, sql, d1, d2));
	}

	/**
	 * 判断ident是否存在
	 * 
	 * @param id_ent
	 * @return
	 */
	public boolean isIdentExist(String id_ent) {
		String sql = "SELECT COUNT(id) FROM " + this.TableName()
				+ " WHERE ident = ?";
		if (0 != QueryHelper.stat(sql, id_ent))
			return true;
		return false;
	}

	/**
	 * 代码是否已发布
	 * 
	 * @return
	 */
	public boolean IsPosted() {
		return this.status == Code.STATUS_POST;
	}

	/**
	 * 判断该代码是否被某用户收藏
	 * 
	 * @param user
	 * @return
	 */
	public boolean IsFavor(long user) {
		List<Favor> f_l = Favor.INSTANCE.getFavorListByUser(user);
		if (f_l == null)
			return false;
		for (Favor f : f_l) {
			if (this.getId() == f.getCode())
				return true;
		}
		return false;
	}

	/**
	 * 更新代码，更新css、html、js和代码签名
	 * 
	 * @return
	 */
	public boolean update() {
		// 生成代码签名
		String new_sign = RandomStringUtils.randomAlphanumeric(8).toLowerCase();
		String sql = "UPDATE " + this.TableName()
				+ " SET css = ? , html = ? , js = ? ,update_time = ? ,sign = ?"
				+ " WHERE id = ? ";
		if (1 == QueryHelper.update(sql, this.css, this.html, this.js,
				this.update_time, new_sign, this.getId())) {
			this.sign = new_sign;
			// 清楚id缓存
			this.Evict(true);
			// 清除ident指定的缓存
			Code.evictCache(this.CacheRegion(),
					Code.CODE_IDENT + this.getIdent());
			if (SquareTool.IsCodeInSquare(this.getId())) {
				SquareCode sc = SquareCode.INSTANCE.GetSquareCodeByCode(this
						.getId());
				if (sc != null)
					sc.HaveAnUpdate();
			}
			return true;
		}
		return false;
	}

	/**
	 * 清除缓存
	 */
	public void clearCache() {
		// 清除项目版本列表缓存
		Code.evictCache(this.CacheRegion(),
				Code.PRO_CODE_LIST + this.getProject());
		Code.evictCache(
				this.CacheRegion(),
				Code.CATALOG_CODE_LIST + this.getUser() + "#"
						+ this.getCatalog());
		// 清除ident指定的缓存
		Code.evictCache(this.CacheRegion(), Code.CODE_IDENT + this.getIdent());
		// 如果此代码已经发布，清除缓存
		if (this.IsPosted())
			Code.evictCache(this.CacheRegion(), Code.POST_LIST + this.getUser());
		if (this.getFork() != 0) {
			// 清除被fork的代码的缓存列表
			Project.evictCache(Project.INSTANCE.CacheRegion(),
					Project.FORK_PROJECTS + this.getFork());
			Code.evictCache(this.CacheRegion(),
					Code.FORK_COUNT + this.getFork());
		}
	}

	/**
	 * 重写Delete()清楚部分缓存
	 */
	@Override
	public boolean Delete() {
		this.clearCache();
		return super.Delete();
	}

	/**
	 * 判断某用户是否已经对该代码投过票了
	 * 
	 * @param uid
	 * @return
	 */
	public boolean IsVotedByUser(long uid) {
		return Vote.INSTANCE.isVoteExist(uid, this.getId());
	}

	/**
	 * 验证md5
	 * 
	 * @param md5
	 * @return
	 */
	public boolean verifySign(String sign) {
		return sign.equals(this.sign);
	}

	/**
	 * 重写Save，在Save之前生成小写字符加数字的ident。
	 */
	@Override
	public long Save() {
		if (this.code_type < 111)
			code_type = Code.DEFAULT_TYPE;
		String ident = RandomStringUtils.randomAlphanumeric(8).toLowerCase();
		while (this.isIdentExist(ident))
			ident = RandomStringUtils.randomAlphanumeric(8).toLowerCase();
		this.setIdent(ident);
		// 初始代码签名
		this.sign = RandomStringUtils.randomAlphanumeric(8).toLowerCase();
		Code.evictCache(
				this.CacheRegion(),
				Code.CATALOG_CODE_LIST + this.getUser() + "#"
						+ this.getCatalog());
		return super.Save();
	}

	/**
	 * 分页列出最新发布的代码
	 * 
	 * @return
	 */
	public List<Code> ListPostCode(int page, int count) {
		String sql = "SELECT * FROM " + this.TableName()
				+ " WHERE status > ? AND fork = 0 ORDER BY post_time DESC";
		return QueryHelper.query_slice_cache(Code.class, "1h", NEW_POST_LIST
				+ page + "#" + count, 200, sql, page, count, 0);
	}

	/**
	 * 列出最热门代码
	 * 
	 * @return
	 */
	public List<Code> ListHotCode(Date from, int count) {
		String sql = "SELECT * FROM "
				+ this.TableName()
				+ " WHERE status > ? AND post_time >= ? ORDER BY post_time DESC";
		List<Code> l = QueryHelper.query_cache(Code.class, "1h", HOT_POST_LIST,
				sql, 0, from);
		if (l == null)
			return l;
		Collections.sort(l, new Comparator<Code>() {
			@Override
			public int compare(Code o1, Code o2) {
				return o2.GetHotValue(o2) - o1.GetHotValue(o1);
			}
		});
		if (l.size() > count)
			return l.subList(0, count - 1);
		return l;
	}

	/**
	 * 热门代码排序依据
	 * 
	 * @param c
	 * @return
	 */
	public int GetHotValue(Code c) {
		// 距今天的天数
		int days = DateTimeTool.daysBetweenDate(new Date(c.getCreate_time()
				.getTime()), new Date());
		int value = c.getForkCountById(c.getId()) * 5 + c.GetCommentCount() * 2
				+ Vote.INSTANCE.getVoteCountById(c.getId()) * 2
				+ c.getView_count() - days * 4;
		return value;
	}

	public long getUser() {
		return user;
	}

	public void setUser(long user) {
		this.user = user;
	}

	public long getProject() {
		return project;
	}

	public void setProject(long project) {
		this.project = project;
	}

	public long getFork() {
		return fork;
	}

	public void setFork(long fork) {
		this.fork = fork;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public String getCss() {
		return css;
	}

	public void setCss(String css) {
		this.css = css;
	}

	public String getJs() {
		return js;
	}

	public void setJs(String js) {
		this.js = js;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getIdent() {
		return ident;
	}

	public void setIdent(String ident) {
		this.ident = ident;
	}

	@Override
	protected String TableName() {
		return "osc_codes";
	}

	public Timestamp getCreate_time() {
		return create_time;
	}

	public void setCreate_time(Timestamp create_time) {
		this.create_time = create_time;
	}

	public String getName() {
		if (!StringUtils.isBlank(name))
			return name;
		else {
			Project p = Project.INSTANCE.Get(this.project);
			return p == null ? "" : p.getName();
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	public Timestamp getUpdate_time() {
		return update_time;
	}

	public void setUpdate_time(Timestamp update_time) {
		this.update_time = update_time;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Timestamp getPost_time() {
		return post_time;
	}

	public void setPost_time(Timestamp post_time) {
		this.post_time = post_time;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public int getView_count() {
		return view_count;
	}

	public void setView_count(int view_count) {
		this.view_count = view_count;
	}

	public long getCatalog() {
		return catalog;
	}

	public void setCatalog(long catalog) {
		this.catalog = catalog;
	}

	public void UpdateViewCount(Map<Long, Integer> datas, boolean update_cache) {
		String sql = "UPDATE " + this.TableName()
				+ " SET view_count = view_count+? WHERE id=?";
		int i = 0;
		Object[][] args = new Object[datas.size()][2];
		for (long id : datas.keySet()) {
			int count = datas.get(id);
			args[i][1] = id;
			args[i][0] = count;
			if (update_cache) {
				Code code = CacheManager.get(getClass(), CacheRegion(), id);
				if (code != null) {
					code.setView_count(code.getView_count() + count);
					CacheManager.set(CacheRegion(), id, code);
				}
			}
			i++;
		}
		QueryHelper.batch(sql, args);
	}

	public int getCode_type() {
		return code_type;
	}

	public void setCode_type(int code_type) {
		this.code_type = code_type;
	}

}
