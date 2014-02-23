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
import jp.vocalendar.animation.vocalendar.DotCharacterAnimation;
import jp.vocalendar.animation.vocalendar.LoadingAnimation;
import jp.vocalendar.animation.vocalendar.LoadingAnimationUtil;
import jp.vocalendar.animation.vocalendar.AnnouncementAnimation;
import jp.vocalendar.googleapi.OAuthManager;
import jp.vocalendar.model.Event;
import jp.vocalendar.model.EventDataBase;
import jp.vocalendar.model.EventDataBaseRow;
import jp.vocalendar.task.CheckAnnouncementTask;
import jp.vocalendar.task.GoogleCalendarLoadEventTask;
import jp.vocalendar.task.LoadEventTask;
import jp.vocalendar.util.DateUtil;
import jp.vocalendar.util.DialogUtil;
import jp.vocalendar.util.UncaughtExceptionSavingHandler;
import android.accounts.Account;
import android.app.AlertDialog;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.api.client.util.DateTime;

/**
 * イベント読み込み中の画面のActivity。
 * バックグランド処理でイベントを読み込む。
 */
public class EventLoadingActivity extends ActionBarActivity implements LoadEventTask.TaskCallback {
	private static String TAG = "EventLoadingActivity";
	
	// イベントを取り込む開始日を指定する 年月日 をIntentに格納するキー
	public static String KEY_YEAR = "year";
	public static String KEY_MONTH = "month";
	public static String KEY_DATE = "date";	
	
	/**
	 * この画面が手動で呼ばれた場合にtrueをIntentに格納するためのキー。
	 * trueの場合(手動)、告知画面がある場合は、OKボタンが押されるまで待つ。
	 */
	public static String KEY_MANUAL_LOADING = "manual_loading";
	
	/**
	 * お知らせの有無を確認するかどうかをIntentに格納するためのキー。
	 * trueの場合、お知らせの有無を確認する。
	 */
	public static String KEY_CHECK_ANNOUNCEMENT = "check_announcement";
	
	/**
	 * お知らせの有無を確認するだけで終了する指定をIntentに格納するためのキー
	 */
	public static String KEY_CHECK_ANNOUNCEMENT_ONLY = "check_announcement_only";
	
	/** イベント取込に認証で失敗して読み込めなかった場合の結果コード */
	public static int RESULT_AUTH_FAILED = RESULT_FIRST_USER + 1;
	
	/**
	 * 認証系の処理(アカウント追加やアカウント利用許可など)をする時のリクエストコード。
	 */
	private static int LOGIN_REQUEST_CODE = Constants.GET_LOGIN;
	
	private GoogleCalendarLoadEventTask task = null;	
	private TextView loadingItemView = null;
	private LoadingAnimation loadingAnimation = null;
	
	// 読み込み中の日付
	private int loadingYear, loadingMonth, loadingDateOfMonth;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		UncaughtExceptionSavingHandler.init(this);

		
		loadingAnimation = makeLoadingAnimation();
		setContentViewForLoadingAnimation();
        getSupportActionBar().hide();
        
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
        
        if(getIntent().getBooleanExtra(KEY_CHECK_ANNOUNCEMENT, false)
        		|| getIntent().getBooleanExtra(KEY_CHECK_ANNOUNCEMENT_ONLY, false)) {
        	checkAnnouncement();
        } else {     
        	prepareLoadEventTask();
        }
	}

	protected void setContentViewForLoadingAnimation() {
		if(loadingAnimation instanceof DotCharacterAnimation.LinearDotCharacterAnimation) {
			setContentView(R.layout.loading_dot_character_animation);
		} else {
			setContentView(R.layout.loading);
		}
	}

	protected void initAnimation() {
		AnimationSurfaceView view = getAnimationSurfaceView();
		view.setLoadingAnimation(loadingAnimation);
		TextView tv = (TextView)findViewById(R.id.loadingImageCreatorText);
		tv.setMovementMethod(LinkMovementMethod.getInstance());		
		tv.setText(loadingAnimation.getCreatorText());
	}

	private void updateAnimation() {
		getAnimationSurfaceView().destroy();
		loadingAnimation = makeLoadingAnimation();
		setContentViewForLoadingAnimation();
		initAnimation();		
	}
	
	private void showAnnoucementAnimation() {
		getAnimationSurfaceView().destroy();
		loadingAnimation = new AnnouncementAnimation();
		setContentViewForLoadingAnimation();
		initAnimation();				
	}
	
	private AnimationSurfaceView getAnimationSurfaceView() {
		AnimationSurfaceView view =
				(AnimationSurfaceView)findViewById(R.id.loadingAnimationSurfaceView);
		return view;
	}
	
	private LoadingAnimation makeLoadingAnimation() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		if(pref.getString(Constants.ANNOUNCEMENT_URL_PREFERENCE_NAME, null) != null) {
			//告知有り
			if(!VocalendarApplication.isAnnouncementOnceOnly(this)					
					|| !VocalendarApplication.isAnnouncementDisplayed(this)) {				
				return new AnnouncementAnimation();
			}
		}
		String value = pref.getString(
				Constants.LOADING_PAGE_PREFERENCE_NAME, null);
		if(value == null) { // 初回起動時(未設定時)はドット絵を表示して、その後はランダム。
			value = Constants.LOADING_PAGE_DOT_ANIMATION;
			Editor e = pref.edit();
			e.putString(Constants.LOADING_PAGE_PREFERENCE_NAME, Constants.LOADING_PAGE_RANDOM);
			e.commit();			
		}
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
		
		if(loadingAnimation instanceof AnnouncementAnimation) {
			// お知らせを表示していた場合
			boolean firstDisplay = !VocalendarApplication.isAnnouncementDisplayed(this); // 初回表示			
			VocalendarApplication.setAnnouncementDisplayed(this, true); //表示したことを記録			
	        if(firstDisplay) {
	        	// 初回表示ならばOKボタン押下まで待つ
	        	waitOkButtonPressed();
	        	return;
	        }
		}
		transitToEventListActivity();        
	}

	/**
	 * 告知後にOKボタンが押されるまで待つ画面に変更する。
	 */
	protected void waitOkButtonPressed() {
		// 手動で更新した場合は、明示的にOKボタンを押して、イベント一覧画面へ戻る
		waitOkButtonPressed(R.string.check_the_announcement_above_and_push_ok_button);
	}

	/**
	 * OKボタンが押されるまで待つ画面に変更する。
	 * @param resid 表示するメッセージ
	 */
	protected void waitOkButtonPressed(int resid) {
		Button b = (Button)findViewById(R.id.cancelButton);
		b.setText(R.string.ok);
		b.setOnClickListener(new View.OnClickListener() {					
			@Override
			public void onClick(View v) {
				transitToEventListActivity();
			}
		});
		ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
		progressBar.setVisibility(View.INVISIBLE);
		TextView loadingView = (TextView)findViewById(R.id.loadingView);
		loadingView.setText(resid);
		TextView loadingItemView = (TextView)findViewById(R.id.loadingItemView);
		loadingItemView.setText("");		
	}	

	/**
	 * イベント読み込みタスクで読み込み失敗時の処理。
	 */
	private void doLoadingError() {
		if(task.getException() != null) { //例外発生時にダイアログ表示
			String msg = null;
			if(task.getException() instanceof IOException) { // 通信エラー
				doCommunicationError();
			} else { // 予期しないエラー
				msg = getResources().getString(R.string.unexpected_error);
				if(task.getException().getMessage() != null) {
					msg = msg + ": " + task.getException().getMessage();
				}
				DialogUtil.openErrorDialog(this, msg);
			}
		}
	}

	protected void doCommunicationError() {
		String msg;
		msg = getResources().getString(R.string.fail_to_connect_server) + "\n"
				+ getResources().getString(R.string.loaded_data_will_be_shown);					
		DialogUtil.openMessageDialog(this, msg, true);
	}
	
	/**
	 * イベント読み込みがキャンセル(認証失敗でリトライ)時に呼ばれる。
	 */
	public void retry(int tryNumber) {		
		Log.d(TAG, "retry()");
		startLoadEventTask();
	}
	
	private void prepareLoadEventTask() {
		OAuthManager.getInstance().doLogin(false, this, this, new OAuthManager.AuthHandler() {			
			@Override
			public void handleAuth(Account account, String authToken, Exception ex) {
				onAuthToken(account, authToken, ex);
			}
		});
	}
	
	private void onAuthToken(Account account, String authToken, Exception ex) {
		if(account == null || authToken == null) {
			if(ex instanceof IOException) {
				doCommunicationError();
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
			prepareLoadEventTask();
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
	
	/**
	 * お知らせの有無のチェック
	 */
	private void checkAnnouncement() {
		CheckAnnouncementTask task =
				new CheckAnnouncementTask(this,
						new CheckAnnouncementTask.Callback() {					
					@Override
					public void onPostExecute(Context context, boolean result) {
						if(getIntent().getBooleanExtra(KEY_CHECK_ANNOUNCEMENT_ONLY, false)) {
							if(result) {
								showAnnoucementAnimation();
								VocalendarApplication.setAnnouncementDisplayed(
										EventLoadingActivity.this, true);
								waitOkButtonPressed();
							} else {
								if(loadingAnimation instanceof AnnouncementAnimation) {
									// お知らせが有りから無しに変わった場合は、アニメーション変更
									updateAnimation(); 
								}
								showNoAnnouncementAndWaitOkButtonPressed();
							}
							return;
						}
						if(result) {
							//お知らせ無し/有り から 有りになった場合に更新。
							//お知らせ有り から 無しになった場合はそのまま。次回の表示時に反映される
							updateAnimation();
						}
						prepareLoadEventTask();
					}
				});
		task.execute(Constants.ANNOUNCEMENT_URL);
	}
	
	private void showNoAnnouncementAndWaitOkButtonPressed() {
		waitOkButtonPressed(R.string.no_announcement_from_vocalendar);
	}
}
