package jp.vocalendar.activity;

import jp.vocalendar.R;
import jp.vocalendar.model.Event;
import jp.vocalendar.model.EventDataBase;
import jp.vocalendar.model.EventDataBaseRow;
import jp.vocalendar.util.CalendarAppUtilICS;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class SwipableEventDescriptionActivity extends FragmentActivity {
	/** Intentに表示するEventDataBaseRowを格納するときに使うキー */
	public static final String KEY_EVENT_DATA_BASE_ROW = "event_database_row";
	
	/** ページング用のアダプタ */
	private EventDescriptionPagerAdapter pagerAdapter;
	
	/** ページング用のビュー */
	private ViewPager viewPager;
	
	/** 表示中のイベントインデックス(0始まり) */
	private int eventIndex = 0;
	
	/** 現在表示中のイベント */
	private EventDataBaseRow currentRow;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.swipable_evenet_description);
        
		setTitle(R.string.vocalendar);
		setupButtons();				
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

	private void initPagerAdapter() {
		pagerAdapter =
        		new EventDescriptionPagerAdapter(
        				getSupportFragmentManager(), this);
        viewPager = (ViewPager) findViewById(R.id.swipable_event_description_pager);
        viewPager.setOnPageChangeListener(new ButtonStateUpdater());
        viewPager.setAdapter(pagerAdapter);
	}
	
	private void updateEventDescription(Intent intent) {
		int eventIndex = 0;
		EventDataBaseRow row = null;
    	row = (EventDataBaseRow)intent.getSerializableExtra(KEY_EVENT_DATA_BASE_ROW);
		if(row == null) { // イベントが存在しない場合は無視
			finish();
			return;
		}
    	eventIndex = row.getEventIndex();
		moveToEvent(eventIndex);
	}
		
	private void moveToEvent(int eventIndex)
	{
		pageIndexUpdated(eventIndex);
		viewPager.setCurrentItem(eventIndex);
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
		EventDataBase db = new EventDataBase(this);
		db.open();	
        currentRow = db.getEventByEventIndex(pageIndex);
        db.close();
        
		Button previousButton = (Button)findViewById(R.id.previous_button);
		if(currentRow.isFirstEvent()) {
			previousButton.setEnabled(false);
		} else {
			previousButton.setEnabled(true);
		}
		
		Button nextButton = (Button)findViewById(R.id.next_button);
		if((currentRow.getEventIndex() + 1) ==  pagerAdapter.getCount()) {
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
	
	
}
