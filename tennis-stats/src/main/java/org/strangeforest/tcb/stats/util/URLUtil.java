package org.strangeforest.tcb.stats.util;

import java.io.*;
import java.net.*;

public abstract class URLUtil {

	private static final int CONNECT_TIMEOUT = 10000;
	private static final int READ_TIMEOUT = 10000;

	public static int checkURL(String url) throws IOException {
		var con = (HttpURLConnection)new URL(url).openConnection();
		con.setRequestMethod("HEAD");
		con.setConnectTimeout(CONNECT_TIMEOUT);
		con.setReadTimeout(READ_TIMEOUT);
		return con.getResponseCode();
	}
}
