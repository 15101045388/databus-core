package com.blueline.databus.core.helper;

import java.io.IOException;

import javax.servlet.ServletResponse;

import org.springframework.util.StringUtils;

public class MessageHelper {
	
	/**
	 * 返回错误信息
	 * @param res  
	 * @param outSteam 错误原因
	 */
	public static void messageInfo(ServletResponse res, String outSteam) {
		try {
			res.setCharacterEncoding("UTF-8");
			byte[] b = null;
			if (!StringUtils.isEmpty(outSteam)) {
				b = outSteam.getBytes();
			}
			res.setContentLength(b.length);
			res.getOutputStream().write(b);
			res.getOutputStream().flush();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
