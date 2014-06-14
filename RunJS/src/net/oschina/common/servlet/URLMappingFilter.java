package net.oschina.common.servlet;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import net.oschina.common.db.DBManager;

import org.apache.commons.lang.StringUtils;

/**
 * 此过滤器负责对编码进行自动处理，以及执行URL映射规则
 * <pre>
	无言独上西楼，
	月如钩，
	寂寞梧桐深院锁清秋。

	剪不断，
	理还乱，
	是离愁，
	别是一般滋味在心头。 
 * </pre>
 * @author Winter Lau
 * @date 2011-1-10 下午02:20:49
 */
public class URLMappingFilter implements Filter {

	private ServletContext context;
	
	public final static String REQUEST_URI = "request_uri";	//{/index}
	private String PATH_PREFIX;
	private final static String VM_EXT = ".vm";
	private final static String VM_INDEX = "/index" + VM_EXT;

	private String rootDomain = "oschina.net";	
	private String default_base;
	private HashMap<String, String> other_base = new HashMap<String, String>();
	private List<String> ignoreURIs = new ArrayList<String>();
	private List<String> ignoreExts = new ArrayList<String>();
	
	@Override
	public void init(FilterConfig cfg) throws ServletException {
		this.context = cfg.getServletContext();
		
		//模板存放路径
		this.PATH_PREFIX = cfg.getInitParameter("template-path-prefix");
		if(this.PATH_PREFIX == null)
			this.PATH_PREFIX = "/WEB-INF/templates";
		else if(this.PATH_PREFIX.endsWith("/"))
			this.PATH_PREFIX = this.PATH_PREFIX.substring(0, this.PATH_PREFIX.length()-1);

		//某些URL前缀不予处理（例如 /img/***）
		String ignores = cfg.getInitParameter("ignore");
		if(ignores != null)
		for(String ig : StringUtils.split(ignores, ','))
			ignoreURIs.add(ig.trim());
		
		//某些URL扩展名不予处理（例如 *.jpg）
		ignores = cfg.getInitParameter("ignoreExts");
		if(ignores != null)
		for(String ig : StringUtils.split(ignores, ','))
			ignoreExts.add('.'+ig.trim());
		
		//主域名，必须指定
		String tmp = cfg.getInitParameter("domain");
		if(StringUtils.isNotBlank(tmp))
			rootDomain = tmp;
		
		//二级域名和对应页面模板路径
		Enumeration<String> names = cfg.getInitParameterNames();
		while(names.hasMoreElements()){
			String name = names.nextElement();
			String v = cfg.getInitParameter(name);
			if(v.endsWith("/"))
				v = v.substring(0, v.length()-1);
			if("ignore".equalsIgnoreCase(name) || "ignoreExts".equalsIgnoreCase(name))
				continue;
			if("default".equalsIgnoreCase(name))
				default_base = PATH_PREFIX + v;
			else
				other_base.put(name, PATH_PREFIX + v);
		}
	}

	/**
	 * 检查是否允许访问，包括 User-Agent 和 IP 地址
	 * @param ctx
	 * @return
	 */
	protected boolean checkAccess(RequestContext ctx) {
		return true;
	}
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		//自动编码处理	

		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)res;
		RequestContext rc = RequestContext.begin(this.context, request, response);
		
		String req_uri = rc.uri();

		try{
			if(!checkAccess(rc)){
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
				return ;
			}
			
			//过滤URL前缀
			for(String ignoreURI : ignoreURIs){
				if(req_uri.startsWith(ignoreURI)){
					chain.doFilter(rc.request(), rc.response());
					//System.out.println("-------->"+rc.request().getServletPath());
					req_uri = (String)rc.request().getAttribute(ActionServlet.ACTION_FORWARD);
					if(StringUtils.isBlank(req_uri))									
						return ;					
				}
			}
			//过滤URL后缀
			for(String ignoreExt : ignoreExts){
				if(req_uri.endsWith(ignoreExt)){
					chain.doFilter(rc.request(), rc.response());
					return ;
				}
			}

			rc.request().setAttribute(REQUEST_URI, req_uri);
			String[] paths = StringUtils.split(req_uri, '/');
			String vm = _GetTemplate(rc.request(), paths, paths.length);

			beforeFilter(rc);
			rc.forward(vm);
			afterFilter(rc);
			
		} finally {
			try{
				DBManager.closeConnection();
			}catch(Exception e){}
			if(rc!=null) rc.end();
		}
	}

	protected void beforeFilter(RequestContext ctx) {}
	protected void afterFilter(RequestContext ctx) {}

	@Override
	public void destroy() {
	}

	private String _GetTemplate(HttpServletRequest req, String[] paths, int idx_base) {		
		StringBuilder vm = new StringBuilder(_GetTemplateBase(req));
		
		if(idx_base == 0)
			return vm.toString() + VM_INDEX + _MakeQueryString(paths, idx_base);
		
		for(int i=0;i<idx_base;i++){
			vm.append('/');
			vm.append(paths[i]);
		}
		String vms = vm.toString();
		String the_path = vms;
		
		if(_IsVmExist(the_path + VM_EXT))
			return the_path + VM_EXT + _MakeQueryString(paths, idx_base);
		
		the_path += VM_INDEX;

		if(_IsVmExist(the_path))
			return the_path + _MakeQueryString(paths, idx_base);
		
		vms += VM_EXT;
		if(_IsVmExist(vms))
			return vms + _MakeQueryString(paths, idx_base);
		
		return _GetTemplate(req, paths, idx_base-1);
	}
	
	private String _MakeQueryString(String[] paths, int idx_base) {
		StringBuilder params = new StringBuilder();
		int idx = 1;
		for(int i=idx_base;i<paths.length;i++){
			if(params.length()==0)
				params.append('?');
			if(i>idx_base)
				params.append('&');
			params.append("p");
			params.append(idx++);
			params.append('=');
			params.append(paths[i]);
		}
		return params.toString();
	}
	
	private String _GetTemplateBase(HttpServletRequest req) {
		String base = null;
		String prefix = req.getServerName().toLowerCase();
		int idx = (rootDomain!=null)?prefix.indexOf(rootDomain):0;
		if(idx > 0){
			prefix = prefix.substring(0, idx - 1);
			base = other_base.get(prefix);
		}
		return (base==null)?default_base:base;
	}

	private final static List<String> vm_cache = new Vector<String>();
	
	/**
	 * 判断某个页面是否存在，如果存在则缓存此结果
	 * @param path
	 * @return
	 */
	private boolean _IsVmExist(String path){
		if(vm_cache.contains(path))
			return true;
		File testFile = new File(context.getRealPath(path));
		boolean isVM = testFile.exists() && testFile.isFile();
		if(isVM)
			vm_cache.add(path);
		return isVM;
	}

}
