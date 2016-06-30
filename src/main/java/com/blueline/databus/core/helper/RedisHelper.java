package com.blueline.databus.core.helper;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.Jedis;

public class RedisHelper extends BinaryJedis {

	private static final String host = "127.0.0.1";
	private static final int port = 6379;
	private static Jedis jedisClient = null;

	/**
	 * 单例获取jedis对象
	 * @return
	 */
	public static Jedis getJedisClient() {
		if (jedisClient == null || !jedisClient.isConnected()) {
			jedisClient = new Jedis(host, port);
			jedisClient.select(1);
			return jedisClient;
		} else {
			jedisClient.select(1);
			return jedisClient;
		}
	}

	/**
	 * 根据key获取value值
	 * @param hashKey
	 * @return
	 */
	public static String getValue(String hashKey) {
		jedisClient = getJedisClient();
		return jedisClient.get(hashKey);
	}

	// 添加或修改redis数据
	public static boolean insertOrUpdate(String key, String value) {
		jedisClient = getJedisClient();
		jedisClient.set(key, value);
		return true;
	}

	// 删除redis数据
	public static boolean redisDataDel(String hashKey) {
		jedisClient = getJedisClient();
		jedisClient.del(hashKey);
		return true;
	}

	// 判断key值是否存在
	public static boolean existsKey(String hashKey) {
		jedisClient = getJedisClient();
		return jedisClient.exists(hashKey);
	}
	
	/**
	 * 对list操作 添加到redis关联表
	 * @param hashKey 
	 * @param list
	 * @return
	 */
	public static boolean insertListMap(String tableName,List<Map<Object,Object>> list,String hashKey) {
		jedisClient = getJedisClient();
		jedisClient.del(tableName);
		jedisClient.lpush(tableName, hashKey);
		StringBuilder sb = new StringBuilder("[");
		for(int i=0;i<list.size();i++){
			JSONObject jsonObject = new JSONObject(list.get(i));
			String sto = jsonObject.toString();
			sb.append(sto);
			if(i != list.size()-1){
				sb.append(",");
			}
		}
		sb.append("]");
		insertOrUpdate(hashKey,sb.toString());
		return jedisClient.exists(tableName);
	}
	
	/**
	 * 如果有tableName 添加listValue
	 * @param list 
	 * @param tableName
	 * @param url
	 */
	public static void addListValue(List<Map<Object, Object>> list, String tableName, String hashKey) {
		jedisClient = getJedisClient();
		jedisClient.lpush(tableName, hashKey);
		StringBuilder sb = new StringBuilder("[");
		for(int i=0;i<list.size();i++){
			JSONObject jsonObject = new JSONObject(list.get(i));
			String sto = jsonObject.toString();
			sb.append(sto);
			if(i != list.size()-1){
				sb.append(",");
			}
		}
		sb.append("]");
		insertOrUpdate(hashKey,sb.toString());
	}
	
	/**
	 * 对list操作 清空tableName缓存
	 */
	public static void deltListMap(String tableName) {
		jedisClient = getJedisClient();
		List<String> list = jedisClient.lrange(tableName,0,-1);
		jedisClient.del(tableName);
		for(int i=0;i<list.size();i++){
			redisDataDel(list.get(i));
		}
	}

	public static List<String> getListValue(String string) {
		jedisClient = getJedisClient();
		return jedisClient.lrange(string,0,-1);
	}

	/**
	 * 自增长api访问次数
	 * @param api
	 */
	public static void addCountForApi(String api){
		jedisClient = getJedisClient();
		jedisClient.incr(api);
	}

	public static void insertAccessibities(String hashKey, String time) {
		jedisClient = getJedisClient();
		jedisClient.select(0);
		jedisClient.set(hashKey, time);
	}

}