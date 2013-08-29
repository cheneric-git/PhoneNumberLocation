package com.ty.PhoneNumLocation;
/**
 * @author zhaolong
 * create on 20100118 for the db updating func.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.ContentValues;
import android.content.Context;
import android.os.Handler;
import android.provider.Settings;
import android.util.Config;
import android.util.Log;

public class UpdateManager {
	private static boolean DBG = Config.DEBUG;
	private static String TAG = "PhoneNumLocationDbUpdate";
	
	public static final int UPDATE_NETWORK_ERROR = 0;
	public static final int UPDATE_SUCCESS = 1;
	public static final int UPDATE_JSON_ERROR = 2;
	public static final int UPDATE_DB_ERROR = 3;
	
	private static final int CONNECT_TIMEOUT = 60000;
	
	private static final String DATABASE_URL_PRFIX = "http://cloud.k-touch.cn/Interface/location/update.ashx?ver=";
	private static final String JSON_DBVERSION = "DbVersion";
	private static final String JSON_DBLIST = "list";
	private static final String JSON_ACTION = "action";
	private static final String JSON_PHONENUM = "numprefix";
	private static final String JSON_PROVINCE = "province";
	private static final String JSON_LOCATION = "location";
	private static final String JSON_LISTVERSION = "version";
	
	private static final String PHONE_NUM_LOCATION_DBVERSION = "phone_num_location_db_version";
	
	private static final String BASE_VERSION = "201010101010";
	private String mServerVersion;
	private String mLocalVersion;
	
	private ArrayList<DbItem> mAddItemList;
	private ArrayList<DbItem> mDeleteItemList;
	private ArrayList<DbItem> mModifyItemList;
	
	private Context mContext;
	private boolean mNeedUpdate;
	
	public UpdateManager(Context c) {
		mContext = c;
	}
	
	public void update(Handler handler) {
		mNeedUpdate = true;
		mLocalVersion = Settings.System.getString(mContext.getContentResolver(), PHONE_NUM_LOCATION_DBVERSION);
		if(mLocalVersion == null) {
			if(DBG) Log.d(TAG, "Init Base VerNumber!");
			Settings.System.putString(mContext.getContentResolver(), PHONE_NUM_LOCATION_DBVERSION, BASE_VERSION);
			mLocalVersion = BASE_VERSION;
		}
		String strToParse = getStreamByUrl();
	    if (strToParse == null) {
			handler.obtainMessage(PhoneNumLocationActivity.MSG_UPDATE_FAILED).sendToTarget();
			return;
		}
		if (!jsonParser(strToParse)) {
			handler.obtainMessage(PhoneNumLocationActivity.MSG_UPDATE_FAILED).sendToTarget();
			return;
		}
		if (mNeedUpdate && !updateDb()) {
			handler.obtainMessage(PhoneNumLocationActivity.MSG_UPDATE_FAILED).sendToTarget(); 
			return;
		}
		if(mNeedUpdate) {
		    Settings.System.putString(mContext.getContentResolver(), PHONE_NUM_LOCATION_DBVERSION, mServerVersion);
		    handler.obtainMessage(PhoneNumLocationActivity.MSG_UPDATE_SUCCESS).sendToTarget();
		} else {
			handler.obtainMessage(PhoneNumLocationActivity.MSG_NO_NEED_UPDATE).sendToTarget();
		}
	}
	
	private String getStreamByUrl() {
		InputStream in = null;
		String strStream = "";
		try {
			URL streamUrl = new URL(DATABASE_URL_PRFIX + mLocalVersion);
			Log.e("willie", streamUrl.toString());
			URLConnection conn = streamUrl.openConnection();
			conn.setConnectTimeout(CONNECT_TIMEOUT);
			conn.connect();
			in = conn.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, "GB2312"));
			String lines;
			while((lines = reader.readLine()) != null) {
				strStream += lines;
			}
			in.close();
			reader.close();
		} catch (MalformedURLException e) {
			// TODO: handle exception
		    Log.e(TAG, "illegal URL!");
			return null;
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "UnsupportedEncodingException!");
			return null;
		} catch (SocketTimeoutException e) {
			Log.e(TAG, "connect timeout!");
			return null;
		} catch (IOException e) {
			Log.e(TAG, "IO error occured!");
			return null;
		}
		return strStream;
	}
	
	private boolean jsonParser(String json) {
		Log.e("willie", json);
		if (json.length() <= 0) {
			mNeedUpdate = false;
			return true;
		}
		try {
			JSONObject object = (JSONObject) new JSONTokener(json).nextValue();
			mServerVersion = object.getString(JSON_DBVERSION);
			if(mServerVersion.equals(mLocalVersion)) {
				mNeedUpdate = false;
				return true;
			}
			JSONArray dbItemList = new JSONArray();
			dbItemList = object.getJSONArray(JSON_DBLIST);
			
			mAddItemList = new ArrayList<DbItem>();
			mDeleteItemList = new ArrayList<DbItem>();
			mModifyItemList = new ArrayList<DbItem>();
			
			for (int i = 0; i < dbItemList.length(); i++) {
				DbItem tempItem = new DbItem();
				JSONObject tempObj = dbItemList.getJSONObject(i);
				tempItem.phoneNum = tempObj.getString(JSON_PHONENUM);
				tempItem.province = tempObj.getString(JSON_PROVINCE);
				tempItem.location = tempObj.getString(JSON_LOCATION);
				switch (tempObj.getInt(JSON_ACTION)) {
				case 1:
					mAddItemList.add(tempItem);
					break;
				case 2:
					mDeleteItemList.add(tempItem);
					break;
				case 3:
					mModifyItemList.add(tempItem);
					break;

				default:
					break;
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "JSONException \n");
			return false;
		}
		return true;
	}
	
	private boolean updateDb() {
		ContentValues temp = new ContentValues();
		if (mAddItemList.size() > 0) {
			for (int i = 0; i < mAddItemList.size(); i++) {
				temp.clear();
				temp.put(PhoneNumLocation.Location.NUM_PREFIX, Integer.parseInt(mAddItemList.get(i).phoneNum));
				temp.put(PhoneNumLocation.Location.PRIVONCE, mAddItemList.get(i).province);
				temp.put(PhoneNumLocation.Location.AREA, mAddItemList.get(i).location);
				try {
				mContext.getContentResolver().insert(PhoneNumLocation.Location.CONTENT_URI, temp);
				} catch (Exception e){
					Log.e(TAG, "insert num " + mAddItemList.get(i).phoneNum + " failed! May be already exist the same num.");
				}
			}
			if(DBG) Log.d(TAG, "Insert finished");
		}
		if (mDeleteItemList.size() > 0) {
			for (int i = 0; i < mDeleteItemList.size(); i++) {
				String where = PhoneNumLocation.Location.NUM_PREFIX + "=" + mDeleteItemList.get(i).phoneNum;
				mContext.getContentResolver().delete(PhoneNumLocation.Location.CONTENT_URI, where, null);
			}
			if(DBG) Log.d(TAG, "Delete finished");
		}
		if (mModifyItemList.size() > 0) {
			for (int i = 0; i < mModifyItemList.size(); i++) {
				temp.clear();
				temp.put(PhoneNumLocation.Location.NUM_PREFIX, Integer.parseInt(mModifyItemList.get(i).phoneNum));
				temp.put(PhoneNumLocation.Location.PRIVONCE, mModifyItemList.get(i).province);
				//TY :Maqing 20110615 modify for PROD100042756 begin
				temp.put(PhoneNumLocation.Location.AREA, mModifyItemList.get(i).location);
				//TY :Maqing 20110615 modify for PROD100042756 end
				String where = PhoneNumLocation.Location.NUM_PREFIX + "=" + mModifyItemList.get(i).phoneNum;
				mContext.getContentResolver().update(PhoneNumLocation.Location.CONTENT_URI, temp, where, null);
			}
			if(DBG) Log.d(TAG, "Update finished");
		}
		return true;
	}
	
	private class DbItem {
		public String phoneNum;
		public String province;
		public String location;
	}
	
}
