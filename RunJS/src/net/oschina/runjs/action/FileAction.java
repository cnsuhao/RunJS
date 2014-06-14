package net.oschina.runjs.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;

import net.oschina.common.servlet.Annotation.JSONOutputEnabled;
import net.oschina.common.servlet.Annotation.PostMethod;
import net.oschina.common.servlet.Annotation.UserRoleRequired;
import net.oschina.common.servlet.RequestContext;
import net.oschina.common.utils.ImageCaptchaService;
import net.oschina.common.utils.LinkTool;
import net.oschina.common.utils.Multimedia;
import net.oschina.runjs.beans.User;
import net.oschina.runjs.beans.UserFile;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.Gson;

/**
 * 添加删除资源文件，资源文件管理
 * 
 * @author jack
 * 
 */
public class FileAction {

	public static Gson gson = new Gson();

	/**
	 * 新增一个资源文件
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@UserRoleRequired
	@PostMethod
	@JSONOutputEnabled
	public void add_file(RequestContext ctx) throws IOException {
		User user = (User) ctx.user();
		File file = ctx.file("file");
		if (null == file)
			throw ctx.error("file_not_found");
		// 判断是否超出大小限制
		if (file.length() > UserFile.MAX_SIZE)
			throw ctx.error("user_file_max_size", UserFile.MAX_SIZE
					/ (1024 * 1024));
		// 判断是否超出数量限制
		if (!user.IsAdmin()
				&& UserFile.INSTANCE.getUserFileCount(user.getId()) >= UserFile.MAX_COUNT)
			throw ctx.error("user_file_max_count", UserFile.MAX_COUNT);

		String file_name = file.getName();
		UserFile user_file = getNewFile(user, file_name);
		// 判断文件是否已经存在
		if (UserFile.IsUserFileExist(user_file.getName(), user.getId())) {
			throw ctx.error("file_name_repeat");
		}
		// 上传文件类型不对
		if (user_file.getType() == UserFile.F_OTHER)
			throw ctx.error("file_format_error");

		File fileDest = getStorageFile(user_file);

		InputStream is = new FileInputStream(file);

		FileOutputStream fos = new FileOutputStream(fileDest);

		if (saveFile(user_file, is, fos))
			ctx.print(gson.toJson(user_file));
		else
			throw ctx.error("operation_failed");
	}

	/**
	 * 通过URL上传文件
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@UserRoleRequired
	@JSONOutputEnabled
	@PostMethod
	public void add_url_file(RequestContext ctx) throws IOException {
		User user = (User) ctx.user();
		/*
		 * 验证用户身份 String verify_code = ctx.param("v_code", ""); if
		 * (!user.IsCurrentUser(verify_code)) throw
		 * ctx.error("operation_forbidden");
		 */
		// 判断是否超出数量限制
		if (UserFile.INSTANCE.getUserFileCount(user.getId()) >= UserFile.MAX_COUNT)
			throw ctx.error("user_file_max_count", UserFile.MAX_COUNT);

		String u = ctx.param("url", "");
		URL url = null;
		try {
			url = new URL(u);
		} catch (MalformedURLException e) {
			throw ctx.error("url_error");
		}
		String file_name = FilenameUtils.getName(url.getPath());
		UserFile user_file = this.getNewFile(user, file_name);
		// 判断文件是否已经存在
		if (UserFile.IsUserFileExist(user_file.getName(), user.getId())) {
			throw ctx.error("file_name_repeat");
		}
		File fileDest = getStorageFile(user_file);

		HttpClient hc = new DefaultHttpClient();
		try {
			HttpGet httpget = new HttpGet(u);
			HttpResponse response = hc.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				if (!fileDest.exists()) {
					fileDest.getParentFile().mkdirs();
					fileDest.createNewFile();
				}
				if (entity.getContentLength() > UserFile.MAX_SIZE) {
					throw ctx.error("user_file_max_size", UserFile.MAX_SIZE
							/ (1024 * 1024));
				}
				FileOutputStream os = new FileOutputStream(fileDest);
				InputStream in = entity.getContent();
				if (saveFile(user_file, in, os)) {
					ctx.print(gson.toJson(user_file));
				} else {
					throw ctx.error("operation_failed");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw ctx.error("operation_failed");
		}
	}

	/**
	 * 删除资源文件
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	@UserRoleRequired
	@PostMethod
	@JSONOutputEnabled
	public void delete_file(RequestContext ctx) throws IOException {
		if (!ImageCaptchaService.validate(ctx.request())) {
			throw ctx.error("captcha_error");
		}
		User user = (User) ctx.user();
		String verify_code = ctx.param("v_code", "");
		if (!user.IsCurrentUser(verify_code))
			throw ctx.error("operation_forbidden");
		long id = ctx.id();
		UserFile user_file = UserFile.INSTANCE.Get(id);
		if (null == user_file)
			throw ctx.error("file_not_found");
		File file = new File(RequestContext.root() + user_file.getPath());
		if (user_file.Delete()) {
			// 清除用户文件列表缓存
			UserFile.evictCache(user_file.CacheRegion(), UserFile.CACHE_FILES
					+ user_file.getUser() + "#" + user_file.getType());
			file.delete();
			ctx.print(gson.toJson(user_file));
			ImageCaptchaService.clear(ctx.request());
		} else
			throw ctx.error("operation_failed");
	}

	/**
	 * 保存文件
	 * 
	 * @param user_file
	 * @param is
	 * @param fos
	 * @return
	 * @throws IOException
	 */
	private boolean saveFile(UserFile user_file, InputStream is,
			FileOutputStream fos) throws IOException {
		try {

			IOUtils.copy(is, fos);
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(fos);
			String md5 = DigestUtils.md5Hex(new FileInputStream(RequestContext
					.root() + user_file.getPath()));
			user_file.setHash(md5);
			if (0 != user_file.Save()) {
				// 清除用户文件列表缓存
				UserFile.evictCache(user_file.CacheRegion(),
						UserFile.CACHE_FILES + user_file.getUser() + "#"
								+ user_file.getType());
				return true;
			} else {
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 由用户和文件名生成一个UserFile bean
	 * 
	 * @param user
	 * @param file_name
	 * @return
	 */
	private UserFile getNewFile(User user, String file_name) {

		String base_name = FilenameUtils.getBaseName(file_name);
		String ext = FilenameUtils.getExtension(file_name);
		// 如果用户上传的文件名过长则截断成前20位字符
		if (base_name.length() > 20)
			base_name = base_name.substring(0, 19);

		file_name = base_name + "." + ext;

		UserFile user_file = new UserFile();
		// 设置一些成员
		String ident = RandomStringUtils.randomAlphanumeric(8).toLowerCase();
		while (UserFile.INSTANCE.isIdentExist(ident))
			ident = RandomStringUtils.randomAlphanumeric(8).toLowerCase();
		user_file.setIdent(ident);
		user_file.setCreate_time(new Timestamp(new Date().getTime()));
		user_file.setName(file_name);
		user_file.setUser(user.getId());
		// 判断文件类型

		if (ext.equalsIgnoreCase("css") || ext.equalsIgnoreCase("less"))
			user_file.setType(UserFile.F_CSS);
		else if (ext.equalsIgnoreCase("js"))
			user_file.setType(UserFile.F_JS);
		else if (Arrays.asList(UserFile.PIC_STR.split(",")).contains(
				ext.toLowerCase()))
			user_file.setType(UserFile.F_PIC);
		else if (ext.equalsIgnoreCase("json"))
			user_file.setType(UserFile.F_JSON);
		else if (ext.equalsIgnoreCase("xml"))
			user_file.setType(UserFile.F_XML);
		else
			user_file.setType(UserFile.F_OTHER);
		if (StringUtils.isBlank(ext))
			user_file.setType(UserFile.F_OTHER);
		// 设置文件存储路径
		user_file.setPath(UserFile.generatePath(user, user_file));
		return user_file;
	}

	private File getStorageFile(UserFile user_file) {
		File fileDest = new File(RequestContext.root() + user_file.getPath());
		// 创建文件夹
		if (!fileDest.getParentFile().exists())
			fileDest.getParentFile().mkdirs();
		return fileDest;
	}

	public void download(RequestContext ctx) throws IOException {
		long id = ctx.id();
		UserFile uf = UserFile.INSTANCE.Get(id);
		FileInputStream fis = null;
		File r_f = new File(RequestContext.root() + uf.getPath());
		String filename = uf.getName();
		try {
			fis = new FileInputStream(r_f);
			// 设置 content-type
			ctx.response().setContentLength((int) r_f.length());
			String ext = FilenameUtils.getExtension(r_f.getPath());
			String mine_type = Multimedia.mime_types.get(ext);
			if (mine_type != null)
				ctx.response().setContentType(mine_type);
			String ua = ctx.header("user-agent");

			if (ua != null && ua.indexOf("Firefox") >= 0)
				ctx.header(
						"Content-Disposition",
						"attachment; filename*=\"utf8''"
								+ LinkTool.encode_url(filename.toString())
								+ "." + ext + "\"");
			else
				ctx.header("Content-Disposition", "attachment; filename="
						+ LinkTool.encode_url(filename.toString()) + "." + ext);
			IOUtils.copy(fis, ctx.response().getOutputStream());
		} catch (FileNotFoundException e) {
			ctx.not_found();
		} finally {
			IOUtils.closeQuietly(fis);
		}
	}
}