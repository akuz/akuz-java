package me.akuz.core;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public final class UrlUtils {
	
	public static String encodeUtf8(String str) {
		try {
			return URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Unsupported encoding", e);
		}
	}
	
	public static String absolutizeUrl(String baseUrl, String relativeUrl) {
		
		int afterProtocolIndex = baseUrl.indexOf("://");
		if (afterProtocolIndex < 0) {
			throw new IllegalArgumentException("Invalid base URL (must contain ://): " + baseUrl);
		} else {
			afterProtocolIndex += 3; // shift by protocol delimiter
		}

		String result;
		if (relativeUrl.contains("://")) {

			result = relativeUrl;
			
		} else {
			
			if (relativeUrl.startsWith("/")) {
				
				int baseServerEnd = baseUrl.indexOf("/", afterProtocolIndex);
				if (baseServerEnd < 0) {
					
					result = String.format("%s%s", baseUrl, relativeUrl);
				
				} else {
					
					result = String.format("%s%s", baseUrl.substring(0, baseServerEnd), relativeUrl);
				}
				
			} else { // relative url doesn't start with slash

				int baseSlashEnd = baseUrl.lastIndexOf("/");
				if (baseSlashEnd < afterProtocolIndex) {
					
					result = String.format("%s/%s", baseUrl, relativeUrl);
				
				} else {
					
					result = String.format("%s/%s", baseUrl.substring(0, baseSlashEnd), relativeUrl);
				}
			}
		}
		
		result = result.trim().replaceAll(" ", "%20");
		return result;
	}
}
