package net.oschina.common.cache;

import java.io.*;
import java.util.Hashtable;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.SimpleNode;

/**
 * Velocity模板上用于控制缓存的指令
 * 该类必须在 velocity.properties 中配置 userdirective=my.cache.CacheDirective
 * @author Winter Lau
 * @date 2009-3-16 下午04:40:19
 */
public class CacheDirective extends Directive {

	private final static Hashtable<String, String> body_templates = new Hashtable<String, String>();
	
	@Override
	public String getName() { return "cache"; }
	
	@Override
	public int getType() { return BLOCK; }

	/* (non-Javadoc)
	 * @see Directive#render(InternalContextAdapter, java.io.Writer, Node)
	 */
	@Override
	public boolean render(InternalContextAdapter context, Writer writer, Node node)
			throws IOException, ResourceNotFoundException, ParseErrorException,
			MethodInvocationException 
	{
		//获得缓存信息
        SimpleNode sn_region = (SimpleNode) node.jjtGetChild(0);
        String region = (String)sn_region.value(context);
        SimpleNode sn_key = (SimpleNode) node.jjtGetChild(1);
        Serializable key = (Serializable)sn_key.value(context);
        
        Node body = node.jjtGetChild(2);
        //检查内容是否有变化
        String tpl_key = key+"@"+region;
        String body_tpl = body.literal();
        String old_body_tpl = body_templates.get(tpl_key);
        String cache_html = CacheManager.get(String.class, region, key);
        if(cache_html == null || !StringUtils.equals(body_tpl, old_body_tpl)){
        	StringWriter sw = new StringWriter();
        	body.render(context, sw);
        	cache_html = sw.toString();
        	CacheManager.set(region, key, cache_html);
        	body_templates.put(tpl_key, body_tpl);
        }
        writer.write(cache_html);
        return true;
	}
	
}
