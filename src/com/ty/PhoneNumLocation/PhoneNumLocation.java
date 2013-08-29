package com.ty.PhoneNumLocation;

import android.net.Uri;
import android.provider.BaseColumns;

public final class PhoneNumLocation {
	public static final String AUTHORITY = "com.ty.provider.phoneNumberLocation";
	
	public static final Uri CONTENT_URI = Uri
			.parse("content://com.ty.provider.phoneNumberLocation");

	public static final class Location implements BaseColumns {
		public static final Uri CONTENT_URI = Uri
				.parse("content://com.ty.provider.phoneNumberLocation/location");

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ty.phoneNumberLocation";		
		
		public static final String NUM_PREFIX = "num";
        /*TY: dangzhili 20120615 modify for PROD100649130 begin */
        /*
		public static final String PRIVONCE = "province";
		*/
		public static final String PRIVONCE = "province";
        /*TY: dangzhili 20120615 modify for PROD100649130 end */
		public static final String AREA = "area";
	}
}