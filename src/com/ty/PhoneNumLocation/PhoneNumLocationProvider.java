package com.ty.PhoneNumLocation;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Config;
import android.util.Log;

public final class PhoneNumLocationProvider extends ContentProvider {
	private static final String TAG = "PhoneNumLocationProvider";
	private static final boolean DBG = Config.DEBUG;
	
	static final String DATABASE_NAME = "location.db";	
	
	private static final int DATABASE_VERSION = 2;	
	private static final String TABLE_NAME = "NumOwnerShip";
	
	private class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper() {
			super(mContext, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			if (DBG) Log.d(TAG, "Create database: version " + DATABASE_VERSION);			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if (DBG)
				Log.d(TAG, "Upgrading database from version " + oldVersion
						+ " to " + newVersion
						+ ", which will destroy all old data");						
		}			
	}
	
	private DatabaseHelper mOpenHelper;
	private Context mContext;
	
	@Override
	public boolean onCreate() {
		if (DBG) Log.d(TAG, "onCreate...");
		mContext = getContext();		
		mOpenHelper = new DatabaseHelper();
		if (mOpenHelper == null) {
			return false;
		}
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.close();
		Combination combination = new Combination(mContext.getAssets());
		if (!combination.isFileExists()) {
			combination.combFile();
		}
		
		return true;
	}

	@Override
	public String getType(Uri uri) {
		if (DBG) Log.d(TAG, "getType URI " + uri);
		
		switch (sUriMatcher.match(uri)) {
		case LOCATION:
			return PhoneNumLocation.Location.CONTENT_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}
	
	enum NUM_TYPE {
		NUM_TYPE_LOCAL, NUM_TYPE_TELE, NUM_TYPE_MOBILE
	}
	
	static NUM_TYPE CheckTeleOrMobile(String number) {
		if (number.charAt(0) == '0') {
			return NUM_TYPE.NUM_TYPE_TELE;
		} else if (number.length() < 9) {
			return NUM_TYPE.NUM_TYPE_LOCAL;
		} else {
			return NUM_TYPE.NUM_TYPE_MOBILE;
		}
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		if (DBG) Log.d(TAG, "query URI " + uri);
		
		String numPrefix="";
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		switch (sUriMatcher.match(uri)) {
		case LOCATION:
			qb.setTables(TABLE_NAME);
			qb.setProjectionMap(sLocationContentProjectionMap);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		if (selection.startsWith("+86")) {
			selection = selection.substring(3);
		} 
	     /*TY: shixq 20111114 modify for PROD01933122 begin */
		if (selection.startsWith("0086")) {
			selection = selection.substring(4);
		} 
	     /*TY: shixq 20111114 modify for PROD01933122 end*/
		//TY :Maqing 20111223 add for PROD100242333 begin
		if (selection.startsWith("17951") || selection.startsWith("17911") || selection.startsWith("17901")) {
			selection = selection.substring(5);
		}
		//TY :Maqing 20111223 add for PROD100242333
		
		if(!selection.matches("\\d+")) {
			return null;
		}		
		
		NUM_TYPE type = CheckTeleOrMobile(selection);
	
		switch (type) {
		case NUM_TYPE_LOCAL:
			//location
			numPrefix = "12345678";
			break;
		case NUM_TYPE_TELE:	
            /*TY: Houjie 20101115 modify for PROD01933122 */
            numPrefix = (selection.length() >= 4) ? 
                selection.substring(1, 4) : selection.substring(1, 3);                        
            
			if (Integer.parseInt(numPrefix) < 310)
				numPrefix = numPrefix.substring(0, 2);
			break;
		case NUM_TYPE_MOBILE:
			numPrefix = selection.substring(0, 7);
			
			break;
		}
		
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		Cursor c = qb.query(db, projection, PhoneNumLocation.Location.NUM_PREFIX + "=" + numPrefix, selectionArgs, null,
				null, sortOrder);
		/*TY: Maobo 20110307 modify for PROD02263805 begin */
		if (type.equals(NUM_TYPE.NUM_TYPE_MOBILE) && c.getCount() < 1) {
			numPrefix = selection.substring(0, 8);
		    c = qb.query(db, projection, PhoneNumLocation.Location.NUM_PREFIX + "=" + numPrefix, selectionArgs, null,
					null, sortOrder);
		}
		/*TY: Maobo 20110307 modify for PROD02263805 end */
		c.setNotificationUri(getContext().getContentResolver(), uri);		
		return c;
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if (DBG) Log.d(TAG, "insert URI " + uri);
		
		if (sUriMatcher.match(uri) != LOCATION) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			throw new IllegalArgumentException("PhoneNumLocation.Location value is null");
		}

		// Make sure that the fields are all set
		if (values.containsKey(PhoneNumLocation.Location.NUM_PREFIX) == false) {
			throw new IllegalArgumentException("PhoneNumLocation.Location.NUM_PREFIX is null");
		} else if (values.containsKey(PhoneNumLocation.Location.PRIVONCE) == false) {
			throw new IllegalArgumentException("PhoneNumLocation.Location.PRIVONCE is null");
		} else if (values.containsKey(PhoneNumLocation.Location.AREA) == false) {
			throw new IllegalArgumentException("PhoneNumLocation.Location.AREA is null");
		}

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long rowId = db.insert(TABLE_NAME, PhoneNumLocation.Location.NUM_PREFIX, values);
		if (rowId > 0) {
			Uri locationUri = ContentUris.withAppendedId(PhoneNumLocation.Location.CONTENT_URI,
					rowId);
			getContext().getContentResolver().notifyChange(locationUri, null);
			return locationUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		if (DBG) Log.d(TAG, "delete URI " + uri);
		
		int count;
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		
		switch (sUriMatcher.match(uri)) {
		case LOCATION:
			count = db.delete(TABLE_NAME, where, whereArgs);
			break;
		
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
		if (DBG) Log.d(TAG, "update...");
		
		int count = 0;
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		
		switch (sUriMatcher.match(uri)) {		
		case LOCATION:
			count = db.update(TABLE_NAME, values, where, whereArgs);
			break;
		
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	private static final int LOCATION = 1;	

	private static final UriMatcher sUriMatcher;

	private static HashMap<String, String> sLocationContentProjectionMap;

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(PhoneNumLocation.AUTHORITY, "location", LOCATION);

		sLocationContentProjectionMap = new HashMap<String, String>();
		sLocationContentProjectionMap.put(PhoneNumLocation.Location._ID, PhoneNumLocation.Location._ID);
		sLocationContentProjectionMap.put(PhoneNumLocation.Location.NUM_PREFIX, PhoneNumLocation.Location.NUM_PREFIX);		
		sLocationContentProjectionMap.put(PhoneNumLocation.Location.PRIVONCE, PhoneNumLocation.Location.PRIVONCE);
		sLocationContentProjectionMap.put(PhoneNumLocation.Location.AREA, PhoneNumLocation.Location.AREA);
	}
}
