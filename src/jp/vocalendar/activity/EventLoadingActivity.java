package jp.vocalendar.activity;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import jp.vocalendar.Constants;
import jp.vocalendar.R;
import jp.vocalendar.googleapi.OAuthManager;
import jp.vocalendar.model.Event;
import jp.vocalendar.model.EventDataBase;
import jp.vocalendar.model.EventDataBaseRow;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.google.api.client.util.DateTime;

/**
 * イベント読み込み中の画面のActivity。
 * バックグランド処理でイベントを読み込む。
 */
public class EventLoadingActivity extends Activity implements LoadEventTask.TaskCallback {
	private static String TAG = "SplashScreenActivity";
	
	// イベントを取り込む開始日を指定する 年月日 をIntentに格納するキー
	public static String KEY_YEAR = "year";
	public static String KEY_MONTH = "month";
	public static String KEY_DATE = "date";	
		
	/**
	 * 認証系の処理(アカウント追加やアカウント利用許可など)をする時のリクエストコード。
	 */
	private static int LOGIN_REQUEST_CODE = Constants.GET_LOGIN;
	
	private GoogleCalendarLoadEventTask task = null;	
	private TextView loadingItemView = null;
	
	// 読み込み中の日付
	private int loadingYear, loadingMonth, loadingDateOfMonth;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);
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
        initWallpaper();
        initAccount();
	}

	private void initWallpaper() {
		ImageView iv = (ImageView)findViewById(R.id.wallpaperImageView);
		iv.setImageResource(R.drawable.wallpaper);
		iv.setAlpha(64);
	}
	
	private void startLoadEventTask() {
        TimeZone timeZone = TimeZone.getDefault();

        if(getIntent().hasExtra(KEY_YEAR)) {
			Intent intent = getIntent();
			loadingYear = intent.getIntExtra(KEY_YEAR, -1);
			loadingMonth = intent.getIntExtra(KEY_MONTH, -1);			
			loadingDateOfMonth = intent.getIntExtra(KEY_DATE, -1);
		} else {
	        Calendar localCal = Calendar.getInstance(timeZone);		        
	        loadingYear = localCal.get(Calendar.YEAR);
	        loadingMonth = localCal.get(Calendar.MONTH);
	        loadingDateOfMonth = localCal.get(Calendar.DATE);			
		}
		
        startLoadEventTask(timeZone);
	}

	private void startLoadEventTask(TimeZone timeZone) {
        Calendar localCal = Calendar.getInstance(timeZone);
        localCal.set(Calendar.HOUR_OF_DAY, 0);
        localCal.set(Calendar.MINUTE, 0);
        localCal.set(Calendar.SECOND, 0);
        localCal.set(Calendar.MILLISECOND, 0);		        
        localCal.set(Calendar.YEAR, loadingYear);
        localCal.set(Calendar.MONTH, loadingMonth);
        localCal.set(Calendar.DATE, loadingDateOfMonth);
		
		int duration = getNumberOfDateToGetEvent();
        DateTime[] dates = makeStartAndEndDateTime(
        		loadingYear, loadingMonth, loadingDateOfMonth, timeZone, duration);
                                
        EventSeparator[] separators = new EventSeparator[duration];
        for(int i = 0; i < separators.length; i++) {
        	separators[i] = new EventSeparator(localCal.getTime());
        	localCal.add(Calendar.DATE, 1);
        }
        
		task = new GoogleCalendarLoadEventTask(this, this, 5);
		task.setStartAndEndDate(dates[0], dates[1], separators, timeZone);
		task.execute(Constants.MAIN_CALENDAR_ID, Constants.BROADCAST_CALENDAR_ID);
	}
	
	/**
	 * 指定された年月日(year,month,date)から指定された日数(duration)の開始日と終了日を返す。
	 * @param year
	 * @param month
	 * @param date
	 * @param timeZone
	 * @param duration
	 * @return
	 */
	private DateTime[] makeStartAndEndDateTime(int year, int month, int date, TimeZone timeZone, int duration) {
        Calendar utcCal = Calendar.getInstance(timeZone);
        utcCal.set(Calendar.YEAR, year);
        utcCal.set(Calendar.MONTH, month);
        utcCal.set(Calendar.DATE, date);        
        utcCal.set(Calendar.HOUR_OF_DAY, 0);
        utcCal.set(Calendar.MINUTE, 0);
        utcCal.set(Calendar.SECOND, 0);
        utcCal.set(Calendar.MILLISECOND, 0);
                
        DateTime[] dates = new DateTime[2];
    	dates[0] = new DateTime(utcCal.getTime(), timeZone); //開始日時   	
    	utcCal.add(Calendar.DATE, duration);
    	dates[1] = new DateTime(utcCal.getTime(), timeZone); //終了日時
    	return dates;
	}
	
	public void transitToEventListActivity() {
		Intent i = new Intent();
		i.putExtra(KEY_YEAR, loadingYear);
		i.putExtra(KEY_MONTH, loadingMonth);
		i.putExtra(KEY_DATE, loadingDateOfMonth);
		setResult(RESULT_OK, i);
		finish();
	}

	public void onURIOpening(String uri) {
		loadingItemView.setText("開いています: " + uri);		
	}
	
	public void onProgressUpdate(Event event) {
		String str = event.toDateTimeSummaryString(TimeZone.getDefault());
		Log.d("SplashScreenActivity", str);
		loadingItemView.setText(str);
	}
	
	public void onPostExecute(List<EventDataBaseRow> events) {
		if(events == null) {
			return;
		}		
		loadingItemView.setText("表示準備中");
		
		EventDataBase db = new EventDataBase(this);
		db.open();
		db.deleteAllEvent();		
		db.insertEvent(events);
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
