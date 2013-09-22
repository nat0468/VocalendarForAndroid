package jp.vocalendar.activity;

import java.util.Calendar;
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
import jp.vocalendar.util.UncaughtExceptionSavingHandler;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListView;

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
	
	/** イベント一覧の配列 */
	private EventDataBaseRowArray eventDataBaseRowArray;
	
	/** イベント一覧表示用のアダプタ */
	private EventArrayCursorAdapter eventArrayCursorAdapter;
	
	private LoadMoreEventController loadMoreEventController;
	
	private ColorTheme colorTheme;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UncaughtExceptionSavingHandler.init(this);
        
        colorTheme = new ColorTheme(this);
        setContentView(R.layout.event_list);
        
        loadMoreEventController = new LoadMoreEventController(this);
        
        setupButtons();
        loadMoreEventController.setupListView(getListView(), colorTheme);
        
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
	
	/** 最上端(読み込み項目除く)位置にスクロールバーを移動する */
	private void scrollToHead() {
		setSelection(1);
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
		intent.putExtra(SearchableEventActivity.KEY_CURRENT_DATE, loadMoreEventController.getTopDate());
		startActivity(intent);		
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
}