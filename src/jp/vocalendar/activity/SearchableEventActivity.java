package jp.vocalendar.activity;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;

import jp.vocalendar.Constants;
import jp.vocalendar.R;
import jp.vocalendar.VocalendarApplication;
import jp.vocalendar.activity.view.LoadMoreEventView;
import jp.vocalendar.model.ColorTheme;
import jp.vocalendar.model.Event;
import jp.vocalendar.model.EventArrayCursorAdapter;
import jp.vocalendar.model.EventDataBaseRow;
import jp.vocalendar.model.EventDataBaseRowArray;
import jp.vocalendar.model.GoogleCalendarLoadEventTask;
import jp.vocalendar.model.LoadEventTask;
import jp.vocalendar.model.SearchGoogleCalendarEventTask;
import jp.vocalendar.model.SearchLocalEventTask;
import jp.vocalendar.util.DialogUtil;
import android.app.ListActivity;
import android.app.SearchManager;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

/**
 * イベント検索のActivity
 */
public class SearchableEventActivity extends ListActivity {
	private static final String TAG = "SearchableEventActivity";
	
	public static final String KEY_CURRENT_DATE = "current_date"; // Intentに検索の開始日を格納するためのキー
	
	/**
	 * 検索結果表示用のアダプタ
	 */
	private EventArrayCursorAdapter eventArrayCursorAdapter;
	
	/**
	 * イベント検索の起点となる開始日
	 */
	private java.util.Calendar currentDate;
	
	/** 検索中にリストの末尾項目として表示するView */
	private LoadMoreEventView searchingView;
	
	/** 検索実行中のタスク */
	private LoadEventTask searchingTask;
	
	/** GoogleCalendarでイベント検索した結果を格納 */
	private List<EventDataBaseRow> googleCalendarEvents = null;
	
	/** カラーテーマ */
	private ColorTheme colorTheme;
	
	/**
	 * イベント検索処理のコールバック
	 */
	private class SearchGoogleCalendarEventTaskCallback implements LoadEventTask.TaskCallback {		
		
		@Override
		public void retry(int retryNumber) {
			// とりあえず失敗にする。
			failed();						
		}

		private void failed() {
			Log.d(TAG, "GoogleCalendarLoadEventTask failed...");
			Toast.makeText(SearchableEventActivity.this,
					R.string.loading_events_failed, Toast.LENGTH_SHORT).show();
			searchingView.setLoading(false);
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
			searchGoogleCalendarEventFinished(events);
		}
	}
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		colorTheme = new ColorTheme(this);
		setContentView(R.layout.searchable_event);
		
		setupUI();
		
	    Intent intent = getIntent();
	    if(intent.hasExtra(KEY_CURRENT_DATE)) {
	    	currentDate = (java.util.Calendar)intent.getSerializableExtra(KEY_CURRENT_DATE);
	    } else {
	    	currentDate = java.util.Calendar.getInstance();
	    }
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	    	String query = intent.getStringExtra(SearchManager.QUERY);
	    	searchLocalEvent(query);
	    }
	}
	
	private void setupUI() {
		ImageButton search = (ImageButton)findViewById(R.id.searchButton);
		search.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				searchLocalEvents();
			}
		});
		
		EditText searchText = (EditText)findViewById(R.id.searchText);
		searchText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,	KeyEvent event) {
				boolean handled = false;
				if(actionId == EditorInfo.IME_ACTION_SEARCH) {
					searchLocalEvents();
					handled = true;
				}
				return handled;
			}			
		});
		
	}
	
	/**
	 * 検索フィールドに入力された文字列で検索
	 */
	private void searchLocalEvents() {
		EditText searchText = (EditText)findViewById(R.id.searchText);
		searchLocalEvent(searchText.getText().toString());		
        setupSearchingView();
        setListAdapter(null);
	}
	
	private void searchLocalEvent(String query) {
        hideSoftwareKeyboard();
		googleCalendarEvents = null;
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
						searchLocalEventFinished(events);
					}
				});
        searchingTask.execute(query);
	}
	
	private void hideSoftwareKeyboard() {
		EditText searchText = (EditText)findViewById(R.id.searchText);
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(searchText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}
	
	private void searchGoogleCalendarEventFinished(List<EventDataBaseRow> events) {
		googleCalendarEvents = events;
		searchLocalEventFinished(events);
	}
	
	private void searchLocalEventFinished(List<EventDataBaseRow> events) {
		searchingView.setLoading(false);
        TimeZone timeZone = TimeZone.getDefault();
        EventDataBaseRow[] eventRows = events.toArray(new EventDataBaseRow[events.size()]);        
        eventArrayCursorAdapter = new EventArrayCursorAdapter(this, eventRows, timeZone, colorTheme);
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
				if(googleCalendarEvents != null) {
					// Googleカレンダーのもっと検索2回目
					DialogUtil.openNotImplementedDialog(this);
					return;
				}
				searchGoogleCalendarEvents();
				return;
			}
		}
		
		EventDataBaseRow event =
				eventArrayCursorAdapter.getEventArrayCursor().getEventDataBaseRow(position);
		Intent i = new Intent(this, OneEventDescriptionActivity.class);
		i.putExtra(OneEventDescriptionActivity.KEY_EVENT_INSTANCE, event);
		startActivity(i);
	}

	/**
	 * 検索中の表示を行う。
	 */
	private void setupSearchingView() {
		if(searchingView == null) {
			searchingView =
					(LoadMoreEventView)getLayoutInflater().inflate(R.layout.seaching_progress, null);
			searchingView.setBackgroundResource(colorTheme.getLightBackgroundStateList());
			getListView().addFooterView(searchingView);
		}
		searchingView.setLoading(true);
	}
	
	private void cancelSearchTask() {
		if(searchingTask != null) {
			searchingTask.cancel(true);
		}
	}
	
	private void searchGoogleCalendarEvents() {
		Log.d(TAG, "searchGoogleCalendarEvents");
		EditText searchText = (EditText)findViewById(R.id.searchText);
		String query = searchText.getText().toString();
		SearchGoogleCalendarEventTask task = new SearchGoogleCalendarEventTask(
				this, new SearchGoogleCalendarEventTaskCallback());
		TimeZone tz = TimeZone.getDefault();
		DateTime startDateTime = new DateTime(currentDate.getTime(), tz);
		int maxEvents = VocalendarApplication.getNumberOfEventToSearchMore(this);
		task.setCalendarIdAndStartDateTime(
				Constants.CALENDER_IDS, startDateTime, tz, maxEvents);
		task.execute(query);
		searchingView.setLoading(true);
	}
}
