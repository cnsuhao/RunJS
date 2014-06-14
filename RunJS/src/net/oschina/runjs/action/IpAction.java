package net.oschina.runjs.action;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import net.oschina.common.servlet.RequestContext;

import org.apache.commons.lang.StringUtils;

public class IpAction {

	public static Map<String, String> ips;

	static {
		ips = new HashMap<String, String>();
	}

	public void mysql(RequestContext ctx) throws IOException {
		if (ips == null)
			ips = new HashMap<String, String>();
		if (!StringUtils.equals(ips.get("mysql"), ctx.ip())) {
			ips.put("mysql", ctx.ip());
		}
		String ip = ips.get("mysql");
		ctx.print(ip);
	}

	public void set(RequestContext ctx) throws IOException {
		String key = ctx.param("k", "mysql");
		if (StringUtils.equals(key, "mysql")) {
			mysql(ctx);
			return;
		} else {
			if (ips == null) {
				ips = new HashMap<String, String>();
				ips.put(key, ctx.ip());
			}
		}
		String ip = ips.get(key);
		ctx.print(ip);
	}

	public void get(RequestContext ctx) throws IOException {
		String key = ctx.param("k", "");
		if (ips == null || StringUtils.isBlank(key)) {
			InetAddress inet = InetAddress.getLocalHost();
			ctx.print(inet.getHostAddress());
			return;
		}
		String ip = ips.get(key);
		ctx.print(ip);
	}
}
