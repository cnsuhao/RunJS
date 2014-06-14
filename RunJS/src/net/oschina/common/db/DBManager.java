package net.oschina.common.db;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 数据库管理
 * @author Winter Lau
 * @date 2010-2-2 下午10:18:50
 */
public class DBManager {

	private final static Log log = LogFactory.getLog(DBManager.class);
	private final static ThreadLocal<Connection> conns = new ThreadLocal<Connection>();
	private static DataSource dataSource;
	private static boolean show_sql = false;
	private static Properties cp_props = new Properties();
	
	static {
		initDataSource(null);
	}

	/**
	 * 初始化连接池
	 * @param props
	 * @param show_sql
	 */
	private final static void initDataSource(Properties dbProperties) {
		try {
			if(dbProperties == null){
				dbProperties = new Properties();
				dbProperties.load(DBManager.class.getResourceAsStream("/jdbc.properties"));
			}
			//Class.forName(dbProperties.getProperty("jdbc.driverClass"));
			for(Object key : dbProperties.keySet()) {
				String skey = (String)key;
				if(skey.startsWith("jdbc.")){
					String name = skey.substring(5);
					cp_props.put(name, dbProperties.getProperty(skey));
					if("show_sql".equalsIgnoreCase(name)){
						show_sql = "true".equalsIgnoreCase(dbProperties.getProperty(skey));
					}
				}
			}
			dataSource = (DataSource)Class.forName(cp_props.getProperty("datasource")).newInstance();
			if(dataSource.getClass().getName().indexOf("c3p0")>0){
				//Disable JMX in C3P0
				System.setProperty("com.mchange.v2.c3p0.management.ManagementCoordinator", 
						"com.mchange.v2.c3p0.management.NullManagementCoordinator");
			}
			log.info("Using DataSource : " + dataSource.getClass().getName());
			BeanUtils.populate(dataSource, cp_props);

			Connection conn = getConnection();
			DatabaseMetaData mdm = conn.getMetaData();
			log.info("Connected to " + mdm.getDatabaseProductName() + " " + mdm.getDatabaseProductVersion());
			closeConnection();
		} catch (Exception e) {
			e.printStackTrace();
			throw new DBException(e);
		}
	}
	
	/**
	 * 断开连接池
	 */
	public final static void closeDataSource(){
		try {
			dataSource.getClass().getMethod("close").invoke(dataSource);
		} catch (NoSuchMethodException e){ 
		} catch (Exception e) {
			log.error("Unabled to destroy DataSource!!! ", e);
		}
	}

	public final static Connection getConnection() throws SQLException {
		//System.out.println("Fetch a connection from " +RequestContext.get().uri()+ " to Thread#" + Thread.currentThread().getId()+"#"+Thread.currentThread().getName());
		Connection conn = conns.get();
		if(conn ==null || conn.isClosed()){
			conn = _getConnection();
			conns.set(conn);
		}
		return (show_sql && !Proxy.isProxyClass(conn.getClass()))?new _DebugConnection(conn).getConnection():conn;
	}
	
	private static Connection _getConnection() throws SQLException {
		try{
			return dataSource.getConnection();
		}catch(Exception e){
			return DriverManager.getConnection(
					cp_props.getProperty("jdbcUrl"), 
					cp_props.getProperty("user"), 
					cp_props.getProperty("password"));
		}finally {
			//System.out.println("Retrived a connection to Thread#" + Thread.currentThread().getId()+"#"+Thread.currentThread().getName());			
		}
	}
	
	/**
	 * 关闭连接
	 */
	public final static void closeConnection() {
		Connection conn = conns.get();
		try {
			if(conn != null)
				DbUtils.close(conn);
		} catch (SQLException e) {
			log.error("Unabled to close connection!!! ", e);
		} finally {
			conns.set(null);
			//System.out.println("Closed a connection from " +RequestContext.get().uri()+ " to Thread#" + Thread.currentThread().getId()+"#"+Thread.currentThread().getName());			
		}
	}

	/**
	 * 用于跟踪执行的SQL语句
	 * @author liudong
	 */
	static class _DebugConnection implements InvocationHandler {
		
		private final static Log log = LogFactory.getLog(_DebugConnection.class);
		
		private Connection conn = null;

		public _DebugConnection(Connection conn) {
			this.conn = conn;
		}

		/**
		 * Returns the conn.
		 * @return Connection
		 */
		public Connection getConnection() {
			return (Connection) Proxy.newProxyInstance(conn.getClass().getClassLoader(), conn.getClass().getInterfaces(), this);
		}
		
		public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
			try {
				String method = m.getName();
				if("prepareStatement".equals(method) || "createStatement".equals(method))
					log.info("[SQL] >>> " + args[0]);				
				return m.invoke(conn, args);
			} catch (InvocationTargetException e) {
				throw e.getTargetException();
			}
		}

	}
	
}
