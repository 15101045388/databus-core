package com.blueline.databus.core.filter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.blueline.databus.common.helpers.HashStringHelper;
import com.blueline.databus.core.helper.BodyHelper;
import com.blueline.databus.core.helper.DbHelper;
import com.blueline.databus.core.helper.HttpHelper;
import com.blueline.databus.core.helper.MessageHelper;
import com.blueline.databus.core.helper.RedisHelper;
import com.blueline.databus.core.helper.UrlHelper;

@WebFilter(filterName = "cache", urlPatterns = "/*")
public class CacheFilter implements Filter {

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private HttpServletResponse response;
	
	public void destroy() {
	}
	
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		RedisHelper.addCountForApi(request.getServletPath());
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		//防止跨域攻击
		response.setHeader("Access-Control-Allow-Origin","*");
		response.setHeader("Access-Control-Allow-Methods", "POST,GET,DELETE");
		// 判断提交方法
		String method = request.getMethod();
		// 获取mac值
		String mac = request.getHeader("x-mac");
		// 控制中心使用 x-appkey 固定值为 “XYZ12345”目前全部是管理员处理
		String appKey = request.getHeader("x-appKey");
		if(StringUtils.isEmpty(appKey)){
			appKey = "XYZ12345";
		}
		if (appKey.equals("XYZ12345")) {
			// 如果是post提交，获取body值。 
			if (method.equals("POST")) {
				InputStream in = request.getInputStream();
				String body = BodyHelper.readLine(in);
				request.getSession().setAttribute("body", body);
			}
			chain.doFilter(req, res);
		} else {
			// 为空不执行。
			if (StringUtils.isEmpty(appKey)) {
				MessageHelper.messageInfo(res, "appkey is null!!!");
			} else if (StringUtils.isEmpty(mac)) {
				MessageHelper.messageInfo(res, "mac is null!!!");
			} else {
				// 不存在
				// 获取数据库securitykey值，如果为空，返回false。
				Map<String, String> map = DbHelper.getSecurityKey(appKey);
				System.out.println(map);
				if (map.containsKey("securityKey")) {
					// 正确
					String name = map.get("name");
					// 判断securitykey # url的加密后的值与 header中获取的mac值是否一致。
					String macKey = "";
					if (method.equals("GET")) {
						// skey + # + url
						macKey = HashStringHelper.hashKey(map.get("securityKey") + "#" + UrlHelper.getUrl(request));
					} else if (method.equals("POST")) {
						InputStream in = request.getInputStream();
						String body = BodyHelper.readLine(in);
						// skey + # + {body}值hash
						body = body.replace("\n","");
						macKey = HashStringHelper.hashKey(map.get("securityKey") + "#" + body);
						request.getSession().setAttribute("body", body);
					}
					if (!macKey.equals(mac)) {
						// mac值不一致
						MessageHelper.messageInfo(res, "MAC values are not consistent");
					} else {
						String validataURL = "http://localhost:8082/interface/check?a=" + request.getServletPath() + "&n=" + name + "&m=" + method;
						String outSteam = HttpHelper.getData(validataURL);
						request.getSession().setAttribute("outSteam", outSteam);
						chain.doFilter(req, res);
					}
				} else {
					// 不正确的用户请求
					MessageHelper.messageInfo(res, "securitykey is null");
				}
			}
		}
	}

	public void init(FilterConfig arg0) throws ServletException {
	}
}
