package me.akuz.core.http;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;

/**
 * Performs a specified number of attempts to download a resource,
 * either a text or an image, from a given URL, currently 
 * supporting only HTTP(s) protocol.
 *
 */
public class HttpGetCall implements Callable<Boolean> {

	private final int READ_TIMEOUT = 10000;
	
	private final int _attempts;
	private final HttpGetKind _kind;
	private final String _originalUrl;
	private final String _encoding;
	private boolean _isComplete;
	private String _resultUrl;
	private String _resultText;
	private BufferedImage _resultImage;
	private Exception _exception;
	
	public HttpGetCall(int attempts, HttpGetKind kind, String url, String encoding) {
		if (attempts < 1) {
			throw new InvalidParameterException("attempts must be >= 1");
		}
		if (kind == null) {
			throw new InvalidParameterException("kind cannot be null");
		}
		if (encoding == null || encoding.length() == 0) {
			throw new InvalidParameterException("encoding cannot be null or empty");
		}
		_attempts = attempts;
		_kind = kind;
		_originalUrl = url;
		_encoding = encoding;
	}

	public Boolean call() {
		try {
			
			int attemptsLeft = _attempts;
			while (attemptsLeft > 0) {
				
				try {
					
					// check if URL is empty
					if (_originalUrl == null || _originalUrl.length() == 0) {
						throw new MalformedURLException(_originalUrl);
					}
					
					// create URL connection
					URLConnection urlConn = HttpUtils.openGoogleNewsCrawlerConnection(_originalUrl);
					
					// check this is a HTTP connection
					if ((urlConn instanceof HttpURLConnection) == false) {
						throw new NonHttpProtocolException(urlConn.getClass().getSimpleName());
					}
					
					// connect
					HttpURLConnection httpConn = (HttpURLConnection)urlConn;
					httpConn.setInstanceFollowRedirects(true);
					httpConn.setReadTimeout(READ_TIMEOUT);
					httpConn.setRequestMethod("GET");
					httpConn.connect();
					
					// read
					if (_kind.equals(HttpGetKind.Image)) {
						
						int code = httpConn.getResponseCode();
						if (code == 200) {
						
							// only obtain jpeg images for the moment...
							String resultContentType = httpConn.getContentType();
							boolean inputStreamHasBeenRead = false;
							if (resultContentType != null) {
								if ("image/jpeg".equals(resultContentType.toLowerCase())) {
									_resultImage = ImageIO.read(httpConn.getInputStream());
									_resultUrl = httpConn.getURL().toString();
									inputStreamHasBeenRead = true;
								}
							}
							if (inputStreamHasBeenRead == false) {
								// read result, but don't save
								ImageIO.read(httpConn.getInputStream());
							}
							break;
							
						} else {
							throw new Non200HttpCodeExeption(code, httpConn.getURL().toString());
						}
						
					} else if (_kind.equals(HttpGetKind.Text)) {
						
						int code = httpConn.getResponseCode();
						if (code == 200) {
							
							StringBuffer sb = new StringBuffer();
							BufferedReader reader = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), _encoding));
					
							try {
								String line = null;
								while ((line = reader.readLine()) != null) {
									if (sb.length() > 0) {
										sb.append("\n");
									}
									sb.append(line);
								}
							} finally {
								reader.close();
							}
							
							_resultUrl = httpConn.getURL().toString();
							_resultText = sb.toString();
							break;
		
						} else {
							throw new Non200HttpCodeExeption(code, httpConn.getURL().toString());
						}
						
					} else {
						throw new InvalidParameterException("Downloading for " + HttpGetKind.class.getSimpleName() + ":" + _kind + " is not implemented.");
					}

				} catch (SocketTimeoutException e) {
					attemptsLeft -= 1;
					if (attemptsLeft <= 0) {
						InternetCheck.getInstance().checkInternetConnection();
						throw e;
					}
				} catch (UnknownHostException e) {
					attemptsLeft -= 1;
					if (attemptsLeft <= 0) {
						InternetCheck.getInstance().checkInternetConnection();
						throw e;
					}
				}
			}

			return true;
			
		} catch (Exception e) {
			
			_exception = e;
			return false;

		} finally {
			
			_isComplete = true;
		}
	}
	
	public HttpGetKind getKind() {
		return _kind;
	}
	
	public Exception getException() {
		return _exception;
	}
	
	public String getOriginalUrl() {
		return _originalUrl;
	}
	
	public boolean isComplete() {
		return _isComplete;
	}
	
	public String getResultUrl() {
		return _resultUrl;
	}
	
	public String getResultText() {
		return _resultText;
	}
	
	public BufferedImage getResultImage() {
		return _resultImage;
	}
	
	public String toString() {
		return String.format("%s - %s: %s",
				getClass().getSimpleName(), 
				_kind.toString(), 
				_originalUrl.toString());
	}

}
