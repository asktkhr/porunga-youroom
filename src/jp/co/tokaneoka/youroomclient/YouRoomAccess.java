package jp.co.tokaneoka.youroomclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

public class YouRoomAccess {

	private static final String CONSUMER_KEY = "***************";
	private static final String CONSUMER_SECRET = "***************";

	private static final String SIGNATURE_METHOD = "HMAC-SHA1";
	private static final String OAUTH_VERSION = "1.0";
	private static final String ALGOTITHM = "HmacSHA1";
	private Map<String, String> parameterMap;
	private String method = "";
	private String api = "";
	private String oauthToken = null;
	private String oauthTokenSecret = null;

	private SortedMap<String, String> oauthParametersMap;

	/*
	 * youroom.propertiesからconsumer_key/consumer_secretを読み込みたい。。。 static {
	 * loadProperties(); }
	 */

	public void setOauthToken(String oauthToken) {
		this.oauthToken = oauthToken;
	}

	public void setApi(String api) {
		this.api = api;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public void setParameter(Map<String, String> parameterMap) {
		this.parameterMap = parameterMap;
	}

	public void setOauthTokenSecret(String oauthTokenSecret) {
		this.oauthTokenSecret = oauthTokenSecret;
	}

	public HttpResponse requestPost() {

		oauthParametersMap = createParametersMap();
		String apiParamter = createParameters();
		HttpResponse objResponse = null;

		HttpClient objHttp = new DefaultHttpClient();
		HttpPost objPost = new HttpPost(api + (apiParamter.length() > 0 ? "?" + apiParamter : ""));
		try {
			objPost.addHeader("Authorization", createAuthorizationValue());
			objResponse = objHttp.execute(objPost);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return objResponse;
	}

	public HttpResponse requestGet() {

		oauthParametersMap = createParametersMap();
		String apiParamter = createParameters();
		HttpResponse objResponse = null;

		HttpClient objHttp = new DefaultHttpClient();
		HttpGet objGet = new HttpGet(api + (apiParamter.length() > 0 ? "?" + apiParamter : ""));
		try {
			objGet.addHeader("Authorization", createAuthorizationValue());
			objResponse = objHttp.execute(objGet);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return objResponse;
	}

	private SortedMap<String, String> createParametersMap() {
		SortedMap<String, String> map = new TreeMap<String, String>();
		map.put("oauth_consumer_key", CONSUMER_KEY);
		map.put("oauth_nonce", UUID.randomUUID().toString());
		map.put("oauth_signature_method", SIGNATURE_METHOD);
		map.put("oauth_timestamp", getTimeStamp());
		map.put("oauth_version", OAUTH_VERSION);
		if (oauthToken != null)
			map.put("oauth_token", oauthToken);
		return map;
	}

	private String getKey() {
		String result = "";
		StringBuilder builder = new StringBuilder();
		builder.append(CONSUMER_SECRET);
		builder.append("&");
		if (oauthTokenSecret != null)
			builder.append(oauthTokenSecret);
		result = builder.toString();
		return result;
	}

	private String getTimeStamp() {
		return Long.toString(System.currentTimeMillis() / 1000);
	}

	private String createParameters() {
		if (parameterMap == null || parameterMap.size() == 0) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<String, String> param : parameterMap.entrySet()) {
			builder.append(param.getKey() + "=");
			builder.append(param.getValue());
			builder.append("&");
		}
		return builder.toString().substring(0, builder.length() - 1);
	}

	private String createAuthorizationValue() throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException {
		/*
		 * http://oauth.net/core/1.0/#nonce 「5.4.1. Authorization Header」
		 * Authorization Headerの作成
		 */
		String result = "";
		StringBuilder builder = new StringBuilder();
		builder.append("OAuth ");
		for (Map.Entry<String, String> param : oauthParametersMap.entrySet()) {
			builder.append(param.getKey() + "=");
			builder.append("\"" + param.getValue() + "\",");
		}
		// TODO http://oauth.net/core/1.0/#signing_process 9.Signing Requestを参照
		builder.append("oauth_signature" + "=");
		builder.append("\"" + getSignature(getSignatureBaseString(), getKey()) + "\"");
		result = builder.toString();
		return result;
	}

	private String getSignatureBaseString() throws UnsupportedEncodingException {
		return method + "&" + encodeURL(api) + "&" + SignatureEncode.encode(getRequestParameters());
	}

	private String encodeURL(String str) {
		String encord = null;
		try {
			encord = URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException ignore) {
		}
		return encord;
	}

	private String getRequestParameters() {
		if (parameterMap != null && parameterMap.size() > 0) {
			for (Map.Entry<String, String> param : parameterMap.entrySet()) {
				oauthParametersMap.put(param.getKey(), param.getValue());
			}
		}
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<String, String> param : oauthParametersMap.entrySet()) {
			builder.append(param.getKey());
			builder.append("=");
			builder.append(param.getValue());
			builder.append("&");
		}
		return builder.toString().substring(0, builder.length() - 1);
	}

	private String getSignature(String signatureBaseString, String keyString) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
		Mac mac = Mac.getInstance(ALGOTITHM);
		Key key = new SecretKeySpec(keyString.getBytes(), ALGOTITHM);
		mac.init(key);
		byte[] digest = mac.doFinal(signatureBaseString.getBytes());
		return encodeURL(Base64.encodeBytes(digest));
	}

	/*
	 * private static void loadProperties() { try { InputStream inputStream =
	 * new FileInputStream(new File("./youroom.properties")); Properties
	 * configuration = new Properties(); configuration.load(inputStream);
	 * CONSUMER_KEY = configuration.getProperty("consumer_key", "");
	 * CONSUMER_SECRET = configuration.getProperty("consumer_secret", ""); }
	 * catch (FileNotFoundException e) { e.printStackTrace(); } catch
	 * (IOException e) { e.printStackTrace(); } }
	 */

}
