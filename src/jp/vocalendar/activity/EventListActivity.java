package jp.vocalendar.activity;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import jp.vocalendar.Constants;
import jp.vocalendar.Help;
import jp.vocalendar.R;
import jp.vocalendar.VocalendarApplication;
import jp.vocalendar.model.ColorTheme;
import jp.vocalendar.model.EventArrayCursor;
import jp.vocalendar.model.EventArrayCursorAdapter;
import jp.vocalendar.model.EventDataBase;
import jp.vocalendar.model.EventDataBaseRow;
import jp.vocalendar.model.EventDataBaseRowArray;
import jp.vocalendar.model.LoadMoreEventController;
import jp.vocalendar.util.DateUtil;
import jp.vocalendar.util.DialogUtil;
import jp.vocalendar.util.UncaughtExceptionSavingHandler;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.OpenableColumns;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class EventListActivity extends ActionBarActivity {
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
	
	/**
	 * イベントを読み込んだ先頭日付。
	 * イベントを読み込んだ後に更新される。もっと読み込む時にも更新する。
	 */
	private Calendar topDate = Calendar.getInstance();
	
	/** イベント一覧の配列 */
	private EventDataBaseRowArray eventDataBaseRowArray;
	
	/** イベント一覧表示用のアダプタ */
	private EventArrayCursorAdapter eventArrayCursorAdapter;
	
	private LoadMoreEventController loadMoreEventController;
	
	private ColorTheme colorTheme;
	
	private ListView listView;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UncaughtExceptionSavingHandler.init(this);
        
        setupActionBar();
        
        colorTheme = initColorTheme();
        setContentView(R.layout.event_list);
        
        loadMoreEventController = new LoadMoreEventController(this);
        
        setupButtons();
        setupListView();
        loadMoreEventController.setupListView(getListView(), colorTheme);
        
        setDateToToday();
        if(isUpdateRequired()) {
        	openEventLoadingActivity();
        } else {
        	updateList();
        }
    }

	protected ColorTheme initColorTheme() {
		return new ColorTheme(this);
	}

	private void setupActionBar() {
		ActionBar ab = getSupportActionBar();
        ab.setCustomView(R.layout.vocalendar_title_image_view);
        ab.setDisplayShowCustomEnabled(true);
        ab.setHomeButtonEnabled(true);
        ab.setDisplayShowTitleEnabled(false);
        ab.show();
	}

	private void setupButtons() {
		TextView today = (TextView)findViewById(R.id.today_text_view_action);
		today.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				goToToday();
			}
		});
		
		ImageView changeDate = (ImageView)findViewById(R.id.change_date_image_view_action);
        changeDate.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				openDatePicker();
			}
		});

        ImageView favorite = (ImageView)findViewById(R.id.go_to_favorite_list_image_view_action);
        favorite.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				openFavoriteList();
			}
		});
        
        ImageView update = (ImageView)findViewById(R.id.update_image_view_action);
        update.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				openEventLoadingActivity();
			}
		});        
	}
	
	private void setupListView() {
		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ListView l = (ListView)parent;
				onListItemClick(l, view, position, id);
			}
		});
	}
	
	/** 最上端(読み込み項目除く)位置にスクロールバーを移動する */
	private void scrollToHead() {
		setSelection(1);		
	}
	
	public void setSelection(int position) {
		getListView().setSelection(position);
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
	
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if(position == 0) {
			loadMoreEventController.loadPreviousEventsTapped();
			return;
		}
		if(position == (eventArrayCursorAdapter.getCount() + 1)) {
			loadMoreEventController.loadNextEventsTapped();
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
		beforeOpenEventDescriptionActivity(i);
		startActivity(i);
	}
	
	/**
	 * イベント詳細画面を開く前に呼ばれるメソッド。
	 * サブクラスで前処理を追加するときに使う。
	 * @param i
	 */
	protected void beforeOpenEventDescriptionActivity(Intent i) {
		return; // do nothing.
	}
	
	/**
	 * イベント情報の取得が必要かどうか判定する。
	 * @return
	 */
	protected boolean isUpdateRequired() {
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
		loadMoreEventController.setAutoLoading(false);		
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
			topDate = currentDate;

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
			loadMoreEventController.applyColorThemeToListView(getListView(), colorTheme);
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
		loadMoreEventController.setAutoLoading(false);
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
		beforeOpenSeatch(intent);
		startActivity(intent);		
	}
	
	/**
	 * 検索画面を開く前に呼ばれるメソッド。
	 * サブクラスで前処理を追加する時に、このメソッドを上書きする。
	 * @param i
	 */
	protected void beforeOpenSeatch(Intent i){
		return; //何もしない
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
		loadMoreEventController.setAutoLoading(autoLoading);
	}

	public EventArrayCursorAdapter getEventArrayCursorAdapter() {
		return eventArrayCursorAdapter;
	}

	public EventDataBaseRowArray getEventDataBaseRowArray() {
		return eventDataBaseRowArray;
	}

	/**
	 * EventDataBaseRowArrayを設定し直す。イベントを前に追加用。
	 * @param eventDataBaseRowArray
	 */
	public void setEventDataBaseRowArray(EventDataBaseRowArray eventDataBaseRowArray) {
		this.eventDataBaseRowArray = eventDataBaseRowArray;
    	eventArrayCursorAdapter = new EventArrayCursorAdapter(
    			this, eventDataBaseRowArray.getAllRows(), TimeZone.getDefault(), colorTheme);    	
        setListAdapter(eventArrayCursorAdapter);
	}
	
	/**
	 * EventDataBaseRowを更新する。イベントを後に追加用。
	 * @param eventDataBaseRowArray
	 */
	public void updateEventDataBaseRowArray(EventDataBaseRowArray eventDataBaseRowArray) {
		this.eventDataBaseRowArray = eventDataBaseRowArray;
    	eventArrayCursorAdapter.getEventArrayCursor().updateEventDataBaseRows(eventDataBaseRowArray.getAllRows());
		
	}
	
	public ListView getListView() {
		if(listView == null) {
			listView = (ListView)findViewById(R.id.eventList);
		}
		return listView;
	}
	
	public void setListAdapter(ListAdapter adapter) {
		getListView().setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.event_list_action_menu, menu);
	    return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_search:
			openSearch();
			return true;
		case R.id.action_help:
			openHelp();
			return true;
		case R.id.action_about:
			openAbout();
			return true;
		case R.id.action_setting:
			openSetting();
			return true;
		case R.id.action_web_site:
			openWebSite();
			return true;
		default:
			return super.onOptionsItemSelected(item);			
		}
	}

	public Calendar getTopDate() {
		return topDate;
	}

	public void setTopDate(Calendar topDate) {
		this.topDate = topDate;
	}
	
	private void goToToday() {
		Date today = new Date();
		TimeZone timeZone = TimeZone.getDefault();
		if(eventDataBaseRowArray.getTopDate().compareTo(today) <= 0 &&
				today.compareTo(eventDataBaseRowArray.getLastDate()) <= 0) {
			// 読み込んでいる日付の範囲に今日が含まれる場合
			scrollToDate(today, timeZone);
			return;
		}
		if(DateUtil.equalYMD(today, eventDataBaseRowArray.getTopDate(), timeZone) ||
				DateUtil.equalYMD(today, eventDataBaseRowArray.getLastDate(), timeZone)) {
			// 読み込んでいる日付の先頭または末尾と一致する場合
			scrollToDate(today, timeZone);
			return;
		}		
		// 読み込んでいる日付の範囲に今日が含まれない場合
		currentDate = Calendar.getInstance(); // 今日の日付で再読み込み
		openEventLoadingActivity();
	}
	
	private void scrollToDate(Date date, TimeZone timeZone) {
		int pos = findPositionByDate(date, timeZone);
		setSelection(pos);
		return;		
	}
	
	private int findPositionByDate(Date date, TimeZone tz) {
		Calendar calDate = Calendar.getInstance(tz);
		calDate.setTime(date);
		Calendar calTarget = Calendar.getInstance(tz);
		
		EventDataBaseRow[] rows = eventArrayCursorAdapter.getEventArrayCursor().getEventDataBaseRows();
		for(int pos = 0; pos < rows.length; pos++) {
			if(rows[pos].getRowType() == EventDataBaseRow.TYPE_SEPARATOR) {
				calTarget.setTime(rows[pos].getDisplayDate());
				if(DateUtil.equalYMD(calDate, calTarget)) {
					return pos + 1; //もっと読み込むの項目分を足す
				}
			}
		}
		return 0; // ここに来る事はありえないが、 0 を返す。		
	}
	
	private void openFavoriteList() {
		DialogUtil.openNotImplementedDialog(this); // TODO 実装
	}
	
	private void openSetting() {
		Intent intent = new Intent(this, SettingActivity.class);
		startActivityForResult(intent, REQUEST_CODE_OPEN_SETTINGS);		
	}
	
	private void openAbout() {
		Intent intent = new Intent(this, AboutActivity.class);
		startActivity(intent);
	}
	
	private void openWebSite() {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://vocalendar.jp/"));
		startActivity(intent);
	}
}