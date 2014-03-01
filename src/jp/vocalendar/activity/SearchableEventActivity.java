package jp.vocalendar.activity;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.google.api.client.util.DateTime;

import jp.vocalendar.Constants;
import jp.vocalendar.R;
import jp.vocalendar.VocalendarApplication;
import jp.vocalendar.activity.view.LoadMoreEventView;
import jp.vocalendar.model.ColorTheme;
import jp.vocalendar.model.Event;
import jp.vocalendar.model.EventArrayCursorAdapter;
import jp.vocalendar.model.EventDataBase;
import jp.vocalendar.model.EventDataBaseRow;
import jp.vocalendar.model.EventDataBaseRowArray;
import jp.vocalendar.model.FavoriteEventManager;
import jp.vocalendar.task.LoadEventTask;
import jp.vocalendar.task.SearchGoogleCalendarEventTask;
import jp.vocalendar.task.SearchLocalEventTask;
import jp.vocalendar.task.SearchMoreGoogleCalendarEventTask;
import jp.vocalendar.util.DialogUtil;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

/**
 * イベント検索のActivity
 */
public class SearchableEventActivity extends AbstractEventListActivity
implements EventArrayCursorAdapter.FavoriteToggler {
	private static final String TAG = "SearchableEventActivity";
	
	public static final String KEY_CURRENT_DATE = "current_date"; // Intentに検索の開始日を格納するためのキー
	
	/**
	 * 検索結果表示用のリスト
	 */
	private ListView listView;
		
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

		protected void failed() {
			Log.d(TAG, "GoogleCalendarLoadEventTask failed...");
			String msg = getResources().getString(R.string.fail_to_connect_server);					
			DialogUtil.openMessageDialog(SearchableEventActivity.this, msg, false);
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
	
	private class SearchMoreGoogleCalendarEventTaskCallback
	extends SearchGoogleCalendarEventTaskCallback
	implements LoadEventTask.TaskCallback {				
		@Override
		public void onPostExecute(List<EventDataBaseRow> events) {
			Log.d(TAG, "onPostExecute()");
			if(events == null) {
				failed();
				return; // 読み込み失敗なので何もしない。
			}
			searchMoreGoogleCalendarEventFinished(events);
		}
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(getIntent().hasExtra(PreviewEventListActivity.EXTRA_PREVIEW_COLOR_THEME_CODE)) {
			int code = getIntent().getIntExtra(
					PreviewEventListActivity.EXTRA_PREVIEW_COLOR_THEME_CODE,
					ColorTheme.THEME_DEFAULT);
			colorTheme = new ColorTheme(this, code);
		} else {
			colorTheme = new ColorTheme(this);
		}
		setContentView(R.layout.searchable_event);
		
		setupUI();
		setupListView();
        VocalendarApplication app = (VocalendarApplication)getApplication();
        favoriteEventManager = app.getFavoriteEventManager();

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
		setupActionBar();        
		
		EditText searchText = (EditText)findViewById(R.id.searchEditText);
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
		
		View dummy = findViewById(R.id.dummyView);
		registerForContextMenu(dummy);
	}

	private void setupActionBar() {
		ActionBar ab = getSupportActionBar();
        ab.setCustomView(R.layout.search_edit_text);
        ab.setDisplayShowCustomEnabled(true);
        ab.setDisplayShowTitleEnabled(false);
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.seachable_event_action_menu, menu);
	    
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_search:			
			searchLocalEvents();
			return true;
		case R.id.action_input_tag:
			openInputTagMenu();
			return true;
		default:
			return super.onOptionsItemSelected(item);			
		}
	}
	
	private void openInputTagMenu() {
		View dummy = findViewById(R.id.dummyView);		
		openContextMenu(dummy);		
	}
	
	/**
	 * 検索フィールドに入力された文字列で検索
	 */
	private void searchLocalEvents() {
		EditText searchText = (EditText)findViewById(R.id.searchEditText);
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
				}, currentDate.getTime());
        searchingTask.execute(query);
	}
	
	private void hideSoftwareKeyboard() {
		EditText searchText = (EditText)findViewById(R.id.searchEditText);
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(searchText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}
	
	private void searchGoogleCalendarEventFinished(List<EventDataBaseRow> events) {
		googleCalendarEvents = events;
		searchLocalEventFinished(events);
	}
	
	private void searchMoreGoogleCalendarEventFinished(List<EventDataBaseRow> events) {
		googleCalendarEvents = events;
		searchingView.setLoading(false);
        EventDataBaseRow[] eventRows = events.toArray(new EventDataBaseRow[events.size()]);        
        eventArrayCursorAdapter.getEventArrayCursor().updateEventDataBaseRows(eventRows);
	}
	
	private void searchLocalEventFinished(List<EventDataBaseRow> events) {
		searchingView.setLoading(false);
        TimeZone timeZone = TimeZone.getDefault();
        EventDataBaseRow[] eventRows = events.toArray(new EventDataBaseRow[events.size()]);        
        EventDataBase db = new EventDataBase(this);
        db.open();
        favoriteEventManager.loadFavoriteEventFor(eventRows, db);
        db.close();
        eventArrayCursorAdapter = new EventArrayCursorAdapter(
        		this, eventRows, timeZone, colorTheme, favoriteEventManager, this);
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
	
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if(v == searchingView) {
			if(searchingView.isLoading()) {
				cancelSearchTask();
				searchingView.setLoading(false);
				return;
			} else {
				if(googleCalendarEvents != null) {
					searchMoreGoogleCalendarEvents();
					return;
				}
				searchGoogleCalendarEvents();
				return;
			}
		}
		
		if(eventArrayCursorAdapter == null) {
			return; //まだ検索する前の場合は無視
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
			searchingView.setBackgroundDrawable(colorTheme.makeLightBackgroundStateListDrawable());
			getListView().addFooterView(searchingView);
			getListView().setDivider(new ColorDrawable(colorTheme.getDividerColor()));
			getListView().setDividerHeight((int)(getResources().getDisplayMetrics().density * 1.0));
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
		EditText searchText = (EditText)findViewById(R.id.searchEditText);
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

	private void searchMoreGoogleCalendarEvents() {
		Log.d(TAG, "searchMoreGoogleCalendarEvents");
		EditText searchText = (EditText)findViewById(R.id.searchEditText);
		String query = searchText.getText().toString();
		SearchMoreGoogleCalendarEventTask task = new SearchMoreGoogleCalendarEventTask(
				this, new SearchMoreGoogleCalendarEventTaskCallback(), googleCalendarEvents);
		TimeZone tz = TimeZone.getDefault();
		int maxEvents = VocalendarApplication.getNumberOfEventToSearchMore(this);
		task.setCalendarId(Constants.CALENDER_IDS, tz, maxEvents);
		task.execute(query);
		searchingView.setLoading(true);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		EditText searchText = (EditText)findViewById(R.id.searchEditText);
		String keyword = makeKeywordToSearch(item);
		searchText.setText(keyword);
		searchText.setSelection(keyword.length());
		searchLocalEvents();
		return true;
	}
	
	private String makeKeywordToSearch(MenuItem item) {
		String keyword = item.getTitle().toString();
		if(keyword.startsWith("【") && keyword.endsWith("】")) { // タグを外す
			keyword = keyword.substring(keyword.indexOf("【") + 1);
			keyword = keyword.substring(0, keyword.lastIndexOf("】"));
		}
		return keyword;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);		
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.tag_menu, menu);		
	}

	private void setupListView() {
		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ListView l = (ListView)parent;
				onListItemClick(l, view, position, id);
			}
		});
		String msg = getResources().getString(R.string.seach_screen_instruction);
		setListAdapter(
				new ArrayAdapter<String>(this, R.layout.message_list_item, R.id.messageText, new String[]{msg}));
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
	public boolean onSupportNavigateUp() {
		finish();
		return true;
	}	
	
	/**
	 * 指定された行のイベントのお気に入りを切り替える。
	 * まだお気に入りでなければ追加。お気に入りならば削除する。
	 * @param row お気に入りを切り替える行。このメソッドから戻ったら、お気に入りの状態も更新されている。
	 */
	@Override
	public void toggleFavorite(EventDataBaseRow row) {
		favoriteEventManager.toggleFavorite(row, this);
		eventArrayCursorAdapter.notifyDataSetChanged();
	}	
}
