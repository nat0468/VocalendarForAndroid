package jp.vocalendar.activity;

import jp.vocalendar.R;
import jp.vocalendar.VocalendarApplication;
import jp.vocalendar.model.EventDataBaseRow;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.widget.Button;

/**
 * 検索結果イベント一覧から表示するイベント詳細画面に対応するActivity。
 * 画面レイアウトは通常のイベント詳細再画面と同じだが、左右へのページ変更は不可。
 */
public class OneEventDescriptionActivity extends
		SwipableEventDescriptionActivity {

	/** Intentに表示するEventDataBaseRowのインスタンスを格納するときに使うキー */
	public static final String KEY_EVENT_INSTANCE = "event_instance";	
	
	/**
	 * 詳細画面の初期化処理を上書き。
	 * このメソッドだけで全ての初期化を実行する。
	 */
	@Override
	protected void initPagerAdapter() {
		EventDataBaseRow row = (EventDataBaseRow)getIntent().getSerializableExtra(KEY_EVENT_INSTANCE);
		VocalendarApplication app = (VocalendarApplication)getApplication();
    	pagerAdapter =
    			new EventArrayEventDescriptionPagerAdapter(
    					this, getSupportFragmentManager(),
    					new EventDataBaseRow[]{ row }, app.getFavoriteEventManager());
        viewPager = (ViewPager) findViewById(R.id.swipable_event_description_pager);
        viewPager.setAdapter(pagerAdapter);

		Button previousButton = (Button)findViewById(R.id.previous_button);
		previousButton.setEnabled(false);
		
		Button nextButton = (Button)findViewById(R.id.next_button);
		nextButton.setEnabled(false);
	}

	/**
	 * インテント処理を上書き。
	 * initPagerAdapterで初期化済みのため、何もしない。
	 */
	@Override
	protected void updateEventDescription(Intent intent) {
		// 
	}

}
