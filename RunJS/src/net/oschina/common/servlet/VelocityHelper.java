package net.oschina.common.servlet;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Properties;
import java.util.ResourceBundle;

import net.oschina.common.utils.DateTimeTool;
import net.oschina.common.utils.FormatTool;
import net.oschina.common.utils.LinkTool;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;

/**
 * 用于velocity模板合成
 * @author liudong
 */
public class VelocityHelper {

	private final static String weekly_report_template_path = RequestContext.root();
	private final static DateTimeTool 	DT_TOOL	= new DateTimeTool();
	private final static FormatTool 	FMT_TOOL= new FormatTool();
	private final static LinkTool 		LNK_TOOL= new LinkTool();
	
	static {
        Properties props = new Properties();
        props.put(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, weekly_report_template_path);
        try {
			Velocity.init(props);
		} catch (Exception e) {
			throw new RuntimeException("Unabled to init VelocityHelper", e);
		}
	}
	
	private static VelocityContext _GetContext(HashMap<String, Object> args){
		VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("ui", 		ResourceBundle.getBundle("ui"));
        velocityContext.put("date", 	DT_TOOL);
        velocityContext.put("format", 	FMT_TOOL);
        velocityContext.put("link", 	LNK_TOOL);
        for(String key : args.keySet()){
        	velocityContext.put(key, args.get(key));
        }
        return velocityContext;
	}
	
	/**
	 * 执行velocity模板并返回结果
	 * @param template_name
	 * @return
	 * @throws Exception
	 */
	public static String execute(String template_name, HashMap<String,Object> args) throws Exception {
        StringWriter writer = new StringWriter();
		Velocity.mergeTemplate(template_name, "UTF-8", _GetContext(args), writer);
        return writer.toString();
	}
	
}
