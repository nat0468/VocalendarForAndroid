package jp.vocalendar.activity;

import jp.vocalendar.Constants;
import jp.vocalendar.R;
import jp.vocalendar.VocalendarApplication;
import jp.vocalendar.activity.view.AnimationSurfaceView;
import jp.vocalendar.animation.vocalendar.AnnouncementAnimation;
import jp.vocalendar.animation.vocalendar.DotCharacterAnimation;
import jp.vocalendar.animation.vocalendar.LoadingAnimation;
import jp.vocalendar.animation.vocalendar.LoadingAnimationUtil;
import jp.vocalendar.model.FavoriteEventManager.GCalendarIdAndGid;
import jp.vocalendar.task.UpdateFavoriteEventTask;
import jp.vocalendar.util.UncaughtExceptionSavingHandler;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * 読み込みタスクの実行と読み込み中画面の表示を行うActivity
 */
public class LoadingActivity extends ActionBarActivity {
	private static final String KEY_PREFIX = "jp.vocalendar.activity.LoadingActivity.";
	
	/** 実行するタスクのクラス名をIntentに指定するためのキー */
	public static final String KEY_TASK_CLASS_NAME = KEY_PREFIX + "TASK_CLASS_NAME";
	
	/** タスクを実行するときの引数をIntentに指定するためのキー */
	public static final String KEY_ARGS = KEY_PREFIX + "ARGS";
	
	/**
	 * 実行中のタスクからのコールバック用インターフェイス
	 */
	public interface TaskCallback {
		/** タスク開始前の初期化時のコールバック。引数に出力する読み込み中メッセージを指定 */
		public void onInit(String message);
		/** タスクの進捗時のコールバック。引数に読み込み中アイテムのメッセージを指定 */
		public void onProgressUpdate(String itemMessage);		
		/**
		 * タスク終了時のコールバック
		 * @param intent 呼び出し元へ返すデータを格納するIntent
		 */
		public void onPostExecute(Intent intent);
		/** タスクキャンセル時のコールバック */
		public void onCanceled();
	}
	
	private LoadingAnimation loadingAnimation = null;
	private AsyncTask task = null;		
	private TextView loadingItemView = null;
	
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
				taskCanceled();
			}
		});
		loadingItemView = (TextView)findViewById(R.id.loadingItemView);
        initAnimation();
        executeTask();
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
	
	private AnimationSurfaceView getAnimationSurfaceView() {
		AnimationSurfaceView view =
				(AnimationSurfaceView)findViewById(R.id.loadingAnimationSurfaceView);
		return view;
	}	
	
	private void executeTask() {
		String className = getIntent().getStringExtra(KEY_TASK_CLASS_NAME);
		if("UpdateFavoriteEventTask".equals(className)) {			
			UpdateFavoriteEventTask
			updateFavoriteEventTask = new UpdateFavoriteEventTask(this, new TaskCallback() {
				@Override
				public void onInit(String message) {
					updateLoadingMessage(message);
				}				
				@Override
				public void onProgressUpdate(String message) {
					updateProgressMessage(message);
				}				
				@Override
				public void onPostExecute(Intent intent) {
					taskFinished(intent);
				}
				@Override
				public void onCanceled() {
					taskCanceled();
				}
			});
			Object[] objs = (Object[])getIntent().getSerializableExtra(KEY_ARGS);
			GCalendarIdAndGid[] ids = new GCalendarIdAndGid[objs.length];
			for(int i = 0; i < ids.length; i++) {
				ids[i] = (GCalendarIdAndGid)objs[i];
			}
			task = updateFavoriteEventTask;
			updateFavoriteEventTask.execute(ids);
		}
		
	}

	/**
	 * 読み込み中メッセージの更新
	 * @param message
	 */
	private void updateLoadingMessage(String message) {
		TextView tv = (TextView)findViewById(R.id.loadingView);
		tv.setText(message);		
	}	
	/**
	 * 読み込み進捗メッセージの更新
	 * @param message
	 */
	private void updateProgressMessage(String message) {
		loadingItemView.setText(message);		
	}
	
	/**
	 * タスクが正常終了したときに呼ばれる
	 */
	private void taskFinished(Intent intent) {
		setResult(Activity.RESULT_OK, intent);
		finish();
	}
	
	/**
	 * タスクがキャンセルされたときに呼ばれる
	 */
	private void taskCanceled() {
		setResult(Activity.RESULT_CANCELED);
		finish();		
	}
}
