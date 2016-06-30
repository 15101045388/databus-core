package com.blueline.databus.core.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blueline.databus.common.RestResult;
import com.blueline.databus.common.ResultType;
import com.blueline.databus.common.helpers.HashStringHelper;
import com.blueline.databus.core.helper.DbHelper;
import com.blueline.databus.core.helper.RedisHelper;
import com.blueline.databus.core.helper.UrlHelper;
import com.google.gson.Gson;

@RestController
@EnableConfigurationProperties
@RequestMapping("/data")
public class CacheController{

	private final Logger logger = Logger.getLogger(CacheController.class);
	
	@Autowired
	private HttpServletRequest request;
	
	/**
	 * 查看数据库名称
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	public List<Object> getDbInfo(){
		
		List<Object> list = new ArrayList<Object>();
		Object outSteam = request.getSession().getAttribute("outSteam");
		JSONObject jb = new JSONObject();
		
		if (!StringUtils.isEmpty(outSteam)) {
			jb = new JSONObject(outSteam.toString());
		}
		// 判断是否有权限查看
		if (!jb.isNull("type") && (jb.get("type").equals("ERROR")||jb.get("type").equals("FAIL"))) {
			list.add(jb.get("type"));
			list.add(jb.get("message"));
			return list;
		} else {
			// 从数据库查看所有数据库名称
			return DbHelper.getDataInfo();
		}
	}

	// 2、查询单个数据库信息（显示所有表信息）
	/**
	 * 查看单个数据库中的所有表信息
	 * @param dbName
	 * @return
	 */
	@RequestMapping(value = "/{dbName}", method = RequestMethod.GET)
	public List<Map<Object, Object>> getOneDbInfo(@PathVariable("dbName") String dbName){
		List<Map<Object, Object>> list = new ArrayList<Map<Object, Object>>();
		Object outSteam = request.getSession().getAttribute("outSteam");
		JSONObject jb = new JSONObject();
		if (!StringUtils.isEmpty(outSteam)) {
			jb = new JSONObject(outSteam.toString());
		}
		Map<Object, Object> map = new HashMap<Object, Object>();
		// 是否有权限查看
		if (!jb.isNull("type") && (jb.get("type").equals("ERROR")||jb.get("type").equals("FAIL"))) {
			map.put("type", jb.get("type"));
			map.put("message", jb.get("message"));
			list.add(map);
		} else {
			// 查看数据库信息
			list = DbHelper.getTableInfo(dbName);
		}
		return list;
	}

	/**
	 * 查看表的详细信息
	 * @param dbName 表所在数据库
	 * @param tableName 表名
	 * @return 
	 */
	@RequestMapping(value = "/{dbName}/{tableName}", method = RequestMethod.GET)
	public List<Object> getOneTableInfo(
		@PathVariable("dbName") 	String dbName,
		@PathVariable("tableName") 	String tableName
	){
		List<Object> list = new ArrayList<Object>();
		Object outSteam = request.getSession().getAttribute("outSteam");
		JSONObject jb = new JSONObject();
		if (!StringUtils.isEmpty(outSteam)) {
			jb = new JSONObject(outSteam.toString());
		}
		// 判断用户请求是否有权限
		if (!jb.isNull("type") && (jb.get("type").equals("ERROR")||jb.get("type").equals("FAIL"))) {
			list.add(jb.get("type"));
			list.add(jb.get("message"));
		} else {
			// 获取到数据库的表里面的列
			list = DbHelper.getColumn(dbName, tableName);
		}
		return list;
	}

	/**
	 * 删除表数据
	 * @param dbName 表所在数据库
	 * @param tableName 表名
	 * @return
	 */
	@RequestMapping(value = "/{dbName}/{tableName}/_delete", method = RequestMethod.GET)
	public RestResult deleteTableInfo(
		@PathVariable("dbName") 		String dbName,
		@PathVariable("tableName") 		String tableName,
		@RequestParam("accountName") 	String accountName
	){
		Object outSteam = request.getSession().getAttribute("outSteam");
		JSONObject jb = new JSONObject();
		if (!StringUtils.isEmpty(outSteam)) {
			jb = new JSONObject(outSteam.toString());
		}
		if (!jb.isNull("type") && (jb.get("type").equals("ERROR")||jb.get("type").equals("FAIL"))) {
			return new RestResult(ResultType.ERROR,jb.get("message").toString());
		} else {
			try {
				// 查看用户名
				
				int count = DbHelper.deleteData(dbName, tableName+"_"+accountName, request.getQueryString());
				// 删除成功
				if (count > 0) {
					// 清空这个表的缓存
					RedisHelper.deltListMap(tableName);
					return new RestResult(ResultType.OK, "delete success!!!");
				} else{
					return new RestResult(ResultType.FAIL,"delete failed!!!");
				}
			} catch (SQLException ex) {
				logger.info(ex.getMessage());
				return new RestResult(ResultType.ERROR,ex.getMessage());
			}
		}
	}

	/**
	 * 新增数据
	 * @param dbName 表所在数据库
	 * @param tableName 表名
	 * @return
	 */
	@RequestMapping(value = "/{dbName}/{tableName}/_add", method = RequestMethod.POST)
	public RestResult insertTableInfo(
		@PathVariable("dbName") String dbName,
		@PathVariable("tableName") String tableName,
		@RequestParam("accountName") 	String accountName
	) {
		String body = request.getSession().getAttribute("body").toString();
		Object outSteam = request.getSession().getAttribute("outSteam");
		JSONObject jb = new JSONObject();
		if (!StringUtils.isEmpty(outSteam)) {
			jb = new JSONObject(outSteam.toString());
		}
		if (!jb.isNull("type") && (jb.get("type").equals("ERROR")||jb.get("type").equals("FAIL"))) {
			return new RestResult(ResultType.ERROR,jb.get("message").toString());
		} else {
			// 获取插入sql语句
			try {
				int count = DbHelper.insertData(dbName, tableName+"_"+accountName, body);
				if (count > 0) {
					//清空当前表的缓存。
					RedisHelper.deltListMap(tableName);
					logger.info( " insert success ");
					return new RestResult(ResultType.OK, "insert success!!!");
				} else {
					logger.info(  " insert failed ");
					return new RestResult(ResultType.FAIL,"insert failed!!!");
				}
			} catch (SQLException ex) {
				logger.info( ex.getMessage());
				return new RestResult(ResultType.ERROR,ex.getMessage());
			}
		}
	}

	/**
	 * 修改表信息
	 * POST
	 * @param dbName
	 * @param tableName
	 * @return
	 */
	@RequestMapping(value = "/{dbName}/{tableName}/_update", method = RequestMethod.POST)
	public RestResult updateTableInfo(
		@PathVariable("dbName") String dbName,
		@PathVariable("tableName") String tableName,
		@RequestParam("accountName") String accountName
	) {
		String body = request.getSession().getAttribute("body").toString();
		Object outSteam = request.getSession().getAttribute("outSteam");
		JSONObject jb = new JSONObject();
		if (!StringUtils.isEmpty(outSteam)) {
			jb = new JSONObject(outSteam.toString());
		}
		if (!jb.isNull("type") && (jb.get("type").equals("ERROR")||jb.get("type").equals("FAIL"))) {
			return new RestResult(ResultType.ERROR,jb.get("message").toString());
		} else {
			// 获取修改sql语句
			try {
				int count = DbHelper.updateData(dbName, tableName+"_"+accountName, body);
				if (count > 0) {
					//清空当前表的缓存。
					RedisHelper.deltListMap(tableName);
					logger.info(" update success ");
					return new RestResult(ResultType.OK, "update success!!!");
				} else {
					logger.info(" update failed ");
					return new RestResult(ResultType.FAIL,"update failed!!!");
				}
			} catch (SQLException ex) {
				logger.info(new Date() + "——" + ex.getMessage());
				return new RestResult(ResultType.FAIL,ex.getMessage());
			}
		}
	}

	/**
	 * 表数据查询
	 * GET
	 * @param dbName 所在数据库
	 * @param tableName 查询的表
	 * @return 返回结果
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/{dbName}/{tableName}/_data", method = RequestMethod.GET)
	public List<Map<Object, Object>> getTableInfo(
		@PathVariable("dbName") String dbName,
		@PathVariable("tableName") String tableName,
		@RequestParam("accountName") String accountName
	) {
		List<Map<Object, Object>> list = new ArrayList<Map<Object, Object>>();
		Map<Object, Object> map = new HashMap<Object, Object>();
		// 判断用户请求是否有权限
		Object outSteam = request.getSession().getAttribute("outSteam");
		JSONObject jb = new JSONObject();
		if (!StringUtils.isEmpty(outSteam)) {
			jb = new JSONObject(outSteam.toString());
		}
		// 2、根据appke+#+url判断是否有key值
		String appkey = request.getHeader("x-appKey");
		String hashKey = HashStringHelper.hashKey(appkey + "#" + UrlHelper.getUrl(request));
		// 判断用户请求是否有权限
		if (!jb.isNull("type") && (jb.get("type").equals("ERROR")||jb.get("type").equals("FAIL"))) {
			map.put("type", jb.get("type"));
			map.put("message", jb.get("message"));
			list.add(map);
			return list;
		} else {
			if (RedisHelper.existsKey(tableName)) {
				// 读取缓存
				// 1、找到有关表的value值，value为list 查看list中的keys，去一个一个查看redis值。
				List<String> listStr = RedisHelper.getListValue(tableName);
				int count = 0;
				if(listStr.size()>0){
					// 3、有key值，查询缓存并显示
					for(int i=0;i<listStr.size();i++){
						if(listStr.get(i).equals(hashKey)){
							String str = RedisHelper.getValue(hashKey);
							Gson gson = new Gson();
							JSONArray ja = new JSONArray(str);
							for(int k = 0;k<ja.length();k++){
								map = gson.fromJson(ja.get(k).toString(), map.getClass());
								list.add(map);
							}
						    count = 1;
						    break;
						}
					}
				}
				if(count==0){
					// 4、无key值，查询数据库
					list = DbHelper.getDate(dbName, tableName+"_"+accountName, request);
					// 5、添加redis.中的list值
					RedisHelper.addListValue(list,tableName,hashKey);
				}
				return list;
			} else {
				// 读取数据库数据，获取查询sql语句
				list = DbHelper.getDate(dbName, tableName+"_"+accountName, request);
				// 更新缓存——清空tableName缓存，在新增list到缓存
				RedisHelper.insertListMap(tableName, list, hashKey);
				return list;
			}
		}
	}
}
