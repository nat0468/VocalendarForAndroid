package jp.vocalendar.activity;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.google.api.client.util.DateTime;

import jp.vocalendar.BuildConfig;
import jp.vocalendar.Constants;
import jp.vocalendar.Help;
import jp.vocalendar.R;
import jp.vocalendar.VocalendarApplication;
import jp.vocalendar.activity.view.LoadMoreEventView;
import jp.vocalendar.model.ColorTheme;
import jp.vocalendar.model.Event;
import jp.vocalendar.model.EventArrayCursor;
import jp.vocalendar.model.EventArrayCursorAdapter;
import jp.vocalendar.model.EventDataBase;
import jp.vocalendar.model.EventDataBaseRow;
import jp.vocalendar.model.EventDataBaseRowArray;
import jp.vocalendar.model.EventListActivityLoadEventTask;
import jp.vocalendar.model.LoadEventTask;
import jp.vocalendar.util.DateUtil;
import jp.vocalendar.util.UncaughtExceptionSavingHandler;
import jp.vocalendar.util.UncaughtExceptionSavingToFileHandler;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

public class EventListActivity extends ListActivity {
	private static final String TAG = "EventListActivity";
	
	public static final String EXTRA_EVENT_LIST = 
			EventListActivity.class.getPackage().getName() + ".EventList";
	
	// EventListLoadingActivityを呼ぶためのリクエストコード
	private static int REQUEST_CODE_GET_DAYLY_EVENT = 1;
	// SettingActivityを呼ぶためのリクエストコード
	private static int REQUEST_CODE_OPEN_SETTINGS = 2;	

	/** 
	 * イベントを読み込む日付。
	 * 更新ボタンを押したときに読み込む日付、日付変更ダイアログの初期値に使う。
	 * 初回起動時に今日の日付に変更され、イベント読み込み画面から戻ってきたときに変更される。
	 * もっと読み込む時には変更しない。
	 **/
	private Calendar currentDate = Calendar.getInstance();
	
	private Calendar topDate = currentDate;
	
	/** イベント一覧の配列 */
	private EventDataBaseRowArray eventDataBaseRowArray;
	
	/** イベント一覧表示用のアダプタ */
	private EventArrayCursorAdapter eventArrayCursorAdapter;
	
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
	
	// TODO
	/**
	 * イベント一覧表示後にスクロール移動したときにtrueになる。
	 * タップなしでもっと読み込む設定のときに、画面に表示するイベント数が画面より少ないときは、
	 * 強制的にタップして読み込む動きにするために使う。
	 * これがtrueの場合に、上下端
	 */
	private boolean isScrolled = false;
	
	private ColorTheme colorTheme;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UncaughtExceptionSavingHandler.init(this);
        
        colorTheme = new ColorTheme(this);
        setContentView(R.layout.event_list);
        
        setupButtons();
        setupListView();
        
        setDateToToday();
        if(isUpdateRequired()) {
        	openEventLoadingActivity();
        } else {
        	updateList();
        }
    }

	private void setupButtons() {
		Button settingButton = (Button)findViewById(R.id.setting_button);
        settingButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(EventListActivity.this, SettingActivity.class);
				startActivityForResult(intent, REQUEST_CODE_OPEN_SETTINGS);				
			}
		});
        
        Button changeDateButton = (Button)findViewById(R.id.change_date_button);
        changeDateButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				openDatePicker();
			}
		});
        
        Button updateButton = (Button)findViewById(R.id.update_button);
        updateButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				openEventLoadingActivity();
			}
		});
        
        Button helpButton = (Button)findViewById(R.id.help_button);
        helpButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				/* TODO
		        boolean test = true;
		        if(test) {
		        	throw new RuntimeException("test!!");
		        }
				*/
				openHelp();
			}
		});
        
        ImageButton searchButton = (ImageButton)findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				openSearch();
			}
		});
	}
	
	private void setupListView() {
		ListView lv = getListView();
		loadMorePreviousEventView = loadMoreEventView();
		lv.addHeaderView(loadMorePreviousEventView, null, true);
		loadMoreNextEventView = loadMoreEventView();
		lv.addFooterView(loadMoreNextEventView, null, true);
		applyColorThemeToListView();
		
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
	
	private void applyColorThemeToListView() {
		loadMorePreviousEventView.setBackgroundDrawable(colorTheme.makeLightBackgroundStateListDrawable());
		loadMoreNextEventView.setBackgroundDrawable(colorTheme.makeLightBackgroundStateListDrawable());
		getListView().setDivider(new ColorDrawable(colorTheme.getDividerColor()));
		getListView().setDividerHeight((int)(getResources().getDisplayMetrics().density * 1.0));
	}

	/** 最上端(読み込み項目除く)位置にスクロールバーを移動する */
	private void scrollToHead() {
		setSelection(1);
	}
	
	private LoadMoreEventView loadMoreEventView() {
		return (LoadMoreEventView)getLayoutInflater().inflate(R.layout.event_list_progress, null);
	}

	private void updateList() {
		EventDataBase db = new EventDataBase(this);
		db.open();		
        eventDataBaseRowArray = db.getEventDataBaseRowArray();
        db.close();

        VocalendarApplication app = (VocalendarApplication)getApplication();
        app.setEventDataBaseRowArray(eventDataBaseRowArray);

        TimeZone timeZone = TimeZone.getDefault();
        
        eventArrayCursorAdapter = new EventArrayCursorAdapter(
        		this, eventDataBaseRowArray.getAllRows(), timeZone, colorTheme);
        setListAdapter(eventArrayCursorAdapter);
        
		scrollToHead();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if(position == 0) {
			loadPreviousEventsTapped();
			return;
		}
		if(position == (eventArrayCursorAdapter.getCount() + 1)) {
			loadNextEventsTapped();
			return;
		}
		openEventDescriptionActivity(l, position);
	}

	private void openEventDescriptionActivity(ListView l, int position) {
		int cursorPosition = position - 1; //header分を引く
		EventDataBaseRow event =
				eventArrayCursorAdapter.getEventArrayCursor().getEventDataBaseRow(cursorPosition);
		Intent i = new Intent(this, SwipableEventDescriptionActivity.class);
		i.putExtra(SwipableEventDescriptionActivity.KEY_EVENT_INDEX, event.getEventIndex());
		startActivity(i);
	}
	
	/**
	 * イベント情報の取得が必要かどうか判定する。
	 * @return
	 */
	private boolean isUpdateRequired() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		long lastUpdated = pref.getLong(Constants.LAST_UPDATED_PREFERENCE_NAME, 0);
		Calendar cal = Calendar.getInstance();
		if((cal.getTimeInMillis() - lastUpdated) > (24 * 60 * 60 * 1000) ||
				(lastUpdated > cal.getTimeInMillis())) {  // 1日以上経過、または未来の日付で読み込んでいたら、更新
			return true;
		}
		int date = cal.get(Calendar.DATE);
		cal.setTimeInMillis(lastUpdated);
		if(date != cal.get(Calendar.DATE)) { // 日付が変わっていれば更新
			return true;
		}
		return false;		
	}
	
	/**
	 * 読み込み中の日付(loadingDate)でイベント情報を読み込む
	 */
	private void openEventLoadingActivity() {
		autoLoadingNextEvents = autoLoadingPreviousEvents = false;		
		Intent i = new Intent(this, EventLoadingActivity.class);
		i.putExtra(EventLoadingActivity.KEY_YEAR, currentDate.get(Calendar.YEAR));
		i.putExtra(EventLoadingActivity.KEY_MONTH, currentDate.get(Calendar.MONTH));
		i.putExtra(EventLoadingActivity.KEY_DATE, currentDate.get(Calendar.DATE));
		startActivityForResult(i, REQUEST_CODE_GET_DAYLY_EVENT);		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == REQUEST_CODE_GET_DAYLY_EVENT && resultCode == RESULT_OK) {
			int y = data.getExtras().getInt(EventLoadingActivity.KEY_YEAR);
			int m = data.getExtras().getInt(EventLoadingActivity.KEY_MONTH);
			int d = data.getExtras().getInt(EventLoadingActivity.KEY_DATE);
			currentDate = DateUtil.getStartTimeOfDay(y, m, d, TimeZone.getDefault());

		    SharedPreferences.Editor editor =
		            PreferenceManager.getDefaultSharedPreferences(this).edit();
		    editor.putLong(
		    		Constants.LAST_UPDATED_PREFERENCE_NAME,
		    		currentDate.getTimeInMillis()); //読み込み中の日付を最終更新日時とする(日付変更した後にActivityが終了した時、次回起動時に今日の日付で起動するように)
		    editor.commit();		    
		    
			updateList();
		} else if(requestCode == REQUEST_CODE_GET_DAYLY_EVENT
				&& resultCode == EventLoadingActivity.RESULT_AUTH_FAILED) {
			finish(); // アカウント追加でキャンセルされたので終了			
		} else if(requestCode == REQUEST_CODE_OPEN_SETTINGS && resultCode == RESULT_OK) { // 設定更新からの戻り
			colorTheme.updateColor(); // カラーテーマ再読み込み
			eventArrayCursorAdapter.notifyDataSetChanged(); // 表示中のリスト項目にカラーテーマ変更を反映するために、Viewを再生成
			applyColorThemeToListView();
			setDateToToday();
			openEventLoadingActivity(); // 設定が更新されたらイベント情報を再読み込み
		}
		// イベント読み込みのキャンセル時は何もしない
	}
	
	private DatePicker datePicker = null;
	private Dialog datePickerDialog = null;
	
	private void openDatePicker() {
		if(datePickerDialog == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			LayoutInflater inflater = getLayoutInflater();
			View v = inflater.inflate(R.layout.date_picker_dialog, null);
			datePicker = (DatePicker)v.findViewById(R.id.datePicker);
			Button todayButton = (Button)v.findViewById(R.id.setTodayButton);
			todayButton.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View v) {
					setDateToToday();
					updateDatePicker();
				}
			});			
			builder.setView(v)
				   .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {				
					   @Override
					   public void onClick(DialogInterface dialog, int which) {
						   changeDate(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
					   }
				   })
				   .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {				
					   @Override
					   public void onClick(DialogInterface dialog, int which) {
						   dialog.cancel();
					   }
				   });
			datePickerDialog = builder.create();
		}		
		updateDatePicker(); // 読み込み中の日付に変更(ダイアログを開く度に、読み込み中の年月日に指定する)
		datePickerDialog.show();
	}

	private void setDateToToday() {
		currentDate.setTimeInMillis(System.currentTimeMillis());
	}

	private void updateDatePicker() {
		if(datePicker != null) {
			datePicker.updateDate(
					currentDate.get(Calendar.YEAR),
					currentDate.get(Calendar.MONTH),
					currentDate.get(Calendar.DATE));
		}
	}
	
	/**
	 * 指定された日付のイベントを読み込む。
	 * @param year
	 * @param monthOfYear
	 * @param dayOfMonth
	 */
	private void changeDate(int year, int monthOfYear, int dayOfMonth) {
		autoLoadingPreviousEvents = autoLoadingNextEvents = false;
		Intent i = new Intent(this, EventLoadingActivity.class);
		i.putExtra(EventLoadingActivity.KEY_YEAR, year);
		i.putExtra(EventLoadingActivity.KEY_MONTH, monthOfYear);
		i.putExtra(EventLoadingActivity.KEY_DATE, dayOfMonth);
		startActivityForResult(i, REQUEST_CODE_GET_DAYLY_EVENT);		
	}
 	
	private void openHelp() {
		Help.openHelp(this);
	}

	private void openSearch() {
		Intent intent = new Intent(EventListActivity.this, SearchableEventActivity.class);
		intent.putExtra(SearchableEventActivity.KEY_CURRENT_DATE, topDate);
		startActivity(intent);		
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
	
	private void loadPreviousEventsTapped() {
		synchronized (loadMorePreviousEventView) {
			if(loadMorePreviousEventView.isLoading()) {				
				loadMorePreviousEventView.setLoading(false);
				autoLoadingPreviousEvents = false;
				loadMorePreviousEventTask.cancel(true);
			} else {
				loadMorePreviousEventView.setLoading(true);
				loadPreviousEvents();						
				// もっと読み込むをタップしてキャンセルした場合、もう一度タップしたら、もっと読み込む設定を復元する。
				autoLoadingPreviousEvents = VocalendarApplication.getLoadMoreEventWithoutTap(this);
			}
		}
	}
	
	private void loadPreviousEvents() {
		TimeZone timeZone = TimeZone.getDefault();
		Date topDate = eventDataBaseRowArray.getTopDate();
		
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
						this, new LoadEventTaskCallback(true), true);		
		loadMorePreviousEventTask.setStartAndEndDate(dates[0], dates[1], separators, timeZone);
		loadMorePreviousEventTask.execute(Constants.MAIN_CALENDAR_ID, Constants.BROADCAST_CALENDAR_ID);
	}
	
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
			Toast.makeText(EventListActivity.this,
					R.string.loading_events_failed, Toast.LENGTH_SHORT).show();
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
	
	/**
	 * 前方への追加イベント読み込み完了時に呼ばれるメソッド
	 * @param loadedEvents 追加で読み込まれたイベント
	 */
	private void previousEventsLoaded(List<EventDataBaseRow> loadedEvents) {
		VocalendarApplication app = (VocalendarApplication)getApplication();
		eventDataBaseRowArray = loadMorePreviousEventTask.getEventDataBaseRowArray();
    	app.setEventDataBaseRowArray(eventDataBaseRowArray);

    	eventArrayCursorAdapter = new EventArrayCursorAdapter(
    			this, app.getEventDataBaseRowArray().getAllRows(), TimeZone.getDefault(), colorTheme);
        setListAdapter(eventArrayCursorAdapter);
                
		loadMorePreviousEventView.setLoading(false);
		setSelection(loadedEvents.size());
		
		topDate = Calendar.getInstance();
		topDate.setTimeInMillis(loadMorePreviousEventTask.getStart().getValue());
		
		loadMorePreviousEventTask = null;
	}
	
	private void loadNextEventsTapped() {
		synchronized (loadMoreNextEventView) {
			if(loadMoreNextEventView.isLoading()) {				
				loadMoreNextEventView.setLoading(false);
				loadMoreNextEventTask.cancel(true);
				autoLoadingNextEvents = false;
			} else {
				loadMoreNextEventView.setLoading(true);
				loadNextEvents();
				// もっと読み込むをタップしてキャンセルした場合、もう一度タップしたら、もっと読み込む設定を復元する。
				autoLoadingNextEvents = VocalendarApplication.getLoadMoreEventWithoutTap(this);
			}
		}
	}

	private void loadNextEvents() {		
		TimeZone timeZone = TimeZone.getDefault();
		Date lastDate = eventDataBaseRowArray.getLastDate();		
		
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
						this, new LoadEventTaskCallback(false), false);		
		loadMoreNextEventTask.setStartAndEndDate(dates[0], dates[1], separators, timeZone);
		loadMoreNextEventTask.execute(Constants.MAIN_CALENDAR_ID, Constants.BROADCAST_CALENDAR_ID);		
	}
	
	private void nextEventsLoaded(List<EventDataBaseRow> events) {
		VocalendarApplication app = (VocalendarApplication)getApplication();
		eventDataBaseRowArray = loadMoreNextEventTask.getEventDataBaseRowArray();
    	app.setEventDataBaseRowArray(eventDataBaseRowArray);
    	
    	eventArrayCursorAdapter.getEventArrayCursor()
    		.updateEventDataBaseRows(eventDataBaseRowArray.getAllRows());

        loadMoreNextEventView.setLoading(false);
		
        //setSelection()を実行しても、スクロール位置が動く場合と動かない場合あり。
        //動く場合は、以下で指定された位置に移動するが、(一番最下端のイベントの位置) - (画面内のイベントの数) に
        //設定する必要がある。(画面内のイベントの数)を知る方法が未調査のため、コメントアウト。
        //今の所、スクロール位置を動かさなくても、スクロールの動きはおかしくないので、この仕様にする。
        //setSelection(eventArrayCursorAdapter.getCount() - events.size() + 1); // 読み込んだイベントの位置に移動
        
		loadMoreNextEventTask = null;
	}
	
	/**
	 * もっと読み込むイベント取得日数を返す。
	 * @return
	 */
	private int getNumberOfDateToLoadMoreEvents() {		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		String value = pref.getString(Constants.NUMBER_OF_DATE_TO_LOAD_MORE_EVENTS_PREFRENCE_NAME, "3");
		try {
			return Integer.parseInt(value);
		} catch(NumberFormatException e) {
			Log.e(TAG, "Invalid number of date to get events: " + value);
		}
		return 3;
	}	
	
	public EventArrayCursor getEventArrayCursor() {
		return eventArrayCursorAdapter.getEventArrayCursor();
	}

	@Override
	protected void onResume() {
		super.onResume();		
		// 起動時または設定画面から戻ってきた時を想定して、タップ無しでもっと読み込む設定を更新
		resetAutoLosdingSetting();
	}

	private void resetAutoLosdingSetting() {
		boolean autoLoading = VocalendarApplication.getLoadMoreEventWithoutTap(this);
		autoLoadingNextEvents = autoLoadingPreviousEvents = autoLoading;
	}
	
}