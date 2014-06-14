package net.oschina.common.utils;

import java.io.File;
import java.io.FileReader;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import net.oschina.common.servlet.RequestContext;

public class HighLight {
	public static ScriptEngineManager mgr = new ScriptEngineManager();
	public static ScriptEngine jsEngine = mgr.getEngineByName("JavaScript");

	static {
		try {
			jsEngine.eval(new FileReader(new File(RequestContext.root()
					+ "/WEB-INF/classes/syntaxhighlighter.js")));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static String render_code(String code, String type) {
		try {
			Invocable invocableEngine = (Invocable) jsEngine;
			String output = (String) invocableEngine.invokeFunction(
					"render_code", code, type);
			return output;
		} catch (NoSuchMethodException ex) {
			ex.printStackTrace();
			return "";
		} catch (ScriptException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static void main(String[] args) throws ScriptException {
		System.out.println(render_code("function a(){for(i=0;i<=10;i++){}}","js"));
	}
}
