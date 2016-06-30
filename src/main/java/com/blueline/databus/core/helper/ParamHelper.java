package com.blueline.databus.core.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

public class ParamHelper {

	/**
	 * 验证是否为空，都是空返回false，都不为空返回true
	 * 
	 * @param name
	 * @param api
	 * @return
	 * @throws Exception
	 */
	public static boolean validateParam(String name, String api) throws Exception {
		if (StringUtils.isEmpty(name) || StringUtils.isEmpty(api)) {
			return false;
		}
		return true;
	}

	// 通过body信息创建 建表SQL
	public static String createQueryForNewTable(String requestBody) throws Exception {
		StringBuffer tmp = new StringBuffer();
		
		JSONObject jsonBody = null;
		JSONArray jsonFields = null;
		
		try {
			jsonBody = new JSONObject(requestBody);
			jsonFields = jsonBody.getJSONArray("fields");
		}
		catch(JSONException jex) {
			throw new Exception("JSON格式非法或未包含fields字段");
		}
		
		// 拼接创建表字符串
		String tableName   = jsonBody.get("name").toString();
		String accountName = jsonBody.get("account_name").toString();
		
		if (StringUtils.isEmpty(tableName) || StringUtils.isEmpty(accountName)) {
			throw new Exception("JSON中table name或account name为空");
		}	
		
		if (jsonFields.length() < 1) {
			throw new Exception("fields字段为空");
		}	
		
		tmp.append("CREATE TABLE " + tableName + "_" + accountName + " ( ");
		for (int i = 0; i < jsonFields.length(); i++) {
			JSONObject field_info = jsonFields.getJSONObject(i);
			
			if (field_info.isNull("col_name")) {
				throw new Exception("fields中无col_name字段");
			}
			else {
				Object col_name = field_info.get("col_name");
				if (col_name != null) {
					tmp.append(col_name + " ");
				}
				else {
					throw new Exception("fields中col_name字段值为空");
				}
			}
			
			if (field_info.isNull("data_type")) {
				throw new Exception("fields中无data_type字段");
			}
			
			else {
				Object data_type = field_info.get("data_type");
				if (data_type != null) {
					String real_type = TypesHelper.typeConvert(data_type.toString());
					if(real_type == null){
						throw new Exception("data_type字段值：" + data_type + " 不是当前系统有效type");
					}
					tmp.append(real_type + " ");
				}
				else {
					throw new Exception("fields中data_type字段值为空");
				}
			}
			
			if (!field_info.isNull("length")) {
				tmp.append(" (" + field_info.get("length") + ") ");
			}

			if (!field_info.isNull("is_pk") && field_info.get("is_pk").equals("true")) {
				tmp.append(" PRIMARY KEY AUTO_INCREMENT ");
			}
			
			if (!field_info.isNull("default")) {
				tmp.append(" DEFAULT " + field_info.get("default") + " ");
			}
			
			if (!field_info.isNull("is_null") && field_info.get("is_null").equals("true")) {
				tmp.append(" NOT NULL ");
			}
			
			if (!field_info.isNull("comment")){
				tmp.append(" COMMENT '"+field_info.get("comment") + "' ");
			}
			
			// for each loop time (not last time), append a comma at the end
			if (i != jsonFields.length() - 1) {
				tmp.append(", ");
			}
		}
			
		// append comments for the table
		if (!jsonBody.isNull("comment")) {
			String tableComment = jsonBody.get("comment").toString();
			tmp.append(" COMMENT '" + tableComment + "' ");
		}
		
		tmp.append(")");
		return tmp.toString();
	}

	// 多条数据插入
	public static Map<List<String>, List<String>> insertData(String query) {
		// 解析json拼接字符串
		JSONArray ja = new JSONArray(query);
		// 存放name、age字段
		List<String> keyList = new ArrayList<String>();
		// 存放name、age字段的值
		List<String> valueList = new ArrayList<String>();
		// 存放name、age字段的集合（插入多条数据）
		List<List<String>> listKey = new ArrayList<List<String>>();
		// 插入name、age字段的值得集合（插入多条数据）
		List<List<String>> listValue = new ArrayList<List<String>>();
		// 存放name、age字段、值得集合的map（一条数据也存）
		Map<List<String>, List<String>> map = new HashMap<List<String>, List<String>>();

		// 解析json
		for (int i = 0; i < ja.length(); i++) {
			JSONObject jb = (JSONObject) ja.get(i);
			@SuppressWarnings("unchecked")
			Iterator<String> iterator = jb.keys();
			keyList = new ArrayList<String>();
			valueList = new ArrayList<String>();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				String value = jb.getString(key);
				keyList.add(key);
				valueList.add(value);
			}
			// 将json中字段名称存在 listKey中
			listKey.add(keyList);
			// 将json中字段的值存在 listValue中
			listValue.add(valueList);
		}
		String ke = "";
		keyList = new ArrayList<String>();
		valueList = new ArrayList<String>();
		for (int i = 0; i < listKey.size(); i++) {
			List<String> list = listKey.get(i);
			ke = "";
			for (int k = 0; k < list.size(); k++) {
				if (k == list.size() - 1) {
					ke += list.get(k);
				} else {
					ke += list.get(k) + ",";
				}
			}
			keyList.add(ke);
		}
		for (int i = 0; i < listValue.size(); i++) {
			List<String> list = listValue.get(i);
			ke = "";
			for (int k = 0; k < list.size(); k++) {
				if (k == list.size() - 1) {
					ke += "'" + list.get(k) + "'";
				} else {
					ke += "'" + list.get(k) + "',";
				}
			}
			valueList.add(ke);
		}
		map.put(keyList, valueList);
		return map;
	}

	/**
	 * 获取修改语句
	 * 
	 * @param body
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static String updateData(String body) {
		JSONArray ja = new JSONArray(body);
		StringBuilder sb = new StringBuilder();
		String id = "";
		for(int i=0;i<ja.length();i++){
			JSONObject jb = new JSONObject(ja.get(i).toString());
			Iterator<String> iterator = jb.keys();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				String value = jb.getString(key);
				if(key.equals("id")){
					id = " where id = '" + value + "'";
				}else{
					sb.append(" " + key + "='" + value + "',");
				}
			}
		}
//		String i = sb.substring(0, sb.length()-1) + id;
		return sb.substring(0, sb.length()-1) + id;
	}

	public static String getTableName(String requestBody) throws Exception {
		JSONObject jsonBody = null;
		
		try {
			jsonBody = new JSONObject(requestBody);
		}
		catch(JSONException jex) {
			throw new Exception("JSON格式非法或未包含fields字段");
		}
		
		Object tableName = jsonBody.get("name");
		if (StringUtils.isEmpty(tableName)) {
			throw new Exception("JSON中table name为空");
		}
		return tableName.toString();
	}

	public static String getUserName(String requestBody) throws Exception {
		JSONObject jsonBody = null;
		
		try {
			jsonBody = new JSONObject(requestBody);
		}
		catch(JSONException jex) {
			throw new Exception("JSON格式非法或未包含fields字段");
		}
		
		Object accountName = jsonBody.get("account_name");
		if (StringUtils.isEmpty(accountName)) {
			throw new Exception("JSON中account name为空");
		}
		return accountName.toString();
	}
	
}
