package jp.co.tokaneoka.youroomclient;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;

public class YouRoomUtil extends ContextWrapper {

	private static final String PREFERENCE_KEY = "AccessToken";
	SharedPreferences sharedpref;

	public YouRoomUtil(Context base) {
		super(base);
	}
	
	// "2011-03-02T12:46:06Z" -> "2011/03/02 21:46:06"
    public static String convertDatetime(String unformattedTime) {
    	
    	String[] updateTimes = unformattedTime.substring(0, unformattedTime.length() -1).split("T");
    	String[] date = updateTimes[0].split("-");
    	String[] times = updateTimes[1].split(":");
    	int year = Integer.parseInt(date[0]);
    	int month = Integer.parseInt(date[1]);
    	int day = Integer.parseInt(date[2]);
    	int hour = Integer.parseInt(times[0]);
    	int minute = Integer.parseInt(times[1]);
    	int second = Integer.parseInt(times[2]);
    	
    	Calendar cal = new GregorianCalendar(year, month ,day, hour, minute, second);
    	cal.add(Calendar.HOUR, 9);
    	
    	return cal.get(Calendar.YEAR) + "/" + cal.get(Calendar.MONTH) +"/" + cal.get(Calendar.DAY_OF_MONTH) + " " + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
    	
	}
    
	public HashMap<String, String> getOauthTokenFromLocal(){
		
		HashMap<String, String> oAuthTokenMap = new HashMap<String, String>();
		
    	sharedpref = getSharedPreferences(PREFERENCE_KEY, Activity.MODE_APPEND );
		String oauthToken = sharedpref.getString("oauthToken", null);
		String oauthTokenSecret = sharedpref.getString("oauthTokenSecret", null);
		
		oAuthTokenMap.put("oauth_token", oauthToken);
		oAuthTokenMap.put("oauth_token_secret", oauthTokenSecret);
		
		return oAuthTokenMap;
	}

	
}
