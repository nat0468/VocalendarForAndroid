package jp.vocalendar.activity;

import java.util.TimeZone;

import jp.vocalendar.R;
import jp.vocalendar.model.Event;
import jp.vocalendar.model.EventArrayCursor;
import jp.vocalendar.model.EventDataBase;
import jp.vocalendar.model.EventDataBaseRow;
import jp.vocalendar.util.DialogUtil;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * イベント詳細情報を表示するActivity。
 * 表示するイベントはIntentにKEY_EVENTまたはKEY_EVENT_INDEXで指定する。
 */
public class EventDescriptionActivity extends Activity {
	/** Intentに表示するEventDataBaseRowを格納するときに使うキー */
	public static final String KEY_EVENT_DATA_BASE_ROW = "event_database_row";
	
	/** Intentに表示するEventのインデックス(int値)を格納するときに使うキー */
	public static final String KEY_EVENT_INDEX = "event_index"; 
	
	/** 表示するイベント */
	private EventDataBaseRow row = null;
	
	/** ジェスチャー判定用 */
	private GestureDetector gestureDetector;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_description);
		setTitle(R.string.vocalendar);

		setupButtons();		
		// setGestureDetecor(); // TODO スクロールビューへの対応
		
		updateEventDescription(getIntent());
		updateButtonState();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		updateEventDescription(intent);
		updateButtonState();
	}
	
	private void setupButtons() {
		Button shareButton = (Button)findViewById(R.id.share_button);
		shareButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openSendDialog();
			}
		});

		Button nextButton = (Button)findViewById(R.id.next_button);
		nextButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				moveToEvent(row.getNextIndex());
			}
		});		
		
		Button previousButton = (Button)findViewById(R.id.previous_button);
		previousButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				moveToEvent(row.getPreviousIndex());
			}
		});
	}
	
	private void setGestureDetecor() {
		View v = (View)findViewById(R.id.eventDescriptionScrollView);
		v.setOnTouchListener(new View.OnTouchListener() {			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(gestureDetector.onTouchEvent(event)) {
					return true;
				}
				return false;
			}
		});
		gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
			@Override
			// フリック時に呼ばれる
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				Log.d("EventDescriptionActivity", "v(" + velocityX + "," + velocityY + ")");
				if(Math.abs(velocityX) > Math.abs(velocityY)) { // 横方向のフリックかどうか判定
					if(velocityX > 0) { // 右方向へのフリック
						moveToEvent(row.getPreviousIndex());					
					} else { // 左方向へのフリック
						moveToEvent(row.getNextIndex());						
					}
				}
				return false;
			}
		});
	}
	
	private void updateEventDescription(Intent intent) {
		if(intent.hasExtra(KEY_EVENT_INDEX)) {
			EventDataBase db = new EventDataBase(this);
			db.open();	
	        row = db.getEventByIndex(intent.getIntExtra(KEY_EVENT_INDEX, -1));
	        db.close();		
	    } else {		
	    	row = (EventDataBaseRow)intent.getSerializableExtra(KEY_EVENT_DATA_BASE_ROW);
	    }
		if(row == null) { // イベントが存在しない場合は無視
			finish();
			return;
		}
		
		//setTitle(event.getSummary());
		TextView tv = (TextView)findViewById(R.id.descriptionView);
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		StringBuilder sb = new StringBuilder();

		sb.append(row.getEvent().formatDateTime(TimeZone.getDefault())); // デフォルトのタイムゾーン
		sb.append("<h3>"); sb.append(row.getEvent().getSummary()); sb.append("</h3>");
		sb.append(changeURItoAnchorTag(row.getEvent().getDescription()));
		CharSequence html = Html.fromHtml(sb.toString());
		tv.setText(html);
	}
	
	private void updateButtonState() {
		Button previousButton = (Button)findViewById(R.id.previous_button);
		if(row.isFirstEvent()) {
			previousButton.setEnabled(false);
		} else {
			previousButton.setEnabled(true);
		}
		
		Button nextButton = (Button)findViewById(R.id.next_button);
		if(row.isLastEvent()) {
			nextButton.setEnabled(false);
		} else {
			nextButton.setEnabled(true);
		}		
	}
	
	private static String changeURItoAnchorTag(String src) {
		if(src == null) {
			return "";
		}
		String dest = src.replaceAll("[\n|\r|\f]", "<br/>");
		dest = dest.replaceAll("\\\\n", "<br/>");
		dest =  dest.replaceAll("(https?://[-_.!~*'\\(\\)a-zA-Z0-9;\\/?:\\@&=+\\$,%#]+)",
								"<a href=\"$1\">$1</a>");
		return dest;
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
		String noTagSummary = row.getEvent().getSummary().replaceAll("【[^】]+】", "");
		sb.append(noTagSummary);
		sb.append(' ');
		sb.append(getString(R.string.share_footer));
		return sb.toString();
	}

	private void moveToEvent(int index)
	{
		Intent i = new Intent(this, EventDescriptionActivity.class);
		i.putExtra(EventDescriptionActivity.KEY_EVENT_INDEX, index);
		startActivity(i);		
	}


}
