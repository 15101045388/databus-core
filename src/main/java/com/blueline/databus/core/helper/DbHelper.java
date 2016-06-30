package com.blueline.databus.core.helper;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ho.yaml.Yaml;
import org.springframework.util.StringUtils;

public class DbHelper{

	private static Logger logger = Logger.getLogger(DbHelper.class);
	
	private static Connection conn = null;
	private static PreparedStatement ps = null;
	private static ResultSet rs = null;
	private static String url;
	private static String username;
	private static String password;
	private static String driverManager;
	
	@SuppressWarnings("rawtypes")
	private static void getConfig() throws Exception{
		InputStream is = DbHelper.class.getClassLoader().getResourceAsStream("application.yml");
		if (is != null) {
			Map d1 = Yaml.loadType(is, HashMap.class);
			Map s = (Map) d1.get("spring");
			Map d = (Map) s.get("db");
			username = d.get("username").toString();
			password = d.get("password").toString();
			url = d.get("url").toString();
			driverManager = d.get("driverManager").toString();
		}
	}

	/**
	 * jdbc连接数据库
	 */
	public static Connection openConnection() {
		try {
			if(conn==null||conn.isClosed()){
				getConfig();
				Class.forName(driverManager);
				conn = DriverManager.getConnection(url, username, password);
			}
		} catch (ClassNotFoundException cnfe) {
			logger.info(cnfe.getMessage());
			cnfe.printStackTrace();
		} catch (SQLException se) {
			logger.info( se.getMessage());
			se.printStackTrace();
		} catch (Exception ex) {
			logger.info( ex.getMessage());
			ex.printStackTrace();
		}
		return conn;
	}
	
	/**
	 * 切换数据库
	 * @param dbName
	 * @return
	 */
	public static Connection getConnection(String dbName) {
		// TODO: return db connection by db name
		return null;
	}

	/**
	 * 根据appkey查看 用户secretkey，name
	 * @param key
	 * @return
	 */
	public static Map<String,String> getSecurityKey(String key) {
		Map<String,String> retMap = new HashMap<String,String>();
		try {
			conn = openConnection();
			String sql = " select secretkey,name from account where appkey = ? ";
			ps = conn.prepareStatement(sql);
			ps.setString(1, key);
			rs = ps.executeQuery();
			if (rs.next()) {
				String securityKey = rs.getString(1);
				retMap.put("securityKey",securityKey);
				String name = rs.getString(2);
				retMap.put("name",name);
			}
		} catch (Exception ex) {
			logger.info( ex.getMessage());
			ex.printStackTrace();
		}
		close();
		return retMap;
	}

	// 获取appkey
	public static boolean checkAppkey(String appkey) {
		boolean bool = false;
		try {
			conn = openConnection();
			String sql = " select appkey from test3 where appkey = ? ";
			ps = conn.prepareStatement(sql);
			ps.setString(1, appkey);
			rs = ps.executeQuery();
			if (rs.next()) {
				bool = true;
			}
		} catch (Exception ex) {
			logger.info( ex.getMessage());
			ex.printStackTrace();
		}
		close();
		return bool;
	}

	// 获取数据详细信息
	public static List<Map<Object, Object>> getDate(String dbName, String tableName, HttpServletRequest request){
		List<Map<Object, Object>> list = new ArrayList<Map<Object, Object>>();
		Map<Object, Object> map = new HashMap<Object, Object>();
		conn = openConnection();
		List<Object> col = getColumn(dbName, tableName);
		String s = request.getQueryString();
		String sqlQuery = SqlHelper.sqlQuerys(s);
		StringBuffer sql = new StringBuffer(" select * from " + tableName + " ");
		if(!StringUtils.isEmpty(sqlQuery)){
			sql.append(" where " + sqlQuery); 
		}
		try {
			ps = conn.prepareStatement(sql.toString());
			rs = ps.executeQuery();
			Object obj = null;
			while (rs.next()) {
				map = new HashMap<Object, Object>();
				for (int i = 0; i < col.size(); i++) {
					if (i == col.size()) {
						obj = rs.getObject(i);
					} else {
						obj = rs.getObject(i + 1);
					}
					map.put(col.get(i), obj);
				}
				list.add(map);
			}
		} catch (SQLException ex) {
			logger.info( ex.getMessage());
			ex.printStackTrace();
		}
		close();
		return list;
	}

	// 获取到数据库的表里面的列
	public static List<Object> getColumn(String dbName, String tableName) {
		List<Object> list = new ArrayList<Object>();
		try {
			conn = openConnection();
			String sql = " select COLUMN_NAME cols from information_schema.COLUMNS where table_name = ? and table_schema = ? ";
			ps = conn.prepareStatement(sql);
			ps.setString(1, tableName);
			ps.setString(2, dbName);
			rs = ps.executeQuery();
			while (rs.next()) {
				list.add(rs.getObject("cols"));
			}
		} catch (SQLException ex) {
			logger.info( ex.getMessage());
			ex.printStackTrace();
		}
		return list;
	}

	// 查询数据库里面的表信息（可以根据表名查看）
	public static List<Map<Object, Object>> getTableInfo(String dbName) {
		Map<Object, Object> map = new HashMap<Object, Object>();
		List<Map<Object, Object>> list = new ArrayList<Map<Object, Object>>();
		conn = openConnection();
		String sql = "";
		// information_schema 存放所有表信息，table_schema表所在的数据库，table_type表的类型
		sql += "select TABLE_NAME,TABLE_TYPE,CREATE_TIME from information_schema.tables where table_schema = ? ";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, dbName);
			rs = ps.executeQuery();
			while (rs.next()) {
				map = new HashMap<Object, Object>();
				map.put("TABLE_NAME", rs.getString(1));
				map.put("TABLE_TYPE", rs.getString(2));
				map.put("CREATE_TIME", rs.getDate(3));
				list.add(map);
			}
		} catch (SQLException ex) {
			logger.info( ex.getMessage());
			ex.printStackTrace();
		}
		close();
		return list;
	}

	// 查看所有数据库名称
	public static List<Object> getDataInfo(){
		List<Object> list = new ArrayList<Object>();
		try {
			conn = openConnection();
			String sql = " show databases ";
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				list.add(rs.getObject(1));
			}
		} catch (Exception ex) {
			logger.info( ex.getMessage());
			ex.printStackTrace();
		}
		close();
		return list;
	}

	// 创建数据库
	public static int createDb(String dbName){
		int count = 0;
		try {
			conn = openConnection();
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			StringBuffer sql = new StringBuffer( " create database " + dbName + " default charset=utf8 ");
			ps = conn.prepareStatement(sql.toString());
			conn.commit();
			count = ps.executeUpdate();
		} catch (Exception ex) {
			logger.info( ex.getMessage());
			ex.printStackTrace();
		}
		close();
		return count;
	}

	// 创建数据库表（需要知道在那个数据库下创建表）
	public static int createDbTable(String dbName, String query){
		int count = 0;
		try {
			conn = openConnection();
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			ps = conn.prepareStatement(query);
			ps.executeUpdate();
			conn.commit();
			count = 1;
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.info( ex.getMessage());
			System.out.println(query);
			count = -1;
		}
//		close();
		return count;
	}

	// 插入数据（需要知道在那个数据库的那个表）
	public static int insertData(String dbName, String tableName,String body) throws SQLException{
		int count = 0;
		
		try {
			conn = openConnection();
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			String sql = "";
			Map<List<String>, List<String>> map = ParamHelper.insertData(body);
			for (List<String> key : map.keySet()) {
				List<String> valueList = map.get(key);
				for (int i = 0; i < key.size(); i++) {
					sql = "insert into " + tableName + " ( " + key.get(i) + " ) values ( "
							+ valueList.get(i) + " ) ";
					ps = conn.prepareStatement(sql);
					count = ps.executeUpdate() + count;
				}
			}
			conn.commit();
		} catch (SQLException ex) {
			conn.rollback();
			logger.info( ex.getMessage());
			ex.printStackTrace();
		}
		close();
		return count;
	}

	// 删除数据
	public static int deleteData(String dbName, String tableName, String query) throws SQLException{
		int count = 0;
		try {
			conn = openConnection();
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			String sqlQuery = SqlHelper.sqlQuerys(query);
			StringBuffer sql = new StringBuffer("delete from  " + tableName  +"  where    " + sqlQuery);
			ps = conn.prepareStatement(sql.toString());
			count = ps.executeUpdate();
			conn.commit();
		} catch (Exception ex) {
			conn.rollback();
			logger.info( ex.getMessage());
			ex.printStackTrace();
		}
		close();
		return count;
	}

	//更新缓存
	public static Map<String, String> getMysqlData(){
		Map<String, String> map = new HashMap<String, String>();
		try {
			conn = openConnection();
			String sql = " select mkey, mvalue from test2 where status = 0 ";
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery(); // 待修改
			while (rs.next()) {
				String key = rs.getString(1);
				String value = rs.getString(2);
				map.put(key, value);
			}
			close();
		} catch (Exception ex) {
			logger.info( ex.getMessage());
			ex.printStackTrace();
		}
		close();
		return map;
	}
	
	//修改数据
	public static int updateData(String dbName, String tableName, String body) throws SQLException {
		int count = 0;
		try {
			conn = openConnection();
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			String sqlQuery = ParamHelper.updateData(body);
			String sql = " update " + tableName + " set " + sqlQuery + " ";
			ps = conn.prepareStatement(sql);
			count = ps.executeUpdate();
			if(count>0){
				conn.commit();
			}else{
				conn.rollback();
			}
		} catch (Exception ex) {
			conn.rollback();
			logger.info( ex.getMessage());
			ex.printStackTrace();
		}
		close();
		return count;
	}
	
	// 关闭数据库连接
	public static void close(){
		try {
			if (rs != null) {
				rs.close();
			}
			if (ps != null) {
				ps.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
