package net.oschina.runjs.action;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import net.oschina.common.servlet.Annotation.JSONOutputEnabled;
import net.oschina.common.servlet.Annotation.PostMethod;
import net.oschina.common.servlet.Annotation.UserRoleRequired;
import net.oschina.common.servlet.RequestContext;
import net.oschina.runjs.beans.Catalog;
import net.oschina.runjs.beans.Code;
import net.oschina.runjs.beans.User;

import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;

public class CatalogAction {

	public Gson gson = new Gson();

	/**
	 * 创建一个分类
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@UserRoleRequired
	@PostMethod
	@JSONOutputEnabled
	public void add(RequestContext ctx) throws IOException {
		User user = (User) ctx.user();
		String name = ctx.param("name", "").trim();
		if (StringUtils.isBlank(name) || name.length() > 20)
			throw ctx.error("catalog_name_invalid");
		if (Catalog.INSTANCE.IsCatalogExist(user.getId(), name))
			throw ctx.error("catalog_exist");
		Catalog catalog = new Catalog();
		catalog.setCreate_time(new Timestamp(new Date().getTime()));
		catalog.setUser(user.getId());
		catalog.setName(name);
		catalog.setParent(0);
		if (0 != catalog.Save())
			ctx.print(gson.toJson(catalog));
		else
			throw ctx.error("operation_failed");
	}

	/**
	 * 重命名一个分类
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@UserRoleRequired
	@PostMethod
	@JSONOutputEnabled
	public void rename(RequestContext ctx) throws IOException {
		User user = (User) ctx.user();
		long id = ctx.id();
		Catalog catalog = Catalog.INSTANCE.Get(id);
		if (catalog == null)
			throw ctx.error("catalog_not_exist");
		if (!user.IsAdmin() && user.getId() != catalog.getUser())
			throw ctx.error("operation_forbidden");
		String new_name = ctx.param("name", "").trim();
		if (StringUtils.isBlank(new_name) || new_name.length() > 20)
			throw ctx.error("catalog_name_invalid");
		if (Catalog.INSTANCE.IsCatalogExist(user.getId(), new_name))
			throw ctx.error("catalog_name_repeat");
		catalog.setName(new_name);
		if (catalog.UpdateField("name", new_name))
			ctx.print(gson.toJson(catalog));
		else
			throw ctx.error("operation_failed");
	}

	/**
	 * 删除一个分类，该分类下面必须没有代码
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@UserRoleRequired
	@PostMethod
	@JSONOutputEnabled
	public void delete(RequestContext ctx) throws IOException {
		User user = (User) ctx.user();
		long id = ctx.id();
		if (id <= 0)
			throw ctx.error("catalog_not_exist");
		Catalog catalog = Catalog.INSTANCE.Get(id);
		if (catalog == null)
			throw ctx.error("catalog_not_exist");

		if (!user.IsAdmin() && user.getId() != catalog.getUser())
			throw ctx.error("operation_forbidden");

		List<Code> codes = Code.INSTANCE.listCodeByCatalog(user.getId(), id);
		Catalog.evictCache(catalog.CacheRegion(),
				Catalog.CATALOG_LIST + user.getId());
		if (codes != null) {
			for (Code code : codes) {
				code.setCatalog(Code.DEFAULT_CATALOG);
				code.UpdateField("catalog", Code.DEFAULT_CATALOG);
			}
		}
		if (catalog.Delete()) {
			// 清空缓存
			Code.evictCache(Code.INSTANCE.CacheRegion(), Code.CATALOG_CODE_LIST
					+ catalog.getUser() + "#" + catalog.getId());
			Code.evictCache(Code.INSTANCE.CacheRegion(), Code.CATALOG_CODE_LIST
					+ catalog.getUser() + "#" + "0");
			ctx.print(gson.toJson(catalog));
		} else
			throw ctx.error("operation_failed");
	}

	/**
	 * 将代码移动到分类
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@UserRoleRequired
	@PostMethod
	@JSONOutputEnabled
	public void move_to(RequestContext ctx) throws IOException {
		User user = (User) ctx.user();
		long id = ctx.id();
		long catalog_id = ctx.param("catalog", 0l);
		Code code = Code.INSTANCE.Get(id);
		if (code == null)
			throw ctx.error("code_not_exist");
		if (!user.IsAdmin(code))
			throw ctx.error("operation_forbidden");
		if (catalog_id != 0) {
			Catalog catalog = Catalog.INSTANCE.Get(catalog_id);
			if (catalog == null)
				throw ctx.error("catalog_not_exist");
			if (!user.IsAdmin() && user.getId() != catalog.getUser())
				throw ctx.error("operation_forbidden");

			Code.evictCache(
					code.CacheRegion(),
					Code.CATALOG_CODE_LIST + code.getUser() + "#"
							+ code.getCatalog());

			code.setCatalog(catalog_id);
			if (code.UpdateField("catalog", catalog_id)) {
				Code.evictCache(code.CacheRegion(), Code.CATALOG_CODE_LIST
						+ code.getUser() + "#" + code.getCatalog());
				ctx.print(gson.toJson(catalog));
			} else
				throw ctx.error("operation_failed");
		} else {
			code.setCatalog(catalog_id);
			if (code.UpdateField("catalog", catalog_id)) {
				Code.evictCache(code.CacheRegion(), Code.CATALOG_CODE_LIST
						+ code.getUser() + "#" + code.getCatalog());
				ctx.output_json("success", "1");
			} else
				throw ctx.error("operation_failed");
		}
	}
}