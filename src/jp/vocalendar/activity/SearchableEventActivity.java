package jp.vocalendar.activity;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import jp.vocalendar.R;
import jp.vocalendar.VocalendarApplication;
import jp.vocalendar.activity.view.LoadMoreEventView;
import jp.vocalendar.model.Event;
import jp.vocalendar.model.EventArrayCursorAdapter;
import jp.vocalendar.model.EventDataBaseRow;
import jp.vocalendar.model.EventDataBaseRowArray;
import jp.vocalendar.model.LoadEventTask;
import jp.vocalendar.model.SearchLocalEventTask;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

/**
 * イベント検索のActivity
 */
public class SearchableEventActivity extends ListActivity {
	private EventArrayCursorAdapter eventArrayCursorAdapter;
	
	/** 検索中にリストの末尾項目として表示するView */
	private LoadMoreEventView searchingView;
	
	/** 検索実行中のタスク */
	private LoadEventTask searchingTask;
	
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
        setupSearchingView();
        setListAdapter(null);
	}
	
	private void searchEvent(String query) {
        searchingTask = new SearchLocalEventTask(
        		this,
        		new LoadEventTask.TaskCallback() {					
					@Override
					public void retry(int retryNumber) {
						// なにもしない
					}
					
					@Override
					public void onProgressUpdate(Event event) {
						// なにもしない
					}
					
					@Override
					public void onPostExecute(List<EventDataBaseRow> events) {
						searchEventFinished(events);
					}
				});
        searchingTask.execute(query);        
	}
	
	private void searchEventFinished(List<EventDataBaseRow> events) {
		searchingView.setLoading(false);
        TimeZone timeZone = TimeZone.getDefault();
        EventDataBaseRow[] eventRows = events.toArray(new EventDataBaseRow[events.size()]);        
        eventArrayCursorAdapter = new EventArrayCursorAdapter(this, eventRows, timeZone);
        setListAdapter(eventArrayCursorAdapter);
	}
	
	private EventDataBaseRow[] searchBySummaryWithoutSeparator(String query) {
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
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if(v == searchingView) {
			if(searchingView.isLoading()) {
				cancelSearchTask();
				searchingView.setLoading(false);
				return;
			} else {
				// TODO もっと検索する
				return;
			}
		}
		
		EventDataBaseRow event =
				eventArrayCursorAdapter.getEventArrayCursor().getEventDataBaseRow(position);
		Intent i = new Intent(this, OneEventDescriptionActivity.class);
		i.putExtra(SwipableEventDescriptionActivity.KEY_EVENT_INDEX, event.getEventIndex());
		startActivity(i);
	}

	/**
	 * 検索中の表示を行う。
	 */
	private void setupSearchingView() {
		if(searchingView == null) {
			searchingView =
					(LoadMoreEventView)getLayoutInflater().inflate(R.layout.seaching_progress, null);
			getListView().addFooterView(searchingView);
		}
		searchingView.setLoading(true);
	}
	
	private void cancelSearchTask() {
		if(searchingTask != null) {
			searchingTask.cancel(true);
		}
	}
}
