package com.blueline.databus.core.helper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.util.StringUtils;

public class SqlHelper {

	public static String sqlQuerys(String query) {
		String sql = "";
		//query为?后面的参数字符串   例如：id=1&name=3&_order=id&_desc
		if (!StringUtils.isEmpty(query)) {
			Map<String, String> retMap = new HashMap<String, String>();
			//分隔所有的查询条件
			String arr[] = query.split("&");
			StringBuffer sqlQuery = new StringBuffer(" ");
			StringBuffer order = new StringBuffer(" ");
			// 把参数写入map
			for (int i = 0; i < arr.length; i++) {
				String start[] = arr[i].split("=");
				// 判断value值是否为空，空新增key，不为空追加value
				if (StringUtils.isEmpty(retMap.get(start[0]))) {
					//获取=后面的值，存在map中以key-value形式
					if (start.length == 1) {
						retMap.put(start[0], "");
					} else {
						retMap.put(start[0], start[1]);
					}
				} else {
					retMap.put(start[0], retMap.get(start[0]) + "#" + start[1]);
				}
			}
			//获取的数据为map格式，例如：{"id":"1","name":"3","_order":"id","_desc":""}
			//遍历map
			Iterator<String> it = retMap.keySet().iterator();
			boolean bool = true;
			while (it.hasNext()) {
				String key = (String) it.next();
				String value = retMap.get(key);
				if (key.startsWith("_") && bool == true) {
					//order by 和 desc、asc 同时存在
					if (key.equals("_order")) {
						order.append(" order by " + retMap.get(key) + " ");
						if (retMap.containsKey("_desc")) {
							order.append(" desc ");
						} else if (retMap.containsKey("_asc")) {
							order.append(" asc ");
						}
					}
					//分页使用
					if (retMap.containsKey("_skip")) {
						order.append(" limit " + retMap.get("_skip"));
						if (retMap.containsKey("_take")) {
							order.append(" , " + retMap.get("_take") + " ");
						}
					}
					//order条件只添加一次，存在临时变量中，最后添加到sql尾部
					bool = false;
				} else if (key.endsWith("start")) {
					sqlQuery.append(" " + key.split("_")[0] + " > " + retMap.get(key) + " and ");
				} else if (key.endsWith("end")) {
					sqlQuery.append(" " + key.split("_")[0] + " < " + retMap.get(key) + " and ");
				} else if (value.contains("#")) {
					String val[] = value.split("#");
					for (int k = 0; k < val.length; k++) {
						if (k == 0) {
							sqlQuery.append(" (" + key + " = '" + val[k] + "' or ");
						} else if (k == val.length - 1) {
							sqlQuery.append(" " + key + " = '" + val[k] + "' ) and ");
						} else {
							sqlQuery.append(" " + key + " = '" + val[k] + "' or ");
						}
					}
				} else if (!key.startsWith("_")) {
					sqlQuery.append(" " + key + " = '" + retMap.get(key) + "' and ");
				}
			}
			sql = sqlQuery.append(" 1 = 1 " + order).toString();
		}
		return sql;
	}
}
