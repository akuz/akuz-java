package me.akuz.core.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.InvalidParameterException;
import java.util.Map;

public final class HttpUtils {

	public static final URLConnection openGoogleNewsCrawlerConnection(String urlString) throws IOException {
		return openCrawlerConnection("Googlebot-News", urlString);
	}
	
	public static final URLConnection openReadrzCrawlerConnection(String urlString) throws IOException {
		return openCrawlerConnection("Readrzbot", urlString);
	}
	
	public static final URLConnection openCrawlerConnection(String agent, String urlString) throws IOException {
		
		if (urlString == null || urlString.length() == 0) {
			throw new InvalidParameterException("urlString must not be empty");
		}
		URL url = new URL(urlString);
		
		URLConnection conn = url.openConnection();
		conn.setRequestProperty("User-Agent", agent);
		conn.setConnectTimeout(10000);
		
		return conn;
	}
	
	public static final String getPOST(String urlString, Map<String, String> params) throws IOException {
		return getPOST(urlString, params, "UTF-8");
	}	
	
	public static final String getPOST(String urlString, Map<String, String> params, String encoding) throws IOException {

		URLConnection conn = openReadrzCrawlerConnection(urlString);
		
		try {
			HttpURLConnection httpConn = (HttpURLConnection)conn;
			
			httpConn.setRequestMethod("POST");
			StringBuilder sb = new StringBuilder();
			if (params != null) {
				for (String key : params.keySet()) {
					String value = params.get(key);
					if (sb.length() > 0) {
						sb.append("&");
					}
					sb.append(key);
					sb.append("=");
					sb.append(URLEncoder.encode(value, encoding));
				}
			}
			String data = sb.toString();
			httpConn.setDoInput(true);
			httpConn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Content-Length", String.valueOf(data.length()));
			httpConn.connect();

			OutputStream os = conn.getOutputStream();
			os.write(data.getBytes());

			int httpCode = httpConn.getResponseCode();
			if (httpCode != 200) {
				throw new Non200HttpCodeExeption(httpCode, urlString);
			}

			StringBuffer sb2 = new StringBuffer();
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), encoding));
	
			try {
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb2.append(line);
				}
			} finally {
				reader.close();
			}
			
			return sb2.toString();
			
		} finally {
			conn.getInputStream().close();
		}
	}

}
