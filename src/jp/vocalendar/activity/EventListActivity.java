package jp.vocalendar.activity;

import jp.vocalendar.R;
import jp.vocalendar.model.Event;
import jp.vocalendar.model.EventArrayCursor;
import jp.vocalendar.model.EventDataBase;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class EventListActivity extends ListActivity {
	
	public static final String EXTRA_EVENT_LIST = 
			EventListActivity.class.getPackage().getName() + ".EventList";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_list);
        setTitle(R.string.vocalendar);        

		EventDataBase db = new EventDataBase(this);
		db.open();		
        Event[] events = db.getAllEvents();
        db.close();
                
        setListAdapter(new SimpleCursorAdapter(
        		this, 
        		R.layout.event_list_item, 
        		new EventArrayCursor(events),
        		new String[] { "start", "summary" },
        		new int[]{ R.id.dateText, R.id.sumamryText }));        	
        
        Button settingButton = (Button)findViewById(R.id.setting_button);
        settingButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(EventListActivity.this, SettingActivity.class);
				startActivity(intent);
				
			}
		});
    }
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Event event = ((EventArrayCursor)l.getAdapter().getItem(position)).getEvent(position);
		Intent i = new Intent(this, EventDescriptionActivity.class);
		i.putExtra(EventDescriptionActivity.KEY_EVENT, event);
		startActivity(i);
	}
}