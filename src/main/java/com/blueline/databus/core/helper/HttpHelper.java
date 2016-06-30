package com.blueline.databus.core.helper;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.blueline.databus.common.helpers.HashStringHelper;

public class HttpHelper {

	public static String getData(String validataURL) throws IOException {

		// 测试数据
		URL url = new URL(validataURL); // 创建URL对象
		// 返回一个URLConnection对象，它表示到URL所引用的远程对象的连接
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(5000); // 设置连接超时为5秒
		conn.setRequestMethod("GET"); // 设定请求方式
		conn.connect(); // 建立到远程对象的实际连接
		// 返回打开连接读取的输入流
		// 判断是否正常响应数据
		InputStream is = conn.getInputStream();
		ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = -1;
		while ((len = is.read(buffer)) != -1) {
			outSteam.write(buffer, 0, len);
		}
		outSteam.close();
		is.close();
		if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
			System.out.println("网络错误异常！!!!");
		}
		if (conn != null) {
			conn.disconnect(); // 中断连接
		}
		return outSteam != null ? outSteam.toString() : null;
	}

	/**
	 * create interface after creating table request for ALL DAY
	 * 
	 * @param dbName
	 * db to create such table & interface
	 * @param requestBody
	 * used to create table
	 */
	public static void addInterface(String dbName, String userName, String tableName) throws Exception {
		addInterface(dbName, userName, tableName, "1"); // "1" means all day
	}
	
	private static void addInterface(
		String dbName, String userName, String tableName, String time
	) throws Exception {
		
		String add    = "/data/" + dbName + "/" + tableName + "_" + userName + "/_add";
		String delete = "/data/" + dbName + "/" + tableName + "_" + userName + "/_delete";
		String update = "/data/" + dbName + "/" + tableName + "_" + userName + "/_update";
		String data   = "/data/" + dbName + "/" + tableName + "_" + userName + "/_data";
		String addMethod = "POST";
		String deleteMethod = "GET";
		String updateMethod = "POST";
		String dataMethod = "GET";
		List<String> listApi = new ArrayList<String>();
		listApi.add(add);
		listApi.add(delete);
		listApi.add(update);
		listApi.add(data);
		List<String> listMethod = new ArrayList<String>();
		listMethod.add(addMethod);
		listMethod.add(deleteMethod);
		listMethod.add(updateMethod);
		listMethod.add(dataMethod);
		Map<Object, Object> requestParamsMap = new HashMap<Object, Object>();
		requestParamsMap.put("title", " ");
		requestParamsMap.put("desr", " ");
		requestParamsMap.put("accountName", userName);
		requestParamsMap.put("tableName", tableName + "_" + userName);
		requestParamsMap.put("type", " ");
		List<Object> interIdList = new ArrayList<Object>();
		for (int i = 0; i < listApi.size(); i++) {
			requestParamsMap.put("method", listMethod.get(i));
			requestParamsMap.put("address", listApi.get(i));
			String value = postinterfaceData("interface", requestParamsMap);
			System.out.println( " return interface value = " + value);
			JSONObject jo = new JSONObject(value);
			JSONObject jd = new JSONObject(jo.get("message").toString());
			interIdList.add(jd.get("id"));
		}
		addAccessibities(userName, interIdList, listApi, listMethod);
	}

	private static void addAccessibities(
		String userName,  
		List<Object> interIdList, 
		List<String> interAddressList,
		List<String> interMethodList
	)throws Exception {
		Map<Object, Object> requestParamsMap = new HashMap<Object, Object>();
		requestParamsMap.put("accountName", userName);
		requestParamsMap.put("accessTime", "1");
		requestParamsMap.put("status", "0");
		requestParamsMap.put("expiredAt", " ");
		requestParamsMap.put("createdBy", " ");
		for (int i = 0; i < interIdList.size(); i++) {
			requestParamsMap.put("interfaceId", interIdList.get(i));
			String value = postinterfaceData("accessibities", requestParamsMap);
			System.out.println( " return Accessibities value = " + value);
		}
		addAccessibitiesToRedis(userName,interAddressList,interMethodList);
	}

	private static void addAccessibitiesToRedis(
		String userName,
		List<String> addressList,
		List<String> methodList
	) {
		for (int i = 0; i < addressList.size(); i++) {
			String hashKey = HashStringHelper.hashKey(userName + "#" + addressList.get(i) + "#" + methodList.get(i));
			RedisHelper.insertAccessibities(hashKey,"1");
		}
		
	}

	// 发送post请求向tableName中添加四条权限数据
	@SuppressWarnings("rawtypes")
	public static String postinterfaceData(String tableName, Map<Object, Object> requestParamsMap)
			throws Exception {
		String requestUrl = "http://localhost:8082/" + tableName;
		PrintWriter printWriter = null;
		BufferedReader bufferedReader = null;
		StringBuffer responseResult = new StringBuffer();
		HttpURLConnection httpURLConnection = null;
		// 组织请求参数
		StringBuffer params = new StringBuffer();
		Iterator it = requestParamsMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry element = (Map.Entry) it.next();
			params.append(element.getKey());
			params.append("=");
			params.append(element.getValue());
			params.append("&");
		}
		if (params.length() > 0) {
			params.deleteCharAt(params.length() - 1);
		}
		try {
			URL realUrl = new URL(requestUrl);
			// 打开和URL之间的连接
			httpURLConnection = (HttpURLConnection) realUrl.openConnection();
			// 设置通用的请求属性
			httpURLConnection.setRequestProperty("accept", "*/*");
			httpURLConnection.setRequestProperty("connection", "Keep-Alive");
			httpURLConnection.setRequestProperty("Content-Length", String.valueOf(params.length()));
			// 发送POST请求必须设置如下两行
			httpURLConnection.setDoOutput(true);
			httpURLConnection.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			printWriter = new PrintWriter(httpURLConnection.getOutputStream());
			// 发送请求参数
			printWriter.write(params.toString());
			// flush输出流的缓冲
			printWriter.flush();
			// 定义BufferedReader输入流来读取URL的ResponseData
			bufferedReader = new BufferedReader(
					new InputStreamReader(httpURLConnection.getInputStream()));
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				responseResult.append(line);
			}
			httpURLConnection.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return responseResult.toString();
	}

	@SuppressWarnings("rawtypes")
	public static String addTableInfo(String body) {
		JSONObject jb = new JSONObject(body);
		String tablename = jb.get("name").toString();
		String userName = jb.get("account_name").toString();
		JSONArray jb1 = new JSONArray(jb.get("fields").toString());
		//拼接json格式
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < jb1.length(); i++) {
			JSONObject jo = (JSONObject) jb1.get(i);
			if(jo.isNull("comment")){
				if(jo.isNull("length")){
					sb.append("{\"column\":\""+jo.get("col_name").toString()+"\",\"type\":\""+
				jo.get("data_type").toString() + "\",\"description\":\"\",\"length\":\"\"}");
				}else{
					sb.append("{\"column\":\""+jo.get("col_name").toString()+"\",\"type\":\""+
				jo.get("data_type").toString() + "\",\"description\":\"\",\"length\":\""+jo.get("length").toString()+"\"}");
				}
			}else{
				if(jo.isNull("length")){
					sb.append("{\"column\":\""+jo.get("col_name").toString()+"\",\"type\":\""+
				jo.get("data_type").toString() + "\",\"description\":\""+jo.get("comment").toString()+"\",\"length\":\"\"}");
				}else{
					sb.append("{\"column\":\""+jo.get("col_name").toString()+"\",\"type\":\""+
					jo.get("data_type").toString() + "\",\"description\":\""+jo.get("comment").toString()+"\",\"length\":\""+jo.get("length")+"\"}");
				}
			}
			if(i!=jb1.length()-1){
				sb.append(",");
			}
		}
		sb.append("]");
		System.out.println(sb.toString());
		String requestUrl = "http://localhost:8082/table";
		Map<Object, Object> requestParamsMap = new HashMap<Object, Object>();
		requestParamsMap.put("tableName", tablename + "_" + userName);
		requestParamsMap.put("fieldsInfo", sb.toString());
		requestParamsMap.put("accountName", userName);

		PrintWriter printWriter = null;
		BufferedReader bufferedReader = null;
		StringBuffer responseResult = new StringBuffer();
		StringBuffer params = new StringBuffer();
		HttpURLConnection httpURLConnection = null;
		// 组织请求参数
		Iterator it = requestParamsMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry element = (Map.Entry) it.next();
			params.append(element.getKey());
			params.append("=");
			params.append(element.getValue());
			params.append("&");
		}
		if (params.length() > 0) {
			params.deleteCharAt(params.length() - 1);
		}
		try {
			URL realUrl = new URL(requestUrl);
			// 打开和URL之间的连接
			httpURLConnection = (HttpURLConnection) realUrl.openConnection();
			// 设置通用的请求属性
			httpURLConnection.setRequestProperty("accept", "*/*");
			httpURLConnection.setRequestProperty("connection", "Keep-Alive");
			httpURLConnection.setRequestProperty("Content-Length", String.valueOf(params.length()));
			// 发送POST请求必须设置如下两行
			httpURLConnection.setDoOutput(true);
			httpURLConnection.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			printWriter = new PrintWriter(httpURLConnection.getOutputStream());
			// 发送请求参数
			printWriter.write(params.toString());
			// flush输出流的缓冲
			printWriter.flush();
			// 定义BufferedReader输入流来读取URL的ResponseData
			bufferedReader = new BufferedReader(
					new InputStreamReader(httpURLConnection.getInputStream()));
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				responseResult.append(line);
			}
			httpURLConnection.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return userName;
	}

	@SuppressWarnings("rawtypes")
	public static String addDbInfoTable(String dbName,String accountName){
		String requestUrl = "http://localhost:8082/db/" + dbName;
		Map<Object, Object> requestParamsMap = new HashMap<Object, Object>();
		requestParamsMap.put("accountName", accountName);
		PrintWriter printWriter = null;
		BufferedReader bufferedReader = null;
		StringBuffer responseResult = new StringBuffer();
		StringBuffer params = new StringBuffer();
		HttpURLConnection httpURLConnection = null;
		// 组织请求参数
		Iterator it = requestParamsMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry element = (Map.Entry) it.next();
			params.append(element.getKey());
			params.append("=");
			params.append(element.getValue());
			params.append("&");
		}
		if (params.length() > 0) {
			params.deleteCharAt(params.length() - 1);
		}
		try {
			URL realUrl = new URL(requestUrl);
			// 打开和URL之间的连接
			httpURLConnection = (HttpURLConnection) realUrl.openConnection();
			// 设置通用的请求属性
			httpURLConnection.setRequestProperty("accept", "*/*");
			httpURLConnection.setRequestProperty("connection", "Keep-Alive");
			httpURLConnection.setRequestProperty("Content-Length", String.valueOf(params.length()));
			// 发送POST请求必须设置如下两行
			httpURLConnection.setDoOutput(true);
			httpURLConnection.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			printWriter = new PrintWriter(httpURLConnection.getOutputStream());
			// 发送请求参数
			printWriter.write(params.toString());
			// flush输出流的缓冲
			printWriter.flush();
			// 定义BufferedReader输入流来读取URL的ResponseData
			bufferedReader = new BufferedReader(
					new InputStreamReader(httpURLConnection.getInputStream()));
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				responseResult.append(line);
			}
			httpURLConnection.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return responseResult.toString();
	}

}
