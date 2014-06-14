package net.oschina.common.utils;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * 唯一字符串List
 * @date 2010-10-21 上午11:41:38
 */
public class UniqueStringList extends LinkedList<String> {

	private boolean ignoreCase;
	
	public UniqueStringList(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}
	
	@Override
	public boolean add(String e) {    
		ListIterator<String> iterator = listIterator();
		while (iterator.hasNext()) {
			String next = iterator.next();			
			if(ignoreCase?e.equalsIgnoreCase(next):e.equals(next))
				return false;
		}
		return super.add(e);
	}
	
	public static void main(String[] args) {
		UniqueStringList usl = new UniqueStringList(true);
		usl.add("oschina");
		usl.add("OSCHINA");
		usl.add("OSChina");
		for(String s : usl)
			System.out.println(s);
	}
}
