package jp.vocalendar.activity;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import jp.vocalendar.R;
import jp.vocalendar.VocalendarApplication;
import jp.vocalendar.model.EventArrayCursorAdapter;
import jp.vocalendar.model.EventDataBaseRow;
import jp.vocalendar.model.EventDataBaseRowArray;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

/**
 * イベント検索のActivity
 */
public class SearchableEventActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.searchable_event);
		
		setupUI();
		
	    Intent intent = getIntent();
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	    	String query = intent.getStringExtra(SearchManager.QUERY);
	    	searchEvent(query);
	    }
	}
	
	private void setupUI() {
		ImageButton search = (ImageButton)findViewById(R.id.searchButton);
		search.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				searchEvent();
			}
		});
		
		EditText searchText = (EditText)findViewById(R.id.searchText);
		searchText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,	KeyEvent event) {
				boolean handled = false;
				if(actionId == EditorInfo.IME_ACTION_SEARCH) {
					searchEvent();
					handled = true;
				}
				return handled;
			}			
		});
		
	}
	
	/**
	 * 検索フィールドに入力された文字列で検索
	 */
	private void searchEvent() {
		EditText searchText = (EditText)findViewById(R.id.searchText);
		searchEvent(searchText.getText().toString());		
	}
	
	private void searchEvent(String query) {
        VocalendarApplication app = (VocalendarApplication)getApplication();
        EventDataBaseRowArray eventDataBaseRowArray = app.getEventDataBaseRowArray();
        TimeZone timeZone = TimeZone.getDefault();
        
        EventDataBaseRow[] found = searchBySummary(query);
        
        EventArrayCursorAdapter eventArrayCursorAdapter = new EventArrayCursorAdapter(
        		this, found, timeZone);
        setListAdapter(eventArrayCursorAdapter);
	}

	private EventDataBaseRow[] searchBySummary(String query) {
        VocalendarApplication app = (VocalendarApplication)getApplication();
        EventDataBaseRowArray eventDataBaseRowArray = app.getEventDataBaseRowArray();
        EventDataBaseRow[] rows = eventDataBaseRowArray.getNormalRows();
        
        List<EventDataBaseRow> found = new LinkedList<EventDataBaseRow>();
        String q = query.toLowerCase(Locale.US);
        for (int i = 0; i < rows.length; i++) {
			if(rows[i].getEvent().getSummary().toLowerCase(Locale.US).contains(q)) {
				found.add(rows[i]);
			}
		}
        return found.toArray(new EventDataBaseRow[found.size()]);
	}
}
