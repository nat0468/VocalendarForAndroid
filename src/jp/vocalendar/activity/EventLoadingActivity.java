package jp.vocalendar.activity;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import jp.vocalendar.Constants;
import jp.vocalendar.R;
import jp.vocalendar.VocalendarApplication;
import jp.vocalendar.activity.view.AnimationSurfaceView;
import jp.vocalendar.animation.vocalendar.LoadingAnimation;
import jp.vocalendar.animation.vocalendar.LoadingAnimationUtil;
import jp.vocalendar.googleapi.OAuthManager;
import jp.vocalendar.model.Event;
import jp.vocalendar.model.EventDataBase;
import jp.vocalendar.model.EventDataBaseRow;
import jp.vocalendar.model.GoogleCalendarLoadEventTask;
import jp.vocalendar.model.LoadEventTask;
import jp.vocalendar.util.DateUtil;
import jp.vocalendar.util.DialogUtil;
import jp.vocalendar.util.UncaughtExceptionSavingHandler;
import android.accounts.Account;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
	
	/** イベント取込に認証で失敗して読み込めなかった場合の結果コード */
	public static int RESULT_AUTH_FAILED = RESULT_FIRST_USER + 1;
	
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
		UncaughtExceptionSavingHandler.init(this);
		
        setContentView(R.layout.loading);
        setTitle(R.string.vocalendar);
        
        Button cancel = (Button)findViewById(R.id.cancelButton);
        cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(task != null) {
					task.cancel(false);
				}
				transitToEventListActivityOnCancel();
			}
		});
		loadingItemView = (TextView)findViewById(R.id.loadingItemView);
        initAnimation();
        initAccount();
	}

	protected void initAnimation() {
		AnimationSurfaceView view = getAnimationSurfaceView();
		LoadingAnimation anim = makeLoadingAnimation();
		view.addAnimation(anim);
		TextView tv = (TextView)findViewById(R.id.loadingImageCreatorText);
		tv.setMovementMethod(LinkMovementMethod.getInstance());		
		tv.setText(anim.getCreatorText());
	}

	private AnimationSurfaceView getAnimationSurfaceView() {
		AnimationSurfaceView view =
				(AnimationSurfaceView)findViewById(R.id.loadingAnimationSurfaceView);
		return view;
	}
	
	private LoadingAnimation makeLoadingAnimation() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		String value = pref.getString(
				Constants.LOADING_PAGE_PREFERENCE_NAME, Constants.LOADING_PAGE_RANDOM);
		return LoadingAnimationUtil.makeLoadingAnimation(value);
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
		
		int duration = VocalendarApplication.getNumberOfDateToGetEvent(this);
        DateTime[] dates = DateUtil.makeStartAndEndDateTime(
        		loadingYear, loadingMonth, loadingDateOfMonth, timeZone, duration);
                                
        Date[] separators = new Date[duration];
        for(int i = 0; i < separators.length; i++) {
        	separators[i] = localCal.getTime();
        	localCal.add(Calendar.DATE, 1);
        }
        
		task = new GoogleCalendarLoadEventTask(this, this);
		task.setStartAndEndDate(dates[0], dates[1], separators, timeZone);
		task.execute(Constants.MAIN_CALENDAR_ID, Constants.BROADCAST_CALENDAR_ID);
	}
	
	/**
	 * イベント読み込みに成功してEventListActivityに遷移
	 */
	public void transitToEventListActivity() {
		Intent i = new Intent();
		i.putExtra(KEY_YEAR, loadingYear);
		i.putExtra(KEY_MONTH, loadingMonth);
		i.putExtra(KEY_DATE, loadingDateOfMonth);
		setResult(RESULT_OK, i);
		finish();
	}

	/**
	 * キャンセルしてEventListActivityに遷移
	 */
	public void transitToEventListActivityOnCancel() {
		Intent i = new Intent();
		setResult(RESULT_CANCELED, i);
		finish();
	}
	
	public void onURIOpening(String uri) {
		loadingItemView.setText("開いています: " + uri);		
	}
	
	public void onProgressUpdate(Event event) {
		String str = event.toDateTimeSummaryString(TimeZone.getDefault(), this);
		Log.d("SplashScreenActivity", str);
		loadingItemView.setText(str);
	}
	
	public void onPostExecute(List<EventDataBaseRow> events) {
		if(events == null) { // エラー時の処理
			doLoadingError();
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
	 * イベント読み込みタスクで読み込み失敗時の処理。
	 */
	private void doLoadingError() {
		if(task.getException() != null) { //例外発生時にダイアログ表示
			String msg = null;
			if(task.getException() instanceof IOException) { // 通信エラー
				msg = getResources().getString(R.string.fail_to_connect_server) + "\n"
						+ getResources().getString(R.string.loaded_data_will_be_shown);					
				DialogUtil.openMessageDialog(this, msg, true);
			} else { // 予期しないエラー
				msg = getResources().getString(R.string.unexpected_error);
				if(task.getException().getMessage() != null) {
					msg = msg + ": " + task.getException().getMessage();
				}
				DialogUtil.openErrorDialog(this, msg);
			}
		}
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
			public void handleAuth(Account account, String authToken, Exception ex) {
				onAuthToken(account, authToken, ex);
			}
		});
	}
	
	private void onAuthToken(Account account, String authToken, Exception ex) {
		if(account == null || authToken == null) {
			if(ex instanceof IOException) {
				String m = getResources().getString(R.string.communication_error);
				if(ex.getMessage() != null) {
					m = m + ": " + ex.getMessage();
				}				
				DialogUtil.openErrorDialog(this, m);
			} else {
				showGoogleAccountRequredDialog();
			}
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
		    		   setResult(RESULT_AUTH_FAILED);
		    		   finish();
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
	
	@Override
	protected void onPause() {
		super.onPause();
		getAnimationSurfaceView().pause();
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		getAnimationSurfaceView().resume();
	}
}
