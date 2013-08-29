package com.ty.PhoneNumLocation;



import android.app.Activity;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Config;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

//TY:zhaolong add on 20110118 for the db update func begin.
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
//TY:zhaolong add on 20110118 for the db update func end.

//tianyu liushan 20110328 add start
import android.view.MenuItem;
import android.view.Menu;
//tianyu liushan 20110328 end
import android.os.AsyncTask;
import java.util.concurrent.Executors;

//TY:zhaolong modified on 20110118 for the db update func.
public class PhoneNumLocationActivity extends Activity implements TextWatcher, DialogInterface.OnClickListener {
	private static final boolean DBG = Config.DEBUG;
	private static final String TAG = "PhoneNumLocationActivity";

	private static final int NUM_ENQUIRY_MIN_LEN = 3;

	private EditText mPhoneNum;
	private TextView mResult;
	private Button mBtnQuery;
	private Button mBtnClear;
    //TY:zhaolong add on 20110118 for the db update func begin.
	
	//tianyu liushan 20110328 delete 
    // private Button mBtnUpdate;
	
    
    private UpdateManager mUpdateMgr;
    private UpdateHandler mHandler;
    
    public static final int MSG_UPDATE_SUCCESS = 0;
    public static final int MSG_NO_NEED_UPDATE = 1;
    public static final int MSG_UPDATE_FAILED = 2;
    public static final int MSG_UPDATING = 3;
    //TY:zhaolong add on 20110118 for the db update func end.

	private String[] projection = {
		PhoneNumLocation.Location.NUM_PREFIX,
		PhoneNumLocation.Location.PRIVONCE,
		PhoneNumLocation.Location.AREA,
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);		
		setupButton();
		
		mPhoneNum = (EditText) findViewById(R.id.EditTextPhoneNum);
		mResult = (TextView) findViewById(R.id.TextViewResultPrompt);

		mPhoneNum.addTextChangedListener(this);

		//TY:zhaolong modified on 20110118 for the db update func.
		mHandler = new UpdateHandler();
	}
	//TY :Maqing 20110328 add for PROD02308552 begin
	public void onPause () {
		super.onPause ();
		
		safeDismissDialog(MSG_UPDATING);
	}
	//TY :Maqing 20110328 add for PROD02308552 end
	private void setupButton() {
		mBtnQuery = (Button) this.findViewById(R.id.BtnQuery);
		mBtnQuery.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {				
				Editable s = mPhoneNum.getText();				
				
// 				Cursor c = getContentResolver().query(PhoneNumLocation.Location.CONTENT_URI, 
// 						projection, s.toString(), null, null);
// 				mResult.setText(R.string.unknown);
// 				if (c != null) {
// 					if (c.getCount() >= 1) {
// 						c.moveToFirst();			
// 					
// 						if (!c.getString(1).equals(c.getString(2))) {						
// 							mResult.setText(c.getString(1) + " " + c.getString(2));
// 						} else {
// 							mResult.setText(c.getString(1));
// 						}
// 					}
// 					c.close();
// 				}
				QueryAsyncTask task = new QueryAsyncTask();
				task.executeOnExecutor(Executors.newCachedThreadPool(), s.toString());
			}
		});
		mBtnQuery.setEnabled(false);

		mBtnClear = (Button) this.findViewById(R.id.BtnClear);
		mBtnClear.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				mPhoneNum.setText("");
				mResult.setText("");
			}
		});
		mBtnClear.setEnabled(false);
		
		//tianyu liushan 20110328 delete start
        //TY:zhaolong add on 20110118 for the db update func begin.
		/*
        mBtnUpdate = (Button)this.findViewById(R.id.BtnUpdate);
        mBtnUpdate.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
            	showDialog(MSG_UPDATING);
            	new UpdateThread().start();
            }
        });
        */
        //TY:zhaolong add on 20110118 for the db update func end.
		//tianyu liushan 20110328 delete end
		
	}

	public void afterTextChanged(Editable s) {
		if (s.length() == 0) {
			mBtnQuery.setEnabled(false);
			mBtnClear.setEnabled(false);
			//TY:maqing 20101026 for PROD01911146 add begin
			mResult.setText("");
			//TY:maqing 20101026 for PROD01911146 add end
		} else if (s.length() < NUM_ENQUIRY_MIN_LEN) {
			mBtnQuery.setEnabled(false);
			mBtnClear.setEnabled(true);
		} else {
			mBtnQuery.setEnabled(true);
			mBtnClear.setEnabled(true);
		}		
	}

	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		return;
	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
		return;
	}
	//TY:zhaolong add on 20110118 for the db update func begin.
    public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	if(id == MSG_UPDATING) {
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setTitle(getText(R.string.app_name));
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.setMessage(getText(R.string.updating));
            return dialog;
    	}else {  
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            int msgId;
            int titleId = R.string.app_name;
            switch (id) {
	    	case MSG_UPDATE_SUCCESS:
		    	msgId = R.string.update_success;
			    break;
		    case MSG_NO_NEED_UPDATE:
		    	msgId = R.string.no_need_update;
		    	break;
		    case MSG_UPDATE_FAILED:
		    	msgId = R.string.update_failed;
		    	break;
		    default:
			    return null;
		    }
        
        b.setPositiveButton(getString(R.string.ok), this);
        b.setTitle(getText(titleId));
        b.setMessage(getText(msgId));
        AlertDialog dialog = b.create();
        return dialog;
    	}
    }
    
    private void safeDismissDialog(int id) {
        try {
            dismissDialog(id);
        } catch (IllegalArgumentException e) {
        }
    }
    
    private class UpdateThread extends Thread {
    	@Override
    	public void run() {
    		if(mUpdateMgr == null) {
    			mUpdateMgr = new UpdateManager(getApplicationContext());
    		}
    		mUpdateMgr.update(mHandler);
    	}
    }
    
	private class UpdateHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			safeDismissDialog(MSG_UPDATING);
			switch (msg.what) {
			case MSG_NO_NEED_UPDATE:
				showDialog(MSG_NO_NEED_UPDATE);
				break;
			case MSG_UPDATE_FAILED:
				showDialog(MSG_UPDATE_FAILED);
				break;
			case MSG_UPDATE_SUCCESS:
				showDialog(MSG_UPDATE_SUCCESS);
				break;

			default:
				break;
			}
		}
	}
   //TY:zhaolong add on 20110118 for the db update func end.
	
	//tianyu liushan 20110328 add start
	@Override
	public  boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem item;
	    item=menu.add(0, 1, 0,R.string.btn_update);
	    item.setIcon(R.drawable.ty_ic_menu_update);
	    return true;
	}
	@Override
	public  boolean onOptionsItemSelected( MenuItem item) {
		showDialog(MSG_UPDATING);
    	new UpdateThread().start();
    	return super.onOptionsItemSelected(item);
	}
	//tianyu liushan 20110328 add end

	private class QueryAsyncTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String number = params[0];
// 			Log.i("TAG", "-------------------number: " + number + " ----------------------");
			String description = "";
			Cursor cursor = null;
			try {
				Cursor c = getContentResolver().query(PhoneNumLocation.Location.CONTENT_URI,
						projection, number, null, null);
				if (c != null) {
					if (c.getCount() >= 1) {
						c.moveToFirst();
					
						if (!c.getString(1).equals(c.getString(2))) {
							description = c.getString(1) + " " + c.getString(2);
						} else {
							description = c.getString(1);
						}
// 						Log.i("TAG", "-------------------description: " + description + " ----------------------");
					}
					c.close();
				}
			} catch (Exception ex) {
				Log.e("TAG", "setGeocodeInformation Exception msg: " + ex.toString());
				return "";
			} finally {
				if (null != cursor) {
					cursor.close();
					cursor = null;
				}
			}

			return description;
		}

		@Override
		protected void onPostExecute(String result) {
// 			Log.i("TAG", "-------------------result: " + result + " ----------------------");
			mResult.setText(R.string.unknown);
			if (result != null && result.length() > 0) {
				mResult.setText(result);
			}
		}
	}
}