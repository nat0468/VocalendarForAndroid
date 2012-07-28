package jp.vocalendar.model;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import jp.vocalendar.activity.SplashScreenActivity;
import jp.vocalendar.model.EventContentHandler.EventHandler;
import android.os.AsyncTask;
import android.util.Log;

/**
 * イベント情報を読み込むAsyncTaskの共通処理。
 */
public abstract class LoadEventTask extends AsyncTask<URI, Event, List<Event>> {
	private static String TAG = "LoadEventTask";

	/**
	 * イベント読み込み終了時にコールバックするSplashScreenActivity
	 */
	protected SplashScreenActivity splashScreenActivity = null;
	/**
	 * タスクがキャンセルされたときにtrueになる。
	 */
	protected boolean canceled = false;
	/**
	 * 読み込み中のイベント。
	 */
	protected List<Event> eventList = new LinkedList<Event>();

	/**
	 * コンストラクタ。
	 * @param activity イベント読み込み終了時にコールバックするSplashScreenActivity
	 */	
	public LoadEventTask(SplashScreenActivity activity) {
			this.splashScreenActivity = activity;
	}
	
	@Override
	protected void onPostExecute(List<Event> result) {
		if(splashScreenActivity != null) { 
			splashScreenActivity.onPostExecute(result);
		}		
	}

	@Override
	protected void onProgressUpdate(Event... events) {
		if(splashScreenActivity != null) {
			splashScreenActivity.onProgressUpdate(events[0]);
		}
	}

	@Override
	protected void onCancelled() {
		Log.d(TAG, "onCancelled()");
		this.canceled = true;
		this.splashScreenActivity = null;
	}	

}