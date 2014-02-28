package jp.vocalendar.activity;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import jp.vocalendar.R;
import jp.vocalendar.VocalendarApplication;
import jp.vocalendar.activity.view.VocalendarDatePicker;
import jp.vocalendar.model.ColorTheme;
import jp.vocalendar.model.Event;
import jp.vocalendar.model.EventArrayCursorAdapter;
import jp.vocalendar.model.EventDataBase;
import jp.vocalendar.model.EventDataBaseRow;
import jp.vocalendar.model.EventDataBaseRowArray;
import jp.vocalendar.model.EventWithAdditionalDateArrayCursorAdapter;
import jp.vocalendar.model.FavoriteEventDataBaseRow;
import jp.vocalendar.model.FavoriteEventManager;
import jp.vocalendar.model.LoadMoreFavoriteEventController;
import jp.vocalendar.model.FavoriteEventManager.GCalendarIdAndGid;
import jp.vocalendar.model.LoadMoreEventController;
import jp.vocalendar.task.UpdateFavoriteEventTask;
import jp.vocalendar.util.DateUtil;
import jp.vocalendar.util.DialogUtil;
import jp.vocalendar.util.UncaughtExceptionSavingHandler;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class FavoriteEventListActivity extends AbstractEventListActivity
implements EventArrayCursorAdapter.FavoriteToggler {
	private static final String TAG = "FavoriteEventListActivity";

	private static final int REQUEST_CODE_UPDATE_FAVORITE_EVENT = 100;
	
	private ColorTheme colorTheme;
	private LoadMoreFavoriteEventController loadMoreFavoriteEventController;

	/** 
	 * リスト中の今日のイベントの位置。
	 * 日付を指定したお気に入り一覧の場合は、Integer.MIN_VALUE になる。
	 */
	private int todayIndex = Integer.MIN_VALUE;
	
	/** お気に入り一覧を読み込み開始する時間。初期値は今日の開始時間(0時0分0秒) */
	private Calendar startTimeToList;
	
	private VocalendarDatePicker datePicker;
		
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UncaughtExceptionSavingHandler.init(this);
        setupActionBar();
        
        colorTheme = initColorTheme();
        setContentView(R.layout.event_list);
        
        loadMoreFavoriteEventController = new LoadMoreFavoriteEventController(this);
        
        setupButtons();
        setupListView();
        updateListToday();
	}

	private void setupActionBar() {
		ActionBar ab = getSupportActionBar();
		ab.setTitle(R.string.favorite_list);
        ab.setDisplayShowTitleEnabled(true);
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.show();
	}

	protected ColorTheme initColorTheme() {
		return new ColorTheme(this);
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
        favorite.setImageResource(R.drawable.ic_action_not_important);
        favorite.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				openEventList();
			}
		});
        
        ImageView update = (ImageView)findViewById(R.id.update_image_view_action);
        update.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				updateFavoriteEvents();
			}
		});        
	}
	
	private void setupListView() {
		ListView listView = (ListView)findViewById(R.id.eventList);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ListView l = (ListView)parent;
				onListItemClick(l, view, position, id);
			}
		});
	}
	
	protected void onListItemClick(ListView l, View v, int position, long id) {
		openEventDescriptionActivity(position);
	}
	
	private void openEventDescriptionActivity(int position) {
		if(loadMoreFavoriteEventController.isLoadMorePreviousEventViewDisplayed()) {
			if(position == 0) {
				loadMoreFavoriteEventController.loadPreviousEventsTapped();
				return;
			}
			position--;
		}
		EventDataBaseRow event =
				eventArrayCursorAdapter.getEventArrayCursor().getEventDataBaseRow(position);
		Intent i = new Intent(this, OneEventDescriptionActivity.class);
		i.putExtra(OneEventDescriptionActivity.KEY_EVENT_INSTANCE, event);
		startActivity(i);
	}

	/**
	 * 一覧を今日で更新
	 */
	private void updateListToday() {
        TimeZone timeZone = TimeZone.getDefault();
        startTimeToList = Calendar.getInstance(timeZone);
        DateUtil.makeStartTimeOfDay(startTimeToList);

        List<FavoriteEventDataBaseRow> rowList = new LinkedList<FavoriteEventDataBaseRow>();
        
        EventDataBase db = new EventDataBase(this);
		db.open();
        db.getFavoriteEvents(startTimeToList, timeZone, rowList);
        db.close();
        
        EventDataBaseRow[] rows = rowList.toArray(new EventDataBaseRow[rowList.size()]);
        VocalendarApplication app = (VocalendarApplication)getApplication();
        favoriteEventManager = app.getFavoriteEventManager();
        favoriteEventManager.loadFavoriteEvent(rows);
        
        loadMoreFavoriteEventController.setupListView(getListView(), colorTheme); // もっと読み込む項目を設定            
        todayIndex = 1; //もっと読み込むアイテムの次が今日のお気に入りイベント        	
        eventArrayCursorAdapter = new EventWithAdditionalDateArrayCursorAdapter(
        		this, rows, timeZone, colorTheme, favoriteEventManager, this);
        getListView().setAdapter(eventArrayCursorAdapter);
        setSelection(todayIndex);
	}

	/**
	 * 一覧を指定日で更新
	 * @param cal
	 */
	private void updateList(int year, int month, int day) {
        TimeZone timeZone = TimeZone.getDefault();
		startTimeToList.set(Calendar.YEAR, year);
		startTimeToList.set(Calendar.MONTH, month);
		startTimeToList.set(Calendar.DAY_OF_MONTH, day);

        List<FavoriteEventDataBaseRow> rowList = new LinkedList<FavoriteEventDataBaseRow>();
        
        EventDataBase db = new EventDataBase(this);
		db.open();
        db.getFavoriteEvents(startTimeToList, timeZone, rowList);
        db.close();
        
        EventDataBaseRow[] rows = rowList.toArray(new EventDataBaseRow[rowList.size()]);
        VocalendarApplication app = (VocalendarApplication)getApplication();
        favoriteEventManager = app.getFavoriteEventManager();
        favoriteEventManager.loadFavoriteEvent(rows);

        int startPosition = 0;
        if(rows.length != 0) {
            loadMoreFavoriteEventController.setupListView(getListView(), colorTheme); // もっと読み込む項目を設定            
            startPosition = 1; //もっと読み込むアイテムの次が今日のお気に入りイベント        	
        }
        eventArrayCursorAdapter = new EventWithAdditionalDateArrayCursorAdapter(
        		this, rows, timeZone, colorTheme, favoriteEventManager, this);
        getListView().setAdapter(eventArrayCursorAdapter);
        setSelection(startPosition);
        todayIndex = Integer.MIN_VALUE;
	}

	@Override
	public boolean onSupportNavigateUp() {
		openEventList();
		return true;
	}		
	
	private void openEventList() {
		Intent i = new Intent(this, EventListActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
	}
	
	private void updateFavoriteEvents() {
		Intent intent = new Intent(this, LoadingActivity.class);
		intent.putExtra(LoadingActivity.KEY_TASK_CLASS_NAME, UpdateFavoriteEventTask.TASK_CLASS_NAME);
		
		EventDataBaseRow[] rows = 
				getEventArrayCursorAdapter().getEventArrayCursor().getEventDataBaseRows();
		FavoriteEventManager.GCalendarIdAndGid ids[] =
				new FavoriteEventManager.GCalendarIdAndGid[rows.length];
		for(int i = 0; i < ids.length; i++) {
			ids[i] = new FavoriteEventManager.GCalendarIdAndGid(rows[i]);					
		}
		intent.putExtra(LoadingActivity.KEY_ARGS, ids);

		startActivityForResult(intent, REQUEST_CODE_UPDATE_FAVORITE_EVENT);
	}

	@Override
	protected void onActivityResult(
			int requestCode, int resultCode, Intent data) {
		if(requestCode == REQUEST_CODE_UPDATE_FAVORITE_EVENT) { // 読み込み中画面から戻ったときの処理
			if(resultCode == Activity.RESULT_OK) {
				loadMoreFavoriteEventController.resetListView();				
				updateList(
						startTimeToList.get(Calendar.YEAR),
						startTimeToList.get(Calendar.MONTH),
						startTimeToList.get(Calendar.DAY_OF_MONTH));
			}
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
	 * お気に入りイベントを更新する。
	 * @param eventDataBaseRowArray
	 */
	public void updateFavoriteEventRows(EventDataBaseRow[] rows) {
		int oldSize = eventArrayCursorAdapter.getEventArrayCursor().getEventDataBaseRows().length;
		int newSize = rows.length - oldSize;
		if(todayIndex >= 0) {
			todayIndex = todayIndex + newSize;
		}
		if(rows.length > 0) {
			Event e = rows[0].getEvent();
			if(e.getStartDateTime() != null) {
				startTimeToList.setTime(e.getStartDateTime());
			} else if(e.getStartDate() != null) {
				startTimeToList.setTime(e.getStartDate());				
			}
		}
		
    	eventArrayCursorAdapter = new EventWithAdditionalDateArrayCursorAdapter(
    			this, rows, TimeZone.getDefault(), colorTheme,
    			favoriteEventManager, this);    	
        setListAdapter(eventArrayCursorAdapter);        
       	setSelection(newSize);        
	}

	private void openDatePicker() {
		if(datePicker == null) {
			datePicker = new VocalendarDatePicker(
					this,
					new VocalendarDatePicker.OkOnClickListener() {
						@Override
						public void onClick(int year, int month, int dayOfMonth) {
							changeDateTo(year, month, dayOfMonth);
						}						
					});
		}
		datePicker.show(startTimeToList);
	}
	
	private void changeDateTo(int year, int month, int day) {
		loadMoreFavoriteEventController.resetListView();
		updateList(year, month, day);
	}

	public Calendar getStartTimeToList() {
		return startTimeToList;
	}
	
	private void goToToday() {
		if(todayIndex >= 0) {
			setSelection(todayIndex);
		} else {
			loadMoreFavoriteEventController.resetListView();
			updateListToday();
		}
	}
}
