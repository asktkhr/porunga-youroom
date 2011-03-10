package jp.co.tokaneoka.youroomclient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;

public class YouRoomCommand {
	
	private YouRoomAccess youRoomAccess;
	
	public YouRoomCommand(Map<String, String> oauthTokenMap){
		this.youRoomAccess = new YouRoomAccess();
		// TODO oauthToken�̐ݒ��YouRoomAccess�ł�肽��
		youRoomAccess.setOauthToken(oauthTokenMap.get("oauth_token"));
		youRoomAccess.setOauthTokenSecret(oauthTokenMap.get("oauth_token_secret"));
	}
	
	/*	
	public HashMap<String,String> getAccessToken(){
		
		String method = "POST";
		String api = "https://www.youroom.in/oauth/access_token";
		HashMap<String,String> oAuthTokenMap = new HashMap<String,String>();

		youRoomAccess.setMethod(method);
		youRoomAccess.setApi(api);		
		HttpResponse objResponse = youRoomAccess.postRequest();
		//TODO if (objResponse == null )
		
		int statusCode = objResponse.getStatusLine().getStatusCode();
		
		if (statusCode == HttpURLConnection.HTTP_OK) {
			try {
				String result = EntityUtils.toString(objResponse.getEntity(), "UTF-8");
				String[] parameters = result.split("&");
				for (String parameter : parameters) {
					String[] keyAndValue = parameter.split("=");
					if (keyAndValue.length < 2) {
						continue;
					}
					String key = keyAndValue[0];
					String value = keyAndValue[1];
					if("oauth_token".equals(key) || "oauth_token_secret".equals(key)){
						oAuthTokenMap.put(key,value);
					}
				}
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return oAuthTokenMap;
	}
	*/
	
	public String getEntry(String groupParam, String entryId){
		
		String method = "GET";
		String api = "https://www.youroom.in/r/" + groupParam + "/entries/" + entryId;
	   	Map<String, String> parameterMap = new HashMap<String, String>();
    	parameterMap.put("format", "json");
    	
    	youRoomAccess.setMethod(method);    	
    	youRoomAccess.setApi(api);
    	youRoomAccess.setParameter(parameterMap);    	
    	HttpResponse objResponse = youRoomAccess.requestGet();
    	
	 	int statusCode = objResponse.getStatusLine().getStatusCode();
	 	String decodeResult = "";
	   	if (statusCode == HttpURLConnection.HTTP_OK) {
			String result;
			try {
				result = EntityUtils.toString(objResponse.getEntity(), "UTF-8");
				decodeResult = UnicodeEscape.decode(result);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	   	}
		return decodeResult;     	
	}
	
	public String getRoomTimeLine(){
		
    	String method = "GET";
    	String api = "https://www.youroom.in/r/726";
	   	Map<String, String> parameterMap = new HashMap<String, String>();
    	parameterMap.put("format", "json");
    		
    	youRoomAccess.setMethod(method);    	
    	youRoomAccess.setApi(api);
    	youRoomAccess.setParameter(parameterMap);    	
    	HttpResponse objResponse = youRoomAccess.requestGet();
		//TODO if (objResponse == null )

	 	int statusCode = objResponse.getStatusLine().getStatusCode();
	 	String decodeResult = "";
	   	if (statusCode == HttpURLConnection.HTTP_OK) {
			String result;
			try {
				result = EntityUtils.toString(objResponse.getEntity(), "UTF-8");
				decodeResult = UnicodeEscape.decode(result);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	   	}
		return decodeResult; 
	}
	
}