package jp.vocalendar.activity;

import jp.vocalendar.R;
import jp.vocalendar.VocalendarApplication;
import jp.vocalendar.model.Event;
import jp.vocalendar.model.EventDataBaseRow;
import jp.vocalendar.util.CalendarAppUtilICS;
import jp.vocalendar.util.UncaughtExceptionSavingHandler;
import jp.vocalendar.util.UncaughtExceptionSavingToFileHandler;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class SwipableEventDescriptionActivity extends ActionBarActivity {
	private static final String TAG = "SwipableEventDescriptionActivity";
	
	/** Intentに表示するEventDataBaseRowの
	 * インデックス(KEY_EVENT_DATA_BASE_ROWS中の位置)を格納するときに使うキー */
	public static final String KEY_EVENT_INDEX = "event_index";
	
	/** ページング用のアダプタ */
	protected PagerAdapter pagerAdapter;
	
	/** ページング用のビュー */
	protected ViewPager viewPager;
	
	/** 表示中のイベントインデックス(0始まり) */
	private int eventIndex = 0;
	
	/** 現在表示中のイベント */
	private EventDataBaseRow currentRow;
	
	/** スワイプで表示可能なイベント一覧 */
	private EventDataBaseRow[] rows;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UncaughtExceptionSavingHandler.init(this);
        
        VocalendarApplication app = (VocalendarApplication)getApplication();
        if(app.getEventDataBaseRowArray() == null) {
        	// プロセスが解放されて、イベント情報が解放されていた場合は、終了(イベント一覧に戻る)する。
        	Log.d(TAG, "EventDataBaseRowArray is null...");
        	finish();
        	return;
        }
        
        setContentView(R.layout.swipable_evenet_description);
        
		setTitle(R.string.vocalendar);
		setupButtons();
		setupActionBar();
        initPagerAdapter();
		updateEventDescription(getIntent());
    }

	private void setupButtons() {
		Button shareButton = (Button)findViewById(R.id.share_button);
		shareButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openSendDialog();
			}
		});

		Button importButton = (Button)findViewById(R.id.import_button);
		if(Build.VERSION.SDK_INT >= 14) {
			importButton.setOnClickListener(new View.OnClickListener() {			
				@Override
				public void onClick(View v) {
					importEvent();
				}
			});
		} else { // API Level 14(ICS)未満の場合はイベントのインポート不可
			LinearLayout layout = (LinearLayout)findViewById(R.id.event_description_button_layout);
			layout.removeView(importButton);
		}
		
		Button nextButton = (Button)findViewById(R.id.next_button);
		nextButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				moveToEvent(currentRow.getEventIndex() + 1);
			}
		});		
		
		Button previousButton = (Button)findViewById(R.id.previous_button);
		previousButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				moveToEvent(currentRow.getEventIndex() - 1);
			}
		});
	}
	
	private void setupActionBar() {
		ActionBar ab = getSupportActionBar();
        ab.setCustomView(R.layout.vocalendar_title_image_view);
        ab.setDisplayShowCustomEnabled(true);
        ab.setDisplayShowTitleEnabled(false);
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
	}
	
	
	protected void initPagerAdapter() {
		VocalendarApplication app = (VocalendarApplication)getApplication();
    	rows = app.getEventDataBaseRowArray().getNormalRows();
    	pagerAdapter =
    			new EventArrayEventDescriptionPagerAdapter(getSupportFragmentManager(), rows);
        viewPager = (ViewPager) findViewById(R.id.swipable_event_description_pager);
        viewPager.setOnPageChangeListener(new ButtonStateUpdater());
        viewPager.setAdapter(pagerAdapter);
	}
	
	protected void updateEventDescription(Intent intent) {
		eventIndex = intent.getIntExtra(KEY_EVENT_INDEX, Integer.MIN_VALUE);
		if(eventIndex == Integer.MIN_VALUE) { //存在しない場合は無視
			finish();
			return;
		}
		moveToEvent(eventIndex);
	}
		
	private void moveToEvent(int eventIndex)
	{
		int currentItem = viewPager.getCurrentItem();
		viewPager.setCurrentItem(eventIndex);
		if(currentItem == eventIndex) {
			Log.d(TAG, "moveToEvent():currentItem==eventIndex:" + eventIndex);
			// viewPagerのcurrentItemが変化しない場合は、ButtonStateUpdaterのonPageSelected()が呼ばれず、
			// pageIndexUpdated()が呼ばれないため、明示的に呼び出し。 
			pageIndexUpdated(eventIndex);
		}
	}
	
		
	private void openSendDialog() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, makeSharingEventText());

		// Create and start the chooser
		Intent chooser = Intent.createChooser(intent, "選択");
		startActivity(chooser);
	}
	
	private String makeSharingEventText() {
		StringBuilder sb = new StringBuilder();
		String noTagSummary = currentRow.getEvent().getSummary().replaceAll("【[^】]+】", "");
		sb.append(noTagSummary);
		sb.append(' ');
		//sb.append(getString(R.string.share_footer));
		sb.append(makeDetailUrl(currentRow.getEvent()));
		return sb.toString();
	}

	private void importEvent() {
		CalendarAppUtilICS.importEvent(currentRow, this);
	}
		
	private String makeDetailUrl(Event event) {
		// return "http://vocalendar.jp/detail2/gid/" + event.getGid();
		return event.getDetailUrl();
	}
	
	private void pageIndexUpdated(int pageIndex) {
		Log.d("SwipableEventDescriptionActivity", "pageIndex:" + pageIndex);
		
		this.eventIndex = pageIndex;
        currentRow = rows[pageIndex];
        
		Button previousButton = (Button)findViewById(R.id.previous_button);
		if(pageIndex == 0) {
			previousButton.setEnabled(false);
		} else {
			previousButton.setEnabled(true);
		}
		
		Button nextButton = (Button)findViewById(R.id.next_button);
		if((pageIndex+1) == rows.length) {
			nextButton.setEnabled(false);
		} else {
			nextButton.setEnabled(true);
		}		
	}
		
	private class ButtonStateUpdater implements ViewPager.OnPageChangeListener {
		@Override
		public void onPageScrollStateChanged(int arg0) {
			// 何もしない
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			// 何もしない
			Log.d("ButtonStateListener", "onPageScrolled:position=" + position);
		}

		@Override
		public void onPageSelected(int pageIndex) {
			Log.d("ButtonStateListener", "onPageSelected:pageIndex=" + pageIndex);
			pageIndexUpdated(pageIndex);
		}		
	}

	@Override
	public boolean onSupportNavigateUp() {
		finish();
		return true;
	}
}
