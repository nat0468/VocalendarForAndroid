package jp.vocalendar.activity;

import jp.vocalendar.R;
import jp.vocalendar.model.Event;
import jp.vocalendar.util.DialogUtil;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class EventDescriptionActivity extends Activity {
	/** Intentに表示するEventを格納するときに使うキー */
	public static final String KEY_EVENT = "event";
	
	/** 表示するイベント */
	private Event event = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_description);
		setTitle(R.string.vocalendar);

		setupButtons();
		
		updateEventDescription();
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
				DialogUtil.openNotImplementedDialog(EventDescriptionActivity.this);
			}
		});		
		
		Button previousButton = (Button)findViewById(R.id.previous_button);
		previousButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				DialogUtil.openNotImplementedDialog(EventDescriptionActivity.this);
			}
		});
	}
	
	private void updateEventDescription() {
		event = (Event)getIntent().getSerializableExtra(KEY_EVENT);
		//setTitle(event.getSummary());
		TextView tv = (TextView)findViewById(R.id.descriptionView);
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		StringBuilder sb = new StringBuilder();

		sb.append(event.formatDateTime());
		sb.append("<h3>"); sb.append(event.getSummary()); sb.append("</h3>");
		sb.append(changeURItoAnchorTag(event.getDescription()));
		CharSequence html = Html.fromHtml(sb.toString());
		tv.setText(html);
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
		String noTagSummary = event.getSummary().replaceAll("【[^】]+】", "");
		sb.append(noTagSummary);
		sb.append(' ');
		sb.append(getString(R.string.share_footer));
		return sb.toString();
	}

}
