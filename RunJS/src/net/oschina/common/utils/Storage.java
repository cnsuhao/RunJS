package net.oschina.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.time.DateFormatUtils;

/**
 * 存储服务
 * @author Winter Lau
 * @date 2010-9-25 下午12:32:05
 */
public abstract class Storage {
	
	private final static String FMT_FN = "yyyyMM/ddHHmmss_";
	
	protected abstract String getBasePath();

	/**
	 * 读取文件数据
	 * @param path
	 * @return
	 * @throws FileNotFoundException
	 */
	public FileInputStream read(String path) throws FileNotFoundException {
		return new FileInputStream(getBasePath() + path);
	}

	/**
	 * 读取文件
	 * @param path
	 * @return
	 * @throws FileNotFoundException
	 */
	public File readFile(String path) {
		return new File(getBasePath() + path);
	}
	
	/**
	 * 判断文件是否存在
	 * @param path
	 * @return
	 */
	public boolean exist(String path) {
		return new File(getBasePath() + path).exists();
	}
	
	/**
	 * 保存文件，并返回对应的文件URI
	 * @param f
	 * @return
	 * @throws IOException 
	 */
	public String save(File f) throws IOException {
		String uri = DateFormatUtils.format(new Date(),FMT_FN) + 
			RandomStringUtils.randomAlphanumeric(4) + '.' + 
			FilenameUtils.getExtension(f.getName()).toLowerCase();
		return save(f, uri);
	}
	
	public String save(InputStream in, String ext) throws IOException {
		String uri = DateFormatUtils.format(new Date(),FMT_FN) + 
				RandomStringUtils.randomAlphanumeric(4) + '.' + ext;
		return saveStream(in, uri);
	}

	public String save(byte[] data, String ext) throws IOException {
		String uri = DateFormatUtils.format(new Date(),FMT_FN) + 
				RandomStringUtils.randomAlphanumeric(4) + '.' + ext;
		return saveBytes(data, uri);
	}

	/**
	 * 保存文件到指定路径
	 * @param f
	 * @param path
	 * @throws IOException 
	 */
	public String save(File f, String path) throws IOException {		
		File dest = new File(getBasePath() + path);
		if(!dest.getParentFile().exists())
			dest.getParentFile().mkdirs();
		FileUtils.copyFile(f, dest);
		return path;
	}

	/**
	 * 保存文件到指定路径
	 * @param f
	 * @param path
	 * @throws IOException 
	 */
	public String saveStream(InputStream in, String path) throws IOException {		
		File dest = new File(getBasePath() + path);
		if(!dest.getParentFile().exists())
			dest.getParentFile().mkdirs();
		FileUtils.copyInputStreamToFile(in, dest);
		return path;
	}

	/**
	 * 保存文件到指定路径
	 * @param f
	 * @param path
	 * @throws IOException 
	 */
	public String saveBytes(byte[] data, String path) throws IOException {		
		File dest = new File(getBasePath() + path);
		if(!dest.getParentFile().exists())
			dest.getParentFile().mkdirs();
		FileUtils.writeByteArrayToFile(dest, data);
		return path;
	}
	
	/**
	 * 根据路径来删除文件
	 * @param path
	 * @throws IOException
	 */
	public void delete(String path) throws IOException {
		File dest = new File(getBasePath() + path);
		FileUtils.forceDelete(dest);
	}
	
}
