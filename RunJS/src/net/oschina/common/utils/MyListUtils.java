package net.oschina.common.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections.ListUtils;

/**
 * 列表工具包扩展
 * @author Winter Lau
 * @date 2010-3-1 下午09:18:33
 */
public class MyListUtils extends ListUtils {
	
	/**
	 * 列表过滤
	 * @param <T>
	 * @param objs
	 * @param filter
	 * @return
	 */
	public static <T> List<T> filter(List<T> objs, ObjectFilter<T> filter) {
		if(objs == null)
			return null;
		List<T> new_objs = new ArrayList<T>();
		for(T obj : objs){
			if(filter == null || filter.filter(obj))
				new_objs.add(obj);
		}
		return new_objs;
	}
	
	/**
	 * 返回一个经过排序的新列表
	 * @param <T>
	 * @param objs
	 * @param cp
	 * @return
	 */
	public static <T> List<T> sort(List<T> objs, Comparator<T> cp) {
		List<T> new_objs = new ArrayList<T>();
		new_objs.addAll(objs);
		Collections.sort(new_objs, cp);
		return new_objs;
	}
	
	/**
	 * 对象过滤
	 */
	public interface ObjectFilter<T> {
		public boolean filter(T obj) ;
	}

}
