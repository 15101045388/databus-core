package com.blueline.databus.core.helper;

import java.io.IOException;
import java.io.InputStream;

public class BodyHelper {

	/**
	 * 读取http body里面的数据
	 * 
	 * @param
	 * @return
	 * @throws IOException
	 */
	public static String readLine(InputStream is) {
		byte[] bytes = new byte[1024 * 1024];
		String body = "";
		try {
			int nRead = 1;
			int nTotalRead = 0;
			while (nRead > 0) {
				nRead = is.read(bytes, nTotalRead, bytes.length - nTotalRead);
				if (nRead > 0)
					nTotalRead = nTotalRead + nRead;
			}
			body = new String(bytes, 0, nTotalRead, "utf-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return body;
	}
}
