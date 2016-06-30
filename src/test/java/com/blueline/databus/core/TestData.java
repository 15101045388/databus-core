package com.blueline.databus.core;

import org.springframework.web.client.RestTemplate;

import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;
import org.junit.runner.RunWith;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class TestData {

	private RestTemplate restTemplate = new TestRestTemplate();
	private final String baseUri = "http://localhost:8083";

	// 创建表
	// @Test
	public void createTable() {
		Object obj = "{\"name\":\"t100\",\"fields\":[{\"col_name\":\"id\",\"data_type\":\"int\",\"is_null\":\"true\",\"auto_increment\":\"true\",\"is_pk\":\"true\",\"length\":\"6\"},{\"col_name\":\"name\",\"data_type\":\"varchar\",\"length\":\"50\"},{\"col_name\":\"age\",\"data_type\":\"int\"}],\"comment\":\"注释\",\"account_name\":\"Administrator\"}";
		String res = restTemplate.postForObject(baseUri + "/db/test", obj, String.class);
		assertThat(res, containsString("ERROR"));
	}

	// 显示表有多少字段信息
	// @Test
	public void showDataTableInfoByColunm() {
		String res = restTemplate.getForObject(baseUri + "/data/test/interface", String.class);
		assertThat(res, containsString("OK"));// 显示表中字段，没有ok，测试错误。
	}
	
	// 显示库里面的表信息
	// @Test
	public void showDataTableInfo() {
		String res = restTemplate.getForObject(baseUri + "/data/test", String.class);
		assertThat(res, containsString("OK"));// 显示表中字段，没有ok，测试错误。
	}
	
	// 显示库信息
	// @Test
	public void showDataInfo() {
		String res = restTemplate.getForObject(baseUri + "/data", String.class);
		assertThat(res, containsString("OK"));// 显示表中字段，没有ok，测试错误。
	}

	// 向zae表新增一条数据
	// @Test
	public void insertDataTable() {
		Object obj = "[{\"name\":\"z\",\"age\":\"12\"},{\"name\":\"z\",\"age\":\"12\"},{\"name\":\"z\",\"age\":\"12\"}]";
		String res = restTemplate.postForObject(baseUri + "/data/test/zae/_add", obj, String.class);
		assertThat(res, containsString("OK"));// 添加表数据，没有ok，测试错误。
	}
	
	//修改一条数据
	//@Test
	public void updateDataTable() {
		Object obj = "{\"name\":\"asdf\",\"age\":\"122\"}";
		String res = restTemplate.postForObject(baseUri + "/data/test/zae/3/_update", obj, String.class);
		assertThat(res, containsString("OK"));// 修改表数据，没有ok，测试错误。
	}

	//查询表数据
//	@Test
	public void DataTable() {
//		Object obj = "{\"name\":\"asdf\",\"age\":\"122\"}";
		String res = restTemplate.getForObject(baseUri + "/data/test/zae/_data?id=1", String.class);
		assertThat(res, containsString("OK"));// 修改表数据，没有ok，测试错误。
	}
	
	//创建db
//	@Test
	public void createDb() {
		Object obj = "{\"name\":\"asdf\"}";
		String res = restTemplate.postForObject(baseUri + "/db", obj, String.class);
		assertThat(res, containsString("OK"));// 修改表数据，没有ok，测试错误。
	}
	
	//删除表数据
	//@Test
	public void deleteTableInfo() {
//		Object obj = "{\"name\":\"asdf\"}";
		String res = restTemplate.getForObject(baseUri + "/data/test/zae/_delete?id=2", String.class);
		assertThat(res, containsString("OK"));// 删除表数据，没有ok，测试错误。
	}
	
}
