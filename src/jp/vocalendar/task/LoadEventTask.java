package jp.vocalendar.task;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;


import jp.vocalendar.R;
import jp.vocalendar.model.Event;
import jp.vocalendar.model.EventDataBaseRow;
import jp.vocalendar.util.DialogUtil;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

/**
 * イベント情報を読み込むAsyncTaskの共通処理。
 */
public abstract class LoadEventTask extends AsyncTask<String, Event, List<EventDataBaseRow>> {
	/** 
	 * AsyncTaskのからのコールバックインターフェイス
	 */
	public static interface TaskCallback {
		/** Task終了後に呼ばれる */
		public void onPostExecute(List<EventDataBaseRow> events);
		/** Event読み込み毎に呼ばれる */
		public void onProgressUpdate(Event event);
		/**
		 * 認証失敗などで再読込が必要なときに呼ばれる。
		 * 典型的には、このLoadEventTaskを再実行する処理をこのメソッドで実装する。
		 * @param retryNumber 残り再試行回数。LoadEventTaskを再実行するときは、この値を使う。
		 */
		public void retry(int retryNumber);
	}
	
	protected static final String TAG = "LoadEventTask";

	/**
	 * イベント読み込み終了時にコールバックするTaskCallback
	 */
	protected TaskCallback taskCallback = null;
	
	/**
	 * このタスクを実行するActivity
	 */
	protected Activity activity = null;
	
	/**
	 * タスク実行中に発生した例外
	 */
	protected Exception exception = null;
	
	/**
	 * コンストラクタ。
	 * @param activity このタスクを実行するActivity
	 */	
	public LoadEventTask(Activity activity, TaskCallback taskCallback) {
		this.activity = activity;
		this.taskCallback = taskCallback;
	}
	
	@Override
	protected void onPostExecute(List<EventDataBaseRow> events) {
		if(taskCallback != null && !isCancelled()) { 
			taskCallback.onPostExecute(events);
		}		
	}

	@Override
	protected void onProgressUpdate(Event... events) {
		if(taskCallback != null) {
			taskCallback.onProgressUpdate(events[0]);
		}
	}

	/**
	 * (認証失敗時に)再読み込みするときに呼ぶ
	 * @param tryNumber 残り試行回数
	 */
	protected void doRetry(int tryNumber) {
		Log.d(TAG, "doRetry()");
		this.taskCallback.retry(tryNumber);
		this.taskCallback = null;
		cancel(false);
	}

	/**
	 * タスク実行中に発生した例外
	 * @return
	 */
	public Exception getException() {
		return exception;
	}
}