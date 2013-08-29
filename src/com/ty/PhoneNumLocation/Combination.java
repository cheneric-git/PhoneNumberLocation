package com.ty.PhoneNumLocation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.res.AssetManager;

public final class Combination {
	private static final String DEST_FILE_NAME = PhoneNumLocationProvider.DATABASE_NAME;
	private static final String DEST_DIRECTORY = "/data/data/com.ty.PhoneNumLocation/databases/";
	/*TY: Maobo 20110307 modify for PROD02263805 begin */
	private static final String[] mSeparatedFiles = {
        /*TY: dangzhili 20120615 modify for PROD100649130 begin */
        /*
		"location.part1",
		"location.part2",
		"location.part3",
		"location.part4",
		"location.part5"
		*/
		"location.db.part1",
		"location.db.part2",
		"location.db.part3",
		"location.db.part4",
		"location.db.part5",
        "location.db.part6",
        "location.db.part7"
        /*TY: dangzhili 20120615 modify for PROD100649130 end */

	};	
	/*TY: Maobo 20110307 modify for PROD02263805 end */
	private AssetManager mAM;	
	
	public Combination(AssetManager am) {
		mAM = am;
	}

	boolean isFileExists() {
		File file = new File(DEST_DIRECTORY, DEST_FILE_NAME); 
		return file.exists() && (file.length() > 1024*1024);
	}
	
	boolean combFile() {
		InputStream fis = null;
		int len = 0;
		byte[] bt = new byte[1024];
		File file = null;
		FileOutputStream out = null; 
		try {
			file = new File(DEST_DIRECTORY, DEST_FILE_NAME);			
	        file.createNewFile();	        
	        out = new FileOutputStream(file);
	        
			for (int i = 0; i < mSeparatedFiles.length; i++) {
				fis = mAM.open(mSeparatedFiles[i]);
				while ((len = fis.read(bt)) > 0) {					
					out.write(bt, 0, len);
					out.flush();
				}
				fis.close();				
			}			
			out.close();
			
		} catch (Exception e) {
			e.printStackTrace();			
			return false;
		}
		
		return true;
	}
}
