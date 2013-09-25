package jp.vocalendar.model;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.google.api.client.util.DateTime;

import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.ListView;
import jp.vocalendar.Constants;
import jp.vocalendar.R;
import jp.vocalendar.VocalendarApplication;
import jp.vocalendar.activity.EventListActivity;
import jp.vocalendar.activity.view.LoadMoreEventView;
import jp.vocalendar.util.DateUtil;
import jp.vocalendar.util.DialogUtil;

/**
 * イベント一覧画面のもっと読み込む処理の制御を担うクラス
 */
public class LoadMoreEventController {
	/**
	 * イベント読み込み処理のコールバック
	 */
	private class LoadEventTaskCallback implements LoadEventTask.TaskCallback {		
		/** もっと読み込む方向を示す。trueなら前のイベント、falseなら後のイベントを読み込む */
		private boolean loadMorePreviousEvent;
		/**
		 * コンストラクタ
		 */
		public LoadEventTaskCallback(boolean loadMorePreviousEvent) {
			this.loadMorePreviousEvent = loadMorePreviousEvent;
		}
		
		@Override
		public void retry(int retryNumber) {
			// とりあえず失敗にする。
			failed();						
		}

		private void failed() {
			Log.d(TAG, "GoogleCalendarLoadEventTask failed...");
			String msg = context.getResources().getString(R.string.fail_to_connect_server);					
			DialogUtil.openMessageDialog(context, msg, false);
			if(loadMorePreviousEvent) {
				loadMorePreviousEventView.setLoading(false);
			} else {
				loadMoreNextEventView.setLoading(false);
			}
		}
		
		@Override
		public void onProgressUpdate(Event event) {
			//何もしない 
		}
		
		@Override
		public void onPostExecute(List<EventDataBaseRow> events) {
			Log.d(TAG, "onPostExecute()");
			if(events == null) {
				failed();
				return; // 読み込み失敗なので何もしない。
			}
			
			if(loadMorePreviousEvent) {				
				previousEventsLoaded(events);
			} else {
				nextEventsLoaded(events);
			}
		}
	}

	private static final String TAG = "LoadMoreEventListController";
	
	/**
	 * イベント一覧画面のActivity
	 */
	private EventListActivity context;
	
	/** 前のイベントをもっと読み込む操作のためのView */
	private LoadMoreEventView loadMorePreviousEventView = null;
	/** 前のイベントをもっと読み込む処理を実行中のTask */
	private EventListActivityLoadEventTask loadMorePreviousEventTask = null;
	
	/**
	 * 前のイベントをスクロール時に自動的に読み込むときにtrueとなる。
	 * 一度でも前のイベントを読み込み操作すると、これがtrueとなる。
	 * 読み込みキャンセルを行うとfalseに戻る。 */
	private boolean autoLoadingPreviousEvents = false;
	
	/** 次のイベントを読み込み操作のためのView */
	private LoadMoreEventView loadMoreNextEventView = null;
	/** 次のイベントをもっと読み込む処理を実行中のTask */
	private EventListActivityLoadEventTask loadMoreNextEventTask = null;
	/**
	 * 次のイベントをスクロール時に自動的に読み込むときにtrueとなる。
	 * 一度でも次のイベントを読み込み操作すると、これがtrueとなる。
	 * 読み込みキャンセルを行うとfalseに戻る。 */
	private boolean autoLoadingNextEvents = false;

	/**
	 * 今までにイベントを読み込んだ日付の中で、最も早い日付。
	 * 前のイベントを読み込むと更新されていく。
	 */
	private Calendar topDate = Calendar.getInstance();	
	
	public LoadMoreEventController(EventListActivity context) {
		this.context = context;
	}
	
	private LoadMoreEventView loadMoreEventView() {
		return (LoadMoreEventView)context.getLayoutInflater().inflate(R.layout.event_list_progress, null);
	}

	public void setupListView(ListView lv, ColorTheme colorTheme) {
		loadMorePreviousEventView = loadMoreEventView();
		lv.addHeaderView(loadMorePreviousEventView, null, true);
		loadMoreNextEventView = loadMoreEventView();
		lv.addFooterView(loadMoreNextEventView, null, true);
		applyColorThemeToListView(lv, colorTheme);
		
		lv.setOnScrollListener(new AbsListView.OnScrollListener() {			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// 何もしない
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				onListScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			}
		});
	}
	
	public void applyColorThemeToListView(ListView listView, ColorTheme colorTheme) {
		loadMorePreviousEventView.setBackgroundDrawable(colorTheme.makeLightBackgroundStateListDrawable());
		loadMoreNextEventView.setBackgroundDrawable(colorTheme.makeLightBackgroundStateListDrawable());
		listView.setDivider(new ColorDrawable(colorTheme.getDividerColor()));
		listView.setDividerHeight((int)(context.getResources().getDisplayMetrics().density * 1.0));
	}
	
	public void loadPreviousEventsTapped() {
		synchronized (loadMorePreviousEventView) {
			if(loadMorePreviousEventView.isLoading()) {				
				loadMorePreviousEventView.setLoading(false);
				autoLoadingPreviousEvents = false;
				loadMorePreviousEventTask.cancel(true);
			} else {
				loadMorePreviousEventView.setLoading(true);
				loadPreviousEvents();						
				// もっと読み込むをタップしてキャンセルした場合、もう一度タップしたら、もっと読み込む設定を復元する。
				autoLoadingPreviousEvents = VocalendarApplication.getLoadMoreEventWithoutTap(context);
			}
		}
	}
		
	private void loadPreviousEvents() {
		TimeZone timeZone = TimeZone.getDefault();
		Date topDate = context.getEventDataBaseRowArray().getTopDate();
		
        Calendar localCal = Calendar.getInstance(timeZone);
        localCal.set(Calendar.HOUR_OF_DAY, 0);
        localCal.set(Calendar.MINUTE, 0);
        localCal.set(Calendar.SECOND, 0);
        localCal.set(Calendar.MILLISECOND, 0);		        
        localCal.set(Calendar.YEAR, 1900 + topDate.getYear());
        localCal.set(Calendar.MONTH, topDate.getMonth());
        localCal.set(Calendar.DATE, topDate.getDate());
		
		int duration = getNumberOfDateToLoadMoreEvents();
    	localCal.add(Calendar.DATE, -duration);
		int y = localCal.get(Calendar.YEAR);
		int m = localCal.get(Calendar.MONTH);
		int d = localCal.get(Calendar.DATE);
		Date newLoadingDate = new Date();
		newLoadingDate.setYear(y);
		newLoadingDate.setMonth(m);
		newLoadingDate.setDate(d);		
		
        Date[] separators = new Date[duration];
        for(int i = 0; i < separators.length; i++) {
        	separators[i] = localCal.getTime();
        	localCal.add(Calendar.DATE, 1);
        }
		
        DateTime[] dates = DateUtil.makeStartAndEndDateTime(y, m, d, timeZone, duration);		

		loadMorePreviousEventTask = 
				new EventListActivityLoadEventTask(
						context, new LoadEventTaskCallback(true), true);		
		loadMorePreviousEventTask.setStartAndEndDate(dates[0], dates[1], separators, timeZone);
		loadMorePreviousEventTask.execute(Constants.MAIN_CALENDAR_ID, Constants.BROADCAST_CALENDAR_ID);
	}
	
	/**
	 * 前方への追加イベント読み込み完了時に呼ばれるメソッド
	 * @param loadedEvents 追加で読み込まれたイベント
	 */
	private void previousEventsLoaded(List<EventDataBaseRow> loadedEvents) {
		VocalendarApplication app = (VocalendarApplication)context.getApplication();
		context.setEventDataBaseRowArray(loadMorePreviousEventTask.getEventDataBaseRowArray());
    	app.setEventDataBaseRowArray(loadMorePreviousEventTask.getEventDataBaseRowArray());

		loadMorePreviousEventView.setLoading(false);
		context.setSelection(loadedEvents.size());
		
		topDate.setTimeInMillis(loadMorePreviousEventTask.getStart().getValue());
		context.setTopDate(topDate);
		
		loadMorePreviousEventTask = null;
	}
	
	public void loadNextEventsTapped() {
		synchronized (loadMoreNextEventView) {
			if(loadMoreNextEventView.isLoading()) {				
				loadMoreNextEventView.setLoading(false);
				loadMoreNextEventTask.cancel(true);
				autoLoadingNextEvents = false;
			} else {
				loadMoreNextEventView.setLoading(true);
				loadNextEvents();
				// もっと読み込むをタップしてキャンセルした場合、もう一度タップしたら、もっと読み込む設定を復元する。
				autoLoadingNextEvents = VocalendarApplication.getLoadMoreEventWithoutTap(context);
			}
		}
	}

	private void loadNextEvents() {		
		TimeZone timeZone = TimeZone.getDefault();
		Date lastDate = context.getEventDataBaseRowArray().getLastDate();		
		
        Calendar localCal = Calendar.getInstance(timeZone);
        localCal.set(Calendar.HOUR_OF_DAY, 0);
        localCal.set(Calendar.MINUTE, 0);
        localCal.set(Calendar.SECOND, 0);
        localCal.set(Calendar.MILLISECOND, 0);		        
        localCal.set(Calendar.YEAR, 1900 + lastDate.getYear());
        localCal.set(Calendar.MONTH, lastDate.getMonth());
        localCal.set(Calendar.DATE, lastDate.getDate());
		
    	localCal.add(Calendar.DATE, +1);

    	int y = localCal.get(Calendar.YEAR);
		int m = localCal.get(Calendar.MONTH);
		int d = localCal.get(Calendar.DATE);
		
		int duration = getNumberOfDateToLoadMoreEvents();
        Date[] separators = new Date[duration];
        for(int i = 0; i < separators.length; i++) {
        	separators[i] = localCal.getTime();
        	localCal.add(Calendar.DATE, 1);
        }
		
        DateTime[] dates = DateUtil.makeStartAndEndDateTime(y, m, d, timeZone, duration);		

		loadMoreNextEventTask = 
				new EventListActivityLoadEventTask(
						context, new LoadEventTaskCallback(false), false);		
		loadMoreNextEventTask.setStartAndEndDate(dates[0], dates[1], separators, timeZone);
		loadMoreNextEventTask.execute(Constants.MAIN_CALENDAR_ID, Constants.BROADCAST_CALENDAR_ID);		
	}
	
	private void nextEventsLoaded(List<EventDataBaseRow> events) {
		VocalendarApplication app = (VocalendarApplication)context.getApplication();
		context.updateEventDataBaseRowArray(loadMoreNextEventTask.getEventDataBaseRowArray());
    	app.setEventDataBaseRowArray(loadMoreNextEventTask.getEventDataBaseRowArray());
    	
        loadMoreNextEventView.setLoading(false);
		
        //setSelection()を実行しても、スクロール位置が動く場合と動かない場合あり。
        //動く場合は、以下で指定された位置に移動するが、(一番最下端のイベントの位置) - (画面内のイベントの数) に
        //設定する必要がある。(画面内のイベントの数)を知る方法が未調査のため、コメントアウト。
        //今の所、スクロール位置を動かさなくても、スクロールの動きはおかしくないので、この仕様にする。
        //setSelection(eventArrayCursorAdapter.getCount() - events.size() + 1); // 読み込んだイベントの位置に移動
        
		loadMoreNextEventTask = null;
	}
	
	/**
	 * スクロール時に呼ばれる。
	 * 一覧上下端の読み込み項目表示を判定する。
	 * @param view
	 * @param firstVisibleItem
	 * @param visibleItemCount
	 * @param totalItemCount
	 */
	protected void onListScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if(visibleItemCount == 0) {
			return; // 無視
		}
		if(autoLoadingPreviousEvents && firstVisibleItem == 0) { // ListView最上端
			if(visibleItemCount == totalItemCount) { // スクロールせずに全リスト(最上端)が見える場合は自動読み込み無効化
				Log.d(TAG,
						"onListScroll():visibleItemCount==totalItemCount:" + visibleItemCount +
						" auto loading disabled.");
				autoLoadingPreviousEvents = false;
				return;
			}
			if(!loadMorePreviousEventView.isLoading()) {
				loadPreviousEventsTapped();
				return;
			}
		}
		if(autoLoadingNextEvents &&
				totalItemCount == (firstVisibleItem + visibleItemCount)) { // ListView最下端
			if(visibleItemCount == totalItemCount) { // スクロールせずに全リスト(最下端)が見える場合は自動読み込み無効化
				Log.d(TAG,
						"onListScroll():visibleItemCount==totalItemCount:" + visibleItemCount +
						"auto loading disabled.");
				autoLoadingNextEvents = false;
				return;
			}
			if(!loadMoreNextEventView.isLoading()) {
				loadNextEventsTapped();
			}
		}
	}	
	
	public void setAutoLoading(boolean b) {
		autoLoadingPreviousEvents = b;
		autoLoadingNextEvents = b;
	}
	
	/**
	 * もっと読み込むイベント取得日数を返す。
	 * @return
	 */
	private int getNumberOfDateToLoadMoreEvents() {		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		String value = pref.getString(Constants.NUMBER_OF_DATE_TO_LOAD_MORE_EVENTS_PREFRENCE_NAME, "3");
		try {
			return Integer.parseInt(value);
		} catch(NumberFormatException e) {
			Log.e(TAG, "Invalid number of date to get events: " + value);
		}
		return 3;
	}

	public Calendar getTopDate() {
		return topDate;
	}	
}
