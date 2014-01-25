package jp.vocalendar.model;

import java.util.List;
import java.util.TimeZone;

import jp.vocalendar.R;
import jp.vocalendar.VocalendarApplication;
import jp.vocalendar.activity.FavoriteEventListActivity;
import jp.vocalendar.activity.view.LoadMoreEventView;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * お気に入り一覧画面のもっと読み込む処理の制御を担うクラス
 */
public class LoadMoreFavoriteEventController {	
	/**
	 * お気に入りを読み込むタスク
	 */
	private class LoadPreviousFavoriteEventTask extends AsyncTask<Long, Void, EventDataBaseRow[]> {
		/** 新しく読み込んだお気に入りイベント */
		private FavoriteEventDataBaseRow[] newFavoriteEvents = null;
		/**
		 * お気に入りを読み込む。
		 * @param ここに指定した START_DATE_INDEX 未満のお気に入りイベントを読み込む。
		 */
		@Override
		protected EventDataBaseRow[] doInBackground(Long... params) {
			int max = VocalendarApplication.getNumberOfFavoriteEventToDisplay(context);
			EventDataBase db = new EventDataBase(context);
			db.open();			
			List<FavoriteEventDataBaseRow> list =
					db.getFavoriteEventsPrevious(
							params[0], context.getStartTimeToList(), max, timeZone);
			db.close();
			newFavoriteEvents = list.toArray(new FavoriteEventDataBaseRow[list.size()]);
			
			EventDataBaseRow[] currentRows =
					context.getEventArrayCursorAdapter().getEventArrayCursor().getEventDataBaseRows();
			
			EventDataBaseRow[] rows =
					new EventDataBaseRow[list.size() + currentRows.length];
			int index = 0;
			for(FavoriteEventDataBaseRow row : list) {
				rows[index++] = row;
			}
			for(EventDataBaseRow row : currentRows) {
				rows[index++] = row;
			}
			return rows;
		}

		@Override
		protected void onPostExecute(EventDataBaseRow[] result) {
			previousEventsLoaded(result);
		}

		public FavoriteEventDataBaseRow[] getNewFavoriteEvents() {
			return newFavoriteEvents;
		}
	}
	
	private static final String TAG = "LoadMoreFavoriteEventController";
	
	/**
	 * お気に入り一覧画面のActivity
	 */
	private FavoriteEventListActivity context;
	
	private ListView listView;
	/** もっと読み込むを表示している場合は true */
	private boolean loadMorePreviousEventViewDisplayed = false;
	
	/** 前のイベントをもっと読み込む操作のためのView */
	private LoadMoreEventView loadMorePreviousEventView = null;
	/** 前のイベントをもっと読み込む処理を実行中のTask */
	private LoadPreviousFavoriteEventTask task = null;
	
	/**
	 * 前のイベントをスクロール時に自動的に読み込むときにtrueとなる。
	 * 一度でも前のイベントを読み込み操作すると、これがtrueとなる。
	 * 読み込みキャンセルを行うとfalseに戻る。 */
	private boolean autoLoadingPreviousEvents = false;
	
	private TimeZone timeZone = TimeZone.getDefault();	
	
	public LoadMoreFavoriteEventController(FavoriteEventListActivity context) {
		this.context = context;
	}
	
	private LoadMoreEventView loadMoreEventView() {
		return (LoadMoreEventView)context.getLayoutInflater().inflate(R.layout.event_list_progress, null);
	}

	/**
	 * もっと読み込む項目を追加する。
	 * @param lv
	 * @param colorTheme
	 */
	public void setupListView(ListView lv, ColorTheme colorTheme) {
		listView = lv;
		loadMorePreviousEventView = loadMoreEventView();
		loadMorePreviousEventViewDisplayed = true;		
		lv.addHeaderView(loadMorePreviousEventView, null, true);
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
	
	/**
	 * もっと読み込む項目を、もし追加していたら削除する。追加していない場合は何もしない。
	 */
	public void resetListView() {
		if(listView != null && loadMorePreviousEventView != null) {
			listView.removeHeaderView(loadMorePreviousEventView);
			listView = null;
			loadMorePreviousEventView = null;
		}
	}
	
	public void applyColorThemeToListView(ListView listView, ColorTheme colorTheme) {
		loadMorePreviousEventView.setBackgroundDrawable(colorTheme.makeLightBackgroundStateListDrawable());
		listView.setDivider(new ColorDrawable(colorTheme.getDividerColor()));
		listView.setDividerHeight((int)(context.getResources().getDisplayMetrics().density * 1.0));
	}
	
	public void loadPreviousEventsTapped() {
		synchronized (loadMorePreviousEventView) {
			if(loadMorePreviousEventView.isLoading()) {				
				loadMorePreviousEventView.setLoading(false);
				autoLoadingPreviousEvents = false;
				task.cancel(true);
			} else {
				loadMorePreviousEventView.setLoading(true);
				loadPreviousEvents();						
				// もっと読み込むをタップしてキャンセルした場合、もう一度タップしたら、もっと読み込む設定を復元する。
				autoLoadingPreviousEvents = VocalendarApplication.getLoadMoreEventWithoutTap(context);
			}
		}
	}
		
	private void loadPreviousEvents() {
		task = new LoadPreviousFavoriteEventTask();		
		task.execute(context.getStartTimeToList().getTimeInMillis());
	}
	
	/**
	 * 前方への追加イベント読み込み完了時に呼ばれるメソッド
	 * @param loadedEvents 追加で読み込まれたイベント
	 */
	private void previousEventsLoaded(EventDataBaseRow[] rows) {
		VocalendarApplication app = (VocalendarApplication)context.getApplication();
		app.getFavoriteEventManager().loadFavoriteEvent(task.getNewFavoriteEvents());		
		context.updateFavoriteEventRows(rows);
	
		loadMorePreviousEventView.setLoading(false);
		
		task = null;
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
	}	
	
	public void setAutoLoading(boolean b) {
		autoLoadingPreviousEvents = b;
	}	
	
	/** もっと読み込むを表示している場合はtrueを返す */	
	public boolean isLoadMorePreviousEventViewDisplayed() {
		return loadMorePreviousEventViewDisplayed;
	}
}
