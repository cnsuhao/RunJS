package net.oschina.common.utils;

import javax.script.ScriptException;

import org.apache.commons.lang.StringUtils;

import com.asual.lesscss.LessEngine;
import com.asual.lesscss.LessException;

public class CompileUtils {
	public static LessEngine engine = new LessEngine();

	// 编译coffeescript
	public static String compile_coffee(String coffee) {
		try {
			return CoffeeScript.compile(coffee);
		} catch (ScriptException e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	// 编译less
	public static String compile_less(String less) {
		String css = "";
		try {
			css = (less != null) ? engine.compile(less) : "";
		} catch (LessException e) {
			e.printStackTrace();
		}
		return StringUtils.replace(css, "\\n", "\n");
	}

	// 编译sass
	public static String compile_sass(String sass) {
		/*
		 * StringWriter result = new StringWriter(); try { new
		 * SassCssProcessor().process(new StringReader(sass), result); } catch
		 * (IOException e) { e.printStackTrace(); return ""; } return
		 * result.toString();
		 */
		return "";
	}

	// 编译markdown
	public static String compile_markdown(String md) {
		return "";
	}
}