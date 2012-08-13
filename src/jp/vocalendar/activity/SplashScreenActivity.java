package jp.vocalendar.activity;

import java.net.URI;
import java.net.URISyntaxException;
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
import jp.vocalendar.model.GoogleCalendarLoadEventTask;
import jp.vocalendar.model.LoadEventTask;
import android.accounts.Account;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import edu.emory.mathcs.backport.java.util.LinkedList;

/**
 * スプラッシュ画面を表示するActivity。
 * バックグランド処理でイベントを読み込む。
 */
public class SplashScreenActivity extends Activity implements LoadEventTask.TaskCallback {
	private static String TAG = "SplashScreenActivity";
	
	/**
	 * 認証系の処理(アカウント追加やアカウント利用許可など)をする時のリクエストコード。
	 */
	private static int LOGIN_REQUEST_CODE = Constants.GET_LOGIN;
	
	private GoogleCalendarLoadEventTask task = null;	
	private TextView loadingItemView = null;
	
	private List<Event> foundEvents = new LinkedList();
		
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
        java.util.Calendar cal = java.util.Calendar.getInstance(timeZone); // いったんローカルのタイムゾーンでカレンダー計算
		cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
		cal.set(java.util.Calendar.MINUTE, 0);
		cal.set(java.util.Calendar.SECOND, 0);
		cal.set(java.util.Calendar.MILLISECOND, 0);
		cal.setTimeZone(TimeZone.getTimeZone("UTC")); // ローカルのタイムゾーンで日付設定したら、タイムゾーンをUTCに強制変更
		DateTime start = new DateTime(cal.getTimeInMillis(), 0); // tzShiftを0。UTCのオフセットを"Z"(=00:00)にする(参考：RFC3339)
		cal.add(java.util.Calendar.DATE, +1);
		DateTime end = new DateTime(cal.getTimeInMillis(), 0);

		task = new GoogleCalendarLoadEventTask(this, this, 5);
		task.setStartAndEndDate(start, end, timeZone);
		task.execute(Constants.MAIN_CALENDAR_ID, Constants.BROADCAST_CALENDAR_ID);
	}
	
	public void transitToEventListActivity() {
		Intent i = new Intent(this, EventListActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);		
		finish();
	}

	public void onURIOpening(String uri) {
		loadingItemView.setText("開いています: " + uri);		
	}
	
	public void onProgressUpdate(Event event) {
		String str = event.toDateTimeSummaryString();
		Log.d("SplashScreenActivity", str);
		loadingItemView.setText(str);
		//if(event.equalByDate(today, timeZone)) {
		foundEvents.add(event);
		//}
	}
	
	public void onPostExecute() {		
		loadingItemView.setText("表示準備中");
		
		Collections.sort(foundEvents, new EventComparator()); 
		
		EventDataBase db = new EventDataBase(this);
		db.open();
		db.deleteAllEvent();
		Iterator<Event> itr = foundEvents.iterator();
		while(itr.hasNext()) {
			db.insertEvent(itr.next());
		}
		db.close();		
		
		transitToEventListActivity();
	}
	
	/**
	 * イベント読み込みがキャンセル(認証失敗でリトライ)時に呼ばれる。
	 */
	public void retry(int tryNumber) {		
		Log.d(TAG, "retry()");
		foundEvents.clear();		
		new GoogleCalendarLoadEventTask(this, this, tryNumber).execute(
				Constants.MAIN_CALENDAR_ID, Constants.BROADCAST_CALENDAR_ID);		
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
}
