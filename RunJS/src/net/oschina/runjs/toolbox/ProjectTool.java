package net.oschina.runjs.toolbox;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import net.oschina.common.servlet.RequestContext;
import net.oschina.common.utils.CompileUtils;
import net.oschina.common.utils.HighLight;
import net.oschina.runjs.beans.Code;
import net.oschina.runjs.beans.Comment;
import net.oschina.runjs.beans.Favor;
import net.oschina.runjs.beans.Project;
import net.oschina.runjs.beans.Setting;
import net.oschina.runjs.beans.User;
import net.oschina.runjs.beans.UserFile;
import net.oschina.runjs.beans.Vote;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 项目velocity工具类
 * 
 * @author jack
 * 
 */
public class ProjectTool {

	/**
	 * 获取指定用户的所有项目（按创建时间倒序）
	 * 
	 * @param uid
	 * @return
	 */
	public static List<Project> getAllProjectByUser(long uid) {
		return Project.INSTANCE.listProjectByUserId(uid);
	}

	/**
	 * 获取指定用户的所有资源文件
	 * 
	 * @param uid
	 * @return
	 */
	public static List<UserFile> getUserFileList(long uid, int type) {
		return UserFile.INSTANCE.getUserFileList(uid, (byte) type);
	}

	/**
	 * 获取指定项目的所有代码版本（按版本号从小到大）
	 * 
	 * @param id
	 * @return
	 */
	public static List<Code> getAllVersionByProject(long id) {
		return Code.INSTANCE.getAllCodeByProject(id);
	}

	/**
	 * 获取指定用户已发布的所有代码，ids为排除的id数组
	 * 
	 * @param uid
	 * @param ids
	 * @return
	 */
	public static List<Code> getAllPosted(long uid, long... ids) {
		return Code.INSTANCE.getAllPosted(uid, ids);
	}

	/**
	 * 获取所有从指定代码fork的项目（按fork时间倒序）
	 * 
	 * @param id
	 * @return
	 */
	public static List<Project> getAllForkProject(long id) {
		return Project.INSTANCE.getAllForkProject(id);
	}

	/**
	 * 获取从某代码fork的个数
	 * 
	 * @param id
	 * @return
	 */
	public static int getForkCount(long id) {
		List<Project> p_l = getAllForkProject(id);
		return p_l == null ? 0 : p_l.size();
	}

	/**
	 * 通过项目id和版本号获取css
	 * 
	 * @param id
	 * @param version
	 * @return
	 */
	public static String getCssByProject(long id, int version) {
		Project pro = Project.INSTANCE.Get(id);
		if (version <= 0 || version > pro.getVersion())
			version = pro.getVersion();
		return Code.INSTANCE.getCodeByVersion(id, version).getCss();
	}

	/**
	 * 通过项目id和版本好获取html
	 * 
	 * @param id
	 * @param version
	 * @return
	 */
	public static String getHtmlByProject(long id, int version) {
		Project pro = Project.INSTANCE.Get(id);
		if (version <= 0 || version > pro.getVersion())
			version = pro.getVersion();
		return Code.INSTANCE.getCodeByVersion(id, version).getHtml();
	}

	/**
	 * 通过项目id和版本号获取js
	 * 
	 * @param id
	 * @param version
	 * @return
	 */
	public static String getJsByProject(long id, int version) {
		Project pro = Project.INSTANCE.Get(id);
		if (version <= 0 || version > pro.getVersion())
			version = pro.getVersion();
		return Code.INSTANCE.getCodeByVersion(id, version).getJs();
	}

	/**
	 * 通过代码id获取所有评论
	 * 
	 * @param id
	 * @return
	 */
	public static List<Comment> getAllCommentByCode(long id, boolean isRevert) {
		if (isRevert) {
			List<Comment> list = Comment.INSTANCE.getAllCommentByCode(id);
			Collections.reverse(list);
			return list;
		}
		return Comment.INSTANCE.getAllCommentByCode(id);
	}

	// 评论数
	public static int getCommentCountByCode(long id) {
		List<Comment> list = Comment.INSTANCE.getAllCommentByCode(id);
		if (list == null)
			return 0;
		return list.size();
	}

	// 分页评论
	public static List<Comment> getCommentByCode(long id, int page, int count,
			boolean isRevert) {
		if (isRevert) {
			List<Comment> list = Comment.INSTANCE.getCommentByCode(id, page,
					count);
			Collections.reverse(list);
			return list;
		}
		return Comment.INSTANCE.getCommentByCode(id, page, count);
	}

	/**
	 * 通过代码id获取所有评论
	 * 
	 * @param id
	 * @return
	 */
	public static List<Comment> getAllCommentByCode(long id) {
		return Comment.INSTANCE.getAllCommentByCode(id);
	}

	/**
	 * 代码的评论数
	 * 
	 * @param id
	 * @return
	 */
	public static int getCommentCount(long id) {
		List<Comment> c_l = getAllCommentByCode(id);
		return c_l == null ? 0 : c_l.size();
	}

	/**
	 * 通过项目id获取所有评论
	 * 
	 * @param id
	 * @return
	 */
	public static List<Comment> getAllCommentByPro(long id) {
		return Comment.INSTANCE.getAllCommentByPro(id);
	}

	/**
	 * 通过ident获取代码(代码所有者和管理员或者已发布的才可以拿到)
	 * 
	 * @param ident
	 * @return
	 */
	public static Code getCodeByIdent(String ident, User user, String v_code) {
		Code code = Code.INSTANCE.getCodeByIdent(ident);
		if (null == code)
			return null;
		if ((user != null && user.IsAdmin(code))
				|| StringUtils.isNotBlank(v_code) && user.IsCurrentUser(v_code))
			return code;
		else
			return code.IsPosted() ? code : null;
	}

	/**
	 * 通过ident获取代码(代码所有者和管理员或者已发布的才可以拿到)
	 * 
	 * @param ident
	 * @return
	 */
	public static Code getCodeByIdent(String ident, User user) {
		Code code = Code.INSTANCE.getCodeByIdent(ident);
		if (null == code)
			return null;
		if (user != null && user.IsAdmin(code))
			return code;
		else
			return code.IsPosted() ? code : null;
	}

	/**
	 * 通过ident获取代码
	 * 
	 * @param ident
	 * @return
	 */
	public static Code getCodeByIdent(String ident) {
		Code code = Code.INSTANCE.getCodeByIdent(ident);
		return code;
	}

	/**
	 * 通过id查询代码
	 * 
	 * @param id
	 * @return
	 */
	public static Code getCodeById(long id) {
		return Code.INSTANCE.Get(id);
	}

	/**
	 * 通过id查询项目
	 * 
	 * @param id
	 * @return
	 */
	public static Project getProjectById(long id) {
		return Project.INSTANCE.Get(id);
	}

	/**
	 * 查询某代码的投票数
	 * 
	 * @param id
	 * @return
	 */
	public static int getVoteCountById(long id) {
		return Vote.INSTANCE.getVoteCountById(id);
	}

	/**
	 * 查询某代码的投票得分
	 */
	public static int getVoteScoreById(long id) {
		return Vote.INSTANCE.getVoteScoreById(id);
	}

	/**
	 * 查询某项目的投票数
	 * 
	 * @param pro_id
	 * @return
	 */
	public static int getVoteCountByPro(long pro_id) {
		List<Code> c_l = Code.INSTANCE.getAllCodeByProject(pro_id);
		int count = 0;
		if (null != c_l) {
			for (Code c : c_l)
				count += getVoteCountById(c.getId());
			return count;
		} else
			return 0;
	}

	/**
	 * 查询某项目的投票得分
	 * 
	 * @param pro_id
	 * @return
	 */
	public static int getVoteScoreByPro(long pro_id) {
		List<Code> c_l = Code.INSTANCE.getAllCodeByProject(pro_id);
		int score = 0;
		if (null != c_l) {
			for (Code c : c_l)
				score += getVoteScoreById(c.getId());
			return score;
		} else
			return 0;
	}

	public static String escape(String value) {
		return StringEscapeUtils.escapeJavaScript(value);
	}

	/**
	 * 用户设置项
	 * 
	 * @param id
	 * @return
	 */
	public static List<Setting> getSetByUser(long id) {
		return Setting.INSTANCE.getAllSettingsByUser(id);
	}

	/**
	 * 用户收藏列表
	 * 
	 * @param user
	 * @return
	 */
	public static List<Favor> getFavorsByUser(long user) {
		return Favor.INSTANCE.getFavorListByUser(user);
	}

	/**
	 * 获取某个时间段内所有更新的代码，主要是用于每天审核。
	 * 
	 * @param d1
	 * @param d2
	 * @return
	 */
	public static List<Code> getUpdateCode(String from, String to) {
		Date date = new Date();
		Date date1 = new Date();
		if (StringUtils.isNotBlank(from) && StringUtils.isNotBlank(to)) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			try {
				date = sdf.parse(from);
				date1 = sdf.parse(to);
			} catch (ParseException e) {
				return null;
			}
		} else {
			Calendar c = Calendar.getInstance();
			c.add(Calendar.DAY_OF_MONTH, -1);
			date = c.getTime();
		}
		return Code.INSTANCE.getUpdateCode(date, date1);
	}

	/**
	 * 分页最新代码
	 * 
	 * @param page
	 * @param count
	 * @return
	 */
	public static List<Code> getPostCode(int page, int count) {
		return Code.INSTANCE.ListPostCode(page, count);
	}

	/**
	 * 热门代码
	 */
	public static List<Code> getHotCode(int count) {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, -7);
		return Code.INSTANCE.ListHotCode(c.getTime(), count);
	}

	/**
	 * 计算字符串的MD5，空串将返回空
	 * 
	 * @param str
	 * @return
	 */
	public static String MD5(String str) {
		if (StringUtils.isBlank(str))
			return "";
		return DigestUtils.md5Hex(str);
	}

	/**
	 * 从cookie中获取上次编辑的代码的ident。
	 * 
	 * @param req
	 * @return
	 */
	public static String getLastIdent(HttpServletRequest req) {
		User user = (User) RequestContext.get().user();
		Cookie cookies[] = req.getCookies();
		if (cookies == null)
			return "";
		for (Cookie co : cookies) {
			if (co.getName().equals("lv_ident")) {
				String ident = co.getValue();
				Code code = Code.INSTANCE.getCodeByIdent(ident);
				if (code == null || user == null)
					return "";
				if (user.getId() != code.getUser())
					return "";
				return ident;
			}
		}
		return "";
	}

	/**
	 * less编译
	 * 
	 * @param less
	 * @return
	 */
	public static String less_compile(String less) {
		return CompileUtils.compile_less(less);
	}

	/**
	 * coffee编译
	 * 
	 * @param coffee
	 * @return
	 */
	public static String coffee_compile(String coffee) {
		return CompileUtils.compile_coffee(coffee);
	}

	/**
	 * 高亮代码
	 * 
	 * @param code
	 * @param type
	 * @return
	 */
	public static String render_code(String code, String type) {
		return HighLight.render_code(code, type);
	}

	/**
	 * test
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(less_compile("@a:#ccc;a{color:@a;}"));
	}
}
