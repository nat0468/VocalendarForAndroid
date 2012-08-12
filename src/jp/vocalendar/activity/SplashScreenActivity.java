package jp.vocalendar.activity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import jp.vocalendar.R;
import jp.vocalendar.googleapi.OAuthManager;
import jp.vocalendar.model.Event;
import jp.vocalendar.model.EventDataBase;
import jp.vocalendar.model.GoogleCalendarLoadEventTask;
import jp.vocalendar.model.LoadEventTask;
import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import edu.emory.mathcs.backport.java.util.LinkedList;

/**
 * スプラッシュ画面を表示するActivity。
 * バックグランド処理でイベントを読み込む。
 */
public class SplashScreenActivity extends Activity {
	private static String TAG = "SplashScreenActivity";
	
	/** VOCALENDARのICSファイルのURL */
	private static String ICS_MAIN_URL = "https://www.google.com/calendar/ical/0mprpb041vjq02lk80vtu6ajgo%40group.calendar.google.com/public/basic.ics"; //メイン
	private static String ICS_BROADCAST_URL = "https://www.google.com/calendar/ical/5fsoru1dfaga56mcleu5mp76kk%40group.calendar.google.com/public/basic.ics"; //放送系 

	private LoadEventTask task = null;	
	private TextView loadingItemView = null;
	
	private Date today, tommorow;
	private TimeZone timeZone;
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
				task.cancel(false);
				transitToEventListActivity();
			}
		});
		loadingItemView = (TextView)findViewById(R.id.loadingItemView);
                
        initAccount();
	}

	private void startLoadEventTask() {
        timeZone = TimeZone.getDefault();
		Calendar cal = Calendar.getInstance(timeZone);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		today = cal.getTime();
		
		cal.add(Calendar.DATE, +1);
		tommorow = cal.getTime();
		
		//task = new ICalendarLoadEventTask(this);
		task = new GoogleCalendarLoadEventTask(this);
		task.execute(ICS_MAIN_URL, ICS_BROADCAST_URL);
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
		String str = event.toDateTimeString();
		Log.d("SplashScreenActivity", str);
		loadingItemView.setText(str);
		if(event.equalByDate(today, timeZone)) {
			foundEvents.add(event);
		}
	}
	
	public void onPostExecute() {		
		loadingItemView.setText("表示準備中");
		
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
	public void onCanceled() {		
		Log.d(TAG, "onCanceled()");
		foundEvents.clear();
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
		Log.i(TAG, "onAuthToken: " + account.name + "," + authToken);
		startLoadEventTask();
	}	
}
