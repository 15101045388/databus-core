package com.blueline.databus.core.helper;

import javax.servlet.http.HttpServletRequest;

public class UrlHelper {
	public static String getUrl(HttpServletRequest request) {
		String url = "";
//		url = request.getScheme() + "://";
//		url += request.getServerName() + ":";
//		url += request.getServerPort() + "/" ;
		url += request.getServletPath();
		if (request.getQueryString() != null) {
			url += "?" + request.getQueryString();
		}
		return url;
	}
}