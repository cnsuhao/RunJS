package net.oschina.runjs.beans;

import java.sql.Timestamp;
import java.util.Date;

import net.oschina.common.db.QueryHelper;

public class PluginCode extends Pojo {
	public static PluginCode INSTANCE = new PluginCode();
	// 最新插件列表缓存
	public static final transient String NEW_LIST = "newlist";

	private int status;
	private long codeid;
	private String html;
	private String js;
	private String css;
	private Timestamp create_time;
	private Timestamp update_time;

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
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

	public PluginCode GetPluginCodeByIdent(String ident) {
		Code code = Code.INSTANCE.getCodeByIdent(ident);
		if (code == null)
			return null;
		String sql = "SELECT * FROM " + this.TableName() + " WHERE codeid = ?";
		return QueryHelper.read(PluginCode.class, sql, code.getId());
	}

	public boolean Update() {
		String sql = "UPDATE "
				+ this.TableName()
				+ " SET html = ? , js = ? ,css = ? , update_time = ? , status = ? WHERE id = ?";
		if (1 == QueryHelper.update(sql, html, js, css, new Timestamp(
				new Date().getTime()), NEW_LIST, this.getId())) {
			this.Evict(true);
			return true;
		}
		return false;
	}

	public PluginCode GetPluginCodeByCode(long cid) {
		String sql = "SELECT * FROM " + this.TableName() + " WHERE codeid = ?";
		return QueryHelper.read(PluginCode.class, sql, cid);
	}

	@Override
	protected String TableName() {
		return "osc_plugin_codes";
	}
}