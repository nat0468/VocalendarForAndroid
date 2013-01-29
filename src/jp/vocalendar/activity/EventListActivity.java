package jp.vocalendar.activity;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import jp.vocalendar.Constants;
import jp.vocalendar.R;
import jp.vocalendar.model.EventArrayCursor;
import jp.vocalendar.model.EventArrayCursorAdapter;
import jp.vocalendar.model.EventDataBase;
import jp.vocalendar.model.EventDataBaseRow;
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
import android.widget.ListView;

public class EventListActivity extends ListActivity {
	
	public static final String EXTRA_EVENT_LIST = 
			EventListActivity.class.getPackage().getName() + ".EventList";
	
	// EventListLoadingActivityを呼ぶためのリクエストコード
	private static int REQUEST_CODE_GET_DAYLY_EVENT = 1;
	// SettingActivityを呼ぶためのリクエストコード
	private static int REQUEST_CODE_OPEN_SETTINGS = 2;

	/** 読み込み中の日付 */
	private Date loadingDate = new Date();
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_list);
        
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
        
    	setDateToToday();
        if(isUpdateRequired()) {
        	openEventLoadingActivity();
        } else {
        	updateList();
        }
    }

	private void updateList() {
		EventDataBase db = new EventDataBase(this);
		db.open();		
        EventDataBaseRow[] events = db.getAllEvents();
        db.close();
                
        TimeZone timeZone = TimeZone.getDefault();
        setListAdapter(new EventArrayCursorAdapter(
        		this, 
        		R.layout.event_list_item, 
        		new EventArrayCursor(events, timeZone, this),
        		new String[] { "time", "date", "summary" },
        		new int[]{ R.id.timeText, R.id.dateText, R.id.summaryText },
        		timeZone));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		openEventDescriptionActivity(l, position);
	}

	private void openEventDescriptionActivity(ListView l, int position) {
		EventDataBaseRow event = ((EventArrayCursor)l.getAdapter().getItem(position)).getEventDataBaseRow(position);
		//Intent i = new Intent(this, EventDescriptionActivity.class);
		Intent i = new Intent(this, SwipableEventDescriptionActivity.class);
		i.putExtra(SwipableEventDescriptionActivity.KEY_EVENT_DATA_BASE_ROW, event);
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
		Intent i = new Intent(this, EventLoadingActivity.class);
		i.putExtra(EventLoadingActivity.KEY_YEAR, loadingDate.getYear());
		i.putExtra(EventLoadingActivity.KEY_MONTH, loadingDate.getMonth());
		i.putExtra(EventLoadingActivity.KEY_DATE, loadingDate.getDate());
		startActivityForResult(i, REQUEST_CODE_GET_DAYLY_EVENT);		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == REQUEST_CODE_GET_DAYLY_EVENT && resultCode == RESULT_OK) {
		    loadingDate.setYear(data.getExtras().getInt(EventLoadingActivity.KEY_YEAR));
		    loadingDate.setMonth(data.getExtras().getInt(EventLoadingActivity.KEY_MONTH));
		    loadingDate.setDate(data.getExtras().getInt(EventLoadingActivity.KEY_DATE));

		    Calendar cal = Calendar.getInstance();
		    cal.set(Calendar.YEAR, loadingDate.getYear());
		    cal.set(Calendar.MONTH, loadingDate.getMonth());
		    cal.set(Calendar.DAY_OF_MONTH, loadingDate.getDate());
		    SharedPreferences.Editor editor =
		            PreferenceManager.getDefaultSharedPreferences(this).edit();
		    editor.putLong(
		    		Constants.LAST_UPDATED_PREFERENCE_NAME,
		    		cal.getTimeInMillis()); //読み込み中の日付を最終更新日時とする(日付変更した後にActivityが終了した時、次回起動時に今日の日付で起動するように)
		    editor.commit();		    
		    
			updateList();
		} else if(requestCode == REQUEST_CODE_OPEN_SETTINGS && resultCode == RESULT_OK) {
			setDateToToday();
			openEventLoadingActivity(); // 設定が更新されたらイベント情報を再読み込み
		}
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
		updateDatePicker();
		datePickerDialog.show();
	}

	private void setDateToToday() {
		Calendar cal = Calendar.getInstance();
		loadingDate.setYear(cal.get(Calendar.YEAR));
		loadingDate.setMonth(cal.get(Calendar.MONTH));
		loadingDate.setDate(cal.get(Calendar.DAY_OF_MONTH));		
	}

	private void updateDatePicker() {
		if(datePicker != null) {
			datePicker.updateDate(
					loadingDate.getYear(),
					loadingDate.getMonth(),
					loadingDate.getDate());
		}
	}
	
	/**
	 * 指定された日付のイベントを読み込む。
	 * @param year
	 * @param monthOfYear
	 * @param dayOfMonth
	 */
	private void changeDate(int year, int monthOfYear, int dayOfMonth) {
		Intent i = new Intent(this, EventLoadingActivity.class);
		i.putExtra(EventLoadingActivity.KEY_YEAR, year);
		i.putExtra(EventLoadingActivity.KEY_MONTH, monthOfYear);
		i.putExtra(EventLoadingActivity.KEY_DATE, dayOfMonth);
		startActivityForResult(i, REQUEST_CODE_GET_DAYLY_EVENT);		
	}
 	
	
 
}