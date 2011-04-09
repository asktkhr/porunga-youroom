package com.porunga.youroomclient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class YouRoomCommandProxy {
	private SQLiteDatabase cacheDb = null;
	private YouRoomCommand youRoomCommand = null;
	
	public YouRoomCommandProxy(Activity activity) {
		cacheDb = ((AppHolder)activity.getApplication()).getCacheDb();
		youRoomCommand = new YouRoomCommand(new YouRoomUtil(activity.getApplication()).getOauthTokenFromLocal());
	}
	
	public YouRoomEntry getEntry(String roomId, String entryId, String updatedTime, int level) {
		YouRoomEntry entry = null;
		
		Cursor c = cacheDb.rawQuery("select entry from entries where entryId = ? and roomId = ? and updatedTime = ? ;", new String[]{entryId, roomId, updatedTime});
		
		if (c.getCount() == 1) {
			Log.i("CACHE", String.format("Cache Hit  [%s]", entryId));
			c.moveToFirst();
			try {
				ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(c.getBlob(0)));
				entry = (YouRoomEntry)ois.readObject();
			} catch (StreamCorruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		else {
			Log.i("CACHE", String.format("Cache Miss [%s]", entryId));
			try {
				JSONObject json = (new JSONObject(youRoomCommand.getEntry(roomId, entryId))).getJSONObject("entry");
				entry = createEntry(json, level);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			cacheDb.beginTransaction();
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				(new ObjectOutputStream(baos)).writeObject(entry);
				cacheDb.execSQL("delete from entries where entryId = ? and roomId = ? ;", new String[]{entryId, roomId});
				cacheDb.execSQL("insert into entries(entryId, roomId, updatedTime, entry) values(?, ?, ?, ?) ;",new Object[]{entryId, roomId, updatedTime, baos.toByteArray()});
				cacheDb.setTransactionSuccessful();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				cacheDb.endTransaction();
			}
		}
		c.close();
		
		return entry;
	}
	
	private YouRoomEntry createEntry(JSONObject json, int level) {
		YouRoomEntry entry = new YouRoomEntry();
		try {
			entry.setId(json.getInt("id"));
			entry.setParticipationName(json.getJSONObject("participation").getString("name"));
			entry.setCreatedTime(json.getString("created_at"));
			entry.setUpdatedTime(json.getString("updated_at"));
			entry.setContent(json.getString("content"));
			entry.setDescendantsCount(json.optInt("descendants_count"));
			entry.setLevel(level);
			if (json.has("children")) {
				JSONArray cArray = json.getJSONArray("children");
				ArrayList<YouRoomEntry> children = new ArrayList<YouRoomEntry>(cArray.length());
				for (int i = 0; i < cArray.length(); i++) {
					JSONObject child = cArray.getJSONObject(i);
					children.add(createEntry(child, level));
				}
				entry.setChildren(children);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return entry;
	}
}
