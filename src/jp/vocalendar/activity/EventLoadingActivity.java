package jp.vocalendar.activity;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import com.google.api.client.util.DateTime;

import jp.vocalendar.Constants;
import jp.vocalendar.R;
import jp.vocalendar.googleapi.OAuthManager;
import jp.vocalendar.model.Event;
import jp.vocalendar.model.EventComparator;
import jp.vocalendar.model.EventDataBase;
import jp.vocalendar.model.EventSeparator;
import jp.vocalendar.model.GoogleCalendarLoadEventTask;
import jp.vocalendar.model.LoadEventTask;
import android.accounts.Account;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * イベント読み込み中の画面のActivity。
 * バックグランド処理でイベントを読み込む。
 */
public class EventLoadingActivity extends Activity implements LoadEventTask.TaskCallback {
	private static String TAG = "SplashScreenActivity";
	
	/**
	 * 認証系の処理(アカウント追加やアカウント利用許可など)をする時のリクエストコード。
	 */
	private static int LOGIN_REQUEST_CODE = Constants.GET_LOGIN;
	
	private GoogleCalendarLoadEventTask task = null;	
	private TextView loadingItemView = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        setTitle(R.string.vocalendar);
        
        Button cancel = (Button)findViewById(R.id.cancelButton);
        cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(task != null) {
					task.cancel(false);
				}
				transitToEventListActivity();
			}
		});
		loadingItemView = (TextView)findViewById(R.id.loadingItemView);
                
        initAccount();
	}

	private void startLoadEventTask() {		
        TimeZone timeZone = TimeZone.getDefault();
        Calendar localCal = Calendar.getInstance(timeZone); // いったんローカルのタイムゾーンでカレンダー計算
		
        int year = localCal.get(Calendar.YEAR);
        int month = localCal.get(Calendar.MONTH);
        int date = localCal.get(Calendar.DATE);
        
        Calendar utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")); // タイムゾーンUTCで、ローカルのタイムゾーンの日付を指定
        utcCal.set(Calendar.YEAR, year);
        utcCal.set(Calendar.MONTH, month);
        utcCal.set(Calendar.DATE, date);        
        utcCal.set(Calendar.HOUR_OF_DAY, 0);
        utcCal.set(Calendar.MINUTE, 0);
        utcCal.set(Calendar.SECOND, 0);
        utcCal.set(Calendar.MILLISECOND, 0);
                
        DateTime[] dates = new DateTime[getNumberOfDateToGetEvent() + 1];
        EventSeparator[] separators = new EventSeparator[dates.length - 1];
    	dates[0] = new DateTime(utcCal.getTimeInMillis(), 0); // tzShiftを0。UTCのオフセットを"Z"(=00:00)にする(参考：RFC3339)
        for(int i = 0; i < separators.length; i++) {
        	separators[i] = new EventSeparator(localCal.getTime());
        	localCal.add(Calendar.DATE, 1);
        	
    		utcCal.add(java.util.Calendar.DATE, 1);
    		dates[i+1] = new DateTime(utcCal.getTimeInMillis(), 0);        	
        }
        
		task = new GoogleCalendarLoadEventTask(this, this, 5);
		task.setStartAndEndDate(dates, separators, timeZone);
		task.execute(Constants.MAIN_CALENDAR_ID, Constants.BROADCAST_CALENDAR_ID);
	}
	
	public void transitToEventListActivity() {
		setResult(RESULT_OK);
		finish();
	}

	public void onURIOpening(String uri) {
		loadingItemView.setText("開いています: " + uri);		
	}
	
	public void onProgressUpdate(Event event) {
		String str = event.toDateTimeSummaryString();
		Log.d("SplashScreenActivity", str);
		loadingItemView.setText(str);
	}
	
	public void onPostExecute(List<Event> events) {
		if(events == null) {
			return;
		}		
		loadingItemView.setText("表示準備中");
		
		EventDataBase db = new EventDataBase(this);
		db.open();
		db.deleteAllEvent();
				
		Iterator<Event> itr = events.iterator();
		int index = 0;
		while(itr.hasNext()) {
			db.insertEvent(index++, itr.next());
		}
		db.close();		
		
		transitToEventListActivity();
	}
	
	/**
	 * イベント読み込みがキャンセル(認証失敗でリトライ)時に呼ばれる。
	 */
	public void retry(int tryNumber) {		
		Log.d(TAG, "retry()");
		startLoadEventTask();
	}
	
	
	
	private void initAccount() {
		OAuthManager.getInstance().doLogin(false, this, new OAuthManager.AuthHandler() {			
			@Override
			public void handleAuth(Account account, String authToken) {
				onAuthToken(account, authToken);
			}
		});
	}
	
	private void onAuthToken(Account account, String authToken) {
		if(account == null || authToken == null) {
			showGoogleAccountRequredDialog();
			return;
		}
		Log.i(TAG, "onAuthToken: " + account.name + "," + authToken);
		startLoadEventTask();
	}	
	
	private void showGoogleAccountRequredDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getText(R.string.google_account_required) )
		       .setPositiveButton(getText(R.string.add_account), new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   openAddAccountActivity();
		           }
		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		    	   public void onClick(DialogInterface dialog, int id) {
		    		   dialog.cancel();
		    	   }
		       });
		AlertDialog alert = builder.create();		
		alert.show();
	}
	
	private void openAddAccountActivity(){
		Intent i =  new Intent(Settings.ACTION_ADD_ACCOUNT);
		startActivityForResult(i, LOGIN_REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == LOGIN_REQUEST_CODE) {
			initAccount();
		}
	}
	
	/**
	 * イベント取得日数を返す。
	 * @return
	 */
	private int getNumberOfDateToGetEvent() {		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		String value = pref.getString(Constants.NUMBER_OF_DATE_TO_GET_EVENTS_PREFERENCE_NAME, "3");
		try {
			return Integer.parseInt(value);
		} catch(NumberFormatException e) {
			Log.e(TAG, "Invalid number of date to get events: " + value);
		}
		return 3;
	}
}
