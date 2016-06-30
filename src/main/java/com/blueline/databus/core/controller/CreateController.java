package com.blueline.databus.core.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.blueline.databus.common.RestResult;
import com.blueline.databus.common.ResultType;
import com.blueline.databus.core.helper.DbHelper;
import com.blueline.databus.core.helper.HttpHelper;
import com.blueline.databus.core.helper.ParamHelper;

@RestController
@EnableConfigurationProperties
@RequestMapping("/db")
public class CreateController{
	
	@Autowired
	private HttpServletRequest request;

	private final Logger logger = Logger.getLogger(CreateController.class);
	
	/**
	 * 创建数据库
	 * POST
	 * @param name
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST)
	public RestResult createDb() throws Exception {
		String body = request.getSession().getAttribute("body").toString();
		Object outSteam = request.getSession().getAttribute("outSteam");
		JSONObject jb = new JSONObject();
		if (!StringUtils.isEmpty(outSteam)) {
			jb = new JSONObject(outSteam.toString());
		}
		if (!jb.isNull("type") && (jb.get("type").equals("ERROR")||jb.get("type").equals("FAIL"))) {
			logger.info(jb.get("message").toString());
			return new RestResult(ResultType.ERROR,jb.get("message").toString());
		} else {
			JSONObject j = new JSONObject(body);
			if (j.isNull("dbName")) {
				logger.info("dbName is empty,create file");
				return new RestResult(ResultType.ERROR,"dbName is empty,create file");
			} else {
				String dbName = j.get("dbName").toString();
				String accountName = j.get("accountName").toString();
				int count = DbHelper.createDb(dbName);
				if (count > 0) {
					// 添加db信息到dbinfo表中_未完成
					HttpHelper.addDbInfoTable(dbName,accountName);
					// 创建成功
					logger.info("create success!!!");
					return new RestResult(ResultType.OK, "create success!!!");
				} else {
					logger.info("create failed!!!");
					return new RestResult(ResultType.ERROR,"create failed!!!");
				}
			}
		}
	}

	/**
	 * 建表
	 * POST /db/{db_name}/table
	 * @param dbName
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/{dbName}/table", method = RequestMethod.POST)
	public RestResult createDbTable(
		@PathVariable("dbName") String dbName
	) {
		
		try {
			// load request body which saved in session
			// to save body in session due to that body could be cleaned in filtering
			String requestBody = request.getSession().getAttribute("body").toString();
			
			String userName = ParamHelper.getUserName(requestBody);
			String tableName = ParamHelper.getTableName(requestBody);
			
			String query = ParamHelper.createQueryForNewTable(requestBody);
			
			System.out.println("#DEBUG# body => SQL");
			System.out.println("Body: " + requestBody);
			System.out.println("SQL: " + query);
			System.out.println();
			
			logger.info("#DEBUG# body => SQL");
			logger.info("Body: " + requestBody);
			logger.info("SQL: " + query);
			
			// create data table in core service and db
			DbHelper.createDbTable(dbName, query);
			
			// create table info in business service
			HttpHelper.addTableInfo(requestBody);
			
			// create 4 interfaces automatically
			// as well as Accessibilities for this user
			HttpHelper.addInterface(dbName, userName, tableName);
		}
		catch (Exception ex) {
			// log
			logger.info(ex.getMessage());
			// print to console
			ex.printStackTrace();
			// TODO: return ex.getMessage()
			return new RestResult(ResultType.ERROR, ex.getMessage());
		}
		logger.info(" create success ");
		return new RestResult(ResultType.OK, "create success");	
	}
}
