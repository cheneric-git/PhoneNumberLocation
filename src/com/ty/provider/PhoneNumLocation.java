/* Houjie add begin*/
package com.ty.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Convenience definitions for IP number settings
 */
public final class PhoneNumLocation {
	
	public static final Uri CONTENT_URI = Uri
			.parse("content://com.ty.provider.phoneNumberLocation");

	public static final class Location implements BaseColumns {
		
		public static final Uri CONTENT_URI = Uri
				.parse("content://com.ty.provider.phoneNumberLocation/location");

		public static final String NUM_PREFIX = "num";
        /*TY: dangzhili 20120615 modify for PROD100649130 begin */
        /*
		public static final String PRIVONCE = "privonce";
		*/
		public static final String PRIVONCE = "province";
        /*TY: dangzhili 20120615 modify for PROD100649130 end */
		public static final String AREA = "area";
	}
}

/* Houjie add end*/