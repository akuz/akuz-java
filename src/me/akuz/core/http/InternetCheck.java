package me.akuz.core.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import me.akuz.core.StringUtils;


public final class InternetCheck {

	private final static int READ_TIMEOUT = 5000;
//	private final static Logger _log = LogUtils.getLogger(InternetCheck.class.getName());
	
	private final static InternetCheck _instance = new InternetCheck();
	public final static InternetCheck getInstance() { return _instance; }
	
	private final Random _random;
	private final List<String> _websitesToCheck;
	
	private InternetCheck() {
		_random = new Random(new Date().getTime());
		_websitesToCheck = new ArrayList<String>();
		_websitesToCheck.add("http://www.google.com");
		_websitesToCheck.add("http://www.amazon.com");
		_websitesToCheck.add("http://www.yahoo.com");
		_websitesToCheck.add("http://www.bing.com");
		_websitesToCheck.add("http://www.gmail.com");
		_websitesToCheck.add("http://www.hotmail.com");
		_websitesToCheck.add("http://www.ebay.com");
		_websitesToCheck.add("http://www.cnn.com");
		_websitesToCheck.add("http://www.bbc.com");
	}
	
	public void checkInternetConnection() throws NoInternetConnectionException {
		
		int count = 0;
		int[] indices = new int[3];
		while (count < indices.length) {
			int index = _random.nextInt(_websitesToCheck.size());
			boolean isDuplicate = false;
			for (int i=0; i<count; i++) {
				if (indices[i] == index) {
					isDuplicate = true;
					break;
				}
			}
			if (isDuplicate) {
				continue;
			}
			indices[count] = index;
			count++;
		}

		int successCount = 0;
		List<String> failedUrls = null;
		
		for (int i=0; i<indices.length; i++) {
			int index = indices[i];
			String urlString = _websitesToCheck.get(index);
			
			try {
				
				URL url;
				try {
					url = new URL(urlString);
				} catch (MalformedURLException e) {
					throw new NoInternetConnectionException("check failed, malformed url: " + urlString);
				}
				URLConnection urlConn = url.openConnection();
				
				if ((urlConn instanceof HttpURLConnection) == false) {
					throw new NoInternetConnectionException("check failed, non-http url: " + urlString);
				}
				
				HttpURLConnection httpConn = (HttpURLConnection)urlConn;
				httpConn.setReadTimeout(READ_TIMEOUT);
				httpConn.setRequestMethod("GET");
				httpConn.connect();
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
		
				try {
					while ((reader.readLine()) != null) {
						// reading all...
					}
				} finally {
					reader.close();
				}
				
				successCount += 1;
				
			} catch (UnknownHostException e) {
				
//				_log.warning("Could not get " + urlString + " - Unknown host");
				
				if (failedUrls == null) {
					failedUrls = new ArrayList<String>();
				}
				failedUrls.add(urlString);
				
			} catch (IOException e) {

//				_log.warning("Could not get " + urlString + " - IOException: " + e.getMessage());
				
				if (failedUrls == null) {
					failedUrls = new ArrayList<String>();
				}
				failedUrls.add(urlString);
			}
		}
		
		if (successCount < indices.length / 2.0) {
			String diagnosticMessage = "no response from " + StringUtils.collectionToString(failedUrls, ", ");
//			_log.warning("No Internet connection (" + diagnosticMessage + ")");
			throw new NoInternetConnectionException(diagnosticMessage);
		}
	}

}
