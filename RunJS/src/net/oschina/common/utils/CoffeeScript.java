package net.oschina.common.utils;

import java.io.File;
import java.io.FileReader;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import net.oschina.common.servlet.RequestContext;

public class CoffeeScript {
	public static ScriptEngineManager mgr = new ScriptEngineManager();
	public static ScriptEngine jsEngine = mgr.getEngineByName("JavaScript");

	static {
		try {
			jsEngine.eval(new FileReader(new File(RequestContext.root()+"/WEB-INF/classes/coffee-script.js")));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static String compile(String coffeescript) throws ScriptException {
		try {
			Invocable invocableEngine = (Invocable) jsEngine;
			String output = (String) invocableEngine.invokeFunction(
					"compile_coffee", coffeescript);
			return output;
		} catch (NoSuchMethodException ex) {
			ex.printStackTrace();
			return "";
		}
	}
}
