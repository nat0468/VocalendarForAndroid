package jp.vocalendar.activity;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import jp.vocalendar.R;
import jp.vocalendar.VocalendarApplication;
import jp.vocalendar.model.ColorTheme;
import jp.vocalendar.model.EventArrayCursorAdapter;
import jp.vocalendar.model.EventDataBase;
import jp.vocalendar.model.EventDataBaseRow;
import jp.vocalendar.model.EventWithAdditionalDateArrayCursorAdapter;
import jp.vocalendar.model.FavoriteEventDataBaseRow;
import jp.vocalendar.model.FavoriteEventManager;
import jp.vocalendar.model.FavoriteEventManager.GCalendarIdAndGid;
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
	private UpdateFavoriteEventTask updateFavoriteEventTask;

	/** リスト中の今日のイベントの位置 */
	private int todayIndex;
		
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UncaughtExceptionSavingHandler.init(this);
        setupActionBar();
        
        colorTheme = initColorTheme();
        setContentView(R.layout.event_list);
        
        setupButtons();
        setupListView();
        updateList();
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
				setSelection(todayIndex);
			}
		});
		
		ImageView changeDate = (ImageView)findViewById(R.id.change_date_image_view_action);
        changeDate.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				DialogUtil.openNotImplementedDialog(FavoriteEventListActivity.this);
			}
		});

        ImageView favorite = (ImageView)findViewById(R.id.go_to_favorite_list_image_view_action);
        favorite.setImageResource(R.drawable.not_favorite);
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
		EventDataBaseRow event =
				eventArrayCursorAdapter.getEventArrayCursor().getEventDataBaseRow(position);
		Intent i = new Intent(this, OneEventDescriptionActivity.class);
		i.putExtra(OneEventDescriptionActivity.KEY_EVENT_INSTANCE, event);
		startActivity(i);
	}

	private void updateList() {
        TimeZone timeZone = TimeZone.getDefault();
        Calendar today = Calendar.getInstance();
        DateUtil.makeStartTimeOfDay(today);
        
        List<FavoriteEventDataBaseRow> rowList = new LinkedList<FavoriteEventDataBaseRow>();
        
        EventDataBase db = new EventDataBase(this);
		db.open();
        todayIndex = db.getAllFavoriteEvents(today, timeZone, rowList);
        db.close();
        
        EventDataBaseRow[] rows = rowList.toArray(new EventDataBaseRow[rowList.size()]);
        VocalendarApplication app = (VocalendarApplication)getApplication();
        favoriteEventManager = app.getFavoriteEventManager();
        favoriteEventManager.loadFavoriteEvent(rows);
        
        eventArrayCursorAdapter = new EventWithAdditionalDateArrayCursorAdapter(
        		this, rows, timeZone, colorTheme, favoriteEventManager, this);
        getListView().setAdapter(eventArrayCursorAdapter);
        setSelection(todayIndex);
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
				updateList();
			}
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
