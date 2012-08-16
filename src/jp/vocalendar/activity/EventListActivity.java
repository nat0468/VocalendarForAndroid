package jp.vocalendar.activity;

import java.util.Calendar;

import jp.vocalendar.Constants;
import jp.vocalendar.R;
import jp.vocalendar.model.Event;
import jp.vocalendar.model.EventArrayCursor;
import jp.vocalendar.model.EventArrayCursorAdapter;
import jp.vocalendar.model.EventDataBase;
import jp.vocalendar.util.DialogUtil;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class EventListActivity extends ListActivity {
	
	public static final String EXTRA_EVENT_LIST = 
			EventListActivity.class.getPackage().getName() + ".EventList";
	
	// EventListLoadingActivityを呼ぶためのリクエストコード
	private static int REQUEST_CODE_GET_DAYLY_EVENT = 1;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_list);
        setTitle(R.string.vocalendar);        
        
        Event.initString(this);
        
        Button settingButton = (Button)findViewById(R.id.setting_button);
        settingButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(EventListActivity.this, SettingActivity.class);
				startActivity(intent);
				
			}
		});
        
        Button changeDateButton = (Button)findViewById(R.id.change_date_button);
        changeDateButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				DialogUtil.openNotImplementedDialog(EventListActivity.this);
			}
		});
        
        Button updateButton = (Button)findViewById(R.id.update_button);
        updateButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				openEventLoadingActivity();
			}
		});
        
        if(isUpdateRequired()) {
        	openEventLoadingActivity();
        } else {
        	updateList();
        }
    }

	private void updateList() {
		EventDataBase db = new EventDataBase(this);
		db.open();		
        Event[] events = db.getAllEvents();
        db.close();
                
        setListAdapter(new EventArrayCursorAdapter(
        		this, 
        		R.layout.event_list_item, 
        		new EventArrayCursor(events),
        		new String[] { "start", "summary" },
        		new int[]{ R.id.dateText, R.id.sumamryText }));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Event event = ((EventArrayCursor)l.getAdapter().getItem(position)).getEvent(position);
		Intent i = new Intent(this, EventDescriptionActivity.class);
		i.putExtra(EventDescriptionActivity.KEY_EVENT, event);
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
		if((cal.getTimeInMillis() - lastUpdated) > (24 * 60 * 60 * 1000)) { // 1日以上経過で更新
			return true;
		}
		int date = cal.get(Calendar.DATE);
		cal.setTimeInMillis(lastUpdated);
		if(date != cal.get(Calendar.DATE)) { // 日付が変わっていれば更新
			return true;
		}
		return false;		
	}
	
	private void openEventLoadingActivity() {
		Intent i = new Intent(this, EventLoadingActivity.class);
		startActivityForResult(i, REQUEST_CODE_GET_DAYLY_EVENT);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == REQUEST_CODE_GET_DAYLY_EVENT && resultCode == RESULT_OK) {
		    SharedPreferences.Editor editor =
		            PreferenceManager.getDefaultSharedPreferences(this).edit();
		    editor.putLong(
		    		Constants.LAST_UPDATED_PREFERENCE_NAME,
		    		System.currentTimeMillis());
		    editor.commit();
			updateList();
		}
	}
	
	
 
}